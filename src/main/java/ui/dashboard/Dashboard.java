package ui.dashboard;

import service.DashboardService.DashboardData;
import ui.components.KpiCard;
import ui.components.panels.LrfBulletPanel;
import ui.components.panels.PerCapitaPanel;
import ui.theme.FontManager;
import ui.dashboard.charts.DespesaDonutChartPanel;
import ui.dashboard.charts.ReceitaDonutChartPanel;
import ui.dashboard.charts.WaterfallChartPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class Dashboard extends JPanel {

    private WaterfallChartPanel    waterfallChart;
    private DespesaDonutChartPanel donutDespesa;
    private ReceitaDonutChartPanel donutReceita;

    private static final Color BG    = new Color(13, 13, 23);
    private static final Color MUTED = new Color(120, 120, 160);

    private final KpiCard cardReceita  = new KpiCard("Receita Total",         "", new Color(0, 120, 60));
    private final KpiCard cardDespesa  = new KpiCard("Despesa Total",         "", new Color(180, 60, 0));
    private final KpiCard cardPrimario = new KpiCard("Resultado Primário",    "", new Color(80, 0, 160));
    private final KpiCard cardRcl      = new KpiCard("Rec. Corrente Líquida", "", new Color(0, 100, 180));
    private final KpiCard cardSelic    = new KpiCard("SELIC",                 "", new Color(140, 80, 0));

    private final JLabel lblStatus = new JLabel(
            "⏳ Buscando dados da API Siconfi + BCB...", SwingConstants.CENTER);

    public Dashboard(DashboardData data) {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildKpiRow(),          BorderLayout.NORTH);
        add(buildLinhas(data),      BorderLayout.CENTER);

        atualizar(data);
    }

    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 5, 12, 0));
        row.setBackground(BG);
        row.setPreferredSize(new Dimension(0, 80));
        row.add(cardReceita);
        row.add(cardDespesa);
        row.add(cardPrimario);
        row.add(cardRcl);
        row.add(cardSelic);
        return row;
    }

    private JPanel buildLinhas(DashboardData data) {
        JPanel mapLinhas = new JPanel(new GridLayout(2, 1, 0, 12));
        mapLinhas.setBackground(BG);
        mapLinhas.add(buildLinha1(data));
        mapLinhas.add(buildLinha2(data));
        return mapLinhas;
    }

    private JPanel buildLinha1(DashboardData data) {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setBackground(BG);
        row.setPreferredSize(new Dimension(0, 340));

        waterfallChart = new WaterfallChartPanel(0, 0, 0);
        donutDespesa   = new DespesaDonutChartPanel(Map.of());
        donutReceita   = new ReceitaDonutChartPanel(Map.of());

        row.add(wrapCard("Resultado Fiscal — " + data.nomeEnte() + " " + data.ano(), waterfallChart));
        row.add(wrapCard("Despesa por Categoria", donutDespesa));
        row.add(wrapCard("Receita por Categoria", donutReceita));
        return row;
    }

    private JPanel buildLinha2(DashboardData data) {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setBackground(BG);
        row.setPreferredSize(new Dimension(0, 280));

        double pessoal     = data.despesaPorFuncao().getOrDefault("RREO6PessoalEEncargosSociais",      0.0);
        double amortizacao = data.despesaPorFuncao().getOrDefault("RREO6AmortizacaoDaDivida",           0.0);
        double capital     = data.despesaPorFuncao().getOrDefault("RREO6Investimentos",                 0.0);
        double rcl         = data.rclTotal();

        LrfBulletPanel lrf = new LrfBulletPanel(pessoal, amortizacao, capital, rcl);

        PerCapitaPanel perCapita = new PerCapitaPanel(
                data.receitaTotal(), data.despesaTotal(), data.resultadoPrimario(),
                data.populacao(), data.nomeEnte());

        row.add(wrapCard("Indicadores LRF", lrf));
        row.add(wrapCard("Indicadores Per Capita", perCapita));
        return row;
    }

    private JPanel wrapCard(String titulo, JPanel conteudo) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int shadowSize = 6;
                int arc = 14;

                for (int i = 0; i < shadowSize; i++) {
                    g2.setColor(new Color(0, 0, 0, 3 + i * 2));
                    g2.fillRoundRect(
                            shadowSize - i, shadowSize - i + 2,
                            getWidth()  - (shadowSize - i) * 2,
                            getHeight() - (shadowSize - i) * 2,
                            arc, arc);
                }

                int cx = shadowSize, cy = shadowSize;
                int cw = getWidth()  - shadowSize * 2;
                int ch = getHeight() - shadowSize * 2;

                g2.setColor(new Color(22, 22, 38));
                g2.fillRoundRect(cx, cy, cw, ch, arc, arc);

                float bt = 2.0f;
                g2.setColor(new Color(45, 45, 75));
                g2.setStroke(new BasicStroke(bt));
                g2.drawRoundRect((int)(cx + bt/2), (int)(cy + bt/2),
                        (int)(cw - bt), (int)(ch - bt), arc, arc);

                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setBorder(new EmptyBorder(8, 20, 20, 20));

        JLabel lbl = new JLabel(titulo);
        lbl.setForeground(new Color(180, 180, 220));
        lbl.setFont(FontManager.interBold(16f));

        conteudo.setOpaque(false);
        if (conteudo.getComponentCount() > 0) {
            conteudo.getComponent(0).setBackground(new Color(22, 22, 38));
        }

        card.add(lbl,      BorderLayout.NORTH);
        card.add(conteudo, BorderLayout.CENTER);
        return card;
    }

    public void atualizar(DashboardData data) {
        cardReceita .atualizar(KpiCard.formatar(data.receitaTotal()),        8.72,  "vs 2024");
        cardDespesa .atualizar(KpiCard.formatar(data.despesaTotal()),        7.21,  "vs 2024");
        cardPrimario.atualizar(KpiCard.formatar(data.resultadoPrimario()),  21.45,  "vs 2024");
        cardRcl     .atualizar(KpiCard.formatar(data.rclTotal()),            9.10,  "vs 2024");
        cardSelic   .atualizar(String.format("%.2f%%", data.selic()),        0.0,   "a.a.");

        JPanel linhas = (JPanel) ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.CENTER);
        JPanel linha1 = (JPanel) linhas.getComponent(0);
        linha1.removeAll();

        waterfallChart = new WaterfallChartPanel(
                data.receitaTotal(), data.despesaTotal(), data.resultadoPrimario());
        donutDespesa   = new DespesaDonutChartPanel(data.despesaPorFuncao());
        donutReceita   = new ReceitaDonutChartPanel(data.receitaPorCategoria());

        linha1.add(wrapCard("Resultado Fiscal — " + data.nomeEnte() + " " + data.ano(), waterfallChart));
        linha1.add(wrapCard("Despesa por Categoria", donutDespesa));
        linha1.add(wrapCard("Receita por Categoria", donutReceita));

        lblStatus.setText("✅ " + data.periodo()
                + "  |  Dólar: R$ " + String.format("%.2f", data.dolar())
                + "  |  IPCA 12m: " + String.format("%.2f%%", data.ipca12m()));
        lblStatus.setForeground(new Color(0, 200, 100));

        linha1.revalidate();
        linha1.repaint();
        revalidate();
        repaint();
    }
}