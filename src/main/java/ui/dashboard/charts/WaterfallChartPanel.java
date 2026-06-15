package ui.dashboard.charts;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.category.*;
import ui.theme.FontManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;

public class WaterfallChartPanel extends JPanel {

    private static final Color BG     = new Color(22, 22, 38);
    private static final Color GRID   = new Color(40, 40, 65);
    private static final Color GREEN  = new Color(0, 210, 110);
    private static final Color RED    = new Color(220, 60, 60);
    private static final Color BLUE   = new Color(0, 120, 255);
    private static final Color TEXT   = new Color(180, 180, 220);

    public WaterfallChartPanel(double receita, double despesa, double resultado) {
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(4, 4, 4, 4));
        add(buildChart(receita, despesa, resultado), BorderLayout.CENTER);
    }

    private ChartPanel buildChart(double receita, double despesa, double resultado) {
        double r = receita  / 1_000_000_000.0;
        double d = despesa  / 1_000_000_000.0;
        double p = resultado / 1_000_000_000.0;

        DefaultCategoryDataset ds = new DefaultCategoryDataset();

        ds.addValue(0,   "base", "Receita");
        ds.addValue(r,   "base", "Despesa");
        ds.addValue(Math.min(r - d, r), "base", "Resultado");

        ds.addValue(r,          "valor", "Receita");
        ds.addValue(-d,         "valor", "Despesa");
        ds.addValue(Math.abs(p),"valor", "Resultado");

        JFreeChart chart = ChartFactory.createBarChart(
                null, null, "R$ Bilhões", ds,
                PlotOrientation.VERTICAL, false, true, false
        );

        chart.setBackgroundPaint(BG);
        chart.setBorderVisible(false);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(BG);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(GRID);
        plot.setRangeGridlineStroke(new BasicStroke(0.5f));
        plot.setDomainGridlinesVisible(false);

        NumberAxis axisY = (NumberAxis) plot.getRangeAxis();
        axisY.setTickLabelFont(FontManager.jetbrains(11f));
        axisY.setTickLabelPaint(TEXT);
        axisY.setAxisLineVisible(false);
        axisY.setNumberFormatOverride(new DecimalFormat("R$ #,##0.0 Bi"));
        axisY.setUpperMargin(0.15);

        CategoryAxis axisX = plot.getDomainAxis();
        axisX.setTickLabelFont(FontManager.inter(13f));
        axisX.setTickLabelPaint(TEXT);
        axisX.setAxisLineVisible(false);
        axisX.setCategoryMargin(0.3);

        BarRenderer renderer = new BarRenderer() {
            @Override
            public Paint getItemPaint(int row, int col) {
                if (row == 0) return new Color(0, 0, 0, 0);
                return switch (col) {
                    case 0 -> GREEN;
                    case 1 -> RED;
                    default -> p >= 0 ? GREEN : RED;
                };
            }

            @Override
            public void drawItem(Graphics2D g2, CategoryItemRendererState state,
                                 java.awt.geom.Rectangle2D dataArea, CategoryPlot plot,
                                 CategoryAxis domainAxis, ValueAxis rangeAxis,
                                 CategoryDataset dataset, int row, int column, int pass) {

                double value = dataset.getValue(row, column).doubleValue();
                if (value == 0 && row == 0) return;

                double x = domainAxis.getCategoryMiddle(column,
                        dataset.getColumnCount(), dataArea,
                        plot.getDomainAxisEdge()) - state.getBarWidth() / 2.0;

                double zero = rangeAxis.valueToJava2D(0, dataArea, plot.getRangeAxisEdge());
                double val  = rangeAxis.valueToJava2D(value, dataArea, plot.getRangeAxisEdge());

                double top    = Math.min(zero, val);
                double height = Math.abs(zero - val);
                double width  = state.getBarWidth();

                if (row == 0) return;

                int arc = 10;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(getItemPaint(row, column));
                g2.fill(new RoundRectangle2D.Double(x, top, width, height, arc, arc));

                String label = String.format("R$ %.1f Bi", Math.abs(value));
                g2.setFont(FontManager.jetbrainsBold(11f));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                double lx = x + (width - fm.stringWidth(label)) / 2.0;
                double ly = top - 6;
                g2.drawString(label, (float) lx, (float) ly);
            }
        };

        renderer.setShadowVisible(false);
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setMaximumBarWidth(0.25);
        plot.setRenderer(renderer);

        plot.addRangeMarker(new org.jfree.chart.plot.ValueMarker(
                0,
                new Color(100, 100, 140),
                new BasicStroke(1.2f)
        ));

        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(BG);
        cp.setPopupMenu(null);
        return cp;
    }
}