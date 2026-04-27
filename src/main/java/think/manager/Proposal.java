package think.manager;

import java.time.Duration;
import think.common.StandardEvaluator;
import think.domain.codec.Serializer;
import think.domain.model.Puzzle;
import think.domain.model.Tile;

// A record will do but class is better so we can maintain the invariant that the score corresponds
// to the score of the state. Also, there's no need to pass a score if we can compute it here.
public final class Proposal {

    private final String submitter;
    private final Puzzle puzzle;
    private final Tile[] state;
    private final int score;
    private final Duration createdAfter;

    public Proposal(
        final String submitter,
        final Puzzle puzzle,
        final Tile[] state,
        final Duration createdAfter
    ) {
        this.submitter = submitter;
        this.puzzle = puzzle;
        this.state = state.clone();
        this.score = new StandardEvaluator(puzzle).evaluate(state);
        this.createdAfter = createdAfter;
        if (!puzzle.isValid(this.state)) {
            throw new IllegalArgumentException(Serializer.serialize(puzzle, this.state));
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

    public Duration getCreatedAfter() {
        return createdAfter;
    }
}
