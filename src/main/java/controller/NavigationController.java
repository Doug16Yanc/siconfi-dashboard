package controller;

import service.DashboardService;
import ui.MainFrame;
import ui.dashboard.Dashboard;
import ui.components.panels.TimeSeriesPanel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;


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
                String nomeEnte = service.getNomeEnte(idEnte);

                SwingUtilities.invokeLater(() -> {
                    frame.setContent(new Dashboard(data, nomeEnte, ano));
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

    public String getIdEnte()  { return idEnte;  }
    public int    getAno()     { return ano;      }
    public int    getPeriodo() { return periodo;  }
}