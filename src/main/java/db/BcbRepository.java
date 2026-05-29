package db;

import api.dto.BcbSerieItem;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BcbRepository {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void salvarBatch(int serie, List<BcbSerieItem> items) {
        if (items == null || items.isEmpty()) return;

        String sql = """
            INSERT INTO bcb_cache (serie, data_ref, valor)
            VALUES (?, ?, ?)
            ON CONFLICT (serie, data_ref)
            DO UPDATE SET valor = EXCLUDED.valor, fetched_at = NOW()
            """;

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            con.setAutoCommit(false);

            for (BcbSerieItem item : items) {
                ps.setInt   (1, serie);
                ps.setDate  (2, Date.valueOf(LocalDate.parse(item.data, FMT)));
                ps.setDouble(3, item.valorDouble());
                ps.addBatch();
            }

            ps.executeBatch();
            con.commit();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar BCB: " + e.getMessage(), e);
        }
    }

    public double buscarUltimoValor(int serie) {
        String sql = """
            SELECT valor FROM bcb_cache
            WHERE serie = ?
            ORDER BY data_ref DESC
            LIMIT 1
            """;

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, serie);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble("valor") : 0.0;

        } catch (SQLException e) {
            return 0.0;
        }
    }

    public boolean cacheValido(int serie) {
        String sql = """
            SELECT COUNT(*) FROM bcb_cache
            WHERE serie = ?
              AND fetched_at > NOW() - INTERVAL '30 minutes'
            """;

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, serie);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            return false;
        }
    }
}
