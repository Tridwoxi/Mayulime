package think.solvers;

import java.util.function.Consumer;
import think.domain.model.Puzzle;
import think.manager.Proposal;
import think.solvers.local.ClimbSolver;
import think.solvers.local.IdentitySolver;
import think.solvers.local.WalkSolver;
import think.solvers.naive.BaselineSolver;
import think.solvers.naive.RandomSolver;

public final class SolverCatalog {

    private final Consumer<Proposal> listener;
    private final Puzzle puzzle;

    public SolverCatalog(final Consumer<Proposal> listener, final Puzzle puzzle) {
        this.listener = listener;
        this.puzzle = puzzle;
    }

    public Solver create(final SolverKind solverKind) {
        return switch (solverKind) {
            case BASELINE -> new BaselineSolver(listener, puzzle);
            case RANDOM -> new RandomSolver(listener, puzzle);
            case CLIMB -> new ClimbSolver(listener, puzzle);
            case IDENTITY -> new IdentitySolver(listener, puzzle);
            case WALK -> new WalkSolver(listener, puzzle);
        };
    }
}
