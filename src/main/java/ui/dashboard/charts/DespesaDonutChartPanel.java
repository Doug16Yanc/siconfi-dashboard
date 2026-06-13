package ui.dashboard.charts;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.general.*;
import ui.theme.FontManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DespesaDonutChartPanel extends JPanel {

    private static final Color BG = new Color(22, 22, 38);
    private static final Color[] PALETTE = {
            new Color(0,   120, 255),
            new Color(0,   210, 110),
            new Color(255, 180,   0),
            new Color(220,  60,  60),
            new Color(160,  80, 220),
            new Color(0,   200, 200),
            new Color(255, 120,  40),
    };

    public DespesaDonutChartPanel(java.util.Map<String, Double> dados) {
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(4, 4, 4, 4));

        add(buildChart(dados), BorderLayout.CENTER);
    }

    private ChartPanel buildChart(java.util.Map<String, Double> dados) {
        DefaultPieDataset<String> ds = new DefaultPieDataset<>();

        java.util.Map<String, String> nomes = java.util.Map.of(
                "RREO6PessoalEEncargosSociais",         "Pessoal",
                "RREO6OutrasDespesasCorrentes",          "Outras Correntes",
                "RREO6AmortizacaoDaDivida",              "Amortização Dívida",
                "DespesasDeCapitalExcetoFontesRPPS",     "Capital",
                "DespesasPrimariasCorrentesComFontesRPPS","RPPS",
                "RREO06ICMS", "ICMS",
                "RREO06IPVA", "IPVA"
        );

        double total = dados.values().stream().mapToDouble(Double::doubleValue).sum();
        double outros = 0.0;

        java.util.List<String> principais = java.util.List.of(
                "RREO6PessoalEEncargosSociais",
                "RREO6OutrasDespesasCorrentes",
                "RREO6AmortizacaoDaDivida",
                "DespesasDeCapitalExcetoFontesRPPS",
                "DespesasPrimariasCorrentesComFontesRPPS",
                "RREO06ICMS",
                "RREO06IPVA"
        );

        for (String key : principais) {
            if (dados.containsKey(key)) {
                String label = nomes.getOrDefault(key, key);
                double pct   = (dados.get(key) / total) * 100.0;
                ds.setValue(String.format("%s (%.1f%%)", label, pct), dados.get(key));
            }
        }

        JFreeChart chart = ChartFactory.createRingChart(
                null, ds, true, true, false
        );

        chart.setBackgroundPaint(BG);
        chart.setBorderVisible(false);

        RingPlot plot = (RingPlot) chart.getPlot();
        plot.setBackgroundPaint(BG);
        plot.setOutlineVisible(false);
        plot.setSectionDepth(0.38);
        plot.setSeparatorsVisible(false);
        plot.setLabelGenerator(null);
        plot.setShadowPaint(null);

        int i = 0;
        for (var key : ds.getKeys()) {
            plot.setSectionPaint((String) key, PALETTE[i++ % PALETTE.length]);
        }

        chart.getLegend().setBackgroundPaint(BG);
        chart.getLegend().setItemFont(FontManager.inter(13f));
        chart.getLegend().setItemPaint(new Color(200, 200, 240));
        chart.getLegend().setFrame(new org.jfree.chart.block.BlockBorder(BG));

        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(BG);
        cp.setPopupMenu(null);
        return cp;
    }
}