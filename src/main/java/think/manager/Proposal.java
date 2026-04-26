package think.manager;

import think.common.StandardEvaluator;
import think.domain.model.Puzzle;
import think.domain.model.Tile;

public final class Proposal {

    private final String submitter;
    private final Puzzle puzzle;
    private final Tile[] state;
    private final int score;
    private final long createdAtNanos;

    public Proposal(final String submitter, final Puzzle puzzle, final Tile[] state) {
        // Grab creation time as early as possible (this matters on sufficiently big maps) because
        // that's what I think of as creation time. Technically nanoTime emits no memory barrier so
        // everything can be rearranged, but this is the best we can do.
        this.createdAtNanos = System.nanoTime();
        this.submitter = submitter;
        this.puzzle = puzzle;
        this.state = state.clone();
        if (!puzzle.isValid(this.state)) {
            throw new IllegalArgumentException();
        }
        this.score = new StandardEvaluator(puzzle).evaluate(this.state);
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

    public long getCreatedAtNanos() {
        return createdAtNanos;
    }
}
