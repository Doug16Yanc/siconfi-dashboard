package controller;

import service.ComparacoesService;
import service.DashboardService;
import ui.MainFrame;
import ui.components.panels.*;
import ui.dashboard.Dashboard;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class NavigationController {

    public enum NavDestino {
        VISAO_GERAL,
        RESULTADO_FISCAL,
        RECEITAS,
        DESPESAS,
        DIVIDA_CONSOLIDADA,
        RESTOS_A_PAGAR,
        ORCAMENTOS,
        COMPARACOES
    }

    private final MainFrame        frame;
    private final DashboardService service;
    private final ComparacoesService comparacoesService = new ComparacoesService();

    private String idEnte  = DashboardService.DEFAULT_ID_ENTE;
    private int    ano     = DashboardService.DEFAULT_ANO;
    private int    periodo = DashboardService.DEFAULT_PERIODO;

    private NavDestino destinoAtual = NavDestino.VISAO_GERAL;

    private final Map<ui.components.NavItem, NavDestino> mapa = new HashMap<>();

    public NavigationController(MainFrame frame, DashboardService service) {
        this.frame   = frame;
        this.service = service;
    }

    public void registrar(JPanel sidebar) {
        NavDestino[] destinos = {
                NavDestino.VISAO_GERAL,
                NavDestino.RECEITAS,
                NavDestino.DESPESAS,
                NavDestino.RESULTADO_FISCAL,
                NavDestino.DIVIDA_CONSOLIDADA,
                NavDestino.RESTOS_A_PAGAR,
                NavDestino.ORCAMENTOS,
                NavDestino.COMPARACOES
        };

        int idx = 0;
        for (Component c : sidebar.getComponents()) {
            if (c instanceof ui.components.NavItem item && idx < destinos.length) {
                NavDestino dest = destinos[idx++];
                mapa.put(item, dest);
                item.setOnClick(() -> navegarPara(dest));
            }
        }
    }

    private void atualizarDestaque(NavDestino destino) {
        mapa.forEach((item, dest) -> item.setActive(dest == destino));
    }

    public void navegarPara(NavDestino destino) {
        destinoAtual = destino;
        atualizarDestaque(destino);

        switch (destino) {
            case VISAO_GERAL      -> carregarVisaoGeral();
            case RESULTADO_FISCAL -> carregarSerie();
            case RECEITAS -> carregarReceitas();
            case DESPESAS -> carregarDespesas();
            case COMPARACOES -> carregarComparacoes();
            case RESTOS_A_PAGAR -> carregarRestosAPagar();
            case DIVIDA_CONSOLIDADA -> carregarDividaConsolidada();
            default               -> frame.setContent(painelPlaceholder(destino.name()));
        }
    }

    public void atualizarFiltro(String novoIdEnte, int novoAno, int novoPeriodo) {
        this.idEnte  = novoIdEnte;
        this.ano     = novoAno;
        this.periodo = novoPeriodo;
        navegarPara(destinoAtual);
    }

    private void carregarVisaoGeral() {
        frame.setContent(painelCarregando("Carregando dados fiscais..."));

        Thread.ofVirtual().start(() -> {
            try {
                DashboardService.DashboardData data = service.carregarTudo(idEnte, ano, periodo);

                SwingUtilities.invokeLater(() -> {
                    frame.setContent(new Dashboard(data));
                    frame.atualizarIndicadores(data);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        frame.setContent(painelErro("Erro ao carregar dados: " + e.getMessage())));
            }
        });
    }

    private void carregarSerie() {
        frame.setContent(painelCarregando("Carregando série histórica..."));

        Thread.ofVirtual().start(() -> {
            try {
                var serie = service.carregarSerie(idEnte, ano);
                SwingUtilities.invokeLater(() ->
                        frame.setContent(new TimeSeriesPanel(serie, idEnte, ano)));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        frame.setContent(painelErro("Erro ao carregar série: " + e.getMessage())));
            }
        });
    }

    private JPanel painelCarregando(String msg) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(ui.theme.Theme.BG_DARK);
        JLabel lbl = new JLabel(msg);
        lbl.setForeground(ui.theme.Theme.TEXT_MUTED);
        lbl.setFont(ui.theme.FontManager.inter(14f));
        p.add(lbl);
        return p;
    }

    private JPanel painelErro(String msg) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(ui.theme.Theme.BG_DARK);
        JLabel lbl = new JLabel(msg);
        lbl.setForeground(new Color(220, 60, 60));
        lbl.setFont(ui.theme.FontManager.inter(14f));
        p.add(lbl);
        return p;
    }

    private JPanel painelPlaceholder(String nome) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(ui.theme.Theme.BG_DARK);
        JLabel lbl = new JLabel(nome + " — em breve");
        lbl.setForeground(ui.theme.Theme.TEXT_MUTED);
        lbl.setFont(ui.theme.FontManager.inter(14f));
        p.add(lbl);
        return p;
    }


    private void carregarReceitas() {
        frame.setContent(painelCarregando("Carregando receitas..."));
        Thread.ofVirtual().start(() -> {
            try {
                var data = service.carregarTudo(idEnte, ano, periodo);
                SwingUtilities.invokeLater(() -> frame.setContent(new ReceitasPanel(data)));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        frame.setContent(painelErro("Erro: " + e.getMessage())));
            }
        });
    }

    private void carregarDespesas() {
        frame.setContent(painelCarregando("Carregando despesas..."));
        Thread.ofVirtual().start(() -> {
            try {
                var data = service.carregarTudo(idEnte, ano, periodo);
                SwingUtilities.invokeLater(() -> frame.setContent(new DespesasPanel(data)));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        frame.setContent(painelErro("Erro: " + e.getMessage())));
            }
        });
    }

    private void carregarDividaConsolidada() {
        frame.setContent(painelCarregando("Carregando dívida consolidada..."));
        Thread.ofVirtual().start(() -> {
            try {
                var data = service.carregarTudo(idEnte, ano, periodo);
                SwingUtilities.invokeLater(() ->
                        frame.setContent(new DividaConsolidadaPanel(data)));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        frame.setContent(painelErro("Erro: " + e.getMessage())));
            }
        });
    }

    private void carregarComparacoes() {
        frame.setContent(new ComparacoesPanel(comparacoesService, ano));
    }

    private void carregarRestosAPagar() {
        frame.setContent(painelCarregando("Carregando restos a pagar..."));
        Thread.ofVirtual().start(() -> {
            try {
                var data = service.carregarTudo(idEnte, ano, periodo);
                SwingUtilities.invokeLater(() ->
                        frame.setContent(new RestosAPagarPanel(data)));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        frame.setContent(painelErro("Erro: " + e.getMessage())));
            }
        });
    }

}