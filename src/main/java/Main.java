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

        DashboardService.DashboardData data = new DashboardService().carregarTudo();
        frame.atualizarIndicadores(data);
    });
}