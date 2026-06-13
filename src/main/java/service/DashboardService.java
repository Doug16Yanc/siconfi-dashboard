package service;

import api.client.BcbClient;
import api.client.SiconfiClient;
import api.dto.RreoItem;
import api.dto.RreoResponse;
import db.BcbRepository;
import db.DatabaseConfig;
import db.RreoRepository;
import db.SnapshotRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DashboardService {

    private final SiconfiClient      siconfi  = new SiconfiClient();
    private final BcbClient          bcb      = new BcbClient();
    private final RreoRepository     rreoRepo = new RreoRepository();
    private final BcbRepository      bcbRepo  = new BcbRepository();
    private final SnapshotRepository snapRepo = new SnapshotRepository();

    public static final String DEFAULT_ID_ENTE = "23"; // Defaults
    public static final int    DEFAULT_ANO     = 2025;
    public static final int    DEFAULT_PERIODO = 2;

    public record DashboardData(
            double receitaTotal,
            double despesaTotal,
            double resultadoPrimario,
            double rclTotal,
            Map<String, Double> despesaPorFuncao,
            Map<String, Double> receitaPorCategoria,
            double selic,
            double ipca12m,
            double dolar,
            String periodo,
            String idEnte,
            int    ano,
            int    numeroPeriodo
    ) {}

    public DashboardData carregarTudo() {
        return carregarTudo(DEFAULT_ID_ENTE, DEFAULT_ANO, DEFAULT_PERIODO);
    }

    public DashboardData carregarTudo(String idEnte, int ano, int periodo) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            System.out.printf("[Service] Carregando ente=%s ano=%d período=%d%n",
                    idEnte, ano, periodo);

            Future<List<RreoItem>> fA06   = executor.submit(() -> fetchComCache(idEnte, ano, periodo, "RREO-Anexo 06"));
            Future<Double>         fSelic = executor.submit(this::fetchSelicComCache);
            Future<Double>         fIpca  = executor.submit(this::fetchIpcaComCache);
            Future<Double>         fDolar = executor.submit(this::fetchDolarComCache);

            List<RreoItem> a06 = fA06.get();
            System.out.println("[Service] Anexo 06 — itens: " + a06.size());

            double despesaTotal      = extrair(a06, "DespesaPrimariaTotalExcetoFontesRPPS", "DESPESAS LIQUIDADAS");
            double resultadoPrimario = extrair(a06, "ResultadoPrimarioSemRPPSAcimaDaLinha", "VALOR");
            double receitaTotal      = extrair(a06, "ReceitasCorrentesExcetoFontesRPPS",    "RECEITAS REALIZADAS (a)");
            double rclTotal          = extrair(a06, "ReceitaCorrenteLiquidaSemRPPS",         "RECEITAS REALIZADAS (a)");

            System.out.printf("[Extrator] Receita=%.2f Despesa=%.2f Primário=%.2f RCL=%.2f%n",
                    receitaTotal, despesaTotal, resultadoPrimario, rclTotal);

            DashboardData data = new DashboardData(
                    receitaTotal,
                    despesaTotal,
                    resultadoPrimario,
                    rclTotal,
                    despesaPorFuncao(a06),
                    receitaPorCategoria(a06),
                    fSelic.get(),
                    fIpca.get(),
                    fDolar.get(),
                    formatarPeriodo(ano, periodo),
                    idEnte,
                    ano,
                    periodo
            );

            Thread.ofVirtual().start(() -> snapRepo.salvar(idEnte, ano, periodo, data));

            return data;

        } catch (Exception e) {
            System.err.println("[Service] ERRO: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public List<DashboardData> carregarSerie(String idEnte, int ano) {
        List<Integer> periodos = List.of(1, 2, 3, 4, 5, 6);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<DashboardData>> futures = periodos.stream()
                    .map(p -> executor.submit(() -> {
                        var snap = snapRepo.buscarPorPeriodo(idEnte, ano, p);
                        if (snap.isPresent()) {
                            System.out.printf("[Serie] Cache snapshot ente=%s ano=%d p=%d%n", idEnte, ano, p);
                            return snap.get();
                        }
                        System.out.printf("[Serie] Fetch API ente=%s ano=%d p=%d%n", idEnte, ano, p);
                        return carregarTudo(idEnte, ano, p);
                    }))
                    .toList();

            return futures.stream()
                    .<DashboardData>map(f -> {
                        try { return f.get(); }
                        catch (Exception e) { return null; }
                    })
                    .filter(d -> d != null && d.receitaTotal() > 0)
                    .toList();
        }
    }

    public String getNomeEnte(String idEnte) {
        String sql = "SELECT nome FROM estado_cache WHERE id_ente = ?";
        try (var con = DatabaseConfig.getConnection(); var ps = con.prepareStatement(sql)) {
            ps.setString(1, idEnte);
            var rs = ps.executeQuery();
            return rs.next() ? rs.getString(1) : "Ente " + idEnte;
        } catch (Exception e) { return "Ente " + idEnte; }
    }


    private List<RreoItem> fetchComCache(String idEnte, int ano, int periodo, String anexo) {
        try {
            if (rreoRepo.cacheValido(idEnte, ano, periodo, anexo)) {
                System.out.println("[Cache] HIT — " + anexo + " ente=" + idEnte);
                return rreoRepo.buscarCache(idEnte, ano, periodo, anexo);
            }
            System.out.println("[Cache] MISS — chamando API: " + anexo);
            RreoResponse resp = siconfi.fetchRreo(ano, periodo, anexo, idEnte, "E");
            if (resp != null && resp.items() != null)
                rreoRepo.salvarBatch(resp.items());
            return resp != null && resp.items() != null ? resp.items() : List.of();
        } catch (Exception e) {
            System.err.println("[Cache] ERRO em " + anexo + ": " + e.getMessage());
            return List.of();
        }
    }

    private double fetchSelicComCache() {
        if (bcbRepo.cacheValido(432)) return bcbRepo.buscarUltimoValor(432);
        var items = bcb.fetchSerie(432, 1);
        bcbRepo.salvarBatch(432, items);
        return items.isEmpty() ? 0.0 : items.get(items.size() - 1).valorDouble();
    }

    private double fetchIpcaComCache() {
        if (bcbRepo.cacheValido(433)) return bcbRepo.buscarUltimoValor(433);
        var items = bcb.fetchSerie(433, 12);
        bcbRepo.salvarBatch(433, items);
        return bcb.fetchIpca12m();
    }

    private double fetchDolarComCache() {
        if (bcbRepo.cacheValido(1)) return bcbRepo.buscarUltimoValor(1);
        var items = bcb.fetchSerie(1, 1);
        bcbRepo.salvarBatch(1, items);
        return items.isEmpty() ? 0.0 : items.get(items.size() - 1).valorDouble();
    }


    private double extrair(List<RreoItem> items, String coConta, String coluna) {
        return items.stream()
                .filter(i -> coConta.equalsIgnoreCase(i.co_conta))
                .filter(i -> coluna.equalsIgnoreCase(i.coluna))
                .mapToDouble(i -> i.vl_resultado)
                .findFirst()
                .orElse(0.0);
    }

    private Map<String, Double> despesaPorFuncao(List<RreoItem> items) {
        List<String> categorias = List.of(
                "RREO6PessoalEEncargosSociais",
                "RREO6OutrasDespesasCorrentes",
                "RREO6AmortizacaoDaDivida",
                "DespesasDeCapitalExcetoFontesRPPS",
                "DespesasPrimariasCorrentesComFontesRPPS"
        );
        Map<String, Double> resultado = new java.util.LinkedHashMap<>();
        items.stream()
                .filter(i -> categorias.contains(i.co_conta))
                .filter(i -> "DESPESAS LIQUIDADAS".equalsIgnoreCase(i.coluna))
                .forEach(i -> resultado.merge(i.co_conta, i.vl_resultado, Double::sum));
        return resultado;
    }

    private Map<String, Double> receitaPorCategoria(List<RreoItem> items) {
        double icms  = extrair(items, "RREO6ICMS",                         "RECEITAS REALIZADAS (a)");
        double ipva  = extrair(items, "RREO6IPVA",                         "RECEITAS REALIZADAS (a)");
        double itcd  = extrair(items, "RREO6ITCD",                         "RECEITAS REALIZADAS (a)");
        double trib  = extrair(items, "RREO6ReceitasTributarias",           "RECEITAS REALIZADAS (a)");
        double total = extrair(items, "ReceitasCorrentesExcetoFontesRPPS",  "RECEITAS REALIZADAS (a)");

        Map<String, Double> map = new java.util.LinkedHashMap<>();
        map.put("ICMS",               icms);
        map.put("IPVA",               ipva);
        map.put("ITCD",               itcd);
        map.put("Outras Tributárias", Math.max(trib - icms - ipva - itcd, 0));
        map.put("Não Tributárias",    Math.max(total - trib, 0));
        return map;
    }

    private String formatarPeriodo(int ano, int periodo) {
        String[] meses = {"Até Fevereiro", "Até Abril", "Até Junho",
                "Até Agosto",    "Até Outubro", "Até Dezembro"};
        String label = periodo >= 1 && periodo <= 6 ? meses[periodo - 1] : "Período " + periodo;
        return ano + " — " + periodo + "º Bimestre (" + label + ")";
    }
}