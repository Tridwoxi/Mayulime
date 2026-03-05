package infra.gui;

import think.manager.StatusUpdate;

record UiState(
    UiPhase phase,
    int puzzleEpoch,
    String puzzleName,
    int rows,
    int cols,
    int wallBudget,
    StatusUpdate bestUpdate,
    int updateCount,
    double cellSizePx,
    boolean canRestart,
    String statusMessage,
    long puzzleStartedAtNanos,
    long lastUpdateAtNanos,
    long timersFrozenAtNanos,
    boolean recenterPending
) {
    static UiState initial() {
        return new UiState(
            UiPhase.IDLE,
            0,
            "Welcome",
            0,
            0,
            0,
            null,
            0,
            Gui.MAX_CELL_SIZE_PX,
            false,
            "Open, drop, or paste a MapCode to begin",
            -1L,
            -1L,
            -1L,
            false
        );
    }
}
