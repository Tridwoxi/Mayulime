package think.solvers.compass;

import think.common.StandardEvaluator;
import think.domain.model.Puzzle;
import think.domain.model.Tile;

final class Improver {

    private final Puzzle puzzle;
    private final StandardEvaluator evaluator;
    private final Fields fields;

    Improver(final Puzzle puzzle) {
        this.puzzle = puzzle;
        this.evaluator = new StandardEvaluator(puzzle);
        this.fields = new Fields(puzzle);
    }

    record ImproveResult(int newScore, int deltaWalls) {}

    ImproveResult improve(final Tile[] state, final int currentScore, final int remainingBudget) {
        if (currentScore == StandardEvaluator.NO_PATH_EXISTS) {
            throw new IllegalArgumentException();
        }
        return null;
    }

    private record PlaceMoreResult(int newScore, int deltaWalls) {}

    private PlaceMoreResult placeMore(final Tile[] state, final int remainingBudget) {
        return null;
    }
}
