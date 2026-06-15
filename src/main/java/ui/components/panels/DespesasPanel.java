package ui.components.panels;

import service.DashboardService.DashboardData;
import ui.components.KpiCard;
import ui.theme.FontManager;
import ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class DespesasPanel extends JPanel {

    private static final Color BG      = Theme.BG_DARK;
    private static final Color BG_CARD = new Color(22, 22, 38);
    private static final Color BORDER  = new Color(45, 45, 75);
    private static final Color TEXT    = new Color(180, 180, 220);
    private static final Color MUTED   = new Color(90, 90, 130);
    private static final Color RED     = new Color(220, 80,  60);
    private static final Color YELLOW  = new Color(220, 180, 0);
    private static final Color GREEN   = new Color(0,   200, 100);

    // Contas do A01 que queremos exibir, em ordem de relevância
    private static final List<String> CONTAS = List.of(
            "PessoalEEncargosSociais",
            "OutrasDespesasCorrentes",
            "TransferenciasAMunicipios",
            "JurosEEncargosDaDivida",
            "Investimentos",
            "AmortizacaoDaDivida",
            "InversoesFinanceiras",
            "DemaisDespesasCorrentes"
    );

    private static final Map<String, Color> CORES = Map.of(
            "PessoalEEncargosSociais",   new Color(220, 80,  60),
            "OutrasDespesasCorrentes",   new Color(220, 140,  0),
            "TransferenciasAMunicipios", new Color(0,   180, 100),
            "JurosEEncargosDaDivida",    new Color(200,  60, 180),
            "Investimentos",             new Color(0,   120, 220),
            "AmortizacaoDaDivida",       new Color(180, 100,   0),
            "InversoesFinanceiras",      new Color(0,   180, 180),
            "DemaisDespesasCorrentes",   new Color(120, 120, 180)
    );

    private static final Map<String, String> LABELS = Map.of(
            "PessoalEEncargosSociais",   "Pessoal e Encargos",
            "OutrasDespesasCorrentes",   "Outras Correntes",
            "TransferenciasAMunicipios", "Transf. Municípios",
            "JurosEEncargosDaDivida",    "Juros da Dívida",
            "Investimentos",             "Investimentos",
            "AmortizacaoDaDivida",       "Amortização",
            "InversoesFinanceiras",      "Inversões Financeiras",
            "DemaisDespesasCorrentes",   "Demais Correntes"
    );

    public DespesasPanel(DashboardData data) {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildHeader(data), BorderLayout.NORTH);
        add(buildCorpo(data),  BorderLayout.CENTER);
    }

    // -------------------------------------------------------------------------
    // Header — 3 KPI cards
    // -------------------------------------------------------------------------

    private JPanel buildHeader(DashboardData data) {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setBackground(BG);
        row.setPreferredSize(new Dimension(0, 80));

        double pessoal = data.dotacaoAtualizada()
                .getOrDefault("PessoalEEncargosSociais", 0.0);
        double investimento = data.dotacaoAtualizada()
                .getOrDefault("Investimentos", 0.0);

        row.add(new KpiCard("Despesa Total",
                KpiCard.formatar(data.despesaTotal()), new Color(180, 60, 0)));
        row.add(new KpiCard("Pessoal (dot. atual.)",
                KpiCard.formatar(pessoal),             new Color(180, 40, 100)));
        row.add(new KpiCard("Investimentos (dot. atual.)",
                KpiCard.formatar(investimento),        new Color(0, 120, 180)));
        return row;
    }

    // -------------------------------------------------------------------------
    // Corpo — waffle + execução lado a lado
    // -------------------------------------------------------------------------

    private JPanel buildCorpo(DashboardData data) {
        JPanel corpo = new JPanel(new GridLayout(1, 2, 12, 0));
        corpo.setBackground(BG);

        Map<String, Double> liquidado = data.despesaLiquidada();
        double totalLiquidado = CONTAS.stream()
                .mapToDouble(c -> liquidado.getOrDefault(c, 0.0))
                .sum();

        corpo.add(wrapCard("Composição da Despesa Liquidada",
                buildWaffle(liquidado, totalLiquidado)));
        corpo.add(wrapCard("Execução por Natureza",
                buildExecucao(data)));
        return corpo;
    }

    // -------------------------------------------------------------------------
    // Waffle chart — 10×10 = 100 células
    // -------------------------------------------------------------------------

    private JPanel buildWaffle(Map<String, Double> liquidado, double total) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setOpaque(false);

        // Calcula quantas células cada categoria ocupa (100 total)
        List<String> sequencia = new ArrayList<>(); // 100 entradas
        List<String> contasPresentes = CONTAS.stream()
                .filter(c -> liquidado.getOrDefault(c, 0.0) > 0)
                .toList();

        // Distribui células proporcionalmente
        Map<String, Integer> celulas = new LinkedHashMap<>();
        int usadas = 0;
        for (int i = 0; i < contasPresentes.size(); i++) {
            String conta = contasPresentes.get(i);
            double pct   = total > 0 ? liquidado.getOrDefault(conta, 0.0) / total : 0;
            int    n     = (i == contasPresentes.size() - 1)
                    ? (100 - usadas)                    // última pega o resto
                    : (int) Math.round(pct * 100);
            celulas.put(conta, Math.max(n, 0));
            usadas += n;
        }

        for (var entry : celulas.entrySet())
            for (int i = 0; i < entry.getValue(); i++)
                sequencia.add(entry.getKey());

        // Grid 10×10
        JPanel grid = new JPanel(new GridLayout(10, 10, 3, 3));
        grid.setOpaque(false);
        grid.setPreferredSize(new Dimension(0, 0));

        for (int i = 0; i < 100; i++) {
            String conta = i < sequencia.size() ? sequencia.get(i) : null;
            Color  cor   = conta != null
                    ? CORES.getOrDefault(conta, MUTED)
                    : new Color(35, 35, 55);

            JPanel cel = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(cor);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                    g2.dispose();
                }
            };
            cel.setOpaque(false);
            if (conta != null) {
                String label  = LABELS.getOrDefault(conta, conta);
                double valor  = liquidado.getOrDefault(conta, 0.0);
                double pct    = total > 0 ? valor / total * 100 : 0;
                cel.setToolTipText(String.format("%s: %s (%.1f%%)",
                        label, KpiCard.formatar(valor), pct));
            }
            grid.add(cel);
        }

        // Legenda
        JPanel legenda = buildLegendaWaffle(liquidado, total, contasPresentes);

        wrapper.add(grid,    BorderLayout.CENTER);
        wrapper.add(legenda, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildLegendaWaffle(Map<String, Double> liquidado,
                                      double total, List<String> contas) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        for (String conta : contas) {
            double valor = liquidado.getOrDefault(conta, 0.0);
            if (valor <= 0) continue;
            double pct = total > 0 ? valor / total * 100 : 0;

            JPanel linha = new JPanel(new BorderLayout(6, 0));
            linha.setOpaque(false);
            linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

            // Quadrado de cor
            Color cor = CORES.getOrDefault(conta, MUTED);
            JPanel quad = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(cor);
                    g2.fillRoundRect(0, 2, 12, 12, 3, 3);
                    g2.dispose();
                }
            };
            quad.setOpaque(false);
            quad.setPreferredSize(new Dimension(14, 16));

            JLabel lblNome = new JLabel(LABELS.getOrDefault(conta, conta));
            lblNome.setForeground(TEXT);
            lblNome.setFont(FontManager.inter(11f));

            JLabel lblVal = new JLabel(String.format("%.1f%%  %s",
                    pct, KpiCard.formatar(valor)));
            lblVal.setForeground(cor);
            lblVal.setFont(FontManager.interBold(11f));

            linha.add(quad,    BorderLayout.WEST);
            linha.add(lblNome, BorderLayout.CENTER);
            linha.add(lblVal,  BorderLayout.EAST);

            panel.add(linha);
            panel.add(Box.createVerticalStrut(3));
        }
        return panel;
    }

    // -------------------------------------------------------------------------
    // Execução por natureza — dot. atualizada × liquidado + % execução
    // -------------------------------------------------------------------------

    private JPanel buildExecucao(DashboardData data) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        Map<String, Double> dotAtual  = data.dotacaoAtualizada();
        Map<String, Double> empenhado = data.despesaEmpenhada();
        Map<String, Double> liquidado = data.despesaLiquidada();

        // Cabeçalho
        panel.add(buildCabecalhoExecucao());
        panel.add(Box.createVerticalStrut(6));

        for (String conta : CONTAS) {
            double da  = dotAtual .getOrDefault(conta, 0.0);
            double emp = empenhado.getOrDefault(conta, 0.0);
            double liq = liquidado.getOrDefault(conta, 0.0);
            if (da == 0 && emp == 0 && liq == 0) continue;

            panel.add(buildLinhaExecucao(conta, da, emp, liq));
            panel.add(Box.createVerticalStrut(8));
        }
        return panel;
    }

    private JPanel buildCabecalhoExecucao() {
        JPanel h = new JPanel(new GridLayout(1, 4, 8, 0));
        h.setOpaque(false);
        h.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        for (String txt : new String[]{"Natureza", "Dot. Atualizada", "Empenhado", "% Exec."}) {
            JLabel l = new JLabel(txt, SwingConstants.RIGHT);
            l.setForeground(new Color(100, 100, 160));
            l.setFont(FontManager.interBold(10f));
            h.add(l);
        }
        ((JLabel) h.getComponent(0)).setHorizontalAlignment(SwingConstants.LEFT);
        return h;
    }

    private JPanel buildLinhaExecucao(String conta, double da, double emp, double liq) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        Color cor    = CORES.getOrDefault(conta, MUTED);
        String label = LABELS.getOrDefault(conta, conta);
        double pct   = da > 0 ? emp / da : 0;
        Color corPct = pct >= 0.8 ? GREEN : pct >= 0.5 ? YELLOW : RED;

        JPanel linha = new JPanel(new GridLayout(1, 4, 8, 0));
        linha.setOpaque(false);

        // Nome com ponto de cor
        JPanel nomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        nomePanel.setOpaque(false);
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(cor);
                g2.fillOval(0, 3, 8, 8);
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(10, 14));
        JLabel lblNome = new JLabel(label);
        lblNome.setForeground(TEXT);
        lblNome.setFont(FontManager.inter(11f));
        nomePanel.add(dot);
        nomePanel.add(lblNome);

        JLabel lblDa  = new JLabel(KpiCard.formatar(da),  SwingConstants.RIGHT);
        JLabel lblEmp = new JLabel(KpiCard.formatar(emp), SwingConstants.RIGHT);
        JLabel lblPct = new JLabel(String.format("%.1f%%", pct * 100), SwingConstants.RIGHT);

        lblDa .setForeground(MUTED); lblDa .setFont(FontManager.inter(11f));
        lblEmp.setForeground(TEXT);  lblEmp.setFont(FontManager.inter(11f));
        lblPct.setForeground(corPct); lblPct.setFont(FontManager.interBold(11f));

        linha.add(nomePanel);
        linha.add(lblDa);
        linha.add(lblEmp);
        linha.add(lblPct);

        // Barra dupla: dotação (fundo) × empenhado (frente) × liquidado (linha)
        final double daF   = da;
        final double empF  = emp;
        final double liqF  = liq;
        final Color  corF  = cor;
        JPanel barra = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Trilho (dotação)
                g2.setColor(new Color(45, 45, 75));
                g2.fillRoundRect(0, 0, w, h, 6, 6);

                // Empenhado
                double ref = Math.max(daF, empF);
                if (ref > 0) {
                    int we = (int)(w * (empF / ref));
                    g2.setColor(corF.darker());
                    g2.fillRoundRect(0, 0, we, h, 6, 6);

                    // Liquidado (mais claro, sobreposto)
                    int wl = (int)(w * (liqF / ref));
                    if (wl > 0) {
                        g2.setColor(corF);
                        g2.fillRoundRect(0, 2, wl, h - 4, 4, 4);
                    }
                }
                g2.dispose();
            }
        };
        barra.setOpaque(false);
        barra.setPreferredSize(new Dimension(0, 10));
        barra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));

        wrapper.add(linha);
        wrapper.add(Box.createVerticalStrut(3));
        wrapper.add(barra);
        return wrapper;
    }

    // -------------------------------------------------------------------------
    // wrapCard
    // -------------------------------------------------------------------------

    private JPanel wrapCard(String titulo, JPanel conteudo) {
        JPanel card = new JPanel(new BorderLayout(0, 12)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int s = 6, arc = 14;
                for (int i = 0; i < s; i++) {
                    g2.setColor(new Color(0, 0, 0, 3 + i * 2));
                    g2.fillRoundRect(s-i, s-i+2,
                            getWidth()-(s-i)*2, getHeight()-(s-i)*2, arc, arc);
                }
                g2.setColor(BG_CARD);
                g2.fillRoundRect(s, s, getWidth()-s*2, getHeight()-s*2, arc, arc);
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(s+1, s+1,
                        getWidth()-s*2-2, getHeight()-s*2-2, arc, arc);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lbl = new JLabel(titulo);
        lbl.setForeground(TEXT);
        lbl.setFont(FontManager.interBold(15f));

        conteudo.setOpaque(false);
        card.add(lbl,      BorderLayout.NORTH);
        card.add(new JScrollPane(conteudo) {{
            setOpaque(false);
            getViewport().setOpaque(false);
            setBorder(null);
        }}, BorderLayout.CENTER);
        return card;
    }
}