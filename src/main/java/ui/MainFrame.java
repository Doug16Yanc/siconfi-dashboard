package ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import service.DashboardService;
import ui.components.NavItem;
import ui.components.StatusBarPanel;
import ui.components.TopBarPanel;
import ui.theme.FontManager;
import ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {

    private JPanel contentArea;
    private StatusBarPanel statusBar;

    public MainFrame() {
        setTitle("Relatório orçamentário - Brasil");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1440, 900);
        setMinimumSize(new Dimension(1200, 700));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BG_DARK);

        statusBar = new StatusBarPanel();

        add(buildSidebar(), BorderLayout.WEST);
        add(buildMain(),    BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(null);

        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(Theme.BG_SIDEBAR);
        logoPanel.setBorder(new EmptyBorder(20, 16, 20, 16));

        FlatSVGIcon logoIcon = new FlatSVGIcon("icons/chart-bar.svg", 24, 24);
        logoIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Theme.ACCENT));

        JLabel logo = new JLabel("DASHBOARD");
        logo.setIcon(logoIcon);
        logo.setIconTextGap(8);
        logo.setForeground(Theme.ACCENT);
        logo.setFont(FontManager.interBold(20f));

        JPanel logoText = new JPanel(new GridLayout(2, 1));
        logoText.setBackground(Theme.BG_SIDEBAR);
        logoText.add(logo);
        logoPanel.add(logoText, BorderLayout.CENTER);
        logoPanel.setMaximumSize(new Dimension(270, 70));
        sidebar.add(logoPanel);

        sidebar.add(separator());

        sidebar.add(sectionLabel("CONSULTAS"));
        sidebar.add(new NavItem("home.svg",        "Visão Geral",        true));
        sidebar.add(new NavItem("receitas.svg",    "Receitas",           false));
        sidebar.add(new NavItem("despesas.svg",    "Despesas",           false));
        sidebar.add(new NavItem("fiscal.svg",      "Resultado Fiscal",   false));
        sidebar.add(new NavItem("divida.svg",      "Dívida Consolidada", false));
        sidebar.add(new NavItem("folder.svg",      "Restos a Pagar",     false));
        sidebar.add(new NavItem("calendar.svg",    "Orçamentos",         false));
        sidebar.add(new NavItem("chart-bar.svg",   "Comparações",        false));

        sidebar.add(sectionLabel("ENTES"));
        sidebar.add(new NavItem("flag.svg",        "União",              false));
        sidebar.add(new NavItem("map.svg",         "Estados",            false));
        sidebar.add(new NavItem("city.svg",        "Municípios",         false));

        sidebar.add(sectionLabel("FERRAMENTAS"));
        sidebar.add(new NavItem("trending-up.svg", "Indicadores Fiscais",false));
        sidebar.add(new NavItem("file-text.svg",   "Relatórios",         false));
        sidebar.add(new NavItem("download.svg",    "Exportar Dados",     false));
        sidebar.add(new NavItem("clock.svg",       "Agendamentos",       false));
        sidebar.add(new NavItem("settings.svg",    "Configurações",      false));
        sidebar.add(new NavItem("info.svg",        "Sobre",              false));

        sidebar.add(Box.createVerticalStrut(12));
        return sidebar;
    }

    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.BG_DARK);
        main.add(new TopBarPanel(), BorderLayout.NORTH);
        main.add(buildContent(),    BorderLayout.CENTER);
        main.add(statusBar,         BorderLayout.SOUTH);
        return main;
    }

    private JScrollPane buildContent() {
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(Theme.BG_DARK);
        contentArea.setBorder(new EmptyBorder(16, 16, 16, 16));
        contentArea.add(new ui.dashboard.DashboardPanel(), BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(contentArea);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(Theme.BG_DARK);
        return scroll;
    }

    public void setContent(JPanel panel) {
        contentArea.removeAll();
        contentArea.add(panel, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    public void atualizarIndicadores(DashboardService.DashboardData data) {
        statusBar.atualizarDados(data);
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
}