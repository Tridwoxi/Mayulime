package infra.gui;

import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import think.domain.model.Feature;
import think.manager.StatusUpdate;

final class MetricsView extends GridPane {

    private static final int ROW_COUNT = 5;
    private final Text[] values;

    MetricsView() {
        this.values = new Text[ROW_COUNT];
        this.setHgap(16.0);
        this.setVgap(8.0);

        this.addRow(0, "Grid");
        this.addRow(1, "Walls");
        this.addRow(2, "Updates");
        this.addRow(3, "Since update");
        this.addRow(4, "Total elapsed");
    }

    public void render(
        final StatusUpdate display,
        final int rows,
        final int cols,
        final int wallBudget,
        final int updateCount,
        final String sinceUpdate,
        final String elapsed
    ) {
        final int spentWalls = this.spentWalls(display);
        final boolean puzzleKnown = rows > 0 && cols > 0;
        final String walls = puzzleKnown ? UiMath.walls(spentWalls, wallBudget) : "-";
        final String updates = puzzleKnown ? String.valueOf(updateCount) : "-";

        this.values[0].setText(UiMath.grid(rows, cols));
        this.values[1].setText(walls);
        this.values[2].setText(updates);
        this.values[3].setText(sinceUpdate);
        this.values[4].setText(elapsed);
    }

    private int spentWalls(final StatusUpdate display) {
        if (display == null) {
            return 0;
        }
        int count = 0;
        for (int row = 0; row < display.getNumRows(); row += 1) {
            for (int col = 0; col < display.getNumCols(); col += 1) {
                if (display.getFeature(row, col) == Feature.PLAYER_WALL) {
                    count += 1;
                }
            }
        }
        return count;
    }

    private void addRow(final int rowIndex, final String labelText) {
        final Text label = new Text(labelText + ":");
        label.setFill(UiPalette.FOREGROUND);
        label.setOpacity(0.72);
        label.setFont(Font.font(Gui.FONT_NAME, 13.0));

        final Text value = new Text("-");
        value.setFill(UiPalette.FOREGROUND);
        value.setFont(Font.font(Gui.FONT_NAME, 13.0));

        this.values[rowIndex] = value;
        this.add(label, 0, rowIndex);
        this.add(value, 1, rowIndex);
    }
}
