package service;

import api.client.BcbClient;
import api.client.SiconfiClient;
import api.dto.RreoItem;
import api.dto.RreoResponse;
import db.*;

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
    private final EstadoRepository estadoRepo = new EstadoRepository();

    public static final String DEFAULT_ID_ENTE = "23";
    public static final int    DEFAULT_ANO     = 2025;
    public static final int    DEFAULT_PERIODO = 2;

    private static final int SERIE_DOLAR      = 1;
    private static final int SERIE_SELIC      = 432;
    private static final int SERIE_IPCA       = 433;
    private static final int SERIE_PIB_CRESC  = 7326;
    private static final int SERIE_DIVIDA_PIB = 13762;

    private static final String ANEXO_01 = "RREO-Anexo 01";
    private static final String ANEXO_02 = "RREO-Anexo 02";
    private static final String ANEXO_03 = "RREO-Anexo 03";
    private static final String ANEXO_06 = "RREO-Anexo 06";
    private static final String ANEXO_09 = "RREO-Anexo 09";
    private static final String ANEXO_12 = "RREO-Anexo 12";
    private static final String ANEXO_13 = "RREO-Anexo 13";

    private static final double LIMITE_MDE_PERCENTUAL = 25.0;
    private static final double LIMITE_OP_CREDITO_RCL = 16.0;

    public record DashboardData(
            double receitaTotal,
            double despesaTotal,
            double resultadoPrimario,
            double rclTotal,
            Map<String, Double> despesaPorFuncao,
            Map<String, Double> receitaPorCategoria,
            Map<String, Double> receitasPorNatureza,
            Map<String, Double> dotacaoInicial,
            Map<String, Double> dotacaoAtualizada,
            Map<String, Double> despesaEmpenhada,
            Map<String, Double> despesaLiquidada,
            Map<String, Double> despesaPorFuncaoA03,
            double selic,
            double ipca12m,
            double dolar,
            double crescimentoPib,
            double dividaBrutaPib,
            long   populacao,
            String nomeEnte,
            String periodo,
            String idEnte,
            int    ano,
            int    numeroPeriodo,
            double mdePercentualAplicado,
            double mdeLimiteConstitucional,
            double opCreditoTotal,
            double opCreditoLimiteRcl,
            double opCreditoPercentualRcl,
            double restosProcessados,
            double restosNaoProcessados,
            double restosTotal,
            double dividaConsolidada,
            double dividaConsolidadaLiquida,
            double deducoesDivida,
            double amortizacaoDotacao,
            double amortizacaoLiquidado,
            double jurosLiquidado,
            double jurosDotacao
    ) {}

    public DashboardData carregarTudo() {
        return carregarTudo(DEFAULT_ID_ENTE, DEFAULT_ANO, DEFAULT_PERIODO);
    }

    public DashboardData carregarTudo(String idEnte, int ano, int periodo) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            System.out.printf("[Service] Carregando ente=%s ano=%d período=%d%n", idEnte, ano, periodo);

            Future<List<RreoItem>> fA01    = executor.submit(() -> fetchComCache(idEnte, ano, periodo, ANEXO_01));
            Future<List<RreoItem>> fA02    = executor.submit(() -> fetchComCache(idEnte, ano, periodo, ANEXO_02));
            Future<List<RreoItem>> fA03    = executor.submit(() -> fetchComCache(idEnte, ano, periodo, ANEXO_03));
            Future<List<RreoItem>> fA06    = executor.submit(() -> fetchComCache(idEnte, ano, periodo, ANEXO_06));
            Future<List<RreoItem>> fA12    = executor.submit(() -> fetchComCache(idEnte, ano, periodo, ANEXO_12));
            Future<Double>         fSelic  = executor.submit(this::fetchSelicComCache);
            Future<Double>         fIpca   = executor.submit(this::fetchIpcaComCache);
            Future<Double>         fDolar  = executor.submit(this::fetchDolarComCache);
            Future<Double>         fPib    = executor.submit(this::fetchPibComCache);
            Future<Double>         fDivida = executor.submit(this::fetchDividaComCache);
            Future<EstadoRepository.InfoEstado> fEstado = executor.submit(() -> estadoRepo.buscarPorId(idEnte));

            List<RreoItem> a01 = fA01.get();
            List<RreoItem> a02 = fA02.get();
            List<RreoItem> a03 = fA03.get();
            List<RreoItem> a06 = fA06.get();
            List<RreoItem> a12 = fA12.get();

            System.out.printf("[Service] A01=%d A02=%d A03=%d A06=%d A12=%d itens%n",
                    a01.size(), a02.size(), a03.size(), a06.size(), a12.size());

            double receitaTotal      = extrair(a06, "ReceitasCorrentesExcetoFontesRPPS",    "RECEITAS REALIZADAS (a)");
            double despesaTotal      = extrair(a06, "DespesaPrimariaTotalExcetoFontesRPPS", "DESPESAS LIQUIDADAS");
            double resultadoPrimario = extrair(a06, "ResultadoPrimarioSemRPPSAcimaDaLinha", "VALOR");
            double rclTotal          = extrair(a06, "ReceitaPrimariaTotalExcetoFontesRPPS", "RECEITAS REALIZADAS (a)");

            System.out.printf("[Extrator] Receita=%.2f Despesa=%.2f Primário=%.2f RCL=%.2f%n",
                    receitaTotal, despesaTotal, resultadoPrimario, rclTotal);

            double restosProcessados    = extrairColunaBimestre(a06, "RestosAPagarProcessadosExcetoPrecatorios");
            double restosNaoProcessados = extrairColunaBimestre(a06, "RestosAPagarNaoProcessados");
            double restosTotal          = restosProcessados + restosNaoProcessados;

            System.out.printf("[Restos] Processados=%.2f NãoProcessados=%.2f Total=%.2f%n",
                    restosProcessados, restosNaoProcessados, restosTotal);

            double opCreditoTotal = extrairOpCreditoTotal(a06);
            double opCreditoLimiteRcl     = rclTotal * (LIMITE_OP_CREDITO_RCL / 100.0);
            double opCreditoPercentualRcl = rclTotal > 0 ? (opCreditoTotal / rclTotal) * 100.0 : 0.0;

            System.out.printf("[OpCredito] Total=%.2f Limite=%.2f (%.2f%% RCL)%n",
                    opCreditoTotal, opCreditoLimiteRcl, opCreditoPercentualRcl);

            double mdePercentualAplicado = extrairMdePercentual(a12, a02, receitaTotal);
            System.out.printf("[MDE] Percentual aplicado=%.2f%%%n", mdePercentualAplicado);

            double dividaConsolidada       = extrairColunaBimestre(a06, "DividaConsolidada");
            double dividaConsolidadaLiquida= extrairColunaBimestre(a06, "DividaConsolidadaLiquida");
            double deducoesDivida          = extrairColunaBimestre(a06, "DeducoesDaDividaConsolidada");
            double amortizacaoDotacao      = extrair(a06, "RREO6AmortizacaoDaDivida",    "DOTAÇÃO ATUALIZADA");
            double amortizacaoLiquidado    = extrair(a06, "RREO6AmortizacaoDaDivida",    "DESPESAS LIQUIDADAS");
            double jurosLiquidado          = extrair(a06, "RREO6JurosEEncargosDaDivida", "DESPESAS LIQUIDADAS");
            double jurosDotacao            = extrair(a06, "RREO6JurosEEncargosDaDivida", "DOTAÇÃO ATUALIZADA");

            System.out.printf("[Divida] DC=%.2f DCL=%.2f Deducoes=%.2f Amort=%.2f Juros=%.2f%n",
                    dividaConsolidada, dividaConsolidadaLiquida, deducoesDivida,
                    amortizacaoLiquidado, jurosLiquidado);

            Map<String, Double> dotacaoInicial    = execucaoPorColuna(a01, "DOTAÇÃO INICIAL");
            Map<String, Double> dotacaoAtualizada = execucaoPorColuna(a01, "DOTAÇÃO ATUALIZADA");
            Map<String, Double> despesaEmpenhada  = execucaoPorColuna(a01, "EMPENHADAS ATÉ O BIMESTRE");
            Map<String, Double> despesaLiquidada  = execucaoPorColuna(a01, "LIQUIDADAS ATÉ O BIMESTRE");

            EstadoRepository.InfoEstado infoEstado = fEstado.get();

            DashboardData data = new DashboardData(
                    receitaTotal,
                    despesaTotal,
                    resultadoPrimario,
                    rclTotal,
                    despesaPorFuncao(a06),
                    receitaPorCategoria(a06),
                    receitasPorNatureza(a02),
                    dotacaoInicial,
                    dotacaoAtualizada,
                    despesaEmpenhada,
                    despesaLiquidada,
                    despesaPorFuncaoA03(a03),
                    fSelic.get(),
                    fIpca.get(),
                    fDolar.get(),
                    fPib.get(),
                    fDivida.get(),
                    infoEstado.populacao(),
                    infoEstado.nome(),
                    formatarPeriodo(ano, periodo),
                    idEnte,
                    ano,
                    periodo,
                    mdePercentualAplicado,
                    LIMITE_MDE_PERCENTUAL,
                    opCreditoTotal,
                    opCreditoLimiteRcl,
                    opCreditoPercentualRcl,
                    restosProcessados,
                    restosNaoProcessados,
                    restosTotal,
                    dividaConsolidada,
                    dividaConsolidadaLiquida,
                    deducoesDivida,
                    amortizacaoDotacao,
                    amortizacaoLiquidado,
                    jurosLiquidado,
                    jurosDotacao
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

        double selic  = fetchSelicComCache();
        double ipca   = fetchIpcaComCache();
        double dolar  = fetchDolarComCache();
        double pib    = fetchPibComCache();
        double divida = fetchDividaComCache();

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


    private double extrairMdePercentual(List<RreoItem> a12, List<RreoItem> a02, double receitaTotal) {
        if (a12 == null || a12.isEmpty()) {
            System.out.println("[MDE] Anexo 12 vazio! Aplicando fallback usando a Função 12 do Anexo 02...");

            double despesaEducacao = a02.stream()
                    .filter(i -> "12".equals(i.co_conta) || "Educacao".equalsIgnoreCase(i.co_conta))
                    .filter(i -> i.coluna.contains("LIQUIDADAS"))
                    .mapToDouble(i -> i.vl_resultado)
                    .sum();

            return receitaTotal > 0 ? (despesaEducacao / receitaTotal) * 100.0 : 0.0;
        }

        return 0.0;
    }

    private double extrairOpCreditoTotal(List<RreoItem> a09) {
        double total = extrair(a09, "OperacoesDeCredito", "ATÉ O BIMESTRE (b)");
        if (total > 0) return total;
        total = extrair(a09, "TotalOperacoesDeCredito", "ATÉ O BIMESTRE (b)");
        if (total > 0) return total;

        System.err.println("[OpCredito] A09 vazio — logando co_conta disponíveis:");
        a09.stream().limit(10).forEach(i ->
                System.out.printf("  co_conta=%s coluna=%s%n", i.co_conta, i.coluna));
        return 0.0;
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
        try {
            if (!bcbRepo.cacheValido(SERIE_SELIC)) {
                bcbRepo.salvarBatch(SERIE_SELIC, bcb.fetchSerie(SERIE_SELIC, 1));
            }
        } catch (Exception e) {
            System.err.println("[BCB] Erro SELIC: " + e.getMessage());
        }
        return bcbRepo.buscarUltimoValor(SERIE_SELIC);
    }

    private double fetchIpcaComCache() {
        try {
            if (!bcbRepo.cacheValido(SERIE_IPCA)) {
                bcbRepo.salvarBatch(SERIE_IPCA, bcb.fetchSerie(SERIE_IPCA, 12));
            }
            return bcb.fetchIpca12m();
        } catch (Exception e) {
            System.err.println("[BCB] Erro IPCA: " + e.getMessage());
            return bcbRepo.buscarUltimoValor(SERIE_IPCA);
        }
    }

    private double fetchDolarComCache() {
        try {
            if (!bcbRepo.cacheValido(SERIE_DOLAR)) {
                bcbRepo.salvarBatch(SERIE_DOLAR, bcb.fetchSerie(SERIE_DOLAR, 1));
            }
        } catch (Exception e) {
            System.err.println("[BCB] Erro DÓLAR: " + e.getMessage());
        }
        return bcbRepo.buscarUltimoValor(SERIE_DOLAR);
    }

    private double fetchPibComCache() {
        try {
            if (!bcbRepo.cacheValido(SERIE_PIB_CRESC)) {
                bcbRepo.salvarBatch(SERIE_PIB_CRESC, bcb.fetchSerie(SERIE_PIB_CRESC, 2));
            }
        } catch (Exception e) {
            System.err.println("[BCB] Erro PIB: " + e.getMessage());
        }
        return bcbRepo.buscarUltimoValor(SERIE_PIB_CRESC);
    }

    private double fetchDividaComCache() {
        double cached = bcbRepo.buscarUltimoValor(SERIE_DIVIDA_PIB);
        if (cached > 0.0) return cached;
        try {
            bcbRepo.salvarBatch(SERIE_DIVIDA_PIB, bcb.fetchSerie(SERIE_DIVIDA_PIB, 2));
        } catch (Exception e) {
            System.err.println("[BCB] Série 13762 indisponível, usando fallback: " + e.getMessage());
            return 76.5;
        }
        return bcbRepo.buscarUltimoValor(SERIE_DIVIDA_PIB);
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
        Map<String, Double> resultado = new java.util.LinkedHashMap<>();
        List.of(
                "RREO6PessoalEEncargosSociais",
                "RREO6OutrasDespesasCorrentes",
                "RREO6AmortizacaoDaDivida",
                "RREO6Investimentos",
                "DespesasPrimariasCorrentesComFontesRPPS"
        ).forEach(cat ->
                items.stream()
                        .filter(i -> cat.equalsIgnoreCase(i.co_conta))
                        .filter(i -> "DESPESAS LIQUIDADAS".equalsIgnoreCase(i.coluna))
                        .forEach(i -> resultado.merge(i.co_conta, i.vl_resultado, Double::sum))
        );
        return resultado;
    }

    private Map<String, Double> receitaPorCategoria(List<RreoItem> items) {
        double icms  = extrair(items, "RREO6ICMS",                        "RECEITAS REALIZADAS (a)");
        double ipva  = extrair(items, "RREO6IPVA",                        "RECEITAS REALIZADAS (a)");
        double itcd  = extrair(items, "RREO6ITCD",                        "RECEITAS REALIZADAS (a)");
        double trib  = extrair(items, "RREO6ReceitasTributarias",          "RECEITAS REALIZADAS (a)");
        double total = extrair(items, "ReceitasCorrentesExcetoFontesRPPS", "RECEITAS REALIZADAS (a)");

        Map<String, Double> map = new java.util.LinkedHashMap<>();
        map.put("ICMS",               icms);
        map.put("IPVA",               ipva);
        map.put("ITCD",               itcd);
        map.put("Outras Tributárias", Math.max(trib - icms - ipva - itcd, 0));
        map.put("Não Tributárias",    Math.max(total - trib, 0));
        return map;
    }


    private Map<String, Double> receitasPorNatureza(List<RreoItem> a02) {
        Map<String, Double> map = new java.util.LinkedHashMap<>();
        List.of(
                "ReceitasTributarias",
                "ReceitasDeContribuicoes",
                "ReceitasPatrimoniais",
                "TransferenciasCorrentes",
                "OutrasReceitasCorrentes",
                "ReceitasDeCapital"
        ).forEach(conta -> {
            double v = extrair(a02, conta, "RECEITAS REALIZADAS");
            if (v == 0) v = extrair(a02, conta, "RECEITAS REALIZADAS (a)");
            if (v > 0) map.put(conta, v);
        });
        return map;
    }

    private Map<String, Double> execucaoPorColuna(List<RreoItem> lista, String termoColuna) {
        Map<String, Double> map = new java.util.LinkedHashMap<>();

        final String colunaBusca = termoColuna.toUpperCase();

        List.of(
                "Legislativa", "Judiciaria", "EssencialJustica",
                "Administracao", "DefesaNacional", "SegurancaPublica",
                "RelacoesExteriores", "AssistenciaSocial", "Previdencia",
                "Saude", "TrabalhoEmprego", "Educacao", "CulturaLazer",
                "DireitoCidadania", "Urbanismo", "Habitacao",
                "SaneamentoBasico", "GestaoAmbiental", "AgriculturaOrdenamentoPesca",
                "OrganizacaoAgraria", "Industria", "ComercioServicos",
                "Comunicacoes", "EnergiaElétrica", "Transporte",
                "DesportoLazer", "EncargosDivida", "Reserva"
        ).forEach(funcao -> {

            double v = lista.stream()
                    .filter(i -> i.co_conta != null && i.coluna != null)
                    .filter(i -> i.co_conta.equalsIgnoreCase(funcao) || i.co_conta.contains(funcao))
                    .filter(i -> i.coluna.toUpperCase().contains(colunaBusca))
                    .mapToDouble(i -> i.vl_resultado)
                    .findFirst()
                    .orElse(0.0);

            if (v > 0) {
                map.put(funcao, v);
            }
        });

        return map;
    }

    private Map<String, Double> despesaPorFuncaoA03(List<RreoItem> a03) {
        return execucaoPorColuna(a03, "DESPESAS LIQUIDADAS");
    }

    private double extrairColunaBimestre(List<RreoItem> items, String coConta) {
        return items.stream()
                .filter(i -> coConta.equalsIgnoreCase(i.co_conta))
                .filter(i -> i.coluna != null &&
                        i.coluna.toLowerCase().contains("até o bimestre"))
                .mapToDouble(i -> i.vl_resultado)
                .findFirst()
                .orElse(0.0);
    }

    private String formatarPeriodo(int ano, int periodo) {
        String[] meses = {"Até Fevereiro", "Até Abril", "Até Junho",
                "Até Agosto",    "Até Outubro", "Até Dezembro"};
        String label = periodo >= 1 && periodo <= 6 ? meses[periodo - 1] : "Período " + periodo;
        return ano + " — " + periodo + "º Bimestre (" + label + ")";
    }
}