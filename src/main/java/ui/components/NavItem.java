package ui.components;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import ui.theme.FontManager;
import ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class NavItem extends JPanel {

    private final JLabel lbl;
    private final JLabel ico;
    private final FlatSVGIcon icon;
    private boolean active;

    private Runnable onClick;

    public NavItem(String iconPath, String label, boolean active) {
        this.active = active;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(240, 36));
        setMaximumSize(new Dimension(240, 36));
        setMinimumSize(new Dimension(240, 36));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        icon = new FlatSVGIcon("icons/" + iconPath, 16, 16);
        ico  = new JLabel(icon);
        lbl  = new JLabel(label);
        lbl.setFont(FontManager.inter(14f));

        JPanel iconText = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        iconText.setOpaque(false);
        iconText.add(ico);
        iconText.add(lbl);
        add(iconText, BorderLayout.CENTER);

        aplicarEstado(active);
        setupEvents();
    }

    public void setOnClick(Runnable callback) {
        this.onClick = callback;
    }

    public void setActive(boolean novoEstado) {
        this.active = novoEstado;
        aplicarEstado(novoEstado);
        repaint();
    }

    public boolean isActive() {
        return active;
    }

    private void aplicarEstado(boolean ativo) {
        setBackground(ativo ? new Color(16, 28, 58) : Theme.BG_SIDEBAR);

        setBorder(ativo
                ? BorderFactory.createMatteBorder(0, 3, 0, 0, Theme.ACCENT)
                : new EmptyBorder(0, 3, 0, 0));

        lbl.setForeground(ativo ? Theme.TEXT_MAIN : Theme.TEXT_MUTED);
        lbl.setText(lbl.getText().trim());

        icon.setColorFilter(new FlatSVGIcon.ColorFilter(c ->
                ativo ? new Color(180, 200, 255) : new Color(120, 120, 160)));

        ico.repaint();
    }

    private void setupEvents() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onClick != null) onClick.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (active) return;
                setBackground(new Color(30, 30, 55));
                lbl.setForeground(Theme.TEXT_MAIN);
                icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> new Color(180, 200, 255)));
                ico.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (active) return;
                setBackground(Theme.BG_SIDEBAR);
                lbl.setForeground(Theme.TEXT_MUTED);
                icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> new Color(120, 120, 160)));
                ico.repaint();
            }
        });
    }
}