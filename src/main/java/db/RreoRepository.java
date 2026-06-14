package db;

import api.dto.RreoItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RreoRepository {

    public void salvarBatch(List<RreoItem> items) {
        if (items == null || items.isEmpty()) return;

        String sqlRreo = """
            INSERT INTO rreo_cache
                (id_ente, an_exercicio, nr_periodo, no_anexo,
                 co_esfera, co_uf, no_uf, no_conta, co_conta,
                 coluna, rotulo, vl_resultado, no_periodo)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)
            ON CONFLICT (id_ente, an_exercicio, nr_periodo, no_anexo, co_conta, coluna)
            DO UPDATE SET
                vl_resultado = EXCLUDED.vl_resultado,
                fetched_at   = NOW()
            """;

        String sqlEstado = """
            UPDATE estado_cache
            SET populacao = ?
            WHERE id_ente = ?
            """;

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement psRreo = con.prepareStatement(sqlRreo);
             PreparedStatement psEstado = con.prepareStatement(sqlEstado)) {

            con.setAutoCommit(false);

            for (RreoItem i : items) {
                psRreo.setString(1,  i.id_ente);
                psRreo.setInt   (2,  i.an_exercicio);
                psRreo.setInt   (3,  i.nr_periodo);
                psRreo.setString(4,  i.no_anexo);
                psRreo.setString(5,  i.co_esfera);
                psRreo.setString(6,  i.co_uf);
                psRreo.setString(7,  i.no_uf);
                psRreo.setString(8,  i.no_conta);
                psRreo.setString(9,  i.co_conta);
                psRreo.setString(10, i.coluna);
                psRreo.setString(11, i.rotulo);
                psRreo.setDouble(12, i.vl_resultado);
                psRreo.setString(13, i.no_periodo);
                psRreo.addBatch();
            }

            psRreo.executeBatch();

            RreoItem primeiroItem = items.get(0);
            if (primeiroItem.populacao > 0 && primeiroItem.id_ente != null) {
                psEstado.setLong(1, primeiroItem.populacao);
                psEstado.setString(2, primeiroItem.id_ente);
                psEstado.executeUpdate();
            }

            con.commit();
            System.out.printf("[DB] %d itens RREO salvos e população (%d) do ente %s atualizada.%n",
                    items.size(), primeiroItem.populacao, primeiroItem.id_ente);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar RREO e atualizar população: " + e.getMessage(), e);
        }
    }

    public List<RreoItem> buscarCache(String idEnte, int ano, int periodo, String anexo) {
        String sql = """
            SELECT id_ente, an_exercicio, nr_periodo, no_anexo,
                   co_esfera, co_uf, no_uf, no_conta, co_conta,
                   coluna, rotulo, vl_resultado, no_periodo
            FROM rreo_cache
            WHERE id_ente = ? AND an_exercicio = ?
              AND nr_periodo = ? AND no_anexo = ?
            ORDER BY co_conta
            """;

        List<RreoItem> result = new ArrayList<>();

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, idEnte);
            ps.setInt   (2, ano);
            ps.setInt   (3, periodo);
            ps.setString(4, anexo);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                RreoItem item = new RreoItem();

                item.id_ente = rs.getString("id_ente");
                item.an_exercicio = rs.getInt ("an_exercicio");
                item.nr_periodo = rs.getInt ("nr_periodo");
                item.no_anexo = rs.getString("no_anexo");
                item.co_esfera = rs.getString("co_esfera");
                item.co_uf = rs.getString("co_uf");
                item.no_uf = rs.getString("no_uf");
                item.no_conta = rs.getString("no_conta");
                item.co_conta = rs.getString("co_conta");
                item.vl_resultado = rs.getDouble("vl_resultado");
                item.no_periodo = rs.getString("no_periodo");
                item.coluna = rs.getString("coluna");
                item.rotulo = rs.getString("rotulo");

                result.add(item);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar cache: " + e.getMessage(), e);
        }

        return result;
    }

    public boolean cacheValido(String idEnte, int ano, int periodo, String anexo) {
        String sql = """
            SELECT COUNT(*) FROM rreo_cache
            WHERE id_ente = ? AND an_exercicio = ?
              AND nr_periodo = ? AND no_anexo = ?
              AND fetched_at > NOW() - INTERVAL '1 hour'
            """;

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, idEnte);
            ps.setInt   (2, ano);
            ps.setInt   (3, periodo);
            ps.setString(4, anexo);

            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            return false;
        }
    }
}