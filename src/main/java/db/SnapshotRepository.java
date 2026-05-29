package db;

import service.DashboardService.DashboardData;
import java.sql.*;

public class SnapshotRepository {

    public void salvar(String idEnte, int ano, int periodo, DashboardData data) {
        String sql = """
            INSERT INTO dashboard_snapshot
                (id_ente, an_exercicio, nr_periodo,
                 receita_total, despesa_total, resultado_primario,
                 rcl_total, selic, ipca_12m, dolar)
            VALUES (?,?,?,?,?,?,?,?,?,?)
            """;

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, idEnte);
            ps.setInt   (2, ano);
            ps.setInt   (3, periodo);
            ps.setDouble(4, data.receitaTotal());
            ps.setDouble(5, data.despesaTotal());
            ps.setDouble(6, data.resultadoPrimario());
            ps.setDouble(7, data.rclTotal());
            ps.setDouble(8, data.selic());
            ps.setDouble(9, data.ipca12m());
            ps.setDouble(10, data.dolar());

            ps.executeUpdate();
            System.out.println("[DB] Snapshot salvo.");

        } catch (SQLException e) {
            System.err.println("[DB] Aviso: snapshot não salvo — " + e.getMessage());
        }
    }
}
