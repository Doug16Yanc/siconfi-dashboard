package ui.components.panels;

import ui.theme.FontManager;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PerCapitaPanel extends JPanel {

    private static final Color BG       = new Color(22, 22, 38);
    private static final Color BG_TRACK = new Color(40, 40, 65);

    private record IndicadorPC(
            String label,
            double valorPC,
            double refMin,
            double refMax,
            Color  cor
    ) {}

    private final List<IndicadorPC> indicadores;
    private final String nomeEnte;
    private final long   populacao;

    public PerCapitaPanel(double receita, double despesa, double resultado,
                          long populacao, String nomeEnte) {
        setOpaque(false);
        this.nomeEnte  = nomeEnte;
        this.populacao = populacao > 0 ? populacao : 1L;

        double recPC  = receita   / this.populacao;
        double despPC = despesa   / this.populacao;
        double resPC  = resultado / this.populacao;

        indicadores = List.of(
                new IndicadorPC("Receita / hab",    recPC,  2_000,  8_000, new Color(0,  180, 100)),
                new IndicadorPC("Despesa / hab",    despPC, 1_800,  8_000, new Color(220, 80,  40)),
                new IndicadorPC("Resultado / hab",  resPC,      0,  1_500, new Color(80,  120, 220))
        );
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int padTop   = 16;
        int padLeft  = 16;
        int padRight = 16;
        int n        = indicadores.size();

        g2.setFont(FontManager.inter(14f));
        g2.setColor(new Color(100, 100, 140));
        String pop = String.format("População: %,d hab. (%s)", populacao, nomeEnte);
        g2.drawString(pop, padLeft, padTop);

        int rowH  = (getHeight() - padTop - 20) / n;
        int labelW = 120;
        int barX   = padLeft + labelW;
        int barW   = getWidth() - barX - padRight - 80;

        for (int i = 0; i < n; i++) {
            int y = padTop + 18 + i * rowH;
            drawIndicador(g2, indicadores.get(i), padLeft, y, labelW, barX, barW, rowH);
        }

        g2.dispose();
    }

    private void drawIndicador(Graphics2D g2, IndicadorPC ind,
                               int padLeft, int y, int labelW,
                               int barX, int barW, int rowH) {
        int barH = 16;
        int barY = y + (rowH - barH) / 2 + 4;

        g2.setFont(FontManager.interBold(16f));
        g2.setColor(new Color(200, 200, 230));
        g2.drawString(ind.label(), padLeft, barY + barH - 2);

        g2.setColor(BG_TRACK);
        g2.fillRoundRect(barX, barY, barW, barH, 5, 5);

        int refMinX = barX + (int)(Math.max(ind.refMin(), 0) / ind.refMax() * barW);
        int refMaxX = barX + barW;
        g2.setColor(new Color(60, 60, 90));
        g2.fillRect(refMinX, barY, refMaxX - refMinX, barH);

        double pct   = Math.min(Math.max(ind.valorPC(), 0) / ind.refMax(), 1.0);
        int    fillW = (int)(pct * barW);
        g2.setColor(ind.cor());
        g2.fillRoundRect(barX, barY, Math.max(fillW, 4), barH, 5, 5);

        String valStr = formatarPC(ind.valorPC());
        g2.setFont(FontManager.interBold(14f));
        g2.setColor(ind.cor());
        g2.drawString(valStr, barX + barW + 8, barY + barH - 2);

        g2.setFont(FontManager.inter(14f));
        g2.setColor(new Color(80, 80, 110));
        g2.drawString("R$0",                          barX,          barY + barH + 14);
        g2.drawString("R$" + formatarPC(ind.refMax()), barX + barW - 30, barY + barH + 14);
    }

    private String formatarPC(double v) {
        if (Math.abs(v) >= 1_000) return String.format("%.1f k", v / 1_000);
        return String.format("%.0f", v);
    }
}