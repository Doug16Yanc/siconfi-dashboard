import db.DatabaseConfig;
import service.DashboardService;
import ui.MainFrame;
import ui.theme.ThemeManager;

import javax.swing.*;

void main() {
    DatabaseConfig.init();

    Runtime.getRuntime().addShutdownHook(
            Thread.ofVirtual().unstarted(DatabaseConfig::close)
    );

    SwingUtilities.invokeLater(() -> {
        ThemeManager.initialize();

        MainFrame frame = new MainFrame();
        frame.setVisible(true);

        Thread.ofVirtual().start(() -> {
            try {
                DashboardService service = new DashboardService();
                DashboardService.DashboardData data = service.carregarTudo();

                SwingUtilities.invokeLater(() -> {
                    frame.atualizarIndicadores(data);
                });

            } catch (Exception e) {
                System.err.println("[Main] Falha ao carregar dados iniciais: " + e.getMessage());

                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame,
                            "Erro ao carregar dados econômicos. O sistema tentará usar o cache local.",
                            "Aviso de Conexão",
                            JOptionPane.WARNING_MESSAGE);
                });
            }
        });
    });
}