package ui.components.panels;

import service.DashboardService.DashboardData;
import ui.components.KpiCard;
import ui.theme.FontManager;
import ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DividaConsolidadaPanel extends JPanel {

    private static final Color BG      = Theme.BG_DARK;
    private static final Color BG_CARD = new Color(22, 22, 38);
    private static final Color BORDER  = new Color(45, 45, 75);
    private static final Color TEXT    = new Color(180, 180, 220);
    private static final Color MUTED   = new Color(90, 90, 130);
    private static final Color RED     = new Color(220, 80,  60);
    private static final Color YELLOW  = new Color(220, 180,  0);
    private static final Color GREEN   = new Color(0,   200, 100);
    private static final Color BLUE    = new Color(0,   120, 220);
    private static final Color PURPLE  = new Color(160,  60, 200);

    private static final double LIMITE_DCL_RCL = 200.0;
    private static final double ALERTA_DCL_RCL = 180.0;

    public DividaConsolidadaPanel(DashboardData data) {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildCorpo(data), BorderLayout.CENTER);
    }


    private JPanel buildCorpo(DashboardData data) {
        JPanel corpo = new JPanel(new GridLayout(2, 2, 12, 12));
        corpo.setBackground(BG);

        corpo.add(wrapCard("Limite LRF — DCL / RCL",       buildGaugeLrf(data)));
        corpo.add(wrapCard("Composição da Dívida",          buildComposicao(data)));
        corpo.add(wrapCard("Serviço da Dívida — Amortização", buildBarraServico(
                "Amortização", data.amortizacaoLiquidado(),
                data.amortizacaoDotacao(), new Color(200, 100, 0))));
        corpo.add(wrapCard("Serviço da Dívida — Juros",    buildBarraServico(
                "Juros e Encargos", data.jurosLiquidado(),
                data.jurosDotacao(), PURPLE)));
        return corpo;
    }

    private JPanel buildGaugeLrf(DashboardData data) {
        double pct = data.rclTotal() > 0
                ? data.dividaConsolidadaLiquida() / data.rclTotal() * 100 : 0;

        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setOpaque(false);

        final double pctF = pct;
        JPanel arco = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                int cx = getWidth()  / 2;
                int cy = getHeight() - 20;
                int r  = Math.min(cx, cy) - 10;

                g2.setColor(new Color(45, 45, 75));
                g2.setStroke(new BasicStroke(18f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND));
                g2.drawArc(cx - r, cy - r, r * 2, r * 2, 0, 180);

                g2.setColor(new Color(0, 100, 50, 80));
                g2.drawArc(cx - r, cy - r, r * 2, r * 2, 0, 162);
                g2.setColor(new Color(180, 140, 0, 80));
                g2.drawArc(cx - r, cy - r, r * 2, r * 2, 162, 18);

                double fill = Math.min(pctF / LIMITE_DCL_RCL, 1.0);
                int arcFill = (int)(fill * 180);
                Color corFill = pctF >= LIMITE_DCL_RCL ? RED
                        : pctF >= ALERTA_DCL_RCL  ? YELLOW : GREEN;

                if (arcFill > 0) {
                    g2.setColor(corFill);
                    g2.setStroke(new BasicStroke(18f, BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND));
                    g2.drawArc(cx - r, cy - r, r * 2, r * 2, 180, arcFill);
                }

                drawMarcador(g2, cx, cy, r, 162, new Color(220, 180, 0), "180%");
                drawMarcador(g2, cx, cy, r, 180, RED,                    "200%");

                g2.setFont(FontManager.interBold(22f));
                g2.setColor(corFill);
                String val = String.format("%.1f%%", pctF);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(val, cx - fm.stringWidth(val) / 2, cy - 8);

                g2.setFont(FontManager.inter(14f));
                g2.setColor(MUTED);
                String sub = "DCL / RCL";
                fm = g2.getFontMetrics();
                g2.drawString(sub, cx - fm.stringWidth(sub) / 2, cy + 10);

                g2.dispose();
            }

            private void drawMarcador(Graphics2D g2, int cx, int cy, int r,
                                      int graus, Color cor, String label) {
                double rad = Math.toRadians(180 - graus);
                int mx = (int)(cx + (r + 12) * Math.cos(rad));
                int my = (int)(cy - (r + 12) * Math.sin(rad));
                g2.setColor(cor);
                g2.setFont(FontManager.inter(14f));
                g2.drawString(label, mx - 10, my);
            }
        };
        arco.setOpaque(false);
        arco.setPreferredSize(new Dimension(0, 180));

        JPanel info = new JPanel(new GridLayout(2, 2, 8, 4));
        info.setOpaque(false);

        addInfo(info, "DCL",   KpiCard.formatar(data.dividaConsolidadaLiquida()), PURPLE);
        addInfo(info, "RCL",   KpiCard.formatar(data.rclTotal()),                 BLUE);
        addInfo(info, "Limite LRF", "200% da RCL",                                MUTED);
        addInfo(info, "Situação",
                pct >= LIMITE_DCL_RCL ? "⚠ Acima do limite"
                        : pct >= ALERTA_DCL_RCL  ? "⚡ Em alerta"
                        : "✓ Dentro do limite",
                pct >= LIMITE_DCL_RCL ? RED : pct >= ALERTA_DCL_RCL ? YELLOW : GREEN);

        wrapper.add(arco, BorderLayout.CENTER);
        wrapper.add(info, BorderLayout.SOUTH);
        return wrapper;
    }

    private void addInfo(JPanel p, String label, String valor, Color cor) {
        JLabel lbl = new JLabel(label + ": ");
        lbl.setForeground(MUTED);
        lbl.setFont(FontManager.inter(14f));

        JLabel val = new JLabel(valor);
        val.setForeground(cor);
        val.setFont(FontManager.interBold(14f));

        p.add(lbl);
        p.add(val);
    }

    private JPanel buildComposicao(DashboardData data) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        double dc  = data.dividaConsolidada();
        double ded = data.deducoesDivida();
        double dcl = data.dividaConsolidadaLiquida();

        panel.add(Box.createVerticalStrut(12));
        panel.add(buildItemComposicao("Dívida Consolidada (DC)",  dc,  dc,  BLUE,   "Base bruta da dívida"));
        panel.add(Box.createVerticalStrut(16));
        panel.add(buildItemComposicao("(–) Deduções", ded, dc, RED,    "Disponib. caixa + haveres financeiros"));
        panel.add(Box.createVerticalStrut(16));

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(12));

        panel.add(buildItemComposicao("Dívida Cons. Líquida (DCL)", dcl, dc, PURPLE, "DC – Deduções"));
        panel.add(Box.createVerticalStrut(8));

        JPanel refRcl = new JPanel(new BorderLayout());
        refRcl.setOpaque(false);
        refRcl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        JLabel lblRef = new JLabel("RCL (referência LRF)");
        JLabel valRef = new JLabel(KpiCard.formatar(data.rclTotal()));
        lblRef.setForeground(MUTED); lblRef.setFont(FontManager.inter(14f));
        valRef.setForeground(MUTED); valRef.setFont(FontManager.interBold(14f));
        refRcl.add(lblRef, BorderLayout.WEST);
        refRcl.add(valRef, BorderLayout.EAST);
        panel.add(refRcl);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel buildItemComposicao(String titulo, double valor,
                                       double referencia, Color cor, String desc) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        JLabel lblTit = new JLabel(titulo);
        lblTit.setForeground(TEXT);
        lblTit.setFont(FontManager.interBold(12f));
        JLabel lblVal = new JLabel(KpiCard.formatar(valor));
        lblVal.setForeground(cor);
        lblVal.setFont(FontManager.interBold(13f));
        topo.add(lblTit, BorderLayout.WEST);
        topo.add(lblVal, BorderLayout.EAST);

        JLabel lblDesc = new JLabel(desc);
        lblDesc.setForeground(MUTED);
        lblDesc.setFont(FontManager.inter(10f));

        double pct = referencia > 0 ? valor / referencia : 0;
        final double pctF = pct;
        final Color  corF = cor;
        JPanel barra = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(45, 45, 75));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                int w = (int)(getWidth() * Math.min(pctF, 1.0));
                if (w > 0) {
                    g2.setColor(corF);
                    g2.fillRoundRect(0, 0, w, getHeight(), 6, 6);
                }
                g2.dispose();
            }
        };
        barra.setOpaque(false);
        barra.setPreferredSize(new Dimension(0, 8));
        barra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));

        wrapper.add(topo);
        wrapper.add(Box.createVerticalStrut(2));
        wrapper.add(lblDesc);
        wrapper.add(Box.createVerticalStrut(4));
        wrapper.add(barra);
        return wrapper;
    }


    private JPanel buildBarraServico(String titulo, double liquidado,
                                     double dotacao, Color cor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        double pctExec  = dotacao  > 0 ? liquidado / dotacao  * 100 : 0;
        double pctRcl   = 0;

        panel.add(Box.createVerticalStrut(12));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        JLabel lblTit = new JLabel(titulo + " liquidado");
        lblTit.setForeground(TEXT);
        lblTit.setFont(FontManager.inter(12f));
        JLabel lblVal = new JLabel(KpiCard.formatar(liquidado));
        lblVal.setForeground(cor);
        lblVal.setFont(FontManager.interBold(16f));
        topo.add(lblTit, BorderLayout.WEST);
        topo.add(lblVal, BorderLayout.EAST);
        panel.add(topo);
        panel.add(Box.createVerticalStrut(12));

        final double dotF = dotacao;
        final double liqF = liquidado;
        final Color  corF = cor;
        JPanel barra = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(45, 45, 75));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                if (dotF > 0) {
                    int w = (int)(getWidth() * Math.min(liqF / dotF, 1.0));
                    if (w > 0) {
                        g2.setColor(corF);
                        g2.fillRoundRect(0, 0, w, getHeight(), 8, 8);
                    }
                }
                g2.dispose();
            }
        };
        barra.setOpaque(false);
        barra.setPreferredSize(new Dimension(0, 20));
        barra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        panel.add(barra);
        panel.add(Box.createVerticalStrut(8));

        for (String[] par : new String[][]{
                {"Dotação Atualizada", KpiCard.formatar(dotacao)},
                {"Liquidado",          KpiCard.formatar(liquidado)},
                {"% Execução",         String.format("%.1f%%", pctExec)},
        }) {
            JPanel linha = new JPanel(new BorderLayout());
            linha.setOpaque(false);
            linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
            JLabel k = new JLabel(par[0]);
            JLabel v = new JLabel(par[1]);
            k.setForeground(MUTED); k.setFont(FontManager.inter(14f));
            v.setForeground(par[0].equals("% Execução")
                    ? (pctExec >= 80 ? GREEN : pctExec >= 50 ? YELLOW : RED)
                    : TEXT);
            v.setFont(FontManager.interBold(14f));
            linha.add(k, BorderLayout.WEST);
            linha.add(v, BorderLayout.EAST);
            panel.add(linha);
            panel.add(Box.createVerticalStrut(4));
        }

        panel.add(Box.createVerticalGlue());
        return panel;
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
