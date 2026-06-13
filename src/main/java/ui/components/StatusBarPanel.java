package ui.components;

import service.DashboardService;
import ui.theme.FontManager;
import ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StatusBarPanel extends JPanel {

    public StatusBarPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 24, 6));
        setBackground(Theme.STATUS_BG);
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_LINE));
        renderStaticContent("—", "—", "—");
    }

    public void atualizarDados(DashboardService.DashboardData data) {
        String dolar = String.format("R$ %.2f", data.dolar()).replace(".", ",");
        String selic = String.format("%.2f%%", data.selic());
        String ipca  = String.format("%.2f%%", data.ipca12m());
        renderStaticContent(dolar, selic, ipca);
    }

    private void renderStaticContent(String dolar, String selic, String ipca) {
        removeAll();

        add(statusItem("DÓLAR",      dolar, "+0,00%", true));
        add(vDivider());
        add(statusItem("SELIC",      selic, "0,00%",  true));
        add(vDivider());
        add(statusItem("IPCA (12m)", ipca,  "0,00%",  true));
        add(vDivider());
        add(statusItem("PIB (2025)", "2,90%", "+0,10%", true));
        add(vDivider());
        add(statusItem("Dívida Bruta do Gov.", "76,5% do PIB", "-0,3 pp", true));

        JLabel next = new JLabel("Próxima atualização SICONFI: 01/06/2026 08:00");
        next.setForeground(Theme.TEXT_MAIN);
        next.setFont(FontManager.interBold(16f));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setBackground(Theme.STATUS_BG);
        right.add(next);

        add(Box.createHorizontalGlue());
        add(right);

        revalidate();
        repaint();
    }

    private JPanel statusItem(String label, String value, String variation, boolean positive) {
        JPanel p = new JPanel(new GridLayout(1, 3, 8, 0));
        p.setBackground(Theme.STATUS_BG);

        JLabel lbl = new JLabel(label);
        lbl.setForeground(Theme.TEXT_MUTED);
        lbl.setFont(FontManager.inter(15f));

        JLabel val = new JLabel(value);
        val.setForeground(Theme.TEXT_MAIN);
        val.setFont(FontManager.jetbrainsBold(15f));

        JLabel var = new JLabel(variation);
        var.setForeground(positive ? new Color(0, 200, 100) : new Color(220, 60, 60));
        var.setFont(FontManager.jetbrains(15f));

        p.add(lbl); p.add(val); p.add(var);
        return p;
    }

    private JSeparator vDivider() {
        JSeparator s = new JSeparator(SwingConstants.VERTICAL);
        s.setForeground(new Color(40, 40, 65));
        s.setPreferredSize(new Dimension(1, 20));
        return s;
    }
}