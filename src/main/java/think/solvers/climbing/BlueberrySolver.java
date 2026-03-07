package think.solvers.climbing;

import java.util.function.IntPredicate;
import think.common.IntArrays;
import think.common.StandardEvaluator;
import think.domain.model.Feature;
import think.domain.model.Puzzle;
import think.solvers.Solver;

/**
    Blind random restart hill climbing.
 */
public final class BlueberrySolver extends Solver {

    private final int[] blankCellIndices;

    public BlueberrySolver(final ProposedSolution listener, final Puzzle puzzle) {
        super(listener, puzzle);
        this.blankCellIndices = getBlankCellIndices(puzzle.getFeatures());
    }

    @Override
    protected void solve() throws KilledException {
        for (;;) {
            checkAlive();
            propose(hillClimb());
        }
    }

    private Feature[] hillClimb() {
        final Feature[] features = seed();
        return features;
    }

    private Feature[] seed() {
        IntArrays.shuffleInPlace(blankCellIndices);
        final Feature[] features = getPuzzle().getFeatures();
        final int budget = getPuzzle().getBlockingBudget();
        for (int placement = 0; placement < budget; placement += 1) {
            features[blankCellIndices[placement]] = Feature.PLAYER_WALL;
            if (StandardEvaluator.evaluate(getPuzzle(), features) < 0) {
                features[blankCellIndices[placement]] = Feature.BLANK;
                break;
            }
        }
        return features;
    }

    private static int[] getBlankCellIndices(final Feature[] features) {
        final IntPredicate isBlank = index -> features[index] == Feature.BLANK;
        return IntArrays.ofRangeWhere(0, features.length, isBlank);
    }
}
