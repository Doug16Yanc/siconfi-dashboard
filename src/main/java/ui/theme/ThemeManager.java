package ui.theme;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;

public class ThemeManager {

    public static void initialize() {
        try {
            FontManager.load();

            FlatDarkLaf.setup();

            configureFonts();
            configureComponentArcs();
            configureButtonStyles();
            configureComboBoxAndLists();

        } catch (Exception e) {
            System.err.println("Erro ao inicializar o tema visual: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void configureFonts() {
        UIManager.put("defaultFont",       FontManager.inter(13f));
        UIManager.put("Label.font",         FontManager.inter(16f));
        UIManager.put("Button.font",        FontManager.interMedium(16f));
        UIManager.put("ComboBox.font",      FontManager.inter(16f));
        UIManager.put("Table.font",         FontManager.inter(15f));
        UIManager.put("TableHeader.font",   FontManager.interBold(15f));
        UIManager.put("TitledBorder.font",  FontManager.interBold(15f));
    }

    private static void configureComponentArcs() {
        UIManager.put("Component.arc",     14);
        UIManager.put("Button.arc",        14);
        UIManager.put("TextComponent.arc", 14);
        UIManager.put("ComboBox.arc",      14);
    }

    private static void configureButtonStyles() {
        UIManager.put("Panel.background",        new Color(18, 18, 28));
        UIManager.put("Button.innerFocusWidth",  0);
        UIManager.put("Button.focusWidth",       0);
        UIManager.put("Button.borderWidth",      0);
        UIManager.put("Button.margin",           new Insets(6, 14, 6, 14));
    }

    private static void configureComboBoxAndLists() {
        UIManager.put("ComboBox.background",          new Color(28, 28, 48));
        UIManager.put("ComboBox.foreground",          new Color(230, 230, 255));
        UIManager.put("ComboBox.selectionBackground", new Color(0, 100, 220));
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("ComboBox.buttonBackground",    new Color(28, 28, 48));
        UIManager.put("ComboBox.borderColor",         new Color(50, 50, 80));

        UIManager.put("PopupMenu.borderCornerRadius", 12);
        UIManager.put("PopupMenu.border",             BorderFactory.createEmptyBorder(4, 4, 4, 4));
        UIManager.put("ComboBox.popupBackground",     new Color(22, 22, 40));

        UIManager.put("List.background",              new Color(22, 22, 40));
        UIManager.put("List.foreground",              new Color(230, 230, 255));
        UIManager.put("List.selectionBackground",     new Color(0, 100, 220, 180));
        UIManager.put("List.selectionForeground",     Color.WHITE);
    }
}