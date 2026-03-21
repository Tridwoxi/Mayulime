package infra.gui;

import javafx.application.ColorScheme;
import javafx.scene.paint.Color;

/**
    Supposedly colorblind-friendly.
 */
final class UiPalette {

    private static final UiPalette LIGHT = new UiPalette(
        Color.web("dddddd"),
        Color.web("eeeeee"),
        Color.web("cccccc"),
        Color.web("999999"),
        Color.web("111111"),
        Color.web("777777"),
        Color.web("333333"),
        Color.web("eeeeee"),
        Color.web("0077bb"),
        Color.web("ee7733"),
        Color.web("009988"),
        Color.web("00000022")
    );

    private static final UiPalette DARK = new UiPalette(
        Color.web("111111"),
        Color.web("222222"),
        Color.web("333333"),
        Color.web("555555"),
        Color.web("eeeeee"),
        Color.web("999999"),
        Color.web("cccccc"),
        Color.web("222222"),
        Color.web("0088cc"),
        Color.web("ee8844"),
        Color.web("00aa88"),
        Color.web("00000066")
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
