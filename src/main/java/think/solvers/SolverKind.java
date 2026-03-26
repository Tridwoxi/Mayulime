package think.solvers;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import think.domain.model.Puzzle;
import think.manager.Proposal;
import think.solvers.local.ChokepointSolver;
import think.solvers.local.ClimbSolver;
import think.solvers.local.IdentitySolver;
import think.solvers.local.WalkSolver;
import think.solvers.naive.BaselineSolver;
import think.solvers.naive.RandomSolver;

public enum SolverKind {
    BASELINE,
    RANDOM,
    CLIMB,
    IDENTITY,
    WALK,
    CHOKEPOINT;

    public static final class NoSuchSolverException extends Exception {}

    public Solver create(final Consumer<Proposal> listener, final Puzzle puzzle) {
        return switch (this) {
            case BASELINE -> new BaselineSolver(listener, puzzle);
            case RANDOM -> new RandomSolver(listener, puzzle);
            case CLIMB -> new ClimbSolver(listener, puzzle);
            case IDENTITY -> new IdentitySolver(listener, puzzle);
            case WALK -> new WalkSolver(listener, puzzle);
            case CHOKEPOINT -> new ChokepointSolver(listener, puzzle);
        };
    }

    public static SolverKind parse(final String name) throws NoSuchSolverException {
        for (final SolverKind candidate : SolverKind.values()) {
            if (candidate.toString().equalsIgnoreCase(name.strip())) {
                return candidate;
            }
        }
        throw new NoSuchSolverException();
    }

    public static List<SolverKind> asList() {
        return Arrays.asList(values());
    }

    public static SolverKind getBest() {
        return CHOKEPOINT;
    }
}
