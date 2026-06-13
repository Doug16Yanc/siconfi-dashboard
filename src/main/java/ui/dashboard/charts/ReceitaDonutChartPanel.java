package ui.dashboard.charts;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.general.*;
import ui.theme.FontManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class ReceitaDonutChartPanel extends JPanel {

    private static final Color BG = new Color(22, 22, 38);
    private static final Color[] PALETTE = {
            new Color(0,   180, 255),
            new Color(0,   210, 110),
            new Color(255, 180,   0),
            new Color(160,  80, 220),
            new Color(220,  60,  60),
    };

    public ReceitaDonutChartPanel(Map<String, Double> dados) {
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(4, 4, 4, 4));
        add(buildChart(dados), BorderLayout.CENTER);
    }

    private ChartPanel buildChart(Map<String, Double> dados) {
        DefaultPieDataset<String> ds = new DefaultPieDataset<>();

        double total = dados.values().stream().mapToDouble(Double::doubleValue).sum();

        for (Map.Entry<String, Double> entry : dados.entrySet()) {
            if (entry.getValue() <= 0) continue;
            double pct = (entry.getValue() / total) * 100.0;
            ds.setValue(
                    String.format("%s (%.1f%%)", entry.getKey(), pct),
                    entry.getValue()
            );
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