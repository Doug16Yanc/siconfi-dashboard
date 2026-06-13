package ui.components.panels;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import service.DashboardService.DashboardData;
import ui.theme.FontManager;
import ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class TimeSeriesPanel extends JPanel {

    private static final Color RECEITA  = new Color(0, 200, 120);
    private static final Color DESPESA  = new Color(220, 80, 60);
    private static final Color PRIMARIO = new Color(100, 160, 255);

    private final List<DashboardData> serie;
    private final String idEnte;
    private final int    ano;

    public TimeSeriesPanel(List<DashboardData> serie, String idEnte, int ano) {
        this.serie   = serie;
        this.idEnte  = idEnte;
        this.ano     = ano;

        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildHeader(),  BorderLayout.NORTH);

        if (serie.isEmpty()) {
            add(buildVazio(), BorderLayout.CENTER);
        } else {
            add(buildConteudo(), BorderLayout.CENTER);
        }
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG_DARK);

        JLabel titulo = new JLabel("Resultado Fiscal — Série Histórica " + ano);
        titulo.setForeground(Theme.TEXT_MAIN);
        titulo.setFont(FontManager.interBold(18f));

        JLabel sub = new JLabel("Evolução bimestral de " + resolverNomeEnte() + " — valores em R$ bilhões");
        sub.setForeground(Theme.TEXT_MUTED);
        sub.setFont(FontManager.inter(13f));
        sub.setBorder(new EmptyBorder(4, 0, 0, 0));

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setBackground(Theme.BG_DARK);
        textos.add(titulo);
        textos.add(sub);

        p.add(textos, BorderLayout.WEST);
        return p;
    }


    private JPanel buildConteudo() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(Theme.BG_DARK);
        p.add(buildKpisSumario(), BorderLayout.NORTH);
        p.add(buildGrafico(),     BorderLayout.CENTER);
        return p;
    }

    private JPanel buildKpisSumario() {
        DashboardData ultimo = serie.get(serie.size() - 1);

        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setBackground(Theme.BG_DARK);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        row.add(kpiCard("Receita Acumulada",
                formatarBilhoes(ultimo.receitaTotal()), RECEITA));
        row.add(kpiCard("Despesa Acumulada",
                formatarBilhoes(ultimo.despesaTotal()), DESPESA));
        row.add(kpiCard("Resultado Primário",
                formatarBilhoes(ultimo.resultadoPrimario()), PRIMARIO));

        return row;
    }

    private JPanel kpiCard(String label, String valor, Color cor) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 4));
        card.setBackground(Theme.BG_SIDEBAR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 80), 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel lbl = new JLabel(label);
        lbl.setForeground(Theme.TEXT_MUTED);
        lbl.setFont(FontManager.inter(13f));

        JLabel val = new JLabel(valor);
        val.setForeground(cor);
        val.setFont(FontManager.jetbrainsBold(20f));

        card.add(lbl);
        card.add(val);
        return card;
    }


    private ChartPanel buildGrafico() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String[] labels = {
                "1º Bim", "2º Bim", "3º Bim", "4º Bim", "5º Bim", "6º Bim"
        };

        for (DashboardData d : serie) {
            String cat = labels[Math.min(d.numeroPeriodo() - 1, 5)];
            double fator = 1_000_000_000.0;
            dataset.addValue(d.receitaTotal()      / fator, "Receita",            cat);
            dataset.addValue(d.despesaTotal()      / fator, "Despesa",            cat);
            dataset.addValue(d.resultadoPrimario() / fator, "Resultado Primário", cat);
        }

        JFreeChart chart = ChartFactory.createLineChart(
                null, null, "R$ Bilhões", dataset
        );

        aplicarTemaDark(chart);

        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(Theme.BG_DARK);
        cp.setMouseWheelEnabled(false);
        cp.setPopupMenu(null);
        return cp;
    }

    private void aplicarTemaDark(JFreeChart chart) {
        chart.setBackgroundPaint(Theme.BG_DARK);
        chart.getLegend().setBackgroundPaint(Theme.BG_SIDEBAR);
        chart.getLegend().setItemPaint(Theme.TEXT_MUTED);
        chart.getLegend().setItemFont(FontManager.inter(12f));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Theme.BG_SIDEBAR);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(new Color(50, 50, 80));
        plot.setDomainGridlinePaint(new Color(50, 50, 80));

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelPaint(Theme.TEXT_MUTED);
        domainAxis.setTickLabelFont(FontManager.inter(12f));
        domainAxis.setAxisLinePaint(new Color(60, 60, 90));
        domainAxis.setLabelPaint(Theme.TEXT_MUTED);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelPaint(Theme.TEXT_MUTED);
        rangeAxis.setTickLabelFont(FontManager.jetbrains(11f));
        rangeAxis.setAxisLinePaint(new Color(60, 60, 90));
        rangeAxis.setLabelPaint(Theme.TEXT_MUTED);
        rangeAxis.setLabelFont(FontManager.inter(12f));

        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, RECEITA);
        renderer.setSeriesPaint(1, DESPESA);
        renderer.setSeriesPaint(2, PRIMARIO);

        for (int i = 0; i < 3; i++) {
            renderer.setSeriesStroke(i, new BasicStroke(2.5f));
            renderer.setSeriesShapesVisible(i, true);
            renderer.setSeriesShape(i, new java.awt.geom.Ellipse2D.Double(-4, -4, 8, 8));
        }

        plot.setRenderer(renderer);
    }


    private JPanel buildVazio() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Theme.BG_DARK);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.BG_SIDEBAR);
        card.setBorder(new EmptyBorder(32, 48, 32, 48));

        JLabel ico = new JLabel("⚠");
        ico.setForeground(new Color(180, 140, 0));
        ico.setFont(FontManager.interBold(28f));
        ico.setAlignmentX(CENTER_ALIGNMENT);

        JLabel titulo = new JLabel("Nenhum período carregado ainda");
        titulo.setForeground(Theme.TEXT_MAIN);
        titulo.setFont(FontManager.interBold(15f));
        titulo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel dica = new JLabel(
                "<html><center>Acesse <b>Visão Geral</b> para cada bimestre e os dados<br>" +
                        "serão armazenados automaticamente para a série histórica.</center></html>");
        dica.setForeground(Theme.TEXT_MUTED);
        dica.setFont(FontManager.inter(13f));
        dica.setAlignmentX(CENTER_ALIGNMENT);

        card.add(ico);
        card.add(Box.createVerticalStrut(12));
        card.add(titulo);
        card.add(Box.createVerticalStrut(8));
        card.add(dica);

        p.add(card);
        return p;
    }


    private String formatarBilhoes(double valor) {
        return String.format("R$ %.1f Bi", valor / 1_000_000_000.0)
                .replace(".", ",");
    }

    private String resolverNomeEnte() {
        try (var con = db.DatabaseConfig.getConnection();
             var ps  = con.prepareStatement(
                     "SELECT sigla, nome FROM estado_cache WHERE id_ente = ?")) {
            ps.setString(1, idEnte);
            var rs = ps.executeQuery();
            if (rs.next()) return rs.getString("nome");
        } catch (Exception e) {
        }
        return "Ente " + idEnte;
    }
}