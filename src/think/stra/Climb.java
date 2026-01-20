package think.stra;

import java.util.ArrayList;
import java.util.HashSet;
import think.Solver;
import think.ana.Tools;
import think.repr.Cell;
import think.repr.Problem;

public final class Climb implements Runnable {

    private final Problem problem;
    private final HashSet<Cell> rubbers;

    public Climb(final Problem problem) {
        this.problem = problem;
        this.rubbers = new HashSet<>();
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
            Solver.getInstance().consider(Climb.class, problem, rubbers);
            reset();
        }
    }

    public boolean improve() {
        return false;
    }

    public void reset() {
        rubbers.clear();
        Tools.randomly(new ArrayList<>(problem.getEmptyCells()))
            .limit(problem.getNumRubbers())
            .forEach(cell -> rubbers.add(cell));
    }
}
