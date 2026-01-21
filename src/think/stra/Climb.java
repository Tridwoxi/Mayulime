package think.stra;

import java.util.ArrayList;
import java.util.HashSet;
import think.Manager;
import think.Manager.Strategy;
import think.ana.Tools;
import think.repr.Cell;
import think.repr.Problem;

public final class Climb implements Strategy {

    private final Problem problem;
    private final HashSet<Cell> playerWalls;

    public Climb(final Problem problem) {
        this.problem = problem;
        this.playerWalls = new HashSet<>();
        reset();
    }

    @Override
    public void run() {
        while (true) {
            improvement_cycle: while (true) {
                if (!improve()) {
                    break improvement_cycle;
                }
            }
            Manager.getInstance().consider(this, problem, playerWalls);
            reset();
        }
    }

    public boolean improve() {
        return false;
    }

    public void reset() {
        playerWalls.clear();
        Tools.randomly(new ArrayList<>(problem.getEmptyCells()))
            .limit(problem.getNumPlayerWalls())
            .forEach(cell -> playerWalls.add(cell));
    }

    @Override
    public String getName() {
        return "random restart hill climbing";
    }
}
