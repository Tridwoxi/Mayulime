package think.manager;

import think.common.StandardEvaluator;
import think.domain.model.Feature;
import think.domain.model.Puzzle;

public final class Proposal {

    private final String submitter;
    private final Puzzle puzzle;
    private final Feature[] features;
    private final int score;
    private final long createdAtMs;

    public Proposal(final String submitter, final Puzzle puzzle, final Feature[] features) {
        this.submitter = submitter;
        this.puzzle = puzzle;
        this.features = features.clone();
        this.score = new StandardEvaluator(puzzle).evaluate(features);
        this.createdAtMs = System.currentTimeMillis();
        if (!puzzle.isValid(features)) {
            throw new IllegalArgumentException();
        }
    }

    public String getSubmitter() {
        return submitter;
    }

    public Puzzle getPuzzle() {
        return puzzle;
    }

    public Feature[] getFeatures() {
        return features.clone();
    }

    public int getScore() {
        return score;
    }

    public long getCreatedAtMs() {
        return createdAtMs;
    }
}
