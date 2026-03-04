package infra.gui;

import domain.model.Display;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

@SuppressWarnings({ "checkstyle:ClassDataAbstractionCoupling", "checkstyle:ClassFanOutComplexity" })
final class GuiPanels {

    interface Control {
        void adjustZoom(double requestedZoom);
        double getZoom();
        void onMapSubmissionError(String message);
        void onFilePickerCancelled();
    }

    private static final double ROOT_PADDING_PX = 28.0;
    private static final double ROOT_SPACING_PX = 20.0;
    private static final double PANEL_PADDING_PX = 18.0;
    private static final double PANEL_SPACING_PX = 12.0;
    private static final double VIEWPORT_MIN_HEIGHT_PX = 240.0;
    private static final double CARD_RADIUS_PX = 18.0;
    private static final double CHIP_RADIUS_PX = 999.0;
    private static final double KEYCAP_RADIUS_PX = 8.0;

    private final BorderPane root;
    private final Control control;
    private final Consumer<String> mapCodeConsumer;
    private final ScrollPane mapViewport;
    private final Pane mapContainer;
    private final Pane mapSurface;
    private final Text titleText = new Text();
    private final Text stateText = new Text();
    private final Text statusText;
    private final Button detailsButton;
    private final GuiMetrics metrics;
    private final VBox legend;
    private final VBox detailsGroup;
    private final VBox actionsGroup;
    private final VBox legendGroup;
    private final HBox footer;
    private boolean detailsVisible;
    private int currentRows;
    private int currentCols;
    private double currentCellSizePx;
    private boolean recenterPending;
    private double dragLastSceneX;
    private double dragLastSceneY;

    GuiPanels(
        final Parent root,
        final GuiBoard gameDisplay,
        final Consumer<String> mapCodeConsumer,
        final Control control
    ) {
        if (!(root instanceof BorderPane borderPane)) {
            throw new IllegalStateException();
        }
        this.root = borderPane;
        this.control = control;
        this.mapCodeConsumer = mapCodeConsumer;
        this.mapSurface = new Pane(gameDisplay);
        this.mapContainer = new Pane(this.mapSurface);
        this.mapViewport = new ScrollPane(this.mapContainer);
        this.statusText = new Text();
        this.detailsButton = new Button();
        this.metrics = new GuiMetrics();
        this.legend = this.legendColumn();
        this.detailsGroup = this.panelContainer();
        this.actionsGroup = this.panelContainer();
        this.legendGroup = this.panelContainer();
        this.footer = new HBox();
        this.footer.setSpacing(PANEL_SPACING_PX);
        this.footer.setAlignment(Pos.CENTER);
        this.detailsVisible = true;
        this.currentRows = 0;
        this.currentCols = 0;
        this.currentCellSizePx = 0.0;
        this.recenterPending = false;
        this.dragLastSceneX = 0.0;
        this.dragLastSceneY = 0.0;

        this.configureRoot();
        this.configureHeader();
        this.configureViewport();
        this.configureFooter();
        this.configureInteractions();
    }

    static Parent createRoot() {
        return new BorderPane();
    }

    public double getViewportWidth() {
        final double width = this.mapViewport.getViewportBounds().getWidth();
        return Math.max(1.0, width);
    }

    public double getViewportHeight() {
        final double height = this.mapViewport.getViewportBounds().getHeight();
        return Math.max(1.0, height);
    }

    public void requestRecenter() {
        this.recenterPending = true;
    }

    public void render(
        final Display display,
        final String solvingState,
        final String status,
        final String puzzleName,
        final int rows,
        final int cols,
        final int wallBudget,
        final int updateCount,
        final double zoom,
        final double cellSizePx,
        final String sinceUpdate,
        final String elapsed
    ) {
        this.currentRows = rows;
        this.currentCols = cols;
        this.currentCellSizePx = cellSizePx;
        this.resizeMapSurface(rows, cols, cellSizePx);
        this.centerViewportIfPending(rows, cols);
        this.titleText.setText(puzzleName);
        this.stateText.setText(solvingState);
        this.statusText.setText(status);
        this.metrics.render(
            display,
            rows,
            cols,
            wallBudget,
            updateCount,
            zoom,
            sinceUpdate,
            elapsed
        );
    }

    private void configureRoot() {
        this.root.setPadding(new Insets(ROOT_PADDING_PX));
        this.root.setBackground(Background.fill(this.rootBackground()));
    }

    private void configureHeader() {
        this.titleText.setFill(GuiPalette.FOREGROUND);
        this.titleText.setFont(Font.font(Gui.FONT_NAME, 31.0));

        this.stateText.setFill(GuiPalette.FOREGROUND);
        this.stateText.setOpacity(0.85);
        this.stateText.setFont(Font.font(Gui.FONT_NAME, 15.0));

        this.statusText.setFill(GuiPalette.FOREGROUND);
        this.statusText.setOpacity(0.7);
        this.statusText.setFont(Font.font(Gui.FONT_NAME, 13.0));

        final VBox detailStack = new VBox();
        detailStack.setSpacing(6.0);
        detailStack.setAlignment(Pos.CENTER_RIGHT);
        detailStack.getChildren().addAll(this.stateText, this.statusText);

        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        final HBox headerRow = new HBox();
        headerRow.setSpacing(PANEL_SPACING_PX);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.getChildren().addAll(this.titleText, spacer, detailStack);

        final VBox header = this.panelContainer();
        header.getChildren().add(headerRow);
        this.root.setTop(header);
    }

    private void configureViewport() {
        this.mapViewport.setMinHeight(VIEWPORT_MIN_HEIGHT_PX);
        this.mapViewport.setFitToHeight(false);
        this.mapViewport.setFitToWidth(false);
        this.mapViewport.setPannable(true);
        this.mapViewport.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.mapViewport.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.mapViewport.setStyle(
            "-fx-background: transparent; " +
                "-fx-background-color: transparent; " +
                "-fx-control-inner-background: transparent;"
        );
        this.mapViewport.setBackground(Background.EMPTY);
        this.mapViewport.setBorder(Border.EMPTY);
        this.mapSurface.setMinSize(1.0, 1.0);
        this.mapSurface.setPrefSize(1.0, 1.0);
        this.mapSurface.setBackground(
            new Background(
                new BackgroundFill(GuiPalette.SURFACE_VARIANT, new CornerRadii(12.0), Insets.EMPTY)
            )
        );
        this.mapContainer.setMinSize(1.0, 1.0);
        this.mapContainer.setPrefSize(1.0, 1.0);
        this.mapContainer.setBackground(Background.EMPTY);
        this.mapViewport.viewportBoundsProperty().addListener((ignored, oldValue, newValue) -> {
            this.resizeMapSurface(this.currentRows, this.currentCols, this.currentCellSizePx);
            this.centerViewportIfPending(this.currentRows, this.currentCols);
        });

        final StackPane viewportCard = new StackPane(this.mapViewport);
        viewportCard.setPadding(new Insets(PANEL_PADDING_PX));
        viewportCard.setBackground(this.cardBackground());
        viewportCard.setBorder(this.cardBorder(CARD_RADIUS_PX));
        viewportCard.setEffect(this.cardShadow());

        BorderPane.setMargin(viewportCard, new Insets(ROOT_SPACING_PX, 0.0, ROOT_SPACING_PX, 0.0));
        this.root.setCenter(viewportCard);
    }

    private void configureFooter() {
        this.detailsButton.setBackground(
            new Background(
                new BackgroundFill(
                    GuiPalette.SURFACE_VARIANT,
                    new CornerRadii(CHIP_RADIUS_PX),
                    Insets.EMPTY
                )
            )
        );
        this.detailsButton.setBorder(this.cardBorder(CHIP_RADIUS_PX));
        this.detailsButton.setTextFill(GuiPalette.FOREGROUND);
        this.detailsButton.setFont(Font.font(Gui.FONT_NAME, 13.0));
        this.detailsButton.setPadding(new Insets(8.0, 18.0, 8.0, 18.0));
        this.detailsButton.setOnAction(event -> this.toggleDetails());

        final HBox detailsRow = new HBox();
        detailsRow.setAlignment(Pos.CENTER);
        detailsRow.setSpacing(PANEL_SPACING_PX);
        detailsRow.getChildren().add(this.detailsButton);

        this.detailsGroup.getChildren().addAll(this.sectionTitle("Statistics"), this.metrics);
        this.actionsGroup.getChildren().addAll(this.sectionTitle("Commands"), this.controlsRow());
        this.legendGroup.getChildren().addAll(this.sectionTitle("Legend"), this.legend);

        this.footer.getChildren().addAll(this.detailsGroup, this.actionsGroup, this.legendGroup);
        this.toggleDetails();
        final VBox footerColumn = new VBox();
        footerColumn.setSpacing(PANEL_SPACING_PX);
        footerColumn.setAlignment(Pos.CENTER);
        footerColumn.getChildren().addAll(detailsRow, this.footer);

        final StackPane footerWrap = new StackPane(footerColumn);
        footerWrap.setAlignment(Pos.CENTER);
        this.root.setBottom(footerWrap);
    }

    private VBox panelContainer() {
        final VBox panel = new VBox();
        panel.setSpacing(PANEL_SPACING_PX);
        panel.setPadding(new Insets(PANEL_PADDING_PX));
        panel.setBackground(this.cardBackground());
        panel.setBorder(this.cardBorder(CARD_RADIUS_PX));
        panel.setEffect(this.cardShadow());
        return panel;
    }

    private VBox controlsRow() {
        final VBox column = new VBox();
        column.setSpacing(6.0);
        column.setAlignment(Pos.CENTER_LEFT);
        column
            .getChildren()
            .addAll(
                this.shortcutRow("Ctrl/Cmd+O", "Open file"),
                this.shortcutRow("Ctrl/Cmd+V", "Paste mapcode"),
                this.shortcutRow("+ / -", "Zoom"),
                this.shortcutRow("0", "Reset zoom"),
                this.shortcutRow("Drag / Scroll", "Pan map")
            );
        return column;
    }

    private VBox legendColumn() {
        final VBox column = new VBox();
        column.setSpacing(8.0);
        column.setAlignment(Pos.CENTER_LEFT);
        column
            .getChildren()
            .addAll(
                this.legendItem(GuiPalette.EMPTY, "Empty"),
                this.legendItem(GuiPalette.CHECKPOINT, "Checkpoint"),
                this.legendItem(GuiPalette.SYSTEM_WALL, "System wall"),
                this.legendItem(GuiPalette.PLAYER_WALL, "Player wall")
            );
        return column;
    }

    private HBox legendItem(final Color color, final String label) {
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

    private Text sectionTitle(final String title) {
        final Text text = new Text(title);
        text.setFill(GuiPalette.FOREGROUND);
        text.setOpacity(0.65);
        text.setFont(Font.font(Gui.FONT_NAME, 11.0));
        return text;
    }

    private HBox shortcutRow(final String key, final String action) {
        final Label keycap = new Label(key);
        keycap.setTextFill(GuiPalette.FOREGROUND);
        keycap.setFont(Font.font("Monospaced", 12.0));
        keycap.setPadding(new Insets(4.0, 8.0, 4.0, 8.0));
        keycap.setBackground(
            new Background(
                new BackgroundFill(
                    GuiPalette.SURFACE_VARIANT,
                    new CornerRadii(KEYCAP_RADIUS_PX),
                    Insets.EMPTY
                )
            )
        );
        keycap.setBorder(this.cardBorder(KEYCAP_RADIUS_PX));

        final Text label = new Text(action);
        label.setFill(GuiPalette.FOREGROUND);
        label.setOpacity(0.88);
        label.setFont(Font.font(Gui.FONT_NAME, 13.0));

        final HBox row = new HBox();
        row.setSpacing(10.0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(keycap, label);
        return row;
    }

    private Background cardBackground() {
        return new Background(
            new BackgroundFill(GuiPalette.SURFACE, new CornerRadii(CARD_RADIUS_PX), Insets.EMPTY)
        );
    }

    private Border cardBorder(final double radiusPx) {
        return new Border(
            new BorderStroke(
                GuiPalette.OUTLINE,
                BorderStrokeStyle.SOLID,
                new CornerRadii(radiusPx),
                BorderWidths.DEFAULT
            )
        );
    }

    private DropShadow cardShadow() {
        return new DropShadow(12.0, 0.0, 3.0, Color.color(0.08, 0.1, 0.16, 0.14));
    }

    private void configureInteractions() {
        this.root.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
        this.mapViewport.addEventFilter(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        this.mapViewport.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);

        this.root.setOnDragOver(this::handleDragOver);
        this.root.setOnDragDropped(this::handleDragDropped);
    }

    private void handleKeyPress(final KeyEvent event) {
        if (event.isShortcutDown() && event.getCode() == KeyCode.O) {
            this.openChooser();
            event.consume();
            return;
        }
        if (event.isShortcutDown() && event.getCode() == KeyCode.V) {
            this.pasteMapCode();
            event.consume();
            return;
        }

        if (event.getCode() == KeyCode.DIGIT0 || event.getCode() == KeyCode.NUMPAD0) {
            this.control.adjustZoom(GuiMath.DEFAULT_ZOOM);
            event.consume();
            return;
        }

        if (this.isZoomInEvent(event)) {
            this.control.adjustZoom(GuiMath.zoomIn(this.control.getZoom()));
            event.consume();
            return;
        }

        if (this.isZoomOutEvent(event)) {
            this.control.adjustZoom(GuiMath.zoomOut(this.control.getZoom()));
            event.consume();
        }
    }

    private boolean isZoomInEvent(final KeyEvent event) {
        final String text = event.getText();
        if ("+".equals(text) || "=".equals(text)) {
            return true;
        }
        return event.getCode() == KeyCode.ADD;
    }

    private boolean isZoomOutEvent(final KeyEvent event) {
        final String text = event.getText();
        if ("-".equals(text) || "_".equals(text)) {
            return true;
        }
        return event.getCode() == KeyCode.SUBTRACT || event.getCode() == KeyCode.MINUS;
    }

    private void handleMousePressed(final MouseEvent event) {
        this.dragLastSceneX = event.getSceneX();
        this.dragLastSceneY = event.getSceneY();
    }

    private void handleMouseDragged(final MouseEvent event) {
        final double deltaX = event.getSceneX() - this.dragLastSceneX;
        final double deltaY = event.getSceneY() - this.dragLastSceneY;
        this.dragLastSceneX = event.getSceneX();
        this.dragLastSceneY = event.getSceneY();
        this.panByPixels(-deltaX, -deltaY);
        event.consume();
    }

    private void handleDragOver(final DragEvent event) {
        final List<File> files = event.getDragboard().getFiles();
        if (files.size() == 1 && GuiMath.isSupportedMapFile(files.get(0))) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    private void handleDragDropped(final DragEvent event) {
        final List<File> files = event.getDragboard().getFiles();
        if (files.size() != 1) {
            this.control.onMapSubmissionError("Drop exactly one .mapcode file.");
            event.setDropCompleted(false);
            event.consume();
            return;
        }

        final boolean success = this.loadMapCodeFile(files.get(0));
        event.setDropCompleted(success);
        event.consume();
    }

    private void openChooser() {
        final FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new ExtensionFilter("Pathery MapCode", "*.mapcode"));
        final File chosen = chooser.showOpenDialog(this.root.getScene().getWindow());
        if (chosen == null) {
            this.control.onFilePickerCancelled();
            return;
        }
        this.loadMapCodeFile(chosen);
    }

    private boolean loadMapCodeFile(final File file) {
        if (!GuiMath.isSupportedMapFile(file)) {
            this.control.onMapSubmissionError("Unsupported file type. Expected .mapcode.");
            return false;
        }

        final String mapCode;
        try {
            mapCode = Files.readString(file.toPath());
        } catch (IOException exception) {
            this.control.onMapSubmissionError("Can't read that file.");
            return false;
        }

        this.mapCodeConsumer.accept(mapCode);
        return true;
    }

    private void pasteMapCode() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        if (!clipboard.hasString()) {
            this.control.onMapSubmissionError("Clipboard does not contain text.");
            return;
        }
        final String mapCode = clipboard.getString();
        if (mapCode == null || mapCode.isBlank()) {
            this.control.onMapSubmissionError("Clipboard text is empty.");
            return;
        }
        this.mapCodeConsumer.accept(mapCode);
    }

    private void resizeMapSurface(final int rows, final int cols, final double cellSizePx) {
        if (rows <= 0 || cols <= 0 || cellSizePx <= 0.0) {
            return;
        }
        final double width = cols * cellSizePx;
        final double height = rows * cellSizePx;
        final double viewportWidth = Math.max(1.0, this.mapViewport.getViewportBounds().getWidth());
        final double viewportHeight = Math.max(
            1.0,
            this.mapViewport.getViewportBounds().getHeight()
        );
        this.mapSurface.setMinSize(width, height);
        this.mapSurface.setPrefSize(width, height);
        final double containerWidth = Math.max(width, viewportWidth);
        final double containerHeight = Math.max(height, viewportHeight);
        this.mapContainer.setMinSize(containerWidth, containerHeight);
        this.mapContainer.setPrefSize(containerWidth, containerHeight);
        this.mapSurface.setLayoutX((containerWidth - width) * 0.5);
        this.mapSurface.setLayoutY((containerHeight - height) * 0.5);
    }

    private void centerViewportIfPending(final int rows, final int cols) {
        if (!this.recenterPending || rows <= 0 || cols <= 0) {
            return;
        }
        Platform.runLater(() -> {
            this.mapViewport.setHvalue(0.5);
            this.mapViewport.setVvalue(0.5);
        });
        this.recenterPending = false;
    }

    private void panByPixels(final double deltaX, final double deltaY) {
        final double contentWidth = this.mapContainer.getWidth();
        final double contentHeight = this.mapContainer.getHeight();
        final double viewportWidth = this.mapViewport.getViewportBounds().getWidth();
        final double viewportHeight = this.mapViewport.getViewportBounds().getHeight();

        final double scrollableWidth = Math.max(1.0, contentWidth - viewportWidth);
        final double scrollableHeight = Math.max(1.0, contentHeight - viewportHeight);

        if (contentWidth > viewportWidth) {
            final double hDelta = deltaX / scrollableWidth;
            this.mapViewport.setHvalue(clampUnit(this.mapViewport.getHvalue() + hDelta));
        }
        if (contentHeight > viewportHeight) {
            final double vDelta = deltaY / scrollableHeight;
            this.mapViewport.setVvalue(clampUnit(this.mapViewport.getVvalue() + vDelta));
        }
    }

    private static double clampUnit(final double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private void toggleDetails() {
        this.detailsVisible = !this.detailsVisible;
        this.detailsGroup.setManaged(this.detailsVisible);
        this.detailsGroup.setVisible(this.detailsVisible);
        this.actionsGroup.setManaged(this.detailsVisible);
        this.actionsGroup.setVisible(this.detailsVisible);
        this.legendGroup.setManaged(this.detailsVisible);
        this.legendGroup.setVisible(this.detailsVisible);
        this.detailsButton.setText(this.detailsVisible ? "Hide details" : "Show details");
    }

    private Color rootBackground() {
        return GuiPalette.BACKGROUND;
    }
}
