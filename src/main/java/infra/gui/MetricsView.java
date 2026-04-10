package infra.gui;

import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

final class MetricsView extends GridPane {

    private static final int ROW_COUNT = 6;
    private final Text[] labels;
    private final Text[] values;

    MetricsView() {
        this.labels = new Text[ROW_COUNT];
        this.values = new Text[ROW_COUNT];
        this.setHgap(16.0);
        this.setVgap(8.0);

        this.addRow(0, "Maze");
        this.addRow(1, "Walls");
        this.addRow(2, "Best score");
        this.addRow(3, "Updates");
        this.addRow(4, "Since update");
        this.addRow(5, "Total elapsed");
    }

    public void render(final UiState state, final String sinceUpdate, final String elapsed) {
        final boolean puzzleKnown = state.rows() > 0 && state.cols() > 0;
        final String walls = puzzleKnown
            ? UiMath.walls(state.spentWalls(), state.wallBudget())
            : "-";
        final String updates = puzzleKnown ? String.valueOf(state.updateCount()) : "-";

        this.values[0].setText(UiMath.maze(state.rows(), state.cols()));
        this.values[1].setText(walls);
        this.values[2].setText(state.bestScoreText());
        this.values[3].setText(updates);
        this.values[4].setText(sinceUpdate);
        this.values[5].setText(elapsed);
    }

    public void applyPalette(final UiPalette palette) {
        for (int index = 0; index < ROW_COUNT; index += 1) {
            this.labels[index].setFill(palette.mutedForeground());
            this.values[index].setFill(palette.foreground());
        }
    }

    private void addRow(final int rowIndex, final String labelText) {
        final Text label = new Text(labelText + ":");
        label.setFont(Font.font(Gui.FONT_NAME, 12.0));

        final Text value = new Text("-");
        value.setFont(Font.font(Gui.FONT_NAME, 13.0));

        this.labels[rowIndex] = label;
        this.values[rowIndex] = value;
        this.add(label, 0, rowIndex);
        this.add(value, 1, rowIndex);
    }
}
