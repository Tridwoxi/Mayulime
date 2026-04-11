package think.manager;

import think.common.StandardEvaluator;
import think.domain.model.Puzzle;
import think.domain.model.Tile;

public final class Proposal {

    private final String submitter;
    private final Puzzle puzzle;
    private final Tile[] state;
    private final int score;
    private final long createdAtMs;

    public Proposal(final String submitter, final Puzzle puzzle, final Tile[] state) {
        this.submitter = submitter;
        this.puzzle = puzzle;
        this.state = state.clone();
        this.score = new StandardEvaluator(puzzle).evaluate(state);
        this.createdAtMs = System.currentTimeMillis();
        if (!puzzle.isValid(state)) {
            throw new IllegalArgumentException();
        }
    }

    public String getSubmitter() {
        return submitter;
    }

    public Puzzle getPuzzle() {
        return puzzle;
    }

    public Tile[] getState() {
        return state.clone();
    }

    public int getScore() {
        return score;
    }

    public long getCreatedAtMs() {
        return createdAtMs;
    }
}
