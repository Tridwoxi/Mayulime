package infra.gui;

record UiState(
    UiPhase phase,
    int puzzleEpoch,
    String puzzleName,
    int rows,
    int cols,
    int wallBudget,
    Submission bestUpdate,
    int spentWalls,
    int updateCount,
    double cellSizePx,
    boolean canRestart,
    boolean canCopyMapCode,
    String statusMessage,
    String bestScoreText,
    String submitterText,
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
            "-",
            "-",
            -1L,
            -1L,
            -1L,
            false
        );
    }
}
