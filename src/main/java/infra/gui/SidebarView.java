package infra.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import think.solvers.SolverKind;

final class SidebarView extends VBox {

    private static final double PANEL_SPACING_PX = 12.0;
    private static final double PANEL_PADDING_PX = 16.0;
    private static final int DEFAULT_THREAD_COUNT = 10;
    private static final int MIN_THREADS = 1;
    private static final int MAX_THREADS = 64;

    private UiPalette palette;
    private final Text titleText;
    private final Text statusText;
    private final Text solverLabel;
    private final Text threadLabel;
    private final Text[] legendTexts;
    private final Rectangle[] legendSwatches;

    private final Button stopOrRestartButton;
    private final Button uploadMapCodeButton;
    private final Button pasteMapCodeButton;
    private final Button copyMapCodeButton;

    private final ChoiceBox<SolverKind> solverChoice;
    private final Spinner<Integer> threadSpinner;

    private final VBox headerGroup;
    private final VBox settingsGroup;
    private final VBox detailsGroup;
    private final VBox legendGroup;

    private final MetricsView metrics;
    private UiState state;

    SidebarView(final UiPalette initialPalette) {
        this.palette = initialPalette;
        this.setSpacing(PANEL_SPACING_PX);
        this.setPadding(new Insets(0.0));
        this.setMaxHeight(Region.USE_PREF_SIZE);

        this.titleText = new Text();
        this.statusText = new Text();
        this.solverLabel = new Text("Solver:");
        this.solverLabel.setFont(Font.font(Gui.FONT_NAME, 12.0));
        this.threadLabel = new Text("Threads:");
        this.threadLabel.setFont(Font.font(Gui.FONT_NAME, 12.0));
        this.legendTexts = new Text[4];
        this.legendSwatches = new Rectangle[4];
        this.stopOrRestartButton = new Button("Stop");
        this.uploadMapCodeButton = new Button("Upload MapCode");
        this.pasteMapCodeButton = new Button("Paste MapCode");
        this.copyMapCodeButton = new Button("Copy MapCode");
        this.solverChoice = new ChoiceBox<>();
        this.solverChoice.getItems().addAll(SolverKind.asList());
        this.solverChoice.setValue(SolverKind.getBest());
        final SpinnerValueFactory.IntegerSpinnerValueFactory threadFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(
                MIN_THREADS,
                MAX_THREADS,
                DEFAULT_THREAD_COUNT
            );
        threadFactory.setConverter(new SafeIntConverter(DEFAULT_THREAD_COUNT));
        this.threadSpinner = new Spinner<>(threadFactory);
        this.threadSpinner.setEditable(true);
        this.headerGroup = this.panelCard();
        this.settingsGroup = this.panelCard();
        this.detailsGroup = this.panelCard();
        this.legendGroup = this.panelCard();
        this.metrics = new MetricsView();
        this.state = UiState.initial();

        this.configureHeader();
        this.configureButtons();
        this.configureSettings();
        this.configurePanels();
        this.applyPalette(initialPalette);
    }

    public void onStopOrRestart(final Runnable listener) {
        this.stopOrRestartButton.setOnAction(_ -> listener.run());
    }

    public void onUploadMapCode(final Runnable listener) {
        this.uploadMapCodeButton.setOnAction(_ -> listener.run());
    }

    public void onPasteMapCode(final Runnable listener) {
        this.pasteMapCodeButton.setOnAction(_ -> listener.run());
    }

    public void onCopyMapCode(final Runnable listener) {
        this.copyMapCodeButton.setOnAction(_ -> listener.run());
    }

    public SolverKind getSolverKind() {
        return this.solverChoice.getValue();
    }

    public int getThreadCount() {
        return this.threadSpinner.getValue();
    }

    public void render(final UiState state, final String sinceUpdate, final String elapsed) {
        this.state = state;
        this.titleText.setText(state.puzzleName());
        this.statusText.setText(state.statusMessage());
        this.renderButtonStates(state);
        this.metrics.render(state, sinceUpdate, elapsed);
    }

    public void applyPalette(final UiPalette paletteToApply) {
        this.palette = paletteToApply;
        this.titleText.setFill(this.palette.foreground());
        this.statusText.setFill(this.palette.foreground());
        this.solverLabel.setFill(this.palette.mutedForeground());
        this.threadLabel.setFill(this.palette.mutedForeground());
        for (final Text legendText : this.legendTexts) {
            legendText.setFill(this.palette.foreground());
        }
        for (int index = 0; index < this.legendSwatches.length; index += 1) {
            this.legendSwatches[index].setStroke(this.palette.outline());
        }
        this.legendSwatches[0].setFill(this.palette.empty());
        this.legendSwatches[1].setFill(this.palette.checkpoint());
        this.legendSwatches[2].setFill(this.palette.systemWall());
        this.legendSwatches[3].setFill(this.palette.playerWall());

        this.applyCardStyle(this.headerGroup);
        this.applyCardStyle(this.settingsGroup);
        this.applyCardStyle(this.detailsGroup);
        this.applyCardStyle(this.legendGroup);
        this.metrics.applyPalette(this.palette);
        this.renderButtonStates(this.state);
        styleActionButton(this.uploadMapCodeButton, this.palette);
        styleActionButton(this.pasteMapCodeButton, this.palette);
        styleActionButton(this.copyMapCodeButton, this.palette);
        this.styleFormControls();
    }

    private void configureHeader() {
        this.titleText.setFont(Font.font(Gui.FONT_NAME, FontWeight.SEMI_BOLD, 27.0));

        this.statusText.setWrappingWidth(360.0);
        this.statusText.setFont(Font.font(Gui.FONT_NAME, 13.0));

        this.headerGroup.getChildren().addAll(this.titleText, this.statusText);
        this.getChildren().add(this.headerGroup);
    }

    private void configureSettings() {
        this.solverChoice.setMaxWidth(Double.MAX_VALUE);
        this.threadSpinner.setMaxWidth(Double.MAX_VALUE);

        final VBox solverRow = new VBox(4.0, this.solverLabel, this.solverChoice);
        final VBox threadRow = new VBox(4.0, this.threadLabel, this.threadSpinner);

        this.settingsGroup.getChildren().addAll(solverRow, threadRow);
        this.getChildren().add(this.settingsGroup);
    }

    private void configureButtons() {
        final VBox column = new VBox();
        column.setSpacing(PANEL_SPACING_PX);
        column.setAlignment(Pos.CENTER_LEFT);
        this.stopOrRestartButton.setMaxWidth(Double.MAX_VALUE);
        this.uploadMapCodeButton.setMaxWidth(Double.MAX_VALUE);
        this.pasteMapCodeButton.setMaxWidth(Double.MAX_VALUE);
        this.copyMapCodeButton.setMaxWidth(Double.MAX_VALUE);
        column
            .getChildren()
            .addAll(
                this.stopOrRestartButton,
                this.uploadMapCodeButton,
                this.pasteMapCodeButton,
                this.copyMapCodeButton
            );
        this.getChildren().add(column);
    }

    private void configurePanels() {
        this.detailsGroup.getChildren().add(this.metrics);
        this.legendGroup.getChildren().add(this.legendRows());
        this.getChildren().addAll(this.detailsGroup, this.legendGroup);
    }

    private VBox legendRows() {
        final VBox box = new VBox();
        box.setSpacing(8.0);
        box
            .getChildren()
            .addAll(
                this.legendRow(0, "Empty"),
                this.legendRow(1, "Checkpoint"),
                this.legendRow(2, "System wall"),
                this.legendRow(3, "Player wall")
            );
        return box;
    }

    private HBox legendRow(final int index, final String label) {
        final Rectangle swatch = new Rectangle(12.0, 12.0);
        this.legendSwatches[index] = swatch;

        final Text text = new Text(label);
        text.setFont(Font.font(Gui.FONT_NAME, 13.0));
        this.legendTexts[index] = text;

        final HBox row = new HBox();
        row.setSpacing(10.0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(swatch, text);
        return row;
    }

    private void renderButtonStates(final UiState state) {
        this.stopOrRestartButton.setText(state.phase() == UiPhase.SOLVING ? "Stop" : "Restart");
        this.renderActionButtonState(this.copyMapCodeButton, !state.canCopyMapCode());

        final boolean disabled = state.phase() != UiPhase.SOLVING && !state.canRestart();
        this.renderActionButtonState(this.stopOrRestartButton, disabled);
    }

    private void styleFormControls() {
        final String bg = toHex(this.palette.surfaceVariant());
        final String border = toHex(this.palette.outline());
        final String fg = toHex(this.palette.foreground());

        this.solverChoice.setStyle(
            String.format(
                "-fx-background-color: %s; -fx-border-color: %s; " +
                    "-fx-background-radius: 0; -fx-background-insets: 0; " +
                    "-fx-mark-color: %s; -fx-text-base-color: %s; " +
                    "-fx-control-inner-background: %s; " +
                    "-fx-font-size: 13; -fx-font-family: '%s';",
                bg,
                border,
                fg,
                fg,
                bg,
                Gui.FONT_NAME
            )
        );
        this.threadSpinner.setStyle(
            String.format(
                "-fx-background-color: %s; -fx-background-radius: 0; " +
                    "-fx-background-insets: 0; " +
                    "-fx-color: %s; -fx-body-color: %s; " +
                    "-fx-outer-border: transparent; -fx-inner-border: transparent; " +
                    "-fx-shadow-highlight-color: transparent; " +
                    "-fx-mark-color: %s; -fx-text-base-color: %s; " +
                    "-fx-border-color: %s; " +
                    "-fx-font-size: 13; -fx-font-family: '%s';",
                bg,
                bg,
                bg,
                fg,
                fg,
                border,
                Gui.FONT_NAME
            )
        );
        this.threadSpinner.getEditor().setStyle(
            String.format(
                "-fx-background-color: %s; -fx-background-radius: 0; " +
                    "-fx-text-fill: %s; " +
                    "-fx-font-size: 13; -fx-font-family: '%s';",
                bg,
                fg,
                Gui.FONT_NAME
            )
        );
    }

    private VBox panelCard() {
        final VBox card = new VBox();
        card.setSpacing(PANEL_SPACING_PX);
        card.setPadding(new Insets(PANEL_PADDING_PX));
        return card;
    }

    private void applyCardStyle(final VBox card) {
        card.setBackground(UiPalette.fill(this.palette.surface()));
        card.setBorder(UiPalette.stroke(this.palette.outline()));
    }

    private static void styleActionButton(final Button button, final UiPalette palette) {
        applyButtonStyle(button, palette, palette.surfaceVariant(), palette.foreground());
    }

    private void renderActionButtonState(final Button button, final boolean disabled) {
        button.setDisable(disabled);
        if (disabled) {
            applyButtonStyle(
                button,
                this.palette,
                this.palette.surface(),
                this.palette.mutedForeground()
            );
            return;
        }
        styleActionButton(button, this.palette);
    }

    private static void applyButtonStyle(
        final Button button,
        final UiPalette palette,
        final Color backgroundColor,
        final Color textColor
    ) {
        button.setTextFill(textColor);
        button.setFont(Font.font(Gui.FONT_NAME, 13.0));
        button.setPadding(new Insets(9.0, 18.0, 9.0, 18.0));
        button.setBackground(UiPalette.fill(backgroundColor));
        button.setBorder(UiPalette.stroke(palette.outline()));
    }

    private static String toHex(final Color color) {
        return String.format(
            "#%02x%02x%02x",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255)
        );
    }

    private static final class SafeIntConverter extends StringConverter<Integer> {

        private final int fallback;

        SafeIntConverter(final int fallback) {
            this.fallback = fallback;
        }

        @Override
        public String toString(final Integer value) {
            return value == null ? String.valueOf(fallback) : value.toString();
        }

        @Override
        public Integer fromString(final String text) {
            try {
                return Integer.parseInt(text.strip());
            } catch (NumberFormatException _) {
                return fallback;
            }
        }
    }
}
