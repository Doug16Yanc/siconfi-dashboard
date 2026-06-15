package service;

import db.EstadoRepository;
import db.SnapshotRepository;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ComparacoesService {

    private final DashboardService   dashService = new DashboardService();
    private final SnapshotRepository snapRepo    = new SnapshotRepository();
    private final EstadoRepository   estadoRepo  = new EstadoRepository();

    public enum Regiao {
        NORTE     ("Norte",       List.of("11","12","13","14","15","16","17")),
        NORDESTE  ("Nordeste",    List.of("21","22","23","24","25","26","27","28","29")),
        SUDESTE   ("Sudeste",     List.of("31","32","33","35")),
        SUL       ("Sul",         List.of("41","42","43")),
        CENTRO_OESTE("Centro-Oeste", List.of("50","51","52","53"));

        public final String label;
        public final List<String> entes;
        Regiao(String label, List<String> entes) {
            this.label = label;
            this.entes = entes;
        }
    }

    public enum Metrica {
        RESULTADO_RCL   ("Resultado Primário / RCL"),
        PESSOAL_RCL     ("Gastos Pessoal / RCL"),
        INVESTIMENTOS_RCL("Investimentos / RCL"),
        RECEITA_HAB     ("Receita / hab.");

        public final String label;
        Metrica(String label) { this.label = label; }
    }

    public record DadosComparacao(
            Map<String, Map<Integer, Double>> heatmap,
            Map<String, String>               nomes,
            Regiao                            regiao,
            Metrica                           metrica,
            int                               ano
    ) {}

    public DadosComparacao carregar(Regiao regiao, int ano, Metrica metrica) {
        List<String> entes = regiao.entes;

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            Map<String, Future<List<DashboardService.DashboardData>>> futures =
                    new LinkedHashMap<>();

            for (String idEnte : entes) {
                futures.put(idEnte, executor.submit(() ->
                        dashService.carregarSerie(idEnte, ano)));
            }

            Map<String, Map<Integer, Double>> heatmap = new LinkedHashMap<>();
            Map<String, String>               nomes   = new LinkedHashMap<>();

            for (var entry : futures.entrySet()) {
                String idEnte = entry.getKey();
                try {
                    List<DashboardService.DashboardData> serie = entry.getValue().get();
                    Map<Integer, Double> porBimestre = new LinkedHashMap<>();

                    for (DashboardService.DashboardData d : serie) {
                        double valor = extrairMetrica(d, metrica);
                        porBimestre.put(d.numeroPeriodo(), valor);
                    }

                    heatmap.put(idEnte, porBimestre);

                    serie.stream().findFirst().ifPresent(d ->
                            nomes.put(idEnte, sigla(d.nomeEnte())));

                } catch (Exception e) {
                    System.err.printf("[Comparacoes] Erro ente=%s: %s%n",
                            idEnte, e.getMessage());
                    heatmap.put(idEnte, Map.of());
                }
            }

            for (String idEnte : entes) {
                nomes.computeIfAbsent(idEnte, id -> {
                    var info = estadoRepo.buscarPorId(id);
                    return info.sigla() != null ? info.sigla() : id;
                });
            }

            return new DadosComparacao(heatmap, nomes, regiao, metrica, ano);
        }
    }

    private double extrairMetrica(DashboardService.DashboardData d, Metrica m) {
        return switch (m) {
            case RESULTADO_RCL    -> d.rclTotal() > 0
                    ? d.resultadoPrimario() / d.rclTotal() * 100.0 : 0.0;
            case PESSOAL_RCL      -> d.rclTotal() > 0
                    ? d.despesaPorFuncao().getOrDefault(
                    "RREO6PessoalEEncargosSociais", 0.0)
                    / d.rclTotal() * 100.0 : 0.0;
            case INVESTIMENTOS_RCL -> d.rclTotal() > 0
                    ? d.despesaPorFuncao().getOrDefault(
                    "RREO6Investimentos", 0.0)
                    / d.rclTotal() * 100.0 : 0.0;
            case RECEITA_HAB      -> d.populacao() > 0
                    ? d.receitaTotal() / d.populacao() : 0.0;
        };
    }

    private String sigla(String nomeEnte) {
        Map<String, String> map = Map.ofEntries(
                Map.entry("Rondônia","RO"), Map.entry("Acre","AC"),
                Map.entry("Amazonas","AM"), Map.entry("Roraima","RR"),
                Map.entry("Pará","PA"), Map.entry("Amapá","AP"),
                Map.entry("Tocantins","TO"), Map.entry("Maranhão","MA"),
                Map.entry("Piauí","PI"), Map.entry("Ceará","CE"),
                Map.entry("Rio Grande do Norte","RN"), Map.entry("Paraíba","PB"),
                Map.entry("Pernambuco","PE"), Map.entry("Alagoas","AL"),
                Map.entry("Sergipe","SE"), Map.entry("Bahia","BA"),
                Map.entry("Minas Gerais","MG"), Map.entry("Espírito Santo","ES"),
                Map.entry("Rio de Janeiro","RJ"), Map.entry("São Paulo","SP"),
                Map.entry("Paraná","PR"), Map.entry("Santa Catarina","SC"),
                Map.entry("Rio Grande do Sul","RS"), Map.entry("Mato Grosso do Sul","MS"),
                Map.entry("Mato Grosso","MT"), Map.entry("Goiás","GO"),
                Map.entry("Distrito Federal","DF")
        );
        return map.getOrDefault(nomeEnte, nomeEnte);
    }
}