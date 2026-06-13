package db;

import service.DashboardService.DashboardData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class SnapshotRepository {

    public void salvar(String idEnte, int ano, int periodo, DashboardData d) {
        String sql = """
            INSERT INTO dashboard_snapshot
                (id_ente, an_exercicio, nr_periodo,
                 receita_total, despesa_total, resultado_primario, rcl_total,
                 selic, ipca_12m, dolar)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id_ente, an_exercicio, nr_periodo)
            DO UPDATE SET
                receita_total      = EXCLUDED.receita_total,
                despesa_total      = EXCLUDED.despesa_total,
                resultado_primario = EXCLUDED.resultado_primario,
                rcl_total          = EXCLUDED.rcl_total,
                selic              = EXCLUDED.selic,
                ipca_12m           = EXCLUDED.ipca_12m,
                dolar              = EXCLUDED.dolar,
                created_at         = NOW()
            """;

        try (var con = DatabaseConfig.getConnection();
             var ps  = con.prepareStatement(sql)) {
            ps.setString(1, idEnte);
            ps.setInt   (2, ano);
            ps.setInt   (3, periodo);
            ps.setDouble(4, d.receitaTotal());
            ps.setDouble(5, d.despesaTotal());
            ps.setDouble(6, d.resultadoPrimario());
            ps.setDouble(7, d.rclTotal());
            ps.setDouble(8, d.selic());
            ps.setDouble(9, d.ipca12m());
            ps.setDouble(10, d.dolar());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Snapshot] Erro ao salvar: " + e.getMessage());
        }
    }

    public Optional<DashboardData> buscarPorPeriodo(String idEnte, int ano, int periodo) {
        String sql = """
            SELECT receita_total, despesa_total, resultado_primario,
                   rcl_total, selic, ipca_12m, dolar
            FROM dashboard_snapshot
            WHERE id_ente = ? AND an_exercicio = ? AND nr_periodo = ?
            """;

        try (var con = DatabaseConfig.getConnection();
             var ps  = con.prepareStatement(sql)) {
            ps.setString(1, idEnte);
            ps.setInt   (2, ano);
            ps.setInt   (3, periodo);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs, idEnte, ano, periodo));
            }
        } catch (SQLException e) {
            System.err.println("[Snapshot] Erro ao buscar período: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<DashboardData> buscarSerie(String idEnte, int ano) {
        String sql = """
            SELECT receita_total, despesa_total, resultado_primario,
                   rcl_total, selic, ipca_12m, dolar, nr_periodo
            FROM dashboard_snapshot
            WHERE id_ente = ? AND an_exercicio = ?
            ORDER BY nr_periodo ASC
            """;

        List<DashboardData> serie = new ArrayList<>();
        try (var con = DatabaseConfig.getConnection();
             var ps  = con.prepareStatement(sql)) {
            ps.setString(1, idEnte);
            ps.setInt   (2, ano);
            var rs = ps.executeQuery();
            while (rs.next()) {
                serie.add(mapRow(rs, idEnte, ano, rs.getInt("nr_periodo")));
            }
        } catch (SQLException e) {
            System.err.println("[Snapshot] Erro ao buscar série: " + e.getMessage());
        }
        return serie;
    }

    private DashboardData mapRow(ResultSet rs, String idEnte, int ano, int periodo)
            throws SQLException {
        return new DashboardData(
                rs.getDouble("receita_total"),
                rs.getDouble("despesa_total"),
                rs.getDouble("resultado_primario"),
                rs.getDouble("rcl_total"),
                Map.of(),
                Map.of(),
                rs.getDouble("selic"),
                rs.getDouble("ipca_12m"),
                rs.getDouble("dolar"),
                formatarPeriodo(ano, periodo),
                idEnte,
                ano,
                periodo
        );
    }

    private String formatarPeriodo(int ano, int periodo) {
        String[] meses = {"Até Fevereiro", "Até Abril", "Até Junho",
                "Até Agosto",    "Até Outubro", "Até Dezembro"};
        String label = periodo >= 1 && periodo <= 6 ? meses[periodo - 1] : "Período " + periodo;
        return ano + " — " + periodo + "º Bimestre (" + label + ")";
    }
}