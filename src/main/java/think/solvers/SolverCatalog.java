package think.solvers;

import think.domain.model.Puzzle;
import think.manager.SolverKind;
import think.solvers.Solver.ProposedSolution;
import think.solvers.local.ClimbV1Solver;
import think.solvers.naive.BaselineSolver;
import think.solvers.naive.RandomSolver;

public final class SolverCatalog {

    private final ProposedSolution listener;
    private final Puzzle puzzle;

    public SolverCatalog(final ProposedSolution listener, final Puzzle puzzle) {
        this.listener = listener;
        this.puzzle = puzzle;
    }

    public Solver create(final SolverKind solverKind) {
        return switch (solverKind) {
            case BASELINE -> new BaselineSolver(listener, puzzle);
            case RANDOM -> new RandomSolver(listener, puzzle);
            case CLIMBV1 -> new ClimbV1Solver(listener, puzzle);
        };
    }
}
