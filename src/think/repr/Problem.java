package think.repr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Predicate;
import think.tools.Structures.Pair;

/**
    Structurally, the unification of problem metadata and its initial grid.
 */
public final class Problem {

    public enum Feature {
        EMPTY,
        CHECKPOINT,
        SYSTEM_WALL,
        PLAYER_WALL,
        TELEPORT_IN,
        TELEPORT_OUT,
    }

    private final String name;
    private final int playerWallSupply;
    private final Grid<Feature> initial;
    private final ArrayList<Cell> checkpoints;
    private final HashMap<Cell, Cell> teleports;

    /**
        Create a Problem from parsed data. It is the caller's responsibility to ensure the provided information is semantically correct (no validation is performed).
     */
    public Problem(
        final String name,
        final int playerWallSupply,
        final Grid<Feature> initial,
        final ArrayList<Cell> checkpoints,
        final HashMap<Cell, Cell> teleports
    ) {
        this.name = name;
        this.playerWallSupply = playerWallSupply;
        this.initial = new Grid<>(initial);
        this.checkpoints = new ArrayList<>(checkpoints);
        this.teleports = new HashMap<>(teleports);
    }

    public String getName() {
        return name;
    }

    /**
        Returns the initial grid of the problem.

        The initial grid is read-only; modifying it is a design error. Use {@link
        #getAnotherInitial()} if modification is desired.
     */
    public Grid<Feature> getCachedInitial() {
        // SpotBugs will correctly tell you this exposes mutable internal state. This
        // method exists anyway because it is more performant and callers are expected
        // to be well-behaved.
        return initial;
    }

    public Grid<Feature> getAnotherInitial() {
        return new Grid<>(initial);
    }

    public int getPlayerWallSupply() {
        return playerWallSupply;
    }

    public ArrayList<Cell> getCheckpoints() {
        return new ArrayList<>(checkpoints);
    }

    public HashMap<Cell, Cell> getTeleports() {
        return new HashMap<>(teleports);
    }

    public boolean isValid(final Grid<Feature> solution) {
        final Predicate<Cell> legalMove = cell -> {
            final boolean unchanged = initial.get(cell) == solution.get(cell);
            final boolean assigned =
                initial.get(cell) == Feature.EMPTY &&
                solution.get(cell) == Feature.PLAYER_WALL;
            return unchanged || assigned;
        };
        final boolean enoughSupply =
            solution.where(Feature.PLAYER_WALL::equals).count() <= playerWallSupply;
        final boolean sameSize =
            initial.getNumRows() == solution.getNumRows() &&
            initial.getNumCols() == solution.getNumCols();
        return (
            sameSize &&
            enoughSupply &&
            initial.stream().map(Pair::second).allMatch(legalMove)
        );
    }
}
