package ui.components.panels;

import service.DashboardService.DashboardData;
import ui.components.KpiCard;
import ui.theme.FontManager;
import ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class ReceitasPanel extends JPanel {

    private static final Color BG       = Theme.BG_DARK;
    private static final Color BG_CARD  = new Color(22, 22, 38);
    private static final Color BORDER   = new Color(45, 45, 75);
    private static final Color TEXT     = new Color(180, 180, 220);
    private static final Color GREEN    = new Color(0, 200, 100);
    private static final Color MUTED    = new Color(120, 120, 160);

    public ReceitasPanel(DashboardData data) {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildHeader(data),   BorderLayout.NORTH);
        add(buildCorpo(data),    BorderLayout.CENTER);
    }

    private JPanel buildHeader(DashboardData data) {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setBackground(BG);
        row.setPreferredSize(new Dimension(0, 80));

        double total     = data.receitaTotal();
        double tributaria = data.receitasPorNatureza()
                .getOrDefault("ReceitasTributarias", 0.0);
        double transferencias = data.receitasPorNatureza()
                .getOrDefault("TransferenciasCorrentes", 0.0);

        row.add(new KpiCard("Receita Total",        KpiCard.formatar(total),          new Color(0, 120, 60)));
        row.add(new KpiCard("Receitas Tributárias",  KpiCard.formatar(tributaria),     new Color(0, 100, 180)));
        row.add(new KpiCard("Transferências",         KpiCard.formatar(transferencias), new Color(100, 60, 180)));
        return row;
    }

    private JPanel buildCorpo(DashboardData data) {
        JPanel corpo = new JPanel(new GridLayout(1, 2, 12, 0));
        corpo.setBackground(BG);

        corpo.add(wrapCard("Receitas por Natureza",    buildBarras(data.receitasPorNatureza(), data.receitaTotal())));
        corpo.add(wrapCard("Composição Tributária",    buildComposicaoTributaria(data)));

        return corpo;
    }

    private JPanel buildBarras(Map<String, Double> dados, double total) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        Map<String, String> labels = Map.of(
                "ReceitasTributarias",      "Tributárias",
                "ReceitasDeContribuicoes",  "Contribuições",
                "ReceitasPatrimoniais",     "Patrimoniais",
                "TransferenciasCorrentes",  "Transferências",
                "OutrasReceitasCorrentes",  "Outras Correntes",
                "ReceitasDeCapital",        "Capital"
        );

        Color[] cores = {
                new Color(0, 180, 100),
                new Color(0, 120, 200),
                new Color(160, 80, 200),
                new Color(200, 140, 0),
                new Color(0, 180, 180),
                new Color(200, 80, 80)
        };

        int idx = 0;
        for (var entry : dados.entrySet()) {
            double pct    = total > 0 ? entry.getValue() / total : 0;
            String label  = labels.getOrDefault(entry.getKey(), entry.getKey());
            Color  cor    = cores[idx % cores.length];

            panel.add(buildLinhaBar(label, entry.getValue(), pct, cor));
            panel.add(Box.createVerticalStrut(10));
            idx++;
        }
        return panel;
    }

    private JPanel buildLinhaBar(String label, double valor, double pct, Color cor) {
        JPanel linha = new JPanel(new BorderLayout(8, 4));
        linha.setOpaque(false);
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);

        JLabel lblNome = new JLabel(label);
        lblNome.setForeground(TEXT);
        lblNome.setFont(FontManager.inter(12f));

        JLabel lblValor = new JLabel(KpiCard.formatar(valor)
                + String.format("  (%.1f%%)", pct * 100));
        lblValor.setForeground(GREEN);
        lblValor.setFont(FontManager.interBold(12f));

        topo.add(lblNome,  BorderLayout.WEST);
        topo.add(lblValor, BorderLayout.EAST);

        JPanel barraContainer = new JPanel(new BorderLayout());
        barraContainer.setOpaque(false);
        barraContainer.setPreferredSize(new Dimension(0, 8));

        JPanel barra = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(45, 45, 75));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                int w = (int) (getWidth() * pct);
                if (w > 0) {
                    g2.setColor(cor);
                    g2.fillRoundRect(0, 0, w, getHeight(), 6, 6);
                }
                g2.dispose();
            }
        };
        barra.setOpaque(false);
        barraContainer.add(barra, BorderLayout.CENTER);

        linha.add(topo,           BorderLayout.NORTH);
        linha.add(barraContainer, BorderLayout.CENTER);
        return linha;
    }

    private JPanel buildComposicaoTributaria(DashboardData data) {
        Map<String, Double> receita = data.receitaPorCategoria();
        double total = receita.values().stream().mapToDouble(Double::doubleValue).sum();
        return buildBarras(receita, total);
    }

    private JPanel wrapCard(String titulo, JPanel conteudo) {
        JPanel card = new JPanel(new BorderLayout(0, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int s = 6, arc = 14;
                for (int i = 0; i < s; i++) {
                    g2.setColor(new Color(0, 0, 0, 3 + i * 2));
                    g2.fillRoundRect(s-i, s-i+2, getWidth()-(s-i)*2,
                            getHeight()-(s-i)*2, arc, arc);
                }
                g2.setColor(BG_CARD);
                g2.fillRoundRect(s, s, getWidth()-s*2, getHeight()-s*2, arc, arc);
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(s+1, s+1, getWidth()-s*2-2, getHeight()-s*2-2, arc, arc);
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