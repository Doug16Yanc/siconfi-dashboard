package ui.dashboard;

import ui.FontManager;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class KpiCard extends JPanel {

    private static final Color BG      = new Color(22, 22, 38);
    private static final Color BORDER  = new Color(60, 60, 100);
    private static final Color GREEN   = new Color(0, 210, 110);
    private static final Color RED     = new Color(220, 60, 60);

    private final JLabel lblValue;
    private final JLabel lblVariation;

    public KpiCard(String title, String icon, Color iconBg) {
        setLayout(new BorderLayout(12, 0));
        setOpaque(false);
        setBackground(BG);
        setBorder(new EmptyBorder(8, 8, 12, 8));

        JLabel ico = new JLabel(icon, SwingConstants.CENTER);
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        ico.setOpaque(true);
        ico.setBackground(iconBg);
        ico.setPreferredSize(new Dimension(44, 44));
        ico.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel icoWrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        icoWrapper.setOpaque(false);
        icoWrapper.setPreferredSize(new Dimension(44, 44));
        icoWrapper.add(ico, BorderLayout.CENTER);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(new Color(150, 150, 190));
        lblTitle.setFont(FontManager.inter(13f));

        lblValue = new JLabel("—");
        lblValue.setForeground(Color.WHITE);
        lblValue.setFont(FontManager.jetbrainsBold(20f));

        lblVariation = new JLabel("carregando...");
        lblVariation.setForeground(new Color(100, 100, 140));
        lblVariation.setFont(FontManager.jetbrains(11f));

        JPanel textPanel = new JPanel(new GridLayout(3, 1, 0, 3));
        textPanel.setBackground(BG);
        textPanel.add(lblTitle);
        textPanel.add(lblValue);
        textPanel.add(lblVariation);

        add(icoWrapper,  BorderLayout.WEST);
        add(textPanel,   BorderLayout.CENTER);
    }

    public void atualizar(String value, double varPct, String vsLabel) {
        lblValue.setText(value);
        boolean positivo = varPct >= 0;
        String sinal = positivo ? "▲ +" : "▼ ";
        lblVariation.setText(String.format("%s%.2f%% %s", sinal,
                Math.abs(varPct), vsLabel));
        lblVariation.setForeground(positivo ? GREEN : RED);
    }

    public static String formatar(double valor) {
        if (Math.abs(valor) >= 1_000_000_000_000.0)
            return String.format("R$ %.2f Tri", valor / 1_000_000_000_000.0);
        if (Math.abs(valor) >= 1_000_000_000.0)
            return String.format("R$ %.1f Bi",  valor / 1_000_000_000.0);
        if (Math.abs(valor) >= 1_000_000.0)
            return String.format("R$ %.1f Mi",  valor / 1_000_000.0);
        return String.format("R$ %.0f",          valor);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = 20;
        int shadow = 6;
        int bord = 2;

        for (int i = shadow; i >= 1; i--) {
            float alpha = 0.06f * (shadow - i + 1);
            g2.setColor(new Color(0, 0, 0, (int)(alpha * 255)));
            g2.fillRoundRect(i, i + 2, getWidth() - i * 2, getHeight() - i * 2, arc, arc);
        }

        g2.setColor(BG);
        g2.fillRoundRect(shadow, shadow, getWidth() - shadow * 2, getHeight() - shadow * 2, arc, arc);

        g2.setColor(BORDER);
        g2.setStroke(new BasicStroke(bord));
        g2.drawRoundRect(shadow, shadow, getWidth() - shadow * 2, getHeight() - shadow * 2, arc, arc);

        g2.dispose();
    }
}