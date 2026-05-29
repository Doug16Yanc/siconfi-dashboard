package db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConfig {

    private static HikariDataSource ds;

    public static void init() {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl("jdbc:postgresql://localhost:5432/siconfi_db");
        cfg.setUsername(System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "postgres");
        cfg.setPassword(System.getenv("DB_PASS"));
        cfg.setMaximumPoolSize(10);
        cfg.setMinimumIdle(2);
        cfg.setConnectionTimeout(30_000);
        cfg.setIdleTimeout(600_000);
        cfg.setMaxLifetime(1_800_000);
        cfg.addDataSourceProperty("cachePrepStmts", "true");
        cfg.addDataSourceProperty("prepStmtCacheSize", "250");
        cfg.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        cfg.setPoolName("SiconfiPool");

        ds = new HikariDataSource(cfg);
        criarTabelas();
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static void close() {
        if (ds != null && !ds.isClosed()) ds.close();
    }


    private static void criarTabelas() {
        String rreo = """
            CREATE TABLE IF NOT EXISTS rreo_cache (
                id            SERIAL PRIMARY KEY,
                id_ente       VARCHAR(10),
                an_exercicio  INT,
                nr_periodo    INT,
                no_anexo      VARCHAR(60),
                co_esfera     CHAR(1),
                co_uf         CHAR(2),
                no_uf         VARCHAR(100),
                no_conta      VARCHAR(300),
                co_conta      VARCHAR(100),
                coluna        VARCHAR(150),
                rotulo        VARCHAR(250),
                vl_resultado  NUMERIC(18,2),
                no_periodo    VARCHAR(40),
                fetched_at    TIMESTAMP DEFAULT NOW(),
                UNIQUE (id_ente, an_exercicio, nr_periodo, no_anexo, co_conta, coluna)
            );
            """;

        String bcb = """
            CREATE TABLE IF NOT EXISTS bcb_cache (
                id           SERIAL PRIMARY KEY,
                serie        INT,
                data_ref     DATE,
                valor        NUMERIC(18,6),
                fetched_at   TIMESTAMP DEFAULT NOW(),
                UNIQUE (serie, data_ref)
            );
            """;

        String snapshot = """
            CREATE TABLE IF NOT EXISTS dashboard_snapshot (
                id                SERIAL PRIMARY KEY,
                id_ente           VARCHAR(10),
                an_exercicio      INT,
                nr_periodo        INT,
                receita_total     NUMERIC(18,2),
                despesa_total     NUMERIC(18,2),
                resultado_primario NUMERIC(18,2),
                rcl_total         NUMERIC(18,2),
                selic             NUMERIC(8,4),
                ipca_12m          NUMERIC(8,4),
                dolar             NUMERIC(10,4),
                created_at        TIMESTAMP DEFAULT NOW()
            );
            """;

        try (var con = getConnection();
             var st  = con.createStatement()) {
            st.execute(rreo);
            st.execute(bcb);
            st.execute(snapshot);
            System.out.println("[DB] Tabelas verificadas/criadas com sucesso.");
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabelas: " + e.getMessage(), e);
        }
    }
}