package ui.components.panels;

import service.ComparacoesService;
import service.ComparacoesService.*;
import ui.theme.FontManager;
import ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

public class ComparacoesPanel extends JPanel {

    private static final Color BG      = Theme.BG_DARK;
    private static final Color BG_CARD = new Color(22, 22, 38);
    private static final Color BORDER  = new Color(45, 45, 75);
    private static final Color TEXT    = new Color(180, 180, 220);
    private static final Color MUTED   = new Color(90, 90, 130);

    private static final String[] BIMESTRES = {
            "Bim 1\nFev", "Bim 2\nAbr", "Bim 3\nJun",
            "Bim 4\nAgo", "Bim 5\nOut", "Bim 6\nDez"
    };

    private final ComparacoesService service;
    private final int                ano;

    private JPanel         heatmapPanel;
    private JComboBox<Regiao>  cmbRegiao;
    private JComboBox<Metrica> cmbMetrica;
    private JLabel         lblStatus;

    public ComparacoesPanel(ComparacoesService service, int ano) {
        this.service = service;
        this.ano     = ano;

        setLayout(new BorderLayout(0, 12));
        setBackground(BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildControles(), BorderLayout.NORTH);
        add(buildHeatmapContainer(), BorderLayout.CENTER);

        carregarAsync(Regiao.NORDESTE, Metrica.RESULTADO_RCL);
    }


    private JPanel buildControles() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        row.setBackground(BG);
        row.setPreferredSize(new Dimension(0, 44));

        cmbRegiao  = styledCombo(Regiao.values());
        cmbMetrica = styledCombo(Metrica.values());

        cmbRegiao .setSelectedItem(Regiao.NORDESTE);
        cmbMetrica.setSelectedItem(Metrica.RESULTADO_RCL);

        JButton btnAplicar = new JButton("Aplicar") {{
            setBackground(new Color(0, 100, 200));
            setForeground(Color.WHITE);
            setFont(FontManager.interBold(12f));
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }};
        btnAplicar.addActionListener(e -> carregarAsync(
                (Regiao)  cmbRegiao .getSelectedItem(),
                (Metrica) cmbMetrica.getSelectedItem()));

        lblStatus = new JLabel("Selecione região e métrica");
        lblStatus.setForeground(MUTED);
        lblStatus.setFont(FontManager.inter(12f));

        row.add(label("Região:"));
        row.add(cmbRegiao);
        row.add(label("Métrica:"));
        row.add(cmbMetrica);
        row.add(btnAplicar);
        row.add(Box.createHorizontalStrut(16));
        row.add(lblStatus);
        return row;
    }

    private <T> JComboBox<T> styledCombo(T[] items) {
        JComboBox<T> cmb = new JComboBox<>(items) {{
            setBackground(BG_CARD);
            setForeground(TEXT);
            setFont(FontManager.inter(12f));
            ((JLabel) getRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        }};
        cmb.setRenderer((list, value, index, sel, focus) -> {
            JLabel lbl = new JLabel(value instanceof Regiao r ? r.label
                    : value instanceof Metrica m ? m.label : value.toString());
            lbl.setForeground(sel ? Color.WHITE : TEXT);
            lbl.setBackground(sel ? new Color(0, 80, 160) : BG_CARD);
            lbl.setFont(FontManager.inter(12f));
            lbl.setOpaque(true);
            lbl.setBorder(new EmptyBorder(4, 8, 4, 8));
            return lbl;
        });
        return cmb;
    }

    private JLabel label(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(MUTED);
        l.setFont(FontManager.inter(12f));
        return l;
    }


    private JPanel buildHeatmapContainer() {
        heatmapPanel = new JPanel(new BorderLayout());
        heatmapPanel.setBackground(BG);

        JLabel placeholder = new JLabel("Carregando...", SwingConstants.CENTER);
        placeholder.setForeground(MUTED);
        placeholder.setFont(FontManager.inter(14f));
        heatmapPanel.add(placeholder);

        return wrapCard("Heatmap Fiscal — UF × Bimestre", heatmapPanel);
    }

    private void carregarAsync(Regiao regiao, Metrica metrica) {
        lblStatus.setText("⏳ Carregando " + regiao.label + "...");
        lblStatus.setForeground(new Color(220, 180, 0));

        heatmapPanel.removeAll();
        JLabel loading = new JLabel("Buscando dados...", SwingConstants.CENTER);
        loading.setForeground(MUTED);
        loading.setFont(FontManager.inter(13f));
        heatmapPanel.add(loading);
        heatmapPanel.revalidate();
        heatmapPanel.repaint();

        Thread.ofVirtual().start(() -> {
            try {
                DadosComparacao dados = service.carregar(regiao, ano, metrica);
                SwingUtilities.invokeLater(() -> renderHeatmap(dados));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("❌ Erro: " + e.getMessage());
                    lblStatus.setForeground(new Color(220, 60, 60));
                });
            }
        });
    }

    private void renderHeatmap(DadosComparacao dados) {
        heatmapPanel.removeAll();
        heatmapPanel.setLayout(new BorderLayout(0, 8));

        double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
        for (var porBimestre : dados.heatmap().values()) {
            for (double v : porBimestre.values()) {
                if (v != 0) { min = Math.min(min, v); max = Math.max(max, v); }
            }
        }
        if (min == Double.MAX_VALUE) { min = 0; max = 1; }
        final double fMin = min, fMax = max;

        int nEntes = dados.heatmap().size();
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(BG_CARD);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(2, 2, 2, 2);

        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0.08;
        gbc.weighty = 0;
        grid.add(celulaCabecalho("UF"), gbc);

        for (int b = 1; b <= 6; b++) {
            gbc.gridx = b;
            gbc.weightx = 0.15;
            grid.add(celulaCabecalho(BIMESTRES[b - 1].replace("\n", " ")), gbc);
        }

        int row = 1;
        for (var entry : dados.heatmap().entrySet()) {
            String idEnte      = entry.getKey();
            String sigla       = dados.nomes().getOrDefault(idEnte, idEnte);
            Map<Integer, Double> porBim = entry.getValue();

            gbc.gridy   = row;
            gbc.gridx   = 0;
            gbc.weightx = 0.08;
            gbc.weighty = 1.0 / nEntes;
            grid.add(celulaUF(sigla, idEnte.equals("23")), gbc); // destaca CE

            for (int b = 1; b <= 6; b++) {
                double valor = porBim.getOrDefault(b, Double.NaN);
                gbc.gridx   = b;
                gbc.weightx = 0.15;
                grid.add(celulaHeatmap(valor, fMin, fMax, dados.metrica()), gbc);
            }
            row++;
        }

        JPanel legenda = buildLegenda(fMin, fMax, dados.metrica());

        heatmapPanel.add(grid,    BorderLayout.CENTER);
        heatmapPanel.add(legenda, BorderLayout.SOUTH);

        lblStatus.setText("✅ " + dados.regiao().label
                + " — " + dados.metrica().label + " — " + dados.ano());
        lblStatus.setForeground(new Color(0, 200, 100));

        heatmapPanel.revalidate();
        heatmapPanel.repaint();
    }

    private JLabel celulaCabecalho(String txt) {
        JLabel l = new JLabel(txt, SwingConstants.CENTER);
        l.setForeground(new Color(120, 120, 180));
        l.setFont(FontManager.interBold(11f));
        l.setOpaque(true);
        l.setBackground(new Color(18, 18, 32));
        l.setBorder(new EmptyBorder(6, 4, 6, 4));
        return l;
    }

    private JLabel celulaUF(String sigla, boolean destaque) {
        JLabel l = new JLabel(sigla, SwingConstants.CENTER);
        l.setForeground(destaque ? new Color(0, 220, 120) : TEXT);
        l.setFont(destaque ? FontManager.interBold(12f) : FontManager.inter(12f));
        l.setOpaque(true);
        l.setBackground(new Color(18, 18, 32));
        l.setBorder(new EmptyBorder(4, 6, 4, 6));
        return l;
    }

    private JPanel celulaHeatmap(double valor, double min, double max, Metrica metrica) {
        JPanel cell = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };

        if (Double.isNaN(valor) || valor == 0) {
            cell.setBackground(new Color(28, 28, 48));
            JLabel nd = new JLabel("N/D", SwingConstants.CENTER);
            nd.setForeground(new Color(60, 60, 90));
            nd.setFont(FontManager.inter(10f));
            cell.add(nd);
            return cell;
        }

        double t = max > min ? (valor - min) / (max - min) : 0.5;

        Color cor = gradiente(t, metrica);
        cell.setBackground(cor);

        String label = formatarValor(valor, metrica);
        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setForeground(luminancia(cor) > 0.4 ? new Color(20, 20, 20) : Color.WHITE);
        lbl.setFont(FontManager.interBold(11f));

        cell.setToolTipText(String.format("%s: %s", metrica.label, label));
        cell.add(lbl);

        cell.addMouseListener(new MouseAdapter() {
            final Color base = cor;
            @Override public void mouseEntered(MouseEvent e) {
                cell.setBackground(base.brighter());
                cell.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                cell.setBackground(base);
                cell.repaint();
            }
        });

        return cell;
    }

    private Color gradiente(double t, Metrica metrica) {
        boolean inverso = metrica == Metrica.PESSOAL_RCL;
        double  tt      = inverso ? 1.0 - t : t;

        if (tt < 0.5) {
            int r = 200;
            int g = (int)(tt * 2 * 180);
            return new Color(r, g, 20);
        } else {
            int r = (int)((1.0 - (tt - 0.5) * 2) * 200);
            int g = 180;
            return new Color(r, g, 20);
        }
    }

    private double luminancia(Color c) {
        return (0.299 * c.getRed() + 0.587 * c.getGreen()
                + 0.114 * c.getBlue()) / 255.0;
    }

    private String formatarValor(double valor, Metrica metrica) {
        return switch (metrica) {
            case RECEITA_HAB -> String.format("R$%.0f", valor);
            default          -> String.format("%.1f%%", valor);
        };
    }

    private JPanel buildLegenda(double min, double max, Metrica metrica) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(BG_CARD);
        p.setPreferredSize(new Dimension(0, 28));
        p.setBorder(new EmptyBorder(4, 8, 4, 8));

        JPanel barra = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                for (int x = 0; x < getWidth(); x++) {
                    double t = (double) x / getWidth();
                    g2.setColor(gradiente(t, metrica));
                    g2.fillRect(x, 0, 1, getHeight());
                }
                g2.dispose();
            }
        };
        barra.setPreferredSize(new Dimension(200, 16));

        JLabel lblMin = new JLabel(formatarValor(min, metrica));
        JLabel lblMax = new JLabel(formatarValor(max, metrica));
        lblMin.setForeground(MUTED); lblMin.setFont(FontManager.inter(10f));
        lblMax.setForeground(MUTED); lblMax.setFont(FontManager.inter(10f));

        p.add(lblMin,  BorderLayout.WEST);
        p.add(barra,   BorderLayout.CENTER);
        p.add(lblMax,  BorderLayout.EAST);
        return p;
    }

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
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lbl = new JLabel(titulo);
        lbl.setForeground(TEXT);
        lbl.setFont(FontManager.interBold(15f));

        card.add(lbl,      BorderLayout.NORTH);
        card.add(conteudo, BorderLayout.CENTER);
        return card;
    }
}