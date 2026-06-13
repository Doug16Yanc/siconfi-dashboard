package ui.components;

import controller.NavigationController;
import db.DatabaseConfig;
import ui.theme.FontManager;
import ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class StateFilterBar extends JPanel {

    private final JComboBox<EstadoItem> comboEstado;
    private final JComboBox<Integer>    comboAno;
    private final JComboBox<String>     comboPeriodo;

    private NavigationController controller;

    private boolean inicializando = true;

    public StateFilterBar() {
        setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        setBackground(Theme.BG_SIDEBAR);
        setBorder(new EmptyBorder(0, 0, 0, 16));

        JLabel lbl = new JLabel("Filtros:");
        lbl.setForeground(Theme.TEXT_MUTED);
        lbl.setFont(FontManager.inter(13f));

        comboEstado = new JComboBox<>(carregarEstados());
        estilizarCombo(comboEstado);
        comboEstado.setPreferredSize(new Dimension(170, 28));
        selecionarEstadoPadrao(comboEstado);

        Integer[] anos = {2025, 2024, 2023};
        comboAno = new JComboBox<>(anos);
        estilizarCombo(comboAno);
        comboAno.setPreferredSize(new Dimension(72, 28));

        String[] periodos = {
                "1º Bimestre", "2º Bimestre", "3º Bimestre",
                "4º Bimestre", "5º Bimestre", "6º Bimestre"
        };
        comboPeriodo = new JComboBox<>(periodos);
        estilizarCombo(comboPeriodo);
        comboPeriodo.setSelectedIndex(1);
        comboPeriodo.setPreferredSize(new Dimension(120, 28));

        JButton btnAtualizar = new JButton("Atualizar");
        btnAtualizar.setFont(FontManager.interBold(12f));
        btnAtualizar.setForeground(Theme.ACCENT);
        btnAtualizar.setBackground(Theme.BG_SIDEBAR);
        btnAtualizar.setBorder(BorderFactory.createLineBorder(Theme.ACCENT, 1));
        btnAtualizar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAtualizar.setFocusPainted(false);
        btnAtualizar.setPreferredSize(new Dimension(90, 28));
        btnAtualizar.addActionListener(e -> disparar());

        add(lbl);
        add(vDivider());
        add(new JLabel(labelCombo("Estado")) {{ setForeground(Theme.TEXT_MUTED); setFont(FontManager.inter(12f)); }});
        add(comboEstado);
        add(vDivider());
        add(new JLabel(labelCombo("Ano")) {{ setForeground(Theme.TEXT_MUTED); setFont(FontManager.inter(12f)); }});
        add(comboAno);
        add(vDivider());
        add(new JLabel(labelCombo("Período")) {{ setForeground(Theme.TEXT_MUTED); setFont(FontManager.inter(12f)); }});
        add(comboPeriodo);
        add(vDivider());
        add(btnAtualizar);

        inicializando = false;
    }

    public void bindController(NavigationController nav) {
        this.controller = nav;
    }

    public String getIdEnteSelecionado() {
        EstadoItem item = (EstadoItem) comboEstado.getSelectedItem();
        return item != null ? item.idEnte() : "23";
    }

    public int getAnoSelecionado() {
        return (Integer) comboAno.getSelectedItem();
    }

    public int getPeriodoSelecionado() {
        return comboPeriodo.getSelectedIndex() + 1;
    }


    private void disparar() {
        if (inicializando || controller == null) return;
        controller.atualizarFiltro(
                getIdEnteSelecionado(),
                getAnoSelecionado(),
                getPeriodoSelecionado()
        );
    }

    private EstadoItem[] carregarEstados() {
        String sql = "SELECT id_ente, sigla, nome FROM estado_cache ORDER BY nome";
        List<EstadoItem> lista = new ArrayList<>();

        try (var con = DatabaseConfig.getConnection();
             var st  = con.createStatement();
             var rs  = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new EstadoItem(
                        rs.getString("id_ente"),
                        rs.getString("sigla").trim(),
                        rs.getString("nome")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[StateFilterBar] Erro ao carregar estados: " + e.getMessage());
        }

        if (lista.isEmpty()) {
            lista.add(new EstadoItem("23", "CE", "Ceará"));
            lista.add(new EstadoItem("35", "SP", "São Paulo"));
            lista.add(new EstadoItem("33", "RJ", "Rio de Janeiro"));
        }

        return lista.toArray(new EstadoItem[0]);
    }

    private void selecionarEstadoPadrao(JComboBox<EstadoItem> combo) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if ("23".equals(combo.getItemAt(i).idEnte())) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private <T> void estilizarCombo(JComboBox<T> combo) {
        combo.setBackground(Theme.BG_DARK);
        combo.setForeground(Theme.TEXT_MAIN);
        combo.setFont(FontManager.inter(13f));
        combo.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 90), 1));
        combo.setFocusable(false);
    }

    private JSeparator vDivider() {
        JSeparator s = new JSeparator(SwingConstants.VERTICAL);
        s.setForeground(new Color(40, 40, 65));
        s.setPreferredSize(new Dimension(1, 20));
        return s;
    }

    private String labelCombo(String text) {
        return text + ":";
    }

    public record EstadoItem(String idEnte, String sigla, String nome) {
        @Override
        public String toString() {
            return sigla + " — " + nome;
        }
    }
}