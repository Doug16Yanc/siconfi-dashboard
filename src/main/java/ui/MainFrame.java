package ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import controller.NavigationController;
import service.DashboardService;
import ui.components.NavItem;
import ui.components.panels.StatusBarPanel;
import ui.components.panels.TopBarPanel;
import ui.theme.FontManager;
import ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {

    private JPanel          contentArea;
    private StatusBarPanel  statusBar;
    private JPanel          sidebar;

    private final DashboardService     dashboardService = new DashboardService();
    private final NavigationController navController;

    public MainFrame() {
        setTitle("Relatório orçamentário - Brasil");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1440, 900);
        setMinimumSize(new Dimension(1200, 700));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BG_DARK);

        statusBar    = new StatusBarPanel();
        navController = new NavigationController(this, dashboardService);

        sidebar = buildSidebar();

        add(sidebar,      BorderLayout.WEST);
        add(buildMain(),  BorderLayout.CENTER);

        setVisible(true);

        navController.registrar(sidebar);

        navController.navegarPara(NavigationController.NavDestino.VISAO_GERAL);
    }

    private JPanel buildSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(Theme.BG_SIDEBAR);
        sb.setPreferredSize(new Dimension(240, 0));
        sb.setBorder(null);

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
        sb.add(logoPanel);

        sb.add(separator());

        sb.add(sectionLabel("CONSULTAS"));
        sb.add(new NavItem("home.svg",        "Visão Geral",        true));
        sb.add(new NavItem("receitas.svg",    "Receitas",           false));
        sb.add(new NavItem("despesas.svg",    "Despesas",           false));
        sb.add(new NavItem("fiscal.svg",      "Resultado Fiscal",   false));
        sb.add(new NavItem("divida.svg",      "Dívida Consolidada", false));
        sb.add(new NavItem("folder.svg",      "Restos a Pagar",     false));
        sb.add(new NavItem("calendar.svg",    "Orçamentos",         false));
        sb.add(new NavItem("chart-bar.svg",   "Comparações",        false));

        sb.add(sectionLabel("ENTES"));
        sb.add(new NavItem("flag.svg",        "União",              false));
        sb.add(new NavItem("map.svg",         "Estados",            false));
        sb.add(new NavItem("city.svg",        "Municípios",         false));

        sb.add(sectionLabel("FERRAMENTAS"));
        sb.add(new NavItem("trending-up.svg", "Indicadores Fiscais",false));
        sb.add(new NavItem("file-text.svg",   "Relatórios",         false));
        sb.add(new NavItem("download.svg",    "Exportar Dados",     false));
        sb.add(new NavItem("clock.svg",       "Agendamentos",       false));
        sb.add(new NavItem("settings.svg",    "Configurações",      false));
        sb.add(new NavItem("info.svg",        "Sobre",              false));

        sb.add(Box.createVerticalStrut(12));
        return sb;
    }

    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.BG_DARK);

        TopBarPanel topBar = new TopBarPanel();
        topBar.bindNavigationController(navController);

        main.add(topBar,          BorderLayout.NORTH);
        main.add(buildContent(),  BorderLayout.CENTER);
        main.add(statusBar,       BorderLayout.SOUTH);
        return main;
    }

    private JScrollPane buildContent() {
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(Theme.BG_DARK);
        contentArea.setBorder(new EmptyBorder(16, 16, 16, 16));

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