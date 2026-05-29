import com.formdev.flatlaf.FlatDarkLaf;
import db.DatabaseConfig;
import service.DashboardService;
import ui.FontManager;
import ui.MainFrame;

import javax.swing.*;
import java.awt.*;

void main() {

    DatabaseConfig.init();

    Runtime.getRuntime().addShutdownHook(
            Thread.ofVirtual().unstarted(DatabaseConfig::close)
    );

    SwingUtilities.invokeLater(() -> {
        try {
            FontManager.load();

            FlatDarkLaf.setup();

            Font inter13 = FontManager.inter(13f);
            UIManager.put("defaultFont", inter13);

            UIManager.put("Label.font",     FontManager.inter(16f));
            UIManager.put("Button.font",    FontManager.interMedium(16f));
            UIManager.put("ComboBox.font",  FontManager.inter(16f));
            UIManager.put("Table.font",     FontManager.inter(15f));
            UIManager.put("TableHeader.font", FontManager.interBold(15f));
            UIManager.put("TitledBorder.font", FontManager.interBold(15f));

            UIManager.put("Component.arc", 8);
            UIManager.put("Button.arc",    8);
            UIManager.put("Panel.background", new Color(18, 18, 28));

            UIManager.put("Component.arc",          14);
            UIManager.put("Button.arc",             14);
            UIManager.put("TextComponent.arc",      14);
            UIManager.put("ComboBox.arc",           14);

            UIManager.put("Button.innerFocusWidth",  0);
            UIManager.put("Button.focusWidth",       0);
            UIManager.put("Button.borderWidth",      0);
            UIManager.put("Button.margin",           new Insets(6, 14, 6, 14));

            UIManager.put("ComboBox.background",          new Color(28, 28, 48));
            UIManager.put("ComboBox.foreground",          new Color(230, 230, 255));
            UIManager.put("ComboBox.selectionBackground", new Color(0, 100, 220));
            UIManager.put("ComboBox.selectionForeground", Color.WHITE);
            UIManager.put("ComboBox.buttonBackground",    new Color(28, 28, 48));
            UIManager.put("ComboBox.borderColor",         new Color(50, 50, 80));
            UIManager.put("PopupMenu.borderCornerRadius", 12);

            UIManager.put("PopupMenu.border", BorderFactory.createEmptyBorder(4, 4, 4, 4));
            UIManager.put("ComboBox.popupBackground",     new Color(22, 22, 40));
            UIManager.put("List.background",              new Color(22, 22, 40));
            UIManager.put("List.foreground",              new Color(230, 230, 255));
            UIManager.put("List.selectionBackground",     new Color(0, 100, 220, 180));
            UIManager.put("List.selectionForeground",     Color.WHITE);

        } catch (Exception e) {
            e.printStackTrace();
        }

        MainFrame frame = new MainFrame();
        DashboardService.DashboardData data = new DashboardService().carregarTudo();
        frame.atualizarIndicadores(data);
    });
}
