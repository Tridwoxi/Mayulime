package infra.io;

import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import think.domain.repr.Display;

final class MetricsDisplay extends GridPane {

    private static final int ROW_COUNT = 6;
    private final Text[] values;

    MetricsDisplay() {
        this.values = new Text[ROW_COUNT];
        this.setHgap(12.0);
        this.setVgap(4.0);

        this.addRow(0, "Grid");
        this.addRow(1, "Walls");
        this.addRow(2, "Updates");
        this.addRow(3, "Since update");
        this.addRow(4, "Total elapsed");
        this.addRow(5, "Zoom");
    }

    public void render(
        final Display display,
        final int rows,
        final int cols,
        final int wallBudget,
        final int updateCount,
        final double zoom,
        final String sinceUpdate,
        final String elapsed
    ) {
        final int spentWalls = display == null ? 0 : display.getSpentWallsCount();
        final boolean puzzleKnown = rows > 0 && cols > 0;
        final String walls = puzzleKnown ? GuiMath.walls(spentWalls, wallBudget) : "-";
        final String updates = puzzleKnown ? String.valueOf(updateCount) : "-";

        this.values[0].setText(GuiMath.grid(rows, cols));
        this.values[1].setText(walls);
        this.values[2].setText(updates);
        this.values[3].setText(sinceUpdate);
        this.values[4].setText(elapsed);
        this.values[5].setText(GuiMath.zoomPercent(zoom));
    }

    private void addRow(final int rowIndex, final String labelText) {
        final Text label = new Text(labelText + ":");
        label.setFill(PatheryColors.FOREGROUND);
        label.setOpacity(0.75);
        label.setFont(Font.font(Gui.FONT_NAME, 12.0));

        final Text value = new Text("-");
        value.setFill(PatheryColors.FOREGROUND);
        value.setFont(Font.font(Gui.FONT_NAME, 12.0));

        this.values[rowIndex] = value;
        this.add(label, 0, rowIndex);
        this.add(value, 1, rowIndex);
    }
}
