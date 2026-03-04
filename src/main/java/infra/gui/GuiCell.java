package infra.gui;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

final class GuiCell extends Group {

    private static final double HALF = 0.5;
    private static final double BASE_LABEL_SCALE = 0.32;
    private static final double MIN_FONT_PX = 7.0;

    GuiCell(final Color color, final String content, final double sizePx) {
        final Rectangle body = new Rectangle(sizePx, sizePx);
        body.setFill(color);
        body.setStroke(GuiPalette.BACKGROUND.deriveColor(0.0, 1.0, 1.0, 0.7));

        final String labelText = GuiMath.cellLabel(content, sizePx);
        final Text label = new Text(labelText);
        label.setFill(GuiMath.cellLabelColor(color));
        final double fontSize = Math.max(MIN_FONT_PX, sizePx * BASE_LABEL_SCALE);
        label.setFont(Font.font(Gui.FONT_NAME, fontSize));

        final var bounds = label.getLayoutBounds();
        label.setX((sizePx - bounds.getWidth()) * HALF - bounds.getMinX());
        label.setY((sizePx - bounds.getHeight()) * HALF - bounds.getMinY());

        this.getChildren().addAll(body, label);
    }
}
