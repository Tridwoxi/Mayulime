package infra.io;

import javafx.scene.paint.Color;

final class GuiPalette {

    // Mystery unrelated colors.
    public static final Color SURFACE = Color.web("1a1f28");
    public static final Color SURFACE_VARIANT = Color.web("232a35");
    public static final Color OUTLINE = Color.web("495366");
    public static final Color FOREGROUND = Color.web("e6e8ee");
    public static final Color PRIMARY = Color.web("8ab4f8");

    // Approximate average color, picked from Pathery's default color theme.
    public static final Color BACKGROUND = Color.web("10131a");
    public static final Color SYSTEM_WALL = Color.web("723736");
    public static final Color PLAYER_WALL = Color.web("3c3d3c");
    public static final Color EMPTY = Color.web("e2e8eb");
    public static final Color CHECKPOINT = Color.web("8e4793");

    private GuiPalette() {}
}
