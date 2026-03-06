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
    int spentWalls,
    int updateCount,
    double cellSizePx,
    boolean canRestart,
    boolean canCopyMapCode,
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
            "Mayulime",
            0,
            0,
            0,
            null,
            0,
            0,
            Gui.MAX_CELL_SIZE_PX,
            false,
            false,
            "Drop, upload, or paste a MapCode to analyze",
            -1L,
            -1L,
            -1L,
            false
        );
    }
}
