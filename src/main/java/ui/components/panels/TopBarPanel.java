package ui.components.panels;

import controller.NavigationController;
import db.DatabaseConfig;
import ui.theme.FontManager;
import ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;

public class TopBarPanel extends JPanel {

    private final JComboBox<EstadoItem> comboEnte;
    private final JComboBox<Integer>    comboExercicio;
    private final JComboBox<String>     comboPeriodo;

    private NavigationController controller;

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

        // ── Combos tipados ───────────────────────────────────────────────────
        comboEnte      = buildComboEstados();
        comboExercicio = new JComboBox<>(new Integer[]{2025, 2024, 2023, 2022});
        comboPeriodo   = new JComboBox<>(new String[]{
                "1º Bimestre", "2º Bimestre", "3º Bimestre",
                "4º Bimestre", "5º Bimestre", "6º Bimestre"
        });
        comboPeriodo.setSelectedIndex(1);

        estilizarCombo(comboEnte,      175);
        estilizarCombo(comboExercicio, 135);
        estilizarCombo(comboPeriodo,   135);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filters.setBackground(Theme.BG_CARD);

        filters.add(buildFilterPanel("Ente Federativo", comboEnte));
        filters.add(buildFilter("Tipo de Ente",    new String[]{"Estado", "Município"}));
        filters.add(buildFilterPanel("Exercício",  comboExercicio));
        filters.add(buildFilterPanel("Período",    comboPeriodo));

        JPanel btnContainer = new JPanel(new GridLayout(2, 1, 0, 3));
        btnContainer.setBackground(Theme.BG_CARD);
        JLabel spacer = new JLabel(" ");
        spacer.setFont(FontManager.interBold(14f));
        btnContainer.add(spacer);
        btnContainer.add(createGradientButton("Aplicar Filtros"));
        filters.add(btnContainer);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setBackground(Theme.BG_CARD);
        rightPanel.add(filters);

        add(titlePanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }

    public void bindNavigationController(NavigationController nav) {
        this.controller = nav;
    }


    private void aplicarFiltros() {
        if (controller == null) return;
        EstadoItem estado = (EstadoItem) comboEnte.getSelectedItem();
        controller.atualizarFiltro(
                estado != null ? estado.idEnte() : "23",
                (Integer) comboExercicio.getSelectedItem(),
                comboPeriodo.getSelectedIndex() + 1
        );
    }

    private JComboBox<EstadoItem> buildComboEstados() {
        JComboBox<EstadoItem> combo = new JComboBox<>();
        try (var con = DatabaseConfig.getConnection();
             var st  = con.createStatement();
             var rs  = st.executeQuery(
                     "SELECT id_ente, sigla, nome FROM estado_cache ORDER BY nome")) {
            while (rs.next())
                combo.addItem(new EstadoItem(
                        rs.getString("id_ente"),
                        rs.getString("sigla").trim(),
                        rs.getString("nome")));
        } catch (SQLException e) {
            combo.addItem(new EstadoItem("23", "CE", "Ceará"));
            combo.addItem(new EstadoItem("35", "SP", "São Paulo"));
            combo.addItem(new EstadoItem("33", "RJ", "Rio de Janeiro"));
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            if ("23".equals(combo.getItemAt(i).idEnte())) { combo.setSelectedIndex(i); break; }
        }
        return combo;
    }

    private JPanel buildFilterPanel(String label, JComboBox<?> combo) {
        JPanel fp = new JPanel(new GridLayout(2, 1, 0, 3));
        fp.setBackground(Theme.BG_CARD);
        JLabel lbl = new JLabel(label);
        lbl.setForeground(Theme.TEXT_MAIN);
        lbl.setFont(FontManager.interBold(14f));
        fp.add(lbl);
        fp.add(combo);
        return fp;
    }

    private JPanel buildFilter(String label, String[] options) {
        JComboBox<String> combo = new JComboBox<>(options);
        estilizarCombo(combo, 135);
        return buildFilterPanel(label, combo);
    }

    private <T> void estilizarCombo(JComboBox<T> combo, int width) {
        combo.putClientProperty("JComboBox.isPopDown", true);
        combo.setFont(FontManager.inter(16f));
        combo.setPreferredSize(new Dimension(width, 32));
        combo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean hasFocus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, hasFocus);
                c.setFont(FontManager.inter(12f));
                c.setBorder(new EmptyBorder(6, 10, 6, 10));
                c.setBackground(isSelected ? new Color(0, 100, 220, 180) : new Color(22, 22, 40));
                c.setForeground(Theme.TEXT_MAIN);
                return c;
            }
        });
    }

    private JButton createGradientButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(161, 187, 224), 0, getHeight(), new Color(10, 90, 220)));
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
            @Override public void mouseEntered(MouseEvent e) { btn.setForeground(new Color(200, 220, 255)); }
            @Override public void mouseExited (MouseEvent e) { btn.setForeground(Color.WHITE); }
        });
        btn.addActionListener(e -> aplicarFiltros());
        return btn;
    }

    private record EstadoItem(String idEnte, String sigla, String nome) {
        @Override public String toString() { return sigla + " — " + nome; }
    }
}