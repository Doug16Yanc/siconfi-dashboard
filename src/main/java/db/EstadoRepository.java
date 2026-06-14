package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EstadoRepository {

    public record InfoEstado(String nome, long populacao) {}

    public InfoEstado buscarPorId(String idEnte) {
        String sql = """
            SELECT nome, COALESCE(populacao, 1) as populacao 
            FROM estado_cache 
            WHERE id_ente = ?
            """;

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, idEnte);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nome = rs.getString("nome");
                    long populacao = rs.getLong("populacao");
                    return new InfoEstado(nome, populacao);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Erro ao buscar metadados do estado: " + e.getMessage());
        }

        return new InfoEstado("Ente " + idEnte, 1L);
    }
}
