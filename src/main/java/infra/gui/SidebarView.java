package infra.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    private final VBox detailsGroup;
    private final VBox actionsGroup;
    private final VBox legendGroup;

    private final GuiMetrics metrics;

    SidebarView() {
        this.setSpacing(PANEL_SPACING_PX);
        this.setPadding(new Insets(0.0));

        this.titleText = new Text();
        this.statusText = new Text();
        this.stopOrRestartButton = new Button("Stop");
        this.detailsGroup = this.panelCard();
        this.actionsGroup = this.panelCard();
        this.legendGroup = this.panelCard();
        this.metrics = new GuiMetrics();

        this.configureHeader();
        this.configureButtons();
        this.configurePanels();
    }

    public void onStopOrRestart(final Runnable listener) {
        this.stopOrRestartButton.setOnAction(event -> listener.run());
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
        this.titleText.setFill(GuiPalette.FOREGROUND);
        this.titleText.setFont(Font.font(Gui.FONT_NAME, 29.0));

        this.statusText.setFill(GuiPalette.FOREGROUND);
        this.statusText.setOpacity(0.8);
        this.statusText.setWrappingWidth(360.0);
        this.statusText.setFont(Font.font(Gui.FONT_NAME, 13.0));

        final VBox header = this.panelCard();
        header.getChildren().addAll(this.titleText, this.statusText);
        this.getChildren().add(header);
    }

    private void configureButtons() {
        stylePrimaryButton(this.stopOrRestartButton, GuiPalette.PRIMARY, Color.web("#111827"));

        final HBox row = new HBox();
        row.setSpacing(PANEL_SPACING_PX);
        row.setAlignment(Pos.CENTER_LEFT);
        this.stopOrRestartButton.setMaxWidth(Double.MAX_VALUE);
        row.getChildren().add(this.stopOrRestartButton);

        this.getChildren().add(row);
    }

    private void configurePanels() {
        this.detailsGroup.getChildren().addAll(this.sectionTitle("Statistics"), this.metrics);
        this.actionsGroup.getChildren().addAll(this.sectionTitle("Commands"), this.commandsText());
        this.legendGroup.getChildren().addAll(this.sectionTitle("Legend"), this.legendRows());

        this.getChildren().addAll(this.detailsGroup, this.actionsGroup, this.legendGroup);
    }

    private VBox commandsText() {
        final VBox box = new VBox();
        box.setSpacing(8.0);
        box
            .getChildren()
            .addAll(
                this.commandRow("Ctrl/Cmd+O", "Open mapcode"),
                this.commandRow("Ctrl/Cmd+V", "Paste mapcode"),
                this.commandRow("Drag", "Pan the board")
            );
        return box;
    }

    private HBox commandRow(final String key, final String action) {
        final Label keycap = new Label(key);
        keycap.setTextFill(GuiPalette.FOREGROUND);
        keycap.setFont(Font.font("Monospaced", 12.0));
        keycap.setPadding(new Insets(4.0, 8.0, 4.0, 8.0));
        keycap.setBackground(
            new Background(
                new BackgroundFill(GuiPalette.SURFACE_VARIANT, new CornerRadii(8.0), Insets.EMPTY)
            )
        );

        final Text label = new Text(action);
        label.setFill(GuiPalette.FOREGROUND);
        label.setOpacity(0.85);
        label.setFont(Font.font(Gui.FONT_NAME, 13.0));

        final HBox row = new HBox();
        row.setSpacing(10.0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(keycap, label);
        return row;
    }

    private VBox legendRows() {
        final VBox box = new VBox();
        box.setSpacing(8.0);
        box
            .getChildren()
            .addAll(
                this.legendRow(GuiPalette.EMPTY, "Empty"),
                this.legendRow(GuiPalette.CHECKPOINT, "Checkpoint"),
                this.legendRow(GuiPalette.SYSTEM_WALL, "System wall"),
                this.legendRow(GuiPalette.PLAYER_WALL, "Player wall")
            );
        return box;
    }

    private HBox legendRow(final Color color, final String label) {
        final Rectangle swatch = new Rectangle(12.0, 12.0);
        swatch.setFill(color);
        swatch.setStroke(GuiPalette.OUTLINE);

        final Text text = new Text(label);
        text.setFill(GuiPalette.FOREGROUND);
        text.setFont(Font.font(Gui.FONT_NAME, 13.0));

        final HBox row = new HBox();
        row.setSpacing(10.0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(swatch, text);
        return row;
    }

    private Text sectionTitle(final String text) {
        final Text title = new Text(text);
        title.setFill(GuiPalette.FOREGROUND);
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
                new BackgroundFill(GuiPalette.SURFACE, new CornerRadii(16.0), Insets.EMPTY)
            )
        );
        card.setBorder(
            new Border(
                new BorderStroke(
                    GuiPalette.OUTLINE,
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
