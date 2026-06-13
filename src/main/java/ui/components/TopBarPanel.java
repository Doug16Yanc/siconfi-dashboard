package ui.components;

import ui.theme.FontManager;
import ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TopBarPanel extends JPanel {

    public TopBarPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_CARD);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_LINE),
                new EmptyBorder(16, 24, 10, 24)
        ));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setBackground(Theme.BG_CARD);

        JLabel title = new JLabel("🏠  Visão Geral");
        title.setForeground(Theme.TEXT_MAIN);
        title.setFont(FontManager.interBold(20f));

        JLabel titleSub = new JLabel("Dados consolidados da União, Estados e Municípios");
        titleSub.setForeground(Theme.TEXT_MAIN);
        titleSub.setFont(FontManager.interBold(14f));

        titlePanel.add(title);
        titlePanel.add(titleSub);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filters.setBackground(Theme.BG_CARD);

        filters.add(buildFilter("Ente Federativo", new String[]{"Todos", "Ceará", "União"}));
        filters.add(buildFilter("Tipo de Ente",    new String[]{"Todos", "Estado", "Município"}));
        filters.add(buildFilter("Exercício",       new String[]{"2025", "2024", "2023", "2022"}));
        filters.add(buildFilter("Período",         new String[]{"Até Abril", "Até Junho", "Até Dezembro"}));

        JPanel btnContainer = new JPanel(new GridLayout(2, 1, 0, 3));
        btnContainer.setBackground(Theme.BG_CARD);

        JLabel spacer = new JLabel(" ");
        spacer.setFont(FontManager.interBold(14f));

        JButton btnFilter = createGradientButton("Aplicar Filtros");

        btnContainer.add(spacer);
        btnContainer.add(btnFilter);

        filters.add(btnContainer);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setBackground(Theme.BG_CARD);
        rightPanel.add(filters);

        add(titlePanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }

    private JPanel buildFilter(String label, String[] options) {
        JPanel fp = new JPanel(new GridLayout(2, 1, 0, 3));
        fp.setBackground(Theme.BG_CARD);

        JLabel lbl = new JLabel(label);
        lbl.setForeground(Theme.TEXT_MAIN);
        lbl.setFont(FontManager.interBold(14f));

        JComboBox<String> combo = new JComboBox<>(options);
        combo.putClientProperty("JComboBox.isPopDown", true);
        combo.setFont(FontManager.inter(16f));
        combo.setPreferredSize(new Dimension(135, 32));
        combo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean hasFocus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
                c.setFont(FontManager.inter(12f));
                c.setBorder(new EmptyBorder(6, 10, 6, 10));
                c.setBackground(isSelected ? new Color(0, 100, 220, 180) : new Color(22, 22, 40));
                c.setForeground(Theme.TEXT_MAIN);
                return c;
            }
        });

        fp.add(lbl);
        fp.add(combo);
        return fp;
    }

    private JButton createGradientButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, new Color(161, 187, 224), 0, getHeight(), new Color(10, 90, 220));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setForeground(Color.WHITE);

        btn.setFont(FontManager.interBold(14f));
        btn.setPreferredSize(new Dimension(140, 32));

        btn.setBorder(new EmptyBorder(0, 16, 0, 16));

        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setForeground(new Color(200, 220, 255)); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setForeground(Color.WHITE); }
        });
        return btn;
    }
}