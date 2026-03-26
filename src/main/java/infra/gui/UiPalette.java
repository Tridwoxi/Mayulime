package infra.gui;

import javafx.application.ColorScheme;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

/**
    Supposedly colorblind-friendly.
 */
record UiPalette(
    Color background,
    Color surface,
    Color surfaceVariant,
    Color outline,
    Color foreground,
    Color mutedForeground,
    Color empty,
    Color systemWall,
    Color playerWall,
    Color checkpoint,
    Color shadow
) {
    private static final UiPalette LIGHT = new UiPalette(
        Color.web("dddddd"),
        Color.web("eeeeee"),
        Color.web("cccccc"),
        Color.web("999999"),
        Color.web("111111"),
        Color.web("777777"),
        Color.web("eeeeee"),
        Color.web("0077bb"),
        Color.web("ee7733"),
        Color.web("444444"),
        Color.web("00000022")
    );

    private static final UiPalette DARK = new UiPalette(
        Color.web("111111"),
        Color.web("222222"),
        Color.web("333333"),
        Color.web("555555"),
        Color.web("eeeeee"),
        Color.web("999999"),
        Color.web("222222"),
        Color.web("0088cc"),
        Color.web("ee8844"),
        Color.web("dddddd"),
        Color.web("00000066")
    );

    static UiPalette fromColorScheme(final ColorScheme colorScheme) {
        if (colorScheme == ColorScheme.DARK) {
            return DARK;
        }
        return LIGHT;
    }

    static Background fill(final Color color) {
        return new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
    }

    static Border stroke(final Color color) {
        return new Border(
            new BorderStroke(
                color,
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                BorderWidths.DEFAULT
            )
        );
    }
}
