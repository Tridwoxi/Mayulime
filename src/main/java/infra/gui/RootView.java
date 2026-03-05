package infra.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import think.manager.StatusUpdate;

@SuppressWarnings({ "checkstyle:ClassDataAbstractionCoupling", "checkstyle:ClassFanOutComplexity" })
final class RootView {

    interface Intents {
        void submitMapCode(String mapCode);
        void stopOrRestart();
        void reportMapInputError(String message);
    }

    private static final double ROOT_PADDING_PX = 24.0;
    private static final double ROOT_SPACING_PX = 18.0;
    private static final double VIEWPORT_MIN_HEIGHT_PX = 280.0;

    private final BorderPane root;
    private final SidebarView sidebar;
    private final BoardView board;
    private final ScrollPane viewport;
    private final Pane boardContainer;
    private final Pane boardSurface;

    private Intents intents;

    private int currentRows;
    private int currentCols;
    private double currentCellSizePx;
    private double dragLastSceneX;
    private double dragLastSceneY;

    RootView() {
        this.root = new BorderPane();
        this.sidebar = new SidebarView();
        this.board = new BoardView();
        this.boardSurface = new Pane(this.board);
        this.boardContainer = new Pane(this.boardSurface);
        this.viewport = new ScrollPane(this.boardContainer);

        this.intents = null;
        this.currentRows = 0;
        this.currentCols = 0;
        this.currentCellSizePx = Gui.MAX_CELL_SIZE_PX;
        this.dragLastSceneX = 0.0;
        this.dragLastSceneY = 0.0;

        this.configureShell();
        this.configureViewport();
        this.configureSidebar();
    }

    public Parent getRoot() {
        return root;
    }

    public void bindIntents(final Intents intentsToBind) {
        this.intents = intentsToBind;
        this.sidebar.onStopOrRestart(intentsToBind::stopOrRestart);
        this.configureInteractions();
    }

    public double getViewportWidth() {
        return Math.max(1.0, this.viewport.getViewportBounds().getWidth());
    }

    public double getViewportHeight() {
        return Math.max(1.0, this.viewport.getViewportBounds().getHeight());
    }

    public void renderSidebar(
        final UiState state,
        final String sinceUpdate,
        final String elapsed,
        final StatusUpdate display
    ) {
        this.sidebar.render(state, sinceUpdate, elapsed, display);
    }

    public void renderBoard(
        final StatusUpdate display,
        final int rows,
        final int cols,
        final double cellSizePx,
        final boolean recenter
    ) {
        this.currentRows = rows;
        this.currentCols = cols;
        this.currentCellSizePx = cellSizePx;

        if (display == null) {
            this.board.clear();
        } else {
            this.board.render(display, cellSizePx);
        }

        this.resizeBoardSurface(rows, cols, cellSizePx);
        if (recenter) {
            this.centerViewport();
        }
    }

    private void configureShell() {
        this.root.setPadding(new Insets(ROOT_PADDING_PX));
        this.root.setBackground(
            new Background(
                new BackgroundFill(
                    UiPalette.BACKGROUND,
                    CornerRadii.EMPTY,
                    Insets.EMPTY
                )
            )
        );
    }

    private void configureViewport() {
        this.viewport.setMinHeight(VIEWPORT_MIN_HEIGHT_PX);
        this.viewport.setPannable(true);
        this.viewport.setFitToWidth(false);
        this.viewport.setFitToHeight(false);
        this.viewport.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.viewport.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.viewport.setStyle(
            "-fx-background: transparent; " +
                "-fx-background-color: transparent; " +
                "-fx-control-inner-background: transparent;"
        );

        this.boardSurface.setMinSize(1.0, 1.0);
        this.boardSurface.setPrefSize(1.0, 1.0);
        this.boardSurface.setBackground(
            new Background(
                new BackgroundFill(
                    UiPalette.SURFACE_VARIANT,
                    new CornerRadii(12.0),
                    Insets.EMPTY
                )
            )
        );

        this.boardContainer.setMinSize(1.0, 1.0);
        this.boardContainer.setPrefSize(1.0, 1.0);

        this.viewport.viewportBoundsProperty().addListener((ignored, oldValue, newValue) -> {
            this.resizeBoardSurface(this.currentRows, this.currentCols, this.currentCellSizePx);
        });

        final StackPane viewportCard = new StackPane(this.viewport);
        viewportCard.setPadding(new Insets(16.0));
        viewportCard.setBackground(
            new Background(
                new BackgroundFill(UiPalette.SURFACE, new CornerRadii(18.0), Insets.EMPTY)
            )
        );
        viewportCard.setBorder(
            new Border(
                new BorderStroke(
                    UiPalette.OUTLINE,
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(18.0),
                    BorderWidths.DEFAULT
                )
            )
        );
        viewportCard.setEffect(new DropShadow(12.0, 0.0, 3.0, Color.color(0.08, 0.1, 0.16, 0.14)));

        BorderPane.setMargin(viewportCard, new Insets(0.0, ROOT_SPACING_PX, 0.0, 0.0));
        this.root.setCenter(viewportCard);
    }

    private void configureSidebar() {
        final StackPane sidebarWrap = new StackPane(this.sidebar);
        sidebarWrap.setAlignment(Pos.CENTER_LEFT);
        sidebarWrap.setPadding(new Insets(0.0));
        BorderPane.setMargin(sidebarWrap, new Insets(0.0, 0.0, 0.0, ROOT_SPACING_PX));
        this.root.setRight(sidebarWrap);
    }

    private void configureInteractions() {
        this.root.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
        this.viewport.addEventFilter(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        this.viewport.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);

        this.root.setOnDragOver(this::handleDragOver);
        this.root.setOnDragDropped(this::handleDragDropped);
    }

    private void handleKeyPress(final KeyEvent event) {
        if (intents == null) {
            return;
        }
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

    private void panByPixels(final double deltaX, final double deltaY) {
        final double contentWidth = this.boardContainer.getWidth();
        final double contentHeight = this.boardContainer.getHeight();
        final double viewportWidth = this.viewport.getViewportBounds().getWidth();
        final double viewportHeight = this.viewport.getViewportBounds().getHeight();

        final double scrollableWidth = Math.max(1.0, contentWidth - viewportWidth);
        final double scrollableHeight = Math.max(1.0, contentHeight - viewportHeight);

        if (contentWidth > viewportWidth) {
            final double hDelta = deltaX / scrollableWidth;
            this.viewport.setHvalue(clampUnit(this.viewport.getHvalue() + hDelta));
        }
        if (contentHeight > viewportHeight) {
            final double vDelta = deltaY / scrollableHeight;
            this.viewport.setVvalue(clampUnit(this.viewport.getVvalue() + vDelta));
        }
    }

    private void handleDragOver(final DragEvent event) {
        final List<File> files = event.getDragboard().getFiles();
        if (files.size() == 1 && UiMath.isSupportedMapFile(files.get(0))) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    private void handleDragDropped(final DragEvent event) {
        final List<File> files = event.getDragboard().getFiles();
        if (files.size() != 1) {
            if (intents != null) {
                intents.reportMapInputError("Drop exactly one .mapcode file.");
            }
            event.setDropCompleted(false);
            event.consume();
            return;
        }
        final boolean loaded = this.loadMapCodeFile(files.get(0));
        event.setDropCompleted(loaded);
        event.consume();
    }

    private void openChooser() {
        if (intents == null) {
            return;
        }
        final FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new ExtensionFilter("Pathery MapCode", "*.mapcode"));
        final File chosen = chooser.showOpenDialog(this.root.getScene().getWindow());
        if (chosen == null) {
            intents.reportMapInputError("File picker cancelled.");
            return;
        }
        this.loadMapCodeFile(chosen);
    }

    private boolean loadMapCodeFile(final File file) {
        if (intents == null) {
            return false;
        }
        if (!UiMath.isSupportedMapFile(file)) {
            intents.reportMapInputError("Unsupported file type. Expected .mapcode.");
            return false;
        }

        final String mapCode;
        try {
            mapCode = Files.readString(file.toPath());
        } catch (IOException exception) {
            intents.reportMapInputError("Can't read that file.");
            return false;
        }

        intents.submitMapCode(mapCode);
        return true;
    }

    private void pasteMapCode() {
        if (intents == null) {
            return;
        }
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        if (!clipboard.hasString()) {
            intents.reportMapInputError("Clipboard does not contain text.");
            return;
        }
        final String mapCode = clipboard.getString();
        if (mapCode == null || mapCode.isBlank()) {
            intents.reportMapInputError("Clipboard text is empty.");
            return;
        }
        intents.submitMapCode(mapCode);
    }

    private void resizeBoardSurface(final int rows, final int cols, final double cellSizePx) {
        if (rows <= 0 || cols <= 0 || cellSizePx <= 0.0) {
            return;
        }

        final double boardWidth = cols * cellSizePx;
        final double boardHeight = rows * cellSizePx;
        final double viewportWidth = Math.max(1.0, this.viewport.getViewportBounds().getWidth());
        final double viewportHeight = Math.max(1.0, this.viewport.getViewportBounds().getHeight());

        this.boardSurface.setMinSize(boardWidth, boardHeight);
        this.boardSurface.setPrefSize(boardWidth, boardHeight);

        final double containerWidth = Math.max(boardWidth, viewportWidth);
        final double containerHeight = Math.max(boardHeight, viewportHeight);
        this.boardContainer.setMinSize(containerWidth, containerHeight);
        this.boardContainer.setPrefSize(containerWidth, containerHeight);

        this.boardSurface.setLayoutX((containerWidth - boardWidth) * 0.5);
        this.boardSurface.setLayoutY((containerHeight - boardHeight) * 0.5);
    }

    private void centerViewport() {
        Platform.runLater(() -> {
            this.viewport.setHvalue(0.5);
            this.viewport.setVvalue(0.5);
        });
    }

    private static double clampUnit(final double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
