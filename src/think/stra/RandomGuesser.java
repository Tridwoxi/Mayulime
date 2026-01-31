package think.stra;

import java.util.ArrayList;
import think.Manager;
import think.ana.Tools;
import think.repr.Cell;
import think.repr.Grid;
import think.repr.Problem;
import think.repr.Problem.Feature;

/**
    Proof of concept, since it's nice to see some player walls assigned. This strategy
    is inefficient and often takes minutes to tie the best human score on a Simple.
 */
public final class RandomGuesser extends Strategy {

    public RandomGuesser(final Problem problem) {
        super(problem, "guess randomly");
    }

    @Override
    public void run() {
        while (keepGoing()) {
            Manager.getInstance().consider(this, getProblem(), guess());
        }
    }

    private Grid<Feature> guess() {
        final Grid<Feature> guess = getProblem().getAnotherInitial();
        final ArrayList<Cell> emptyCells = getProblem()
            .getCachedInitial()
            .where(Feature.EMPTY::equals, new ArrayList<>());
        Tools.randomly(emptyCells)
            .limit(getProblem().getPlayerWallSupply())
            .forEachOrdered(cell -> guess.set(cell, Feature.PLAYER_WALL));
        return guess;
    }
}
