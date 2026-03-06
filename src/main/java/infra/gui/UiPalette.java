package infra.gui;

import javafx.application.ColorScheme;
import javafx.scene.paint.Color;

final class UiPalette {

    private static final UiPalette LIGHT = new UiPalette(
        Color.web("e8dece"),
        Color.web("eee3d3"),
        Color.web("dacbb5"),
        Color.web("bba88d"),
        Color.web("274463"),
        Color.web("7f725f"),
        Color.web("bc5b3a"),
        Color.web("e1d4c0"),
        Color.web("274463"),
        Color.web("bc5b3a"),
        Color.web("6d8d4e"),
        Color.color(0.17, 0.29, 0.39, 0.12)
    );

    private static final UiPalette DARK = new UiPalette(
        Color.web("181412"),
        Color.web("241d19"),
        Color.web("332822"),
        Color.web("5f5145"),
        Color.web("f0e2cf"),
        Color.web("b7a690"),
        Color.web("c97a58"),
        Color.web("3a312b"),
        Color.web("6a86a4"),
        Color.web("c97a58"),
        Color.web("738a58"),
        Color.color(0.0, 0.0, 0.0, 0.38)
    );

    private final Color background;
    private final Color surface;
    private final Color surfaceVariant;
    private final Color outline;
    private final Color foreground;
    private final Color mutedForeground;
    private final Color primary;
    private final Color empty;
    private final Color systemWall;
    private final Color playerWall;
    private final Color checkpoint;
    private final Color shadow;

    private UiPalette(
        final Color background,
        final Color surface,
        final Color surfaceVariant,
        final Color outline,
        final Color foreground,
        final Color mutedForeground,
        final Color primary,
        final Color empty,
        final Color systemWall,
        final Color playerWall,
        final Color checkpoint,
        final Color shadow
    ) {
        this.background = background;
        this.surface = surface;
        this.surfaceVariant = surfaceVariant;
        this.outline = outline;
        this.foreground = foreground;
        this.mutedForeground = mutedForeground;
        this.primary = primary;
        this.empty = empty;
        this.systemWall = systemWall;
        this.playerWall = playerWall;
        this.checkpoint = checkpoint;
        this.shadow = shadow;
    }

    static UiPalette fromColorScheme(final ColorScheme colorScheme) {
        if (colorScheme == ColorScheme.DARK) {
            return DARK;
        }
        return LIGHT;
    }

    Color background() {
        return this.background;
    }

    Color surface() {
        return this.surface;
    }

    Color surfaceVariant() {
        return this.surfaceVariant;
    }

    Color outline() {
        return this.outline;
    }

    Color foreground() {
        return this.foreground;
    }

    Color mutedForeground() {
        return this.mutedForeground;
    }

    Color primary() {
        return this.primary;
    }

    Color empty() {
        return this.empty;
    }

    Color systemWall() {
        return this.systemWall;
    }

    Color playerWall() {
        return this.playerWall;
    }

    Color checkpoint() {
        return this.checkpoint;
    }

    Color shadow() {
        return this.shadow;
    }
}
