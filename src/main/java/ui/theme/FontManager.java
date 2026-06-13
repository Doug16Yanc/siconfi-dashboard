package ui.theme;

import java.awt.*;
import java.io.InputStream;

public class FontManager {

    public static Font INTER_REGULAR;
    public static Font INTER_MEDIUM;
    public static Font INTER_BOLD;
    public static Font JETBRAINS_REGULAR;
    public static Font JETBRAINS_BOLD;

    public static void load() {
        INTER_REGULAR    = load("/fonts/Inter-Regular.ttf");
        INTER_MEDIUM     = load("/fonts/Inter-Medium.ttf");
        INTER_BOLD       = load("/fonts/Inter-Bold.ttf");
        JETBRAINS_REGULAR= load("/fonts/JetBrainsMono-Regular.ttf");
        JETBRAINS_BOLD   = load("/fonts/JetBrainsMono-Bold.ttf");

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(INTER_REGULAR);
        ge.registerFont(INTER_MEDIUM);
        ge.registerFont(INTER_BOLD);
        ge.registerFont(JETBRAINS_REGULAR);
        ge.registerFont(JETBRAINS_BOLD);
    }

    private static Font load(String path) {
        try (InputStream is = FontManager.class.getResourceAsStream(path)) {
            return Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (Exception e) {
            System.err.println("Fonte não encontrada: " + path);
            return new Font("Segoe UI", Font.PLAIN, 13);
        }
    }

    public static Font inter(float size)          { return INTER_REGULAR.deriveFont(size); }
    public static Font interMedium(float size)    { return INTER_MEDIUM.deriveFont(size); }
    public static Font interBold(float size)      { return INTER_BOLD.deriveFont(size); }
    public static Font jetbrains(float size)      { return JETBRAINS_REGULAR.deriveFont(size); }
    public static Font jetbrainsBold(float size)  { return JETBRAINS_BOLD.deriveFont(size); }
}