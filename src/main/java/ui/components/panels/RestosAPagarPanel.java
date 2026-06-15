package ui.components.panels;

import service.DashboardService.DashboardData;
import ui.components.KpiCard;
import ui.theme.FontManager;
import ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RestosAPagarPanel extends JPanel {

    private static final Color BG      = Theme.BG_DARK;
    private static final Color BG_CARD = new Color(22, 22, 38);
    private static final Color BORDER  = new Color(45, 45, 75);
    private static final Color TEXT    = new Color(180, 180, 220);
    private static final Color MUTED   = new Color(90, 90, 130);
    private static final Color YELLOW  = new Color(220, 180, 0);
    private static final Color RED     = new Color(220, 80, 60);
    private static final Color ORANGE  = new Color(220, 130, 0);

    public RestosAPagarPanel(DashboardData data) {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildHeader(data), BorderLayout.NORTH);
        add(buildCorpo(data),  BorderLayout.CENTER);
    }

    private JPanel buildHeader(DashboardData data) {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setBackground(BG);
        row.setPreferredSize(new Dimension(0, 80));

        double pctRcl = data.rclTotal() > 0
                ? data.restosTotal() / data.rclTotal() * 100.0 : 0.0;

        row.add(new KpiCard("Restos a Pagar Total",
                KpiCard.formatar(data.restosTotal()),       ORANGE));
        row.add(new KpiCard("Processados",
                KpiCard.formatar(data.restosProcessados()), YELLOW));
        row.add(new KpiCard("Não Processados",
                KpiCard.formatar(data.restosNaoProcessados()),
                new Color(160, 80, 200)));
        return row;
    }

    private JPanel buildCorpo(DashboardData data) {
        JPanel corpo = new JPanel(new GridLayout(1, 2, 12, 0));
        corpo.setBackground(BG);

        corpo.add(wrapCard("Composição dos Restos a Pagar",
                buildDecomposicao(data)));
        corpo.add(wrapCard("Comprometimento da RCL",
                buildComprometimentoRcl(data)));
        return corpo;
    }

    private JPanel buildDecomposicao(DashboardData data) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        double total = data.restosTotal();

        panel.add(Box.createVerticalStrut(16));
        panel.add(buildLinhaBar(
                "Processados",
                "Despesas liquidadas ainda não pagas",
                data.restosProcessados(), total, YELLOW));
        panel.add(Box.createVerticalStrut(20));
        panel.add(buildLinhaBar(
                "Não Processados",
                "Despesas empenhadas ainda não liquidadas",
                data.restosNaoProcessados(), total,
                new Color(160, 80, 200)));
        panel.add(Box.createVerticalStrut(20));

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(16));

        panel.add(buildLinhaTotal(total));
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel buildLinhaBar(String titulo, String descricao,
                                 double valor, double total, Color cor) {
        double pct = total > 0 ? valor / total : 0;

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setForeground(TEXT);
        lblTitulo.setFont(FontManager.interBold(13f));

        JLabel lblValor = new JLabel(KpiCard.formatar(valor)
                + String.format("  (%.1f%%)", pct * 100));
        lblValor.setForeground(cor);
        lblValor.setFont(FontManager.interBold(13f));

        topo.add(lblTitulo, BorderLayout.WEST);
        topo.add(lblValor,  BorderLayout.EAST);

        JLabel lblDesc = new JLabel(descricao);
        lblDesc.setForeground(MUTED);
        lblDesc.setFont(FontManager.inter(11f));

        final double pctF = pct;
        final Color  corF = cor;
        JPanel barra = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(45, 45, 75));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                int w = (int)(getWidth() * pctF);
                if (w > 0) {
                    g2.setColor(corF);
                    g2.fillRoundRect(0, 0, w, getHeight(), 8, 8);
                }

                g2.setColor(Color.WHITE);
                g2.setFont(FontManager.interBold(10f));
                String pctLabel = String.format("%.1f%%", pctF * 100);
                FontMetrics fm  = g2.getFontMetrics();
                int tx = Math.min(w - fm.stringWidth(pctLabel) - 6,
                        getWidth() - fm.stringWidth(pctLabel) - 6);
                if (tx > 2)
                    g2.drawString(pctLabel, tx,
                            (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        barra.setOpaque(false);
        barra.setPreferredSize(new Dimension(0, 24));
        barra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        wrapper.add(topo);
        wrapper.add(Box.createVerticalStrut(4));
        wrapper.add(lblDesc);
        wrapper.add(Box.createVerticalStrut(6));
        wrapper.add(barra);
        return wrapper;
    }

    private JPanel buildLinhaTotal(double total) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JLabel lblTitulo = new JLabel("Total Restos a Pagar");
        lblTitulo.setForeground(TEXT);
        lblTitulo.setFont(FontManager.interBold(14f));

        JLabel lblValor = new JLabel(KpiCard.formatar(total));
        lblValor.setForeground(ORANGE);
        lblValor.setFont(FontManager.interBold(14f));

        row.add(lblTitulo, BorderLayout.WEST);
        row.add(lblValor,  BorderLayout.EAST);
        return row;
    }

    private JPanel buildComprometimentoRcl(DashboardData data) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        double rcl   = data.rclTotal();
        double total = data.restosTotal();
        double proc  = data.restosProcessados();
        double nProc = data.restosNaoProcessados();

        double pctTotal = rcl > 0 ? total / rcl * 100 : 0;
        double pctProc  = rcl > 0 ? proc  / rcl * 100 : 0;
        double pctNProc = rcl > 0 ? nProc / rcl * 100 : 0;

        panel.add(Box.createVerticalStrut(16));
        panel.add(buildGaugeRcl("Total / RCL",         pctTotal, ORANGE, 20.0));
        panel.add(Box.createVerticalStrut(24));
        panel.add(buildGaugeRcl("Processados / RCL",   pctProc,  YELLOW, 10.0));
        panel.add(Box.createVerticalStrut(24));
        panel.add(buildGaugeRcl("Não Proc. / RCL",     pctNProc,
                new Color(160, 80, 200), 10.0));
        panel.add(Box.createVerticalStrut(16));

        JLabel nota = new JLabel("<html><i>Referência informal: Restos &gt; 20% da RCL"
                + " sinalizam pressão de caixa relevante</i></html>");
        nota.setForeground(MUTED);
        nota.setFont(FontManager.inter(10f));
        nota.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        panel.add(nota);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel buildGaugeRcl(String titulo, double pct,
                                 Color cor, double referencia) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setForeground(TEXT);
        lblTitulo.setFont(FontManager.inter(12f));

        Color corAlerta = pct > referencia ? RED : pct > referencia * 0.75 ? YELLOW : cor;
        JLabel lblPct = new JLabel(String.format("%.1f%%", pct));
        lblPct.setForeground(corAlerta);
        lblPct.setFont(FontManager.interBold(14f));

        topo.add(lblTitulo, BorderLayout.WEST);
        topo.add(lblPct,    BorderLayout.EAST);

        final double pctF  = pct;
        final double refF  = referencia;
        final Color  corF  = corAlerta;
        JPanel gauge = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                int h = getHeight();
                int w = getWidth();

                double escala  = 40.0;
                double fillPct = Math.min(pctF / escala, 1.0);
                double refPct  = Math.min(refF  / escala, 1.0);

                g2.setColor(new Color(45, 45, 75));
                g2.fillRoundRect(0, h/4, w, h/2, 8, 8);

                int fw = (int)(w * fillPct);
                if (fw > 0) {
                    g2.setColor(corF);
                    g2.fillRoundRect(0, h/4, fw, h/2, 8, 8);
                }

                int rx = (int)(w * refPct);
                g2.setColor(new Color(255, 255, 255, 180));
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND, 0, new float[]{4, 4}, 0));
                g2.drawLine(rx, 0, rx, h);

                g2.setColor(new Color(255, 255, 255, 140));
                g2.setFont(FontManager.inter(9f));
                g2.setStroke(new BasicStroke(1f));
                g2.drawString(String.format("ref %.0f%%", refF), rx + 3, h - 2);

                g2.dispose();
            }
        };
        gauge.setOpaque(false);
        gauge.setPreferredSize(new Dimension(0, 36));
        gauge.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        wrapper.add(topo);
        wrapper.add(Box.createVerticalStrut(6));
        wrapper.add(gauge);
        return wrapper;
    }

    private JPanel wrapCard(String titulo, JPanel conteudo) {
        JPanel card = new JPanel(new BorderLayout(0, 12)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int s = 6, arc = 14;
                for (int i = 0; i < s; i++) {
                    g2.setColor(new Color(0, 0, 0, 3 + i * 2));
                    g2.fillRoundRect(s-i, s-i+2,
                            getWidth()-(s-i)*2, getHeight()-(s-i)*2, arc, arc);
                }
                g2.setColor(BG_CARD);
                g2.fillRoundRect(s, s, getWidth()-s*2, getHeight()-s*2, arc, arc);
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(s+1, s+1,
                        getWidth()-s*2-2, getHeight()-s*2-2, arc, arc);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lbl = new JLabel(titulo);
        lbl.setForeground(TEXT);
        lbl.setFont(FontManager.interBold(15f));

        conteudo.setOpaque(false);
        card.add(lbl,      BorderLayout.NORTH);
        card.add(conteudo, BorderLayout.CENTER);
        return card;
    }
}