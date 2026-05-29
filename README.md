# 📊 SICONFI Dashboard

Dashboard de finanças públicas brasileiras construído com **Java 25 + Swing**, consumindo dados reais da API do Tesouro Nacional (Siconfi) e do Banco Central do Brasil (BCB) em tempo real.

<img width="1912" height="988" alt="Image" src="https://github.com/user-attachments/assets/68e6bc76-b209-421e-881d-ab5b7146e038" />


---

## ✨ Funcionalidades

- **Visão Geral** com KPIs fiscais em tempo real: Receita Total, Despesa Total, Resultado Primário, RCL e SELIC
- **Gráfico de barras** — Receita vs Despesa vs Resultado Primário
- **Gráfico de rosca** — Despesa por categoria (Pessoal, Capital, Amortização, etc.)
- **Gráfico de rosca** — Receita por categoria (ICMS, IPVA, Tributárias, etc.)
- **Status bar** com indicadores macroeconômicos: Dólar, SELIC, IPCA 12m, PIB
- **Cache inteligente** — dados persistidos no PostgreSQL com TTL de 1 hora
- **Tema dark** com tipografia profissional (Inter + JetBrains Mono)
- **Ícones SVG** via FlatLaf Extras

---

## 🏗️ Arquitetura

```
siconfi-dashboard/
├── src/main/java/
│   ├── Main.java                        # Bootstrap — FlatLaf + DB + Virtual Threads
│   ├── api/
│   │   ├── client/
│   │   │   ├── SiconfiClient.java       # HTTP client — API Siconfi (Tesouro Nacional)
│   │   │   └── BcbClient.java           # HTTP client — API BCB (Banco Central)
│   │   └── dto/
│   │       ├── RreoItem.java            # DTO do RREO com @SerializedName
│   │       ├── RreoResponse.java        # Wrapper da resposta paginada
│   │       └── BcbSerieItem.java        # DTO das séries temporais do BCB
│   ├── db/
│   │   ├── DatabaseConfig.java          # HikariCP pool + DDL automático
│   │   ├── RreoRepository.java          # CRUD + batch insert + cache-aside
│   │   ├── BcbRepository.java           # Persistência das séries BCB
│   │   └── SnapshotRepository.java      # Histórico de snapshots do dashboard
│   ├── service/
│   │   └── DashboardService.java        # Orquestração com Virtual Threads
│   └── ui/
│       ├── MainFrame.java               # JFrame principal — sidebar + topbar + statusbar
│       ├── FontManager.java             # Carregamento de Inter e JetBrains Mono
│       └── dashboard/
│           ├── DashboardPanel.java      # Painel principal com KPIs e gráficos
│           ├── KpiCard.java             # Cards de métricas com formatação automática
│           └── charts/
│               ├── BarChartPanel.java   # Gráfico de barras (JFreeChart)
│               └── DonutChartPanel.java # Gráfico de rosca (JFreeChart RingChart)
└── src/main/resources/
    ├── fonts/
    │   ├── Inter-Regular.ttf
    │   ├── Inter-Medium.ttf
    │   ├── Inter-Bold.ttf
    │   ├── JetBrainsMono-Regular.ttf
    │   └── JetBrainsMono-Bold.ttf
    └── icons/
        ├── home.svg
        ├── receitas.svg
        └── ...
```

---

## ⚡ Stack Tecnológica

| Camada | Tecnologia | Versão |
|---|---|---|
| Linguagem | Java | 25 |
| UI Framework | Swing + FlatLaf Dark | 3.4.1 |
| Gráficos | JFreeChart | 1.5.4 |
| HTTP Client | OkHttp | 4.12.0 |
| JSON | Gson | 2.10.1 |
| Banco de dados | PostgreSQL | 42.7.3 |
| Connection Pool | HikariCP | 5.1.0 |
| Build | Maven | 3.x |

---

## 🧵 Virtual Threads — Java 21+

Uma das features mais modernas do projeto. Em vez de `SwingWorker` ou thread pools manuais, usamos **Virtual Threads** (Project Loom) para disparar chamadas paralelas à API sem bloquear a EDT:

```java
// 4 chamadas paralelas — tempo total = o do endpoint mais lento
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

    Future<List<RreoItem>> fA06   = executor.submit(() -> fetchComCache("RREO-Anexo 06"));
    Future<Double>         fSelic = executor.submit(this::fetchSelicComCache);
    Future<Double>         fIpca  = executor.submit(this::fetchIpcaComCache);
    Future<Double>         fDolar = executor.submit(this::fetchDolarComCache);

    // Todos rodam em paralelo — aguarda o mais lento
    List<RreoItem> a06 = fA06.get();
    // ...
}

// Snapshot histórico salvo em background — não bloqueia o retorno
Thread.ofVirtual().start(() -> snapRepo.salvar(ID_ENTE, ANO, PERIODO, data));
```

**Benefícios práticos neste projeto:**
- A API Siconfi pode demorar 3–8s por chamada — com Virtual Threads, 4 chamadas rodam no tempo da mais lenta, não na soma
- O snapshot é persistido em background sem atrasar a atualização da UI
- Código linear e legível — sem callbacks, sem `doInBackground()`, sem `done()`

---

## 🗄️ Cache-aside com PostgreSQL

O padrão **cache-aside** evita chamadas repetidas à API:

```
Requisição de dados
  └── Cache válido? (< 1 hora)
        ├── SIM → retorna do PostgreSQL instantaneamente
        └── NÃO → chama API → salva no PostgreSQL → retorna
```

Na segunda execução do app, todos os dados vêm do banco local em milissegundos.

### Schema

```sql
CREATE TABLE rreo_cache (
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

CREATE TABLE bcb_cache (
    id         SERIAL PRIMARY KEY,
    serie      INT,
    data_ref   DATE,
    valor      NUMERIC(18,6),
    fetched_at TIMESTAMP DEFAULT NOW(),
    UNIQUE (serie, data_ref)
);

CREATE TABLE dashboard_snapshot (
    id                 SERIAL PRIMARY KEY,
    id_ente            VARCHAR(10),
    an_exercicio       INT,
    nr_periodo         INT,
    receita_total      NUMERIC(18,2),
    despesa_total      NUMERIC(18,2),
    resultado_primario NUMERIC(18,2),
    rcl_total          NUMERIC(18,2),
    selic              NUMERIC(8,4),
    ipca_12m           NUMERIC(8,4),
    dolar              NUMERIC(10,4),
    created_at         TIMESTAMP DEFAULT NOW()
);
```

---

## 📡 APIs Utilizadas

### Siconfi — Tesouro Nacional
```
https://apidatalake.tesouro.gov.br/ords/siconfi/tt/rreo
  ?an_exercicio=2025
  &nr_periodo=2
  &co_tipo_demonstrativo=RREO
  &no_anexo=RREO-Anexo%2006
  &id_ente=23          ← Ceará (código IBGE)
  &co_esfera=E         ← Estado
```

**RREO Anexo 06** concentra todos os indicadores necessários:
- Receitas Correntes Realizadas
- Despesa Primária Liquidada
- Resultado Primário
- Receita Corrente Líquida (RCL)
- Despesa por categoria (Pessoal, Capital, Amortização...)

### BCB — Banco Central do Brasil
| Série | Indicador |
|---|---|
| 432 | SELIC Meta (% a.a.) |
| 433 | IPCA Mensal (acumulado 12m) |
| 1 | Dólar PTAX Venda |

---

## 🚀 Como Rodar

### Pré-requisitos

- Java 25+
- Maven 3.x
- PostgreSQL 14+
- Fontes Inter e JetBrains Mono em `src/main/resources/fonts/`

### Configuração do banco

```bash
createdb siconfi_db
```

As tabelas são criadas automaticamente na primeira execução via `DatabaseConfig.criarTabelas()`.

### Variáveis de ambiente

```bash
export DB_USER=postgres
export DB_PASS=sua_senha
```

### Build e execução

```bash
mvn clean package
java -jar target/siconfi-dashboard.jar
```

---

## 🗺️ Roadmap

- [ ] Navegação funcional entre painéis da sidebar (Receitas, Despesas, Dívida...)
- [ ] Filtros funcionais — trocar ente federativo, exercício e período dinamicamente
- [ ] Série histórica — buscar múltiplos bimestres para gráfico de linha
- [ ] **LangChain4j** — análise textual com IA (Claude/GPT) sobre os dados fiscais
- [ ] Geração de relatórios em PDF com iText + JFreeChart
- [ ] Alertas fiscais inteligentes baseados nos limites da LRF
- [ ] Suporte a outros estados e municípios

---

## 📄 Licença

MIT License — veja [LICENSE](LICENSE) para detalhes.

---

## 🙏 Fontes de dados

- [Siconfi — Sistema de Informações Contábeis e Fiscais do Setor Público Brasileiro](https://siconfi.tesouro.gov.br)
- [API de Dados Abertos do BCB](https://dadosabertos.bcb.gov.br)
- [Portal da Transparência do Governo do Estado do Ceará](https://www.transparencia.ce.gov.br)
