package think.solvers;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import think.domain.model.Puzzle;
import think.domain.model.Tile;
import think.solvers.compass.CompassSolver;
import think.solvers.exact.EnumerateSolver;
import think.solvers.local.ChokepointSolver;
import think.solvers.local.ClimbSolver;
import think.solvers.local.DumpSolver;
import think.solvers.local.FrontierSolver;
import think.solvers.local.IdentitySolver;
import think.solvers.local.IntersectSolver;
import think.solvers.local.OverfillSolver;
import think.solvers.local.RuinSolver;
import think.solvers.local.ScrambleSolver;
import think.solvers.local.UncoverSolver;
import think.solvers.local.WalkSolver;
import think.solvers.naive.BaselineSolver;
import think.solvers.naive.RandomSolver;

public enum SolverKind {
    BASELINE,
    CHOKEPOINT,
    CLIMB,
    COMPASS,
    DUMP,
    ENUMERATE,
    FRONTIER,
    IDENTITY,
    INTERSECT,
    OVERFILL,
    RANDOM,
    RUIN,
    SCRAMBLE,
    UNCOVER,
    WALK;

    public static final class NoSuchSolverException extends Exception {}

    public Solver create(final BiConsumer<String, Tile[]> listener, final Puzzle puzzle) {
        return switch (this) {
            case BASELINE -> new BaselineSolver(listener, puzzle);
            case CHOKEPOINT -> new ChokepointSolver(listener, puzzle);
            case CLIMB -> new ClimbSolver(listener, puzzle);
            case COMPASS -> new CompassSolver(listener, puzzle);
            case DUMP -> new DumpSolver(listener, puzzle);
            case ENUMERATE -> new EnumerateSolver(listener, puzzle);
            case FRONTIER -> new FrontierSolver(listener, puzzle);
            case IDENTITY -> new IdentitySolver(listener, puzzle);
            case INTERSECT -> new IntersectSolver(listener, puzzle);
            case OVERFILL -> new OverfillSolver(listener, puzzle);
            case RANDOM -> new RandomSolver(listener, puzzle);
            case RUIN -> new RuinSolver(listener, puzzle);
            case SCRAMBLE -> new ScrambleSolver(listener, puzzle);
            case UNCOVER -> new UncoverSolver(listener, puzzle);
            case WALK -> new WalkSolver(listener, puzzle);
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
        return FRONTIER;
    }
}
