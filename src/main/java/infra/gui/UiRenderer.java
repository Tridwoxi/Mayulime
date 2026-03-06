package infra.gui;

import think.manager.StatusUpdate;

final class UiRenderer {

    private final RootView rootView;
    private StatusUpdate lastRenderedDisplay;
    private int lastRenderedRows;
    private int lastRenderedCols;
    private double lastRenderedCellSizePx;

    UiRenderer(final RootView rootView) {
        this.rootView = rootView;
        this.lastRenderedDisplay = null;
        this.lastRenderedRows = -1;
        this.lastRenderedCols = -1;
        this.lastRenderedCellSizePx = -1.0;
    }

    public UiState render(final UiState state, final long nowNanos) {
        final double baseSize = this.currentBaseCellSize(state);
        final double cellSize = UiMath.clampCellSize(baseSize);

        final StatusUpdate display = state.bestUpdate();
        final long timerNow =
            state.timersFrozenAtNanos() > 0 ? state.timersFrozenAtNanos() : nowNanos;
        final String elapsed = UiMath.elapsed(state.puzzleStartedAtNanos(), timerNow);
        final String sinceUpdate = UiMath.elapsed(state.lastUpdateAtNanos(), timerNow);

        if (this.shouldRenderBoard(display, state.rows(), state.cols(), cellSize, state)) {
            this.rootView.renderBoard(
                display,
                state.rows(),
                state.cols(),
                cellSize,
                state.recenterPending()
            );
            this.lastRenderedDisplay = display;
            this.lastRenderedRows = state.rows();
            this.lastRenderedCols = state.cols();
            this.lastRenderedCellSizePx = cellSize;
        }
        this.rootView.renderSidebar(state, sinceUpdate, elapsed, display);

        if (Double.compare(state.cellSizePx(), cellSize) == 0 && !state.recenterPending()) {
            return state;
        }

        return new UiState(
            state.phase(),
            state.puzzleEpoch(),
            state.puzzleName(),
            state.rows(),
            state.cols(),
            state.wallBudget(),
            state.bestUpdate(),
            state.updateCount(),
            cellSize,
            state.canRestart(),
            state.statusMessage(),
            state.puzzleStartedAtNanos(),
            state.lastUpdateAtNanos(),
            state.timersFrozenAtNanos(),
            false
        );
    }

    private boolean shouldRenderBoard(
        final StatusUpdate display,
        final int rows,
        final int cols,
        final double cellSizePx,
        final UiState state
    ) {
        final boolean dimensionsChanged =
            rows != this.lastRenderedRows || cols != this.lastRenderedCols;
        final boolean displayChanged = display != this.lastRenderedDisplay;
        final boolean cellSizeChanged =
            Double.compare(cellSizePx, this.lastRenderedCellSizePx) != 0;
        return displayChanged || dimensionsChanged || cellSizeChanged || state.recenterPending();
    }

    private double currentBaseCellSize(final UiState state) {
        if (state.rows() <= 0 || state.cols() <= 0) {
            return Gui.MAX_CELL_SIZE_PX;
        }
        final double width = Math.max(1.0, this.rootView.getViewportWidth());
        final double height = Math.max(1.0, this.rootView.getViewportHeight());
        final double fitByWidth = width / state.cols();
        final double fitByHeight = height / state.rows();
        return Math.min(fitByWidth, fitByHeight);
    }
}
