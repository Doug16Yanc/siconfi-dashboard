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
    private final boolean active;

    public NavItem(String iconPath, String label, boolean active) {
        this.active = active;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(240, 36));
        setMaximumSize(new Dimension(240, 36));
        setMinimumSize(new Dimension(240, 36));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBackground(active ? new Color(0, 100, 220, 60) : Theme.BG_SIDEBAR);
        setAlignmentX(Component.LEFT_ALIGNMENT);

        setBorder(active
                ? BorderFactory.createMatteBorder(0, 3, 0, 0, Theme.ACCENT)
                : new EmptyBorder(0, 3, 0, 0));

        icon = new FlatSVGIcon("icons/" + iconPath, 16, 16);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color ->
                active ? new Color(180, 200, 255) : new Color(120, 120, 160)
        ));
        ico = new JLabel(icon);

        lbl = new JLabel(label);
        lbl.setFont(FontManager.inter(14f));
        lbl.setForeground(active ? Theme.TEXT_MAIN : Theme.TEXT_MUTED);

        JPanel iconText = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        iconText.setOpaque(false);
        iconText.add(ico);
        iconText.add(lbl);

        add(iconText, BorderLayout.CENTER);
        setupEvents();
    }

    private void setupEvents() {
        if (active) return;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(new Color(30, 30, 55));
                lbl.setForeground(Theme.TEXT_MAIN);
                icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> new Color(180, 200, 255)));
                ico.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(Theme.BG_SIDEBAR);
                lbl.setForeground(Theme.TEXT_MUTED);
                icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> new Color(120, 120, 160)));
                ico.repaint();
            }
        });
    }
}