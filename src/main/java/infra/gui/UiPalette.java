package infra.gui;

import javafx.scene.paint.Color;

final class UiPalette {

    public static final Color BACKGROUND = Color.web("0f1110");
    public static final Color SURFACE = Color.web("171a18");
    public static final Color SURFACE_VARIANT = Color.web("1d2220");
    public static final Color OUTLINE = Color.web("39403c");
    public static final Color FOREGROUND = Color.web("e4e8e5");
    public static final Color MUTED_FOREGROUND = Color.web("99a29c");
    public static final Color PRIMARY = Color.web("4fa36b");
    public static final Color BOARD_GRID = Color.web("565b57");

    // Pathery-inspired board colors, tuned to sit with the darker shell.
    public static final Color EMPTY = Color.web("dde6e2");
    public static final Color SYSTEM_WALL = Color.web("7a4943");
    public static final Color PLAYER_WALL = Color.web("4a4d49");
    public static final Color CHECKPOINT = Color.web("c09649");

    private UiPalette() {}
}
