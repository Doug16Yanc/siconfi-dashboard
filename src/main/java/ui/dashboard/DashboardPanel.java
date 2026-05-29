package ui.dashboard;

import service.DashboardService;
import service.DashboardService.DashboardData;
import ui.FontManager;
import ui.dashboard.charts.DespesaDonutChartPanel;
import ui.dashboard.charts.ReceitaDonutChartPanel;
import ui.dashboard.charts.WaterfallChartPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class DashboardPanel extends JPanel {

    private WaterfallChartPanel waterfallChart;
    private DespesaDonutChartPanel donutChart;

    private static final Color BG   = new Color(13, 13, 23);
    private static final Color MUTED= new Color(120, 120, 160);

    private final KpiCard cardReceita   = new KpiCard("Receita Total",
            "", new Color(0, 120, 60));
    private final KpiCard cardDespesa   = new KpiCard("Despesa Total",
            "", new Color(180, 60, 0));
    private final KpiCard cardPrimario  = new KpiCard("Resultado Primário",
            "", new Color(80, 0, 160));
    private final KpiCard cardRcl       = new KpiCard("Rec. Corrente Líquida",
            "", new Color(0, 100, 180));
    private final KpiCard cardSelic     = new KpiCard("SELIC",
            "", new Color(140, 80, 0));

    private final JLabel lblStatus = new JLabel(
            "⏳ Buscando dados da API Siconfi + BCB...", SwingConstants.CENTER);

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildKpiRow(),   BorderLayout.NORTH);
        add(buildCenter(),   BorderLayout.CENTER);

        carregarDados();
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

    private DespesaDonutChartPanel donutDespesa;
    private ReceitaDonutChartPanel donutReceita;

    private JPanel buildCenter() {
        JPanel center = new JPanel(new GridLayout(1, 3, 12, 0)); // ← era 2
        center.setBackground(BG);
        center.setPreferredSize(new Dimension(0, 340));

        waterfallChart     = new WaterfallChartPanel(0, 0, 0);
        donutDespesa = new DespesaDonutChartPanel(Map.of());
        donutReceita = new ReceitaDonutChartPanel(Map.of());

        center.add(wrapCard("Resultado Fiscal — Ceará 2025", waterfallChart));
        center.add(wrapCard("Despesa por Categoria",          donutDespesa));
        center.add(wrapCard("Receita por Categoria",          donutReceita));

        return center;
    }

    private JPanel wrapCard(String titulo, JPanel conteudo) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(new Color(22, 22, 38));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(40, 40, 65), 1),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel lbl = new JLabel(titulo);
        lbl.setForeground(new Color(180, 180, 220));
        lbl.setFont(FontManager.interBold(16f));
        card.add(lbl,      BorderLayout.NORTH);
        card.add(conteudo, BorderLayout.CENTER);
        return card;
    }

    private void carregarDados() {
        Thread.ofVirtual().start(() -> {
            try {
                DashboardService service = new DashboardService();
                DashboardData data = service.carregarTudo();

                SwingUtilities.invokeLater(() -> atualizar(data));

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("❌ Erro ao carregar: " + e.getMessage());
                    lblStatus.setForeground(new Color(220, 60, 60));
                });
            }
        });
    }

    private void atualizar(DashboardData data) {
        cardReceita.atualizar(KpiCard.formatar(data.receitaTotal()),    8.72,  "vs 2024");
        cardDespesa.atualizar(KpiCard.formatar(data.despesaTotal()),    7.21,  "vs 2024");
        cardPrimario.atualizar(KpiCard.formatar(data.resultadoPrimario()), 21.45, "vs 2024");
        cardRcl.atualizar(KpiCard.formatar(data.rclTotal()),            9.10,  "vs 2024");
        cardSelic.atualizar(String.format("%.2f%%", data.selic()),      0.0,   "a.a.");

        JPanel center = (JPanel) ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.CENTER);
        center.removeAll();

        waterfallChart     = new WaterfallChartPanel(data.receitaTotal(), data.despesaTotal(), data.resultadoPrimario());
        donutDespesa = new DespesaDonutChartPanel(data.despesaPorFuncao());
        donutReceita = new ReceitaDonutChartPanel(data.receitaPorCategoria());

        center.add(wrapCard("Resultado Fiscal — Ceará 2025", waterfallChart));
        center.add(wrapCard("Despesa por Categoria",          donutDespesa));
        center.add(wrapCard("Receita por Categoria",          donutReceita));

        lblStatus.setText("✅ " + data.periodo()
                + "  |  Dólar: R$ " + String.format("%.2f", data.dolar())
                + "  |  IPCA 12m: " + String.format("%.2f%%", data.ipca12m()));
        lblStatus.setForeground(new Color(0, 200, 100));

        center.revalidate();
        center.repaint();
        revalidate();
        repaint();
    }
}