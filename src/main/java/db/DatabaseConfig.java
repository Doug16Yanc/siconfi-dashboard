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
        migrarTabelas();
        popularEstados();
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
                id         SERIAL PRIMARY KEY,
                serie      INT,
                data_ref   DATE,
                valor      NUMERIC(18,6),
                fetched_at TIMESTAMP DEFAULT NOW(),
                UNIQUE (serie, data_ref)
            );
            """;

        String snapshot = """
            CREATE TABLE IF NOT EXISTS dashboard_snapshot (
                id                        SERIAL PRIMARY KEY,
                id_ente                   VARCHAR(10)   NOT NULL,
                an_exercicio              INT           NOT NULL,
                nr_periodo                INT           NOT NULL,
                receita_total             NUMERIC(18,2),
                despesa_total             NUMERIC(18,2),
                resultado_primario        NUMERIC(18,2),
                rcl_total                 NUMERIC(18,2),
                selic                     NUMERIC(8,4),
                ipca_12m                  NUMERIC(8,4),
                dolar                     NUMERIC(10,4),
                crescimento_pib           NUMERIC(8,4),
                divida_bruta_pib          NUMERIC(8,4),
                populacao                 BIGINT,
                nome_ente                 VARCHAR(60),
                mde_percentual_aplicado   NUMERIC(8,4)  DEFAULT 0,
                op_credito_total          NUMERIC(18,2) DEFAULT 0,
                op_credito_limite_rcl     NUMERIC(18,2) DEFAULT 0,
                op_credito_percentual_rcl NUMERIC(8,4)  DEFAULT 0,
                created_at                TIMESTAMP DEFAULT NOW(),
                UNIQUE (id_ente, an_exercicio, nr_periodo)
            );
            """;

        String estados = """
            CREATE TABLE IF NOT EXISTS estado_cache (
                id_ente   VARCHAR(10) PRIMARY KEY,
                sigla     CHAR(2)     NOT NULL,
                nome      VARCHAR(60) NOT NULL,
                regiao    VARCHAR(20) NOT NULL,
                populacao BIGINT
            );
            """;

        try (var con = getConnection(); var st = con.createStatement()) {
            st.execute(rreo);
            st.execute(bcb);
            st.execute(snapshot);
            st.execute(estados);
            System.out.println("[DB] Tabelas verificadas/criadas com sucesso.");
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabelas: " + e.getMessage(), e);
        }
    }

    private static void migrarTabelas() {
        String[] migracoes = {
                "ALTER TABLE dashboard_snapshot ADD COLUMN IF NOT EXISTS crescimento_pib           NUMERIC(8,4)  DEFAULT 0",
                "ALTER TABLE dashboard_snapshot ADD COLUMN IF NOT EXISTS divida_bruta_pib          NUMERIC(8,4)  DEFAULT 0",
                "ALTER TABLE dashboard_snapshot ADD COLUMN IF NOT EXISTS populacao                 BIGINT        DEFAULT 0",
                "ALTER TABLE dashboard_snapshot ADD COLUMN IF NOT EXISTS nome_ente                 VARCHAR(60)   DEFAULT ''",
                "ALTER TABLE dashboard_snapshot ADD COLUMN IF NOT EXISTS mde_percentual_aplicado   NUMERIC(8,4)  DEFAULT 0",
                "ALTER TABLE dashboard_snapshot ADD COLUMN IF NOT EXISTS op_credito_total          NUMERIC(18,2) DEFAULT 0",
                "ALTER TABLE dashboard_snapshot ADD COLUMN IF NOT EXISTS op_credito_limite_rcl     NUMERIC(18,2) DEFAULT 0",
                "ALTER TABLE dashboard_snapshot ADD COLUMN IF NOT EXISTS op_credito_percentual_rcl NUMERIC(8,4)  DEFAULT 0",
                "ALTER TABLE dashboard_snapshot ADD COLUMN IF NOT EXISTS restos_processados     NUMERIC(18,2) DEFAULT 0",
                "ALTER TABLE dashboard_snapshot ADD COLUMN IF NOT EXISTS restos_nao_processados NUMERIC(18,2) DEFAULT 0",
                "ALTER TABLE dashboard_snapshot ADD COLUMN IF NOT EXISTS restos_total           NUMERIC(18,2) DEFAULT 0",
                "ALTER TABLE dashboard_snapshot ADD COLUMN IF NOT EXISTS divida_consolidada        NUMERIC(18,2) DEFAULT 0",
                "ALTER TABLE dashboard_snapshot ADD COLUMN IF NOT EXISTS divida_consolidada_liquida NUMERIC(18,2) DEFAULT 0",
                "ALTER TABLE dashboard_snapshot ADD COLUMN IF NOT EXISTS deducoes_divida           NUMERIC(18,2) DEFAULT 0",
                "ALTER TABLE dashboard_snapshot ADD COLUMN IF NOT EXISTS amortizacao_dotacao       NUMERIC(18,2) DEFAULT 0",
                "ALTER TABLE dashboard_snapshot ADD COLUMN IF NOT EXISTS amortizacao_liquidado     NUMERIC(18,2) DEFAULT 0",
                "ALTER TABLE dashboard_snapshot ADD COLUMN IF NOT EXISTS juros_liquidado           NUMERIC(18,2) DEFAULT 0",
                "ALTER TABLE dashboard_snapshot ADD COLUMN IF NOT EXISTS juros_dotacao             NUMERIC(18,2) DEFAULT 0"
        };

        try (var con = getConnection(); var st = con.createStatement()) {
            for (String ddl : migracoes) st.execute(ddl);
            System.out.println("[DB] Migrações aplicadas com sucesso.");
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao migrar tabelas: " + e.getMessage(), e);
        }
    }

    private static void popularEstados() {
        String[][] estados = {
                {"11","RO","Rondônia",           "Norte"},
                {"12","AC","Acre",               "Norte"},
                {"13","AM","Amazonas",           "Norte"},
                {"14","RR","Roraima",            "Norte"},
                {"15","PA","Pará",               "Norte"},
                {"16","AP","Amapá",              "Norte"},
                {"17","TO","Tocantins",          "Norte"},
                {"21","MA","Maranhão",           "Nordeste"},
                {"22","PI","Piauí",              "Nordeste"},
                {"23","CE","Ceará",              "Nordeste"},
                {"24","RN","Rio Grande do Norte","Nordeste"},
                {"25","PB","Paraíba",            "Nordeste"},
                {"26","PE","Pernambuco",         "Nordeste"},
                {"27","AL","Alagoas",            "Nordeste"},
                {"28","SE","Sergipe",            "Nordeste"},
                {"29","BA","Bahia",              "Nordeste"},
                {"31","MG","Minas Gerais",       "Sudeste"},
                {"32","ES","Espírito Santo",     "Sudeste"},
                {"33","RJ","Rio de Janeiro",     "Sudeste"},
                {"35","SP","São Paulo",          "Sudeste"},
                {"41","PR","Paraná",             "Sul"},
                {"42","SC","Santa Catarina",     "Sul"},
                {"43","RS","Rio Grande do Sul",  "Sul"},
                {"50","MS","Mato Grosso do Sul", "Centro-Oeste"},
                {"51","MT","Mato Grosso",        "Centro-Oeste"},
                {"52","GO","Goiás",              "Centro-Oeste"},
                {"53","DF","Distrito Federal",   "Centro-Oeste"},
        };

        String sql = """
            INSERT INTO estado_cache (id_ente, sigla, nome, regiao)
            VALUES (?, ?, ?, ?)
            ON CONFLICT (id_ente) DO NOTHING
            """;

        try (var con = getConnection(); var ps = con.prepareStatement(sql)) {
            for (String[] e : estados) {
                ps.setString(1, e[0]);
                ps.setString(2, e[1]);
                ps.setString(3, e[2]);
                ps.setString(4, e[3]);
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("[DB] estado_cache populado.");
        } catch (SQLException e) {
            System.err.println("[DB] Aviso ao popular estados: " + e.getMessage());
        }
    }
}