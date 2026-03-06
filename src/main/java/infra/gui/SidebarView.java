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
import javafx.scene.text.Text;
import think.manager.StatusUpdate;

@SuppressWarnings("checkstyle:ClassDataAbstractionCoupling")
final class SidebarView extends VBox {

    private static final double PANEL_SPACING_PX = 12.0;
    private static final double PANEL_PADDING_PX = 16.0;
    private static final double CHIP_RADIUS_PX = 999.0;

    private final Text titleText;
    private final Text statusText;

    private final Button stopOrRestartButton;
    private final Button uploadMapCodeButton;
    private final Button pasteMapCodeButton;

    private final VBox detailsGroup;
    private final VBox legendGroup;

    private final MetricsView metrics;

    SidebarView() {
        this.setSpacing(PANEL_SPACING_PX);
        this.setPadding(new Insets(0.0));

        this.titleText = new Text();
        this.statusText = new Text();
        this.stopOrRestartButton = new Button("Stop");
        this.uploadMapCodeButton = new Button("Upload MapCode");
        this.pasteMapCodeButton = new Button("Paste MapCode");
        this.detailsGroup = this.panelCard();
        this.legendGroup = this.panelCard();
        this.metrics = new MetricsView();

        this.configureHeader();
        this.configureButtons();
        this.configurePanels();
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

    public void render(
        final UiState state,
        final String sinceUpdate,
        final String elapsed,
        final StatusUpdate display
    ) {
        this.titleText.setText(state.puzzleName());
        this.statusText.setText(state.statusMessage());

        this.stopOrRestartButton.setText(state.phase() == UiPhase.SOLVING ? "Stop" : "Restart");
        this.stopOrRestartButton.setDisable(
            state.phase() != UiPhase.SOLVING && !state.canRestart()
        );

        this.metrics.render(
            display,
            state.rows(),
            state.cols(),
            state.wallBudget(),
            state.updateCount(),
            sinceUpdate,
            elapsed
        );
    }

    private void configureHeader() {
        this.titleText.setFill(UiPalette.FOREGROUND);
        this.titleText.setFont(Font.font(Gui.FONT_NAME, 29.0));

        this.statusText.setFill(UiPalette.FOREGROUND);
        this.statusText.setOpacity(0.8);
        this.statusText.setWrappingWidth(360.0);
        this.statusText.setFont(Font.font(Gui.FONT_NAME, 13.0));

        final VBox header = this.panelCard();
        header.getChildren().addAll(this.titleText, this.statusText);
        this.getChildren().add(header);
    }

    private void configureButtons() {
        stylePrimaryButton(this.stopOrRestartButton, UiPalette.PRIMARY, Color.web("#111827"));
        stylePrimaryButton(
            this.uploadMapCodeButton,
            UiPalette.SURFACE_VARIANT,
            UiPalette.FOREGROUND
        );
        stylePrimaryButton(
            this.pasteMapCodeButton,
            UiPalette.SURFACE_VARIANT,
            UiPalette.FOREGROUND
        );

        final VBox row = new VBox();
        row.setSpacing(PANEL_SPACING_PX);
        row.setAlignment(Pos.CENTER_LEFT);
        this.stopOrRestartButton.setMaxWidth(Double.MAX_VALUE);
        this.uploadMapCodeButton.setMaxWidth(Double.MAX_VALUE);
        this.pasteMapCodeButton.setMaxWidth(Double.MAX_VALUE);
        row
            .getChildren()
            .addAll(this.stopOrRestartButton, this.uploadMapCodeButton, this.pasteMapCodeButton);

        this.getChildren().add(row);
    }

    private void configurePanels() {
        this.detailsGroup.getChildren().addAll(this.sectionTitle("Statistics"), this.metrics);
        this.legendGroup.getChildren().addAll(this.sectionTitle("Legend"), this.legendRows());

        this.getChildren().addAll(this.detailsGroup, this.legendGroup);
    }

    private VBox legendRows() {
        final VBox box = new VBox();
        box.setSpacing(8.0);
        box
            .getChildren()
            .addAll(
                this.legendRow(UiPalette.EMPTY, "Empty"),
                this.legendRow(UiPalette.CHECKPOINT, "Checkpoint"),
                this.legendRow(UiPalette.SYSTEM_WALL, "System wall"),
                this.legendRow(UiPalette.PLAYER_WALL, "Player wall")
            );
        return box;
    }

    private HBox legendRow(final Color color, final String label) {
        final Rectangle swatch = new Rectangle(12.0, 12.0);
        swatch.setFill(color);
        swatch.setStroke(UiPalette.OUTLINE);

        final Text text = new Text(label);
        text.setFill(UiPalette.FOREGROUND);
        text.setFont(Font.font(Gui.FONT_NAME, 13.0));

        final HBox row = new HBox();
        row.setSpacing(10.0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(swatch, text);
        return row;
    }

    private Text sectionTitle(final String text) {
        final Text title = new Text(text);
        title.setFill(UiPalette.FOREGROUND);
        title.setOpacity(0.65);
        title.setFont(Font.font(Gui.FONT_NAME, 11.0));
        return title;
    }

    private VBox panelCard() {
        final VBox card = new VBox();
        card.setSpacing(PANEL_SPACING_PX);
        card.setPadding(new Insets(PANEL_PADDING_PX));
        card.setBackground(
            new Background(
                new BackgroundFill(UiPalette.SURFACE, new CornerRadii(16.0), Insets.EMPTY)
            )
        );
        card.setBorder(
            new Border(
                new BorderStroke(
                    UiPalette.OUTLINE,
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(16.0),
                    BorderWidths.DEFAULT
                )
            )
        );
        return card;
    }

    private static void stylePrimaryButton(
        final Button button,
        final Color fill,
        final Color textColor
    ) {
        button.setTextFill(textColor);
        button.setFont(Font.font(Gui.FONT_NAME, 13.0));
        button.setPadding(new Insets(9.0, 18.0, 9.0, 18.0));
        button.setBackground(
            new Background(new BackgroundFill(fill, new CornerRadii(CHIP_RADIUS_PX), Insets.EMPTY))
        );
    }
}
