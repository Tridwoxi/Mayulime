package infra.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

@SuppressWarnings("checkstyle:ClassDataAbstractionCoupling")
final class SidebarView extends VBox {

    private static final double PANEL_SPACING_PX = 12.0;
    private static final double PANEL_PADDING_PX = 16.0;

    private UiPalette palette;
    private final Text titleText;
    private final Text statusText;
    private final Text[] sectionTitles;
    private final Text[] legendTexts;
    private final Rectangle[] legendSwatches;

    private final Button stopOrRestartButton;
    private final Button uploadMapCodeButton;
    private final Button pasteMapCodeButton;
    private final Button copyMapCodeButton;

    private final VBox headerGroup;
    private final VBox detailsGroup;
    private final VBox legendGroup;

    private final MetricsView metrics;
    private UiState state;

    SidebarView(final UiPalette initialPalette) {
        this.palette = initialPalette;
        this.setSpacing(PANEL_SPACING_PX);
        this.setPadding(new Insets(0.0));

        this.titleText = new Text();
        this.statusText = new Text();
        this.sectionTitles = new Text[2];
        this.legendTexts = new Text[4];
        this.legendSwatches = new Rectangle[4];
        this.stopOrRestartButton = new Button("Stop");
        this.uploadMapCodeButton = new Button("Upload MapCode");
        this.pasteMapCodeButton = new Button("Paste MapCode");
        this.copyMapCodeButton = new Button("Copy MapCode");
        this.headerGroup = this.panelCard();
        this.detailsGroup = this.panelCard();
        this.legendGroup = this.panelCard();
        this.metrics = new MetricsView();
        this.state = UiState.initial();

        this.configureHeader();
        this.configureButtons();
        this.configurePanels();
        this.applyPalette(initialPalette);
    }

    public void onStopOrRestart(final Runnable listener) {
        this.stopOrRestartButton.setOnAction(event -> listener.run());
    }

    public void onUploadMapCode(final Runnable listener) {
        this.uploadMapCodeButton.setOnAction(event -> listener.run());
    }

    public void onPasteMapCode(final Runnable listener) {
        this.pasteMapCodeButton.setOnAction(event -> listener.run());
    }

    public void onCopyMapCode(final Runnable listener) {
        this.copyMapCodeButton.setOnAction(event -> listener.run());
    }

    public void render(final UiState state, final String sinceUpdate, final String elapsed) {
        this.state = state;
        this.titleText.setText(state.puzzleName());
        this.statusText.setText(state.statusMessage());
        this.renderStopOrRestartButton(state);

        this.metrics.render(state, sinceUpdate, elapsed);
    }

    public void applyPalette(final UiPalette paletteToApply) {
        this.palette = paletteToApply;
        this.titleText.setFill(this.palette.foreground());
        this.statusText.setFill(this.palette.foreground());
        for (final Text sectionTitle : this.sectionTitles) {
            sectionTitle.setFill(this.palette.mutedForeground());
        }
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
        this.applyCardStyle(this.detailsGroup);
        this.applyCardStyle(this.legendGroup);
        this.metrics.applyPalette(this.palette);
        this.renderStopOrRestartButton(this.state);
        styleActionButton(this.uploadMapCodeButton, this.palette);
        styleActionButton(this.pasteMapCodeButton, this.palette);
        styleActionButton(this.copyMapCodeButton, this.palette);
    }

    private void configureHeader() {
        this.titleText.setFont(Font.font(Gui.FONT_NAME, FontWeight.SEMI_BOLD, 27.0));

        this.statusText.setWrappingWidth(360.0);
        this.statusText.setFont(Font.font(Gui.FONT_NAME, 13.0));

        this.headerGroup.getChildren().addAll(this.titleText, this.statusText);
        this.getChildren().add(this.headerGroup);
    }

    private void configureButtons() {
        styleActionButton(this.stopOrRestartButton, this.palette);
        styleActionButton(this.uploadMapCodeButton, this.palette);
        styleActionButton(this.pasteMapCodeButton, this.palette);
        styleActionButton(this.copyMapCodeButton, this.palette);

        final VBox row = new VBox();
        row.setSpacing(PANEL_SPACING_PX);
        row.setAlignment(Pos.CENTER_LEFT);
        this.stopOrRestartButton.setMaxWidth(Double.MAX_VALUE);
        this.uploadMapCodeButton.setMaxWidth(Double.MAX_VALUE);
        this.pasteMapCodeButton.setMaxWidth(Double.MAX_VALUE);
        this.copyMapCodeButton.setMaxWidth(Double.MAX_VALUE);
        row
            .getChildren()
            .addAll(
                this.stopOrRestartButton,
                this.uploadMapCodeButton,
                this.pasteMapCodeButton,
                this.copyMapCodeButton
            );

        this.getChildren().add(row);
    }

    private void configurePanels() {
        this.sectionTitles[0] = this.sectionTitle("Statistics");
        this.sectionTitles[1] = this.sectionTitle("Legend");

        this.detailsGroup.getChildren().addAll(this.sectionTitles[0], this.metrics);
        this.legendGroup.getChildren().addAll(this.sectionTitles[1], this.legendRows());

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

    private Text sectionTitle(final String text) {
        final Text title = new Text(text);
        title.setFont(Font.font(Gui.FONT_NAME, 12.0));
        return title;
    }

    private void renderStopOrRestartButton(final UiState state) {
        this.stopOrRestartButton.setText(state.phase() == UiPhase.SOLVING ? "Stop" : "Restart");
        this.copyMapCodeButton.setDisable(!state.canCopyMapCode());

        final boolean disabled = state.phase() != UiPhase.SOLVING && !state.canRestart();
        this.stopOrRestartButton.setDisable(disabled);
        if (disabled) {
            applyButtonStyle(
                this.stopOrRestartButton,
                this.palette,
                this.palette.surface(),
                this.palette.mutedForeground()
            );
            return;
        }
        applyButtonStyle(
            this.stopOrRestartButton,
            this.palette,
            this.palette.surfaceVariant(),
            this.palette.foreground()
        );
    }

    private VBox panelCard() {
        final VBox card = new VBox();
        card.setSpacing(PANEL_SPACING_PX);
        card.setPadding(new Insets(PANEL_PADDING_PX));
        return card;
    }

    private void applyCardStyle(final VBox card) {
        card.setBackground(
            new Background(
                new BackgroundFill(this.palette.surface(), CornerRadii.EMPTY, Insets.EMPTY)
            )
        );
        card.setBorder(
            new Border(
                new BorderStroke(
                    this.palette.outline(),
                    BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY,
                    BorderWidths.DEFAULT
                )
            )
        );
    }

    private static void styleActionButton(final Button button, final UiPalette palette) {
        applyButtonStyle(button, palette, palette.surfaceVariant(), palette.foreground());
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
        button.setBackground(
            new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY))
        );
        button.setBorder(
            new Border(
                new BorderStroke(
                    palette.outline(),
                    BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY,
                    BorderWidths.DEFAULT
                )
            )
        );
    }
}
