package domain.model;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import think.graph.impl.GridGraph;
import think.graph.impl.GridGraph.Cell;

/**
    The solution-specific aspect of a Pathery board is the set of placed walls. This class enables
    independent evolution of a wall set from other candidate boards. It enforces legality of wall
    assignment, but allows for unlimited assignment. A method to extract a graph is provided.
 */
public final class Board {

    private static final Object OBJECT = new Object();
    private final Puzzle puzzle;
    private final Set<Cell> originallyEmpty;
    private final Set<Cell> originallyMissing;
    private final Set<Cell> originallyCheckpoint;
    private final Set<Cell> spentWalls;

    Board(final Puzzle puzzle) {
        this.puzzle = puzzle;
        this.originallyEmpty = new HashSet<>(puzzle.getOriginallyEmpty());
        this.originallyMissing = new HashSet<>(puzzle.getOriginallyMissing());
        this.originallyCheckpoint = new HashSet<>(puzzle.getOriginallyCheckpoint());
        this.spentWalls = new HashSet<>();
    }

    private Board(
        final Puzzle puzzle,
        final Set<Cell> originallyEmpty,
        final Set<Cell> originallyMissing,
        final Set<Cell> originallyCheckpoint,
        final Set<Cell> spentWalls
    ) {
        // We do not expose access to most of these. Only spentWalls is mutable and needs copying.
        this.puzzle = puzzle;
        this.originallyEmpty = originallyEmpty;
        this.originallyMissing = originallyMissing;
        this.originallyCheckpoint = originallyCheckpoint;
        this.spentWalls = new HashSet<>(spentWalls);
    }

    public boolean isSpentWall(final Cell cell) {
        return spentWalls.contains(cell);
    }

    public int getNumSpentWalls() {
        return spentWalls.size();
    }

    public Set<Cell> getSpentWalls() {
        return new HashSet<>(spentWalls);
    }

    public boolean placeWall(final Cell cell) {
        if (!originallyEmpty.contains(cell) || spentWalls.contains(cell)) {
            throw new IllegalArgumentException();
        }
        spentWalls.add(cell);
        return true;
    }

    public boolean removeWall(final Cell cell) {
        if (!originallyEmpty.contains(cell) || !spentWalls.contains(cell)) {
            throw new IllegalArgumentException();
        }
        spentWalls.remove(cell);
        return true;
    }

    public Puzzle getPuzzle() {
        return puzzle;
    }

    public GridGraph<Object> getTraversalGraph() {
        final Function<Cell, Optional<Object>> isPresent = cell ->
            originallyMissing.contains(cell) || spentWalls.contains(cell)
                ? Optional.empty()
                : Optional.of(OBJECT);
        return new GridGraph<>(puzzle.getNumRows(), puzzle.getNumCols(), isPresent);
    }

    public Board shallowCopy() {
        return new Board(
            puzzle,
            originallyEmpty,
            originallyMissing,
            originallyCheckpoint,
            spentWalls
        );
    }
}
