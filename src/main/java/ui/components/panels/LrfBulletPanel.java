package ui.components.panels;

import ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LrfBulletPanel extends JPanel {

    private static final Color BG         = new Color(22, 22, 38);
    private static final Color BG_TRACK   = new Color(40, 40, 65);
    private static final Color ALERTA_CLR = new Color(220, 170, 0, 180);
    private static final Color LIMITE_CLR = new Color(220, 60, 40, 220);

    private record Bullet(
            String label,
            String sublabel,
            double valor,
            double alerta,
            double limite,
            double maxExibido,
            boolean inverter
    ) {}

    private final List<Bullet> bullets;

    public LrfBulletPanel(double pessoal, double amortizacao, double capital, double rcl) {
        setOpaque(false);

        double pP = rcl > 0 ? pessoal     / rcl : 0;
        double pA = rcl > 0 ? amortizacao / rcl : 0;
        double pI = rcl > 0 ? capital     / rcl : 0;

        bullets = List.of(
                new Bullet("Gastos c/ Pessoal", "Limite LRF: 49%  |  Alerta: 46,6%",
                        pP, 0.4655, 0.49, 0.60, true),
                new Bullet("Amortiz. da Dívida", "Referência: 100% da RCL",
                        pA, 0.90, 1.00, 1.20, true),
                new Bullet("Investimentos", "Referência mínima: 5% da RCL",
                        pI, 0.05, 0.20, 0.25, false)
        );
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int n        = bullets.size();
        int padTop   = 16;
        int padLeft  = 16;
        int padRight = 16;
        int rowH     = (getHeight() - padTop * 2) / n;
        int labelW   = 160;
        int barX     = padLeft + labelW;
        int barW     = getWidth() - barX - padRight - 70;

        for (int i = 0; i < n; i++) {
            int y = padTop + i * rowH;
            drawBullet(g2, bullets.get(i), padLeft, y, labelW, barX, barW, rowH);
        }

        g2.dispose();
    }

    private void drawBullet(Graphics2D g2, Bullet b,
                            int padLeft, int y, int labelW,
                            int barX, int barW, int rowH) {

        int barH     = 22;
        int barY     = y + (rowH - barH) / 2 + 4;
        FontMetrics fm;

        g2.setFont(FontManager.interBold(14f));
        g2.setColor(new Color(200, 200, 230));
        g2.drawString(b.label(), padLeft, barY - 4);

        g2.setFont(FontManager.inter(14f));
        g2.setColor(new Color(120, 120, 160));
        g2.drawString(b.sublabel(), padLeft, barY + barH + 16);

        g2.setColor(BG_TRACK);
        g2.fillRoundRect(barX, barY, barW, barH, 6, 6);

        if (b.inverter()) {
            int ax = barX + (int)(b.alerta() / b.maxExibido() * barW);
            int lx = barX + (int)(b.limite() / b.maxExibido() * barW);
            int fw = Math.min(lx - ax, barW - (ax - barX));
            if (fw > 0) {
                g2.setColor(new Color(220, 170, 0, 60));
                g2.fillRect(ax, barY, fw, barH);
            }
        }

        double pct   = Math.min(b.valor() / b.maxExibido(), 1.0);
        int    fillW = (int)(pct * barW);
        Color  cor   = corDoValor(b);

        g2.setColor(cor);
        g2.fillRoundRect(barX, barY, Math.max(fillW, 4), barH, 6, 6);

        if (b.inverter()) {
            int alertaX = barX + (int)(b.alerta() / b.maxExibido() * barW);
            g2.setColor(ALERTA_CLR);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(alertaX, barY - 4, alertaX, barY + barH + 4);
        }

        int limiteX = barX + (int)(b.limite() / b.maxExibido() * barW);
        g2.setColor(LIMITE_CLR);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(limiteX, barY - 6, limiteX, barY + barH + 6);

        String valStr = String.format("%.1f%%", b.valor() * 100);
        g2.setFont(FontManager.interBold(16f));
        g2.setColor(cor);
        fm = g2.getFontMetrics();
        g2.drawString(valStr, barX + barW + 10, barY + (barH / 2) + (fm.getAscent() / 2) - 2);

        g2.setFont(FontManager.inter(15f));
        g2.setColor(new Color(90, 90, 120));
        g2.setStroke(new BasicStroke(1f));
        String maxLabel = String.format("%.0f%%", b.maxExibido() * 100);
        g2.drawString("0%",      barX - 4,         barY + barH + 30);
        g2.drawString(maxLabel,  barX + barW - 20,  barY + barH + 30);
    }

    private Color corDoValor(Bullet b) {
        if (!b.inverter()) {
            return b.valor() >= b.alerta() ? new Color(0, 200, 100) : new Color(220, 80, 40);
        }
        if (b.valor() < b.alerta()) return new Color(0, 200, 100);
        if (b.valor() < b.limite()) return new Color(220, 170, 0);
        return new Color(220, 60, 40);
    }
}