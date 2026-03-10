package infra.gui;

import java.io.File;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.DragEvent;
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
import javafx.scene.layout.StackPane;
import javafx.stage.Window;
import think.manager.Submission;

@SuppressWarnings({ "checkstyle:ClassDataAbstractionCoupling", "checkstyle:ClassFanOutComplexity" })
final class RootView {

    interface Intents {
        void onStopOrRestartRequested();
        void onUploadMapCodeRequested();
        void onPasteMapCodeRequested();
        void onCopyMapCodeRequested();
        void onMapCodeFilesDropped(List<File> files);
    }

    private static final double ROOT_PADDING_PX = 24.0;
    private static final double ROOT_SPACING_PX = 18.0;
    private static final double VIEWPORT_MIN_HEIGHT_PX = 280.0;

    private UiPalette palette;
    private final BorderPane root;
    private final SidebarView sidebar;
    private final BoardView board;
    private final ScrollPane viewport;
    private final StackPane boardViewportContent;
    private final StackPane viewportCard;

    private Intents intents;

    private int currentRows;
    private int currentCols;
    private double currentCellSizePx;
    private double dragLastSceneX;
    private double dragLastSceneY;

    RootView() {
        this.palette = UiPalette.fromColorScheme(Platform.getPreferences().getColorScheme());
        this.root = new BorderPane();
        this.sidebar = new SidebarView(this.palette);
        this.board = new BoardView(this.palette);
        this.boardViewportContent = new StackPane(this.board);
        this.viewport = new ScrollPane(this.boardViewportContent);
        this.viewportCard = new StackPane(this.viewport);

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
        this.sidebar.onStopOrRestart(intentsToBind::onStopOrRestartRequested);
        this.sidebar.onUploadMapCode(intentsToBind::onUploadMapCodeRequested);
        this.sidebar.onPasteMapCode(intentsToBind::onPasteMapCodeRequested);
        this.sidebar.onCopyMapCode(intentsToBind::onCopyMapCodeRequested);
        this.configureInteractions();
    }

    public void onViewportChanged(final Runnable listener) {
        this.viewport.viewportBoundsProperty().addListener((ignored, oldValue, newValue) -> {
            this.resizeBoardViewportContent(
                this.currentRows,
                this.currentCols,
                this.currentCellSizePx
            );
            listener.run();
        });
    }

    public Window getWindow() {
        if (this.root.getScene() == null) {
            return null;
        }
        return this.root.getScene().getWindow();
    }

    public double getViewportWidth() {
        return Math.max(1.0, this.viewport.getViewportBounds().getWidth());
    }

    public double getViewportHeight() {
        return Math.max(1.0, this.viewport.getViewportBounds().getHeight());
    }

    public void renderSidebar(final UiState state, final String sinceUpdate, final String elapsed) {
        this.sidebar.render(state, sinceUpdate, elapsed);
    }

    public void renderBoard(
        final Submission display,
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

        this.resizeBoardViewportContent(rows, cols, cellSizePx);
        if (recenter) {
            this.centerViewport();
        }
    }

    public void applyPalette(final UiPalette paletteToApply) {
        this.palette = paletteToApply;
        this.root.setBackground(
            new Background(
                new BackgroundFill(this.palette.background(), CornerRadii.EMPTY, Insets.EMPTY)
            )
        );
        this.boardViewportContent.setBackground(
            new Background(
                new BackgroundFill(this.palette.surface(), CornerRadii.EMPTY, Insets.EMPTY)
            )
        );
        this.viewportCard.setBackground(
            new Background(
                new BackgroundFill(this.palette.surface(), CornerRadii.EMPTY, Insets.EMPTY)
            )
        );
        this.viewportCard.setBorder(
            new Border(
                new BorderStroke(
                    this.palette.outline(),
                    BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY,
                    BorderWidths.DEFAULT
                )
            )
        );
        this.viewportCard.setEffect(new DropShadow(12.0, 0.0, 3.0, this.palette.shadow()));
        this.sidebar.applyPalette(this.palette);
        this.board.applyPalette(this.palette);
    }

    private void configureShell() {
        this.root.setPadding(new Insets(ROOT_PADDING_PX));
        this.applyPalette(this.palette);
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

        this.boardViewportContent.setMinSize(1.0, 1.0);
        this.boardViewportContent.setPrefSize(1.0, 1.0);
        this.boardViewportContent.setAlignment(Pos.CENTER);

        this.viewportCard.setPadding(new Insets(16.0));

        BorderPane.setMargin(this.viewportCard, new Insets(0.0, ROOT_SPACING_PX, 0.0, 0.0));
        this.root.setCenter(this.viewportCard);
    }

    private void configureSidebar() {
        final StackPane sidebarWrap = new StackPane(this.sidebar);
        sidebarWrap.setAlignment(Pos.CENTER_LEFT);
        sidebarWrap.setPadding(new Insets(0.0));
        BorderPane.setMargin(sidebarWrap, new Insets(0.0, 0.0, 0.0, ROOT_SPACING_PX));
        this.root.setRight(sidebarWrap);
    }

    private void configureInteractions() {
        this.viewport.addEventFilter(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        this.viewport.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);

        this.root.setOnDragOver(this::handleDragOver);
        this.root.setOnDragDropped(this::handleDragDropped);
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
        final double contentWidth = this.boardViewportContent.getWidth();
        final double contentHeight = this.boardViewportContent.getHeight();
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
        if (files.size() == 1) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    private void handleDragDropped(final DragEvent event) {
        if (this.intents == null) {
            event.setDropCompleted(false);
            event.consume();
            return;
        }
        this.intents.onMapCodeFilesDropped(List.copyOf(event.getDragboard().getFiles()));
        event.setDropCompleted(true);
        event.consume();
    }

    private void resizeBoardViewportContent(
        final int rows,
        final int cols,
        final double cellSizePx
    ) {
        if (rows <= 0 || cols <= 0 || cellSizePx <= 0.0) {
            return;
        }

        final double boardWidth = cols * cellSizePx;
        final double boardHeight = rows * cellSizePx;
        final double viewportWidth = Math.max(1.0, this.viewport.getViewportBounds().getWidth());
        final double viewportHeight = Math.max(1.0, this.viewport.getViewportBounds().getHeight());

        final double containerWidth = Math.max(boardWidth, viewportWidth);
        final double containerHeight = Math.max(boardHeight, viewportHeight);
        this.boardViewportContent.setMinSize(containerWidth, containerHeight);
        this.boardViewportContent.setPrefSize(containerWidth, containerHeight);
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
