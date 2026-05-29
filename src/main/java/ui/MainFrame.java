package ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import service.DashboardService;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private static final Color BG_DARK    = new Color(13, 13, 23);
    private static final Color BG_CARD    = new Color(22, 22, 38);
    private static final Color BG_SIDEBAR = new Color(16, 16, 28);
    private static final Color ACCENT     = new Color(0, 120, 255);
    private static final Color TEXT_MAIN  = new Color(230, 230, 255);
    private static final Color TEXT_MUTED = new Color(172, 172, 238);

    private JPanel contentArea;
    private JPanel statusBar;

    public MainFrame() {
        setTitle("Relatório orçamentário - Brasil");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1440, 900);
        setMinimumSize(new Dimension(1200, 700));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_DARK);

        add(buildSidebar(), BorderLayout.WEST);
        add(buildMain(),    BorderLayout.CENTER);

        setVisible(true);
    }


    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(null);

        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(BG_SIDEBAR);
        logoPanel.setBorder(new EmptyBorder(20, 16, 20, 16));

        FlatSVGIcon logoIcon = new FlatSVGIcon("icons/chart-bar.svg", 24, 24);

        logoIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> ACCENT));

        JLabel logo = new JLabel("DASHBOARD");
        logo.setIcon(logoIcon);
        logo.setIconTextGap(8);
        logo.setForeground(ACCENT);
        logo.setFont(FontManager.interBold(20f));


        JPanel logoText = new JPanel(new GridLayout(2, 1));
        logoText.setBackground(BG_SIDEBAR);
        logoText.add(logo);
        logoPanel.add(logoText, BorderLayout.CENTER);
        logoPanel.setMaximumSize(new Dimension(270, 70));
        sidebar.add(logoPanel);

        sidebar.add(separator());

        sidebar.add(sectionLabel("CONSULTAS"));
        sidebar.add(navItem("home.svg",        "Visão Geral",             true));
        sidebar.add(navItem("receitas.svg",    "Receitas",                false));
        sidebar.add(navItem("despesas.svg",    "Despesas",                false));
        sidebar.add(navItem("fiscal.svg",      "Resultado Fiscal",        false));
        sidebar.add(navItem("divida.svg",      "Dívida Consolidada",      false));
        sidebar.add(navItem("folder.svg",      "Restos a Pagar",          false));
        sidebar.add(navItem("calendar.svg",    "Orçamentos",              false));
        sidebar.add(navItem("chart-bar.svg",   "Comparações",   false));

        sidebar.add(sectionLabel("ENTES"));
        sidebar.add(navItem("flag.svg",        "União",                   false));
        sidebar.add(navItem("map.svg",         "Estados",                 false));
        sidebar.add(navItem("city.svg",        "Municípios",              false));

        sidebar.add(sectionLabel("FERRAMENTAS"));
        sidebar.add(navItem("trending-up.svg", "Indicadores Fiscais",     false));
        sidebar.add(navItem("file-text.svg",   "Relatórios", false));
        sidebar.add(navItem("download.svg",    "Exportar Dados",          false));
        sidebar.add(navItem("clock.svg",       "Agendamentos",            false));

        sidebar.add(navItem("settings.svg",    "Configurações",           false));
        sidebar.add(navItem("info.svg",        "Sobre",                   false));
        sidebar.add(Box.createVerticalStrut(12));

        return sidebar;
    }


    private JPanel navItem(String iconPath, String label, boolean active) {
        JPanel item = new JPanel(new BorderLayout());
        item.setMaximumSize(new Dimension(240, 36));
        item.setMinimumSize(new Dimension(240, 36));
        item.setPreferredSize(new Dimension(240, 36));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.setBackground(active ? new Color(0, 100, 220, 60) : BG_SIDEBAR);
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setBorder(active
                ? BorderFactory.createMatteBorder(0, 3, 0, 0, ACCENT)
                : new EmptyBorder(0, 3, 0, 0));

        FlatSVGIcon icon = new FlatSVGIcon("icons/" + iconPath, 16, 16);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color ->
                active ? new Color(180, 200, 255) : new Color(120, 120, 160)
        ));
        JLabel ico = new JLabel(icon);

        JLabel lbl = new JLabel(label);
        lbl.setFont(FontManager.inter(14f));
        lbl.setForeground(active ? TEXT_MAIN : TEXT_MUTED);

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(0, 2, 0, 0));

        JPanel iconText = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        iconText.setOpaque(false);
        iconText.add(ico);
        iconText.add(lbl);

        inner.add(iconText, BorderLayout.CENTER);
        item.add(inner, BorderLayout.CENTER);

        item.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!active) {
                    item.setBackground(new Color(30, 30, 55));
                    lbl.setForeground(TEXT_MAIN);
                    icon.setColorFilter(new FlatSVGIcon.ColorFilter(
                            c -> new Color(180, 200, 255)));
                    ico.repaint();
                }
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!active) {
                    item.setBackground(BG_SIDEBAR);
                    lbl.setForeground(TEXT_MUTED);
                    icon.setColorFilter(new FlatSVGIcon.ColorFilter(
                            c -> new Color(120, 120, 160)));
                    ico.repaint();
                }
            }
        });

        return item;
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(0, 248, 248));
        lbl.setFont(FontManager.interBold(13f));
        lbl.setBorder(new EmptyBorder(12, 2, 12, 6));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return lbl;
    }

    private JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(190, 190, 241));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        return sep;
    }

    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG_DARK);
        main.add(buildTopBar(),    BorderLayout.NORTH);
        main.add(buildContent(),   BorderLayout.CENTER);
        main.add(buildStatusBar(), BorderLayout.SOUTH);
        return main;
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG_CARD);
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(35, 35, 60)),
                new EmptyBorder(16, 24, 10, 24)
        ));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setBackground(BG_CARD);

        JLabel title = new JLabel("🏠  Visão Geral");
        title.setForeground(TEXT_MAIN);
        title.setFont(FontManager.interBold(20f));

        JLabel titleSub = new JLabel("Dados consolidados da União, Estados e Municípios");
        titleSub.setForeground(TEXT_MAIN);
        titleSub.setFont(FontManager.interBold(14f));

        titlePanel.add(title);
        titlePanel.add(titleSub);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filters.setBackground(BG_CARD);

        filters.add(buildFilter("Ente Federativo", new String[]{"Todos", "Ceará", "União"}));
        filters.add(buildFilter("Tipo de Ente",    new String[]{"Todos", "Estado", "Município"}));
        filters.add(buildFilter("Exercício",        new String[]{"2025", "2024", "2023", "2022"}));
        filters.add(buildFilter("Período",          new String[]{"Até Abril", "Até Junho", "Até Dezembro"}));

        JButton btnFilter = new JButton("Aplicar Filtros") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, 0,            new Color(161, 187, 224),
                        0, getHeight(),  new Color(10, 90, 220)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnFilter.setOpaque(false);
        btnFilter.setContentAreaFilled(false);
        btnFilter.setBorderPainted(false);
        btnFilter.setForeground(Color.WHITE);
        btnFilter.setFont(FontManager.interBold(12f));
        btnFilter.setBorder(new EmptyBorder(9, 18, 9, 18));
        btnFilter.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnFilter.setFocusPainted(false);

        btnFilter.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnFilter.setForeground(new Color(200, 220, 255));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnFilter.setForeground(Color.WHITE);
            }
        });

        JPanel statusChips = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        statusChips.setBackground(BG_CARD);

        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        rightPanel.setBackground(BG_CARD);
        rightPanel.add(statusChips);
        rightPanel.add(filters);

        top.add(titlePanel, BorderLayout.WEST);
        top.add(rightPanel, BorderLayout.EAST);

        return top;
    }

    private JPanel buildFilter(String label, String[] options) {
        JPanel fp = new JPanel(new GridLayout(2, 1, 0, 3));
        fp.setBackground(BG_CARD);

        JLabel lbl = new JLabel(label);
        lbl.setForeground(TEXT_MAIN);
        lbl.setFont(FontManager.interBold(14f));

        JComboBox<String> combo = new JComboBox<>(options);

        combo.putClientProperty("JComboBox.isPopDown", true);

        combo.setFont(FontManager.inter(16f));
        combo.setPreferredSize(new Dimension(135, 32));
        combo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value,
                    int index, boolean isSelected, boolean hasFocus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, hasFocus);
                c.setFont(FontManager.inter(12f));
                c.setBorder(new EmptyBorder(6, 10, 6, 10));
                c.setBackground(isSelected
                        ? new Color(0, 100, 220, 180)
                        : new Color(22, 22, 40));
                c.setForeground(new Color(230, 230, 255));
                return c;
            }
        });

        fp.add(lbl);
        fp.add(combo);
        return fp;
    }

    private JLabel statusChip(String text) {
        JLabel chip = new JLabel(text);
        chip.setForeground(TEXT_MUTED);
        chip.setFont(FontManager.inter(11f));
        chip.setBorder(new EmptyBorder(4, 8, 4, 8));
        return chip;
    }


    private JScrollPane buildContent() {
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(BG_DARK);
        contentArea.setBorder(new EmptyBorder(16, 16, 16, 16));

        contentArea.add(new ui.dashboard.DashboardPanel(), BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(contentArea);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(BG_DARK);
        return scroll;
    }

    private JPanel buildStatusBar() {
        statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 6)); // ← campo da classe
        statusBar.setBackground(new Color(10, 10, 20));
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(35, 35, 60)));
        statusBar.add(statusItem("DÓLAR",                "—",   "—", true));
        statusBar.add(vDivider());
        statusBar.add(statusItem("SELIC",                "—",   "—", true));
        statusBar.add(vDivider());
        statusBar.add(statusItem("IPCA (12m)",           "—",   "—", true));
        statusBar.add(vDivider());
        statusBar.add(statusItem("PIB (2025)",           "2,90%", "+0,10%", true));
        statusBar.add(vDivider());
        statusBar.add(statusItem("Dívida Bruta do Gov.", "76,5% do PIB", "-0,3 pp", true));

        JLabel next = new JLabel("Próxima atualização SICONFI: 01/06/2026 08:00");
        next.setForeground(TEXT_MAIN);
        next.setFont(FontManager.interBold(16f));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setBackground(new Color(10, 10, 20));
        right.add(next);

        statusBar.add(Box.createHorizontalGlue());
        statusBar.add(right);

        return statusBar;
    }


    private JPanel statusItem(String label, String value, String variation, boolean positive) {
        JPanel p = new JPanel(new GridLayout(1, 3, 8, 0));
        p.setBackground(new Color(10, 10, 20));

        JLabel lbl = new JLabel(label);
        lbl.setForeground(TEXT_MUTED);
        lbl.setFont(FontManager.inter(15f));

        JLabel val = new JLabel(value);
        val.setForeground(TEXT_MAIN);
        val.setFont(FontManager.jetbrainsBold(15f));

        JLabel var = new JLabel(variation);
        var.setForeground(positive ? new Color(0, 200, 100) : new Color(220, 60, 60));
        var.setFont(FontManager.jetbrains(15f));

        p.add(lbl);
        p.add(val);
        p.add(var);
        return p;
    }

    private JSeparator vDivider() {
        JSeparator s = new JSeparator(SwingConstants.VERTICAL);
        s.setForeground(new Color(40, 40, 65));
        s.setPreferredSize(new Dimension(1, 20));
        return s;
    }

    public void setContent(JPanel panel) {
        contentArea.removeAll();
        contentArea.add(panel, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    public void atualizarIndicadores(DashboardService.DashboardData data) {
        statusBar.removeAll();

        String dolar = String.format("R$ %.2f", data.dolar()).replace(".", ",");
        String selic  = String.format("%.2f%%", data.selic());
        String ipca   = String.format("%.2f%%", data.ipca12m());

        statusBar.add(statusItem("DÓLAR",      dolar,  "+0,00%", true));
        statusBar.add(vDivider());
        statusBar.add(statusItem("SELIC",      selic,  "0,00%",  true));
        statusBar.add(vDivider());
        statusBar.add(statusItem("IPCA (12m)", ipca,   "0,00%",  true));
        statusBar.add(vDivider());
        statusBar.add(statusItem("PIB (2025)",           "2,90%",        "+0,10%", true));
        statusBar.add(vDivider());
        statusBar.add(statusItem("Dívida Bruta do Gov.", "76,5% do PIB", "-0,3pp", true));

        statusBar.revalidate();
        statusBar.repaint();
    }
}