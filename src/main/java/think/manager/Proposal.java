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
        // It takes non-trivial (10 ms) time to evaluate gargantuan1-like maps, and we're measuring
        // submission time, so it's most honest to grab the time as early as possible.
        this.createdAtMs = System.currentTimeMillis();
        this.submitter = submitter;
        this.puzzle = puzzle;
        this.features = features.clone();
        this.score = StandardEvaluator.evaluate(puzzle, features);
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
