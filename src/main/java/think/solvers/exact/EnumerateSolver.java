package think.solvers.exact;

import infra.logging.Logger;
import java.util.function.BiConsumer;
import java.util.function.IntPredicate;
import think.common.StandardEvaluator;
import think.domain.model.Puzzle;
import think.domain.model.Tile;
import think.ints.IntArrays;
import think.solvers.Solver;

public final class EnumerateSolver extends Solver {

    private final int[] blankCellIndices;
    private final StandardEvaluator evaluator;

    public EnumerateSolver(final BiConsumer<String, Tile[]> listener, final Puzzle puzzle) {
        super(listener, puzzle);
        this.blankCellIndices = getBlankCellIndices(puzzle.tiles());
        this.evaluator = new StandardEvaluator(puzzle);
    }

    @Override
    protected void solve() throws KilledException {
        final int numBlanks = blankCellIndices.length;
        Logger.info(
            "Enumerating ~%.2e combinations (%d blanks, up to %d walls)",
            estimateTotalCombinations(numBlanks, getPuzzle().blockingBudget()),
            numBlanks,
            getPuzzle().blockingBudget()
        );

        final Tile[] maze = getPuzzle().tiles();
        int bestScore = StandardEvaluator.NO_PATH_EXISTS;

        for (int numWalls = 0; numWalls <= getPuzzle().blockingBudget(); numWalls += 1) {
            final int[] combination = IntArrays.ofRange(0, numWalls);
            for (;;) {
                checkAlive();
                for (int index = 0; index < numWalls; index += 1) {
                    maze[blankCellIndices[combination[index]]] = Tile.PLAYER_WALL;
                }
                final int score = evaluator.evaluate(maze);
                if (score > bestScore) {
                    bestScore = score;
                    propose(maze);
                }
                for (int index = 0; index < numWalls; index += 1) {
                    maze[blankCellIndices[combination[index]]] = Tile.BLANK;
                }
                if (!nextCombination(combination, numBlanks)) {
                    break;
                }
            }
        }
    }

    private static boolean nextCombination(final int[] combination, final int numElements) {
        final int numChosen = combination.length;
        if (numChosen == 0) {
            return false;
        }
        int position = numChosen - 1;
        while (position >= 0 && combination[position] == numElements - numChosen + position) {
            position -= 1;
        }
        if (position < 0) {
            return false;
        }
        combination[position] += 1;
        for (int subsequent = position + 1; subsequent < numChosen; subsequent += 1) {
            combination[subsequent] = combination[subsequent - 1] + 1;
        }
        return true;
    }

    private static double estimateTotalCombinations(final int numBlanks, final int maxWalls) {
        double total = 0;
        for (int walls = 0; walls <= maxWalls; walls += 1) {
            total += binomialCoefficient(numBlanks, walls);
        }
        return total;
    }

    private static double binomialCoefficient(final int numElements, final int numChosen) {
        final int effectiveK = Math.min(numChosen, numElements - numChosen);
        double result = 1;
        for (int index = 0; index < effectiveK; index += 1) {
            result = (result * (numElements - index)) / (index + 1);
        }
        return result;
    }

    private static int[] getBlankCellIndices(final Tile[] state) {
        final IntPredicate isBlank = index -> state[index] == Tile.BLANK;
        return IntArrays.ofRangeWhere(0, state.length, isBlank);
    }
}
