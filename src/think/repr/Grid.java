package think.repr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import think.tools.Iteration;
import think.tools.Structures.Pair;

/**
    Rectangular two-dimensional grid. Grids store items of type "T", which are indexed
    by "Cell"s.
 */
public final class Grid<T> {

    /**
        Conceptually, a cell is used to index like "item = grid[cell.row][cell.col]".
        Cells are not defined outside of a grid.
     */
    public record Cell(int row, int col) {
        public static final Cell OUT_OF_BOUNDS = new Cell(-1, -1);

        public int manhattanDistanceTo(final Cell other) {
            return Math.abs(row - other.row) + Math.abs(col - other.col);
        }

        /**
            Two cells are neighbors iff they are adjacent horizontally or vertically
            (diagonal does not count). Cells are not neighbors of themselves.
         */
        public boolean isNeighbor(final Cell other) {
            return manhattanDistanceTo(other) == 1;
        }
    }

    // PERF: Use primitives, like int[]. Compare IntStream versus Stream<Integer>.
    private final ArrayList<T> items;
    private final int numRows;
    private final int numCols;

    public Grid(final ArrayList<T> items, final int numRows, final int numCols) {
        assert items.size() == numRows * numCols && numRows >= 0;
        this.items = new ArrayList<>(items);
        this.numRows = numRows;
        this.numCols = numCols;
    }

    public Grid(final T item, final int numRows, final int numCols) {
        this(Iteration.filledArray(item, numRows * numCols), numRows, numCols);
    }

    public Grid(final Supplier<T> supplier, final int numRows, final int numCols) {
        this(
            Iteration.filledArray(ignored -> supplier.get(), numRows * numCols),
            numRows,
            numCols
        );
    }

    public Grid(final Grid<T> grid) {
        this(grid.items, grid.numRows, grid.numCols);
    }

    public T get(final Cell cell) {
        assert inBounds(cell);
        return items.get(cell.row() * numCols + cell.col());
    }

    public void set(final Cell cell, final T item) {
        assert inBounds(cell);
        items.set(cell.row() * numCols + cell.col(), item);
    }

    public void setAll(final Collection<Cell> cells, final T item) {
        assert cells.stream().allMatch(this::inBounds);
        cells.forEach(cell -> set(cell, item));
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    public boolean inBounds(final Cell cell) {
        return (
            cell.row() >= 0 &&
            cell.row() < numRows &&
            cell.col() >= 0 &&
            cell.col() < numCols
        );
    }

    public Stream<Pair<T, Cell>> stream() {
        return IntStream.range(0, items.size()).mapToObj(index ->
            new Pair<>(items.get(index), new Cell(index / numCols, index % numCols))
        );
    }

    public Stream<Cell> where(final Predicate<T> predicate) {
        return stream()
            .filter(pair -> predicate.test(pair.first()))
            .map(Pair::second);
    }

    public static <F, R> Grid<R> map(final Grid<F> grid, final Function<F, R> mapper) {
        final ArrayList<R> results = new ArrayList<>(grid.items.size());
        for (int index = 0; index < grid.items.size(); index += 1) {
            results.add(mapper.apply(grid.items.get(index)));
        }
        return new Grid<>(results, grid.getNumRows(), grid.getNumCols());
    }

    public static <F, S, R> Grid<R> combine(
        final Grid<F> first,
        final Grid<S> second,
        final BiFunction<F, S, R> combiner
    ) {
        assert first.getNumRows() == second.getNumRows();
        assert first.getNumCols() == second.getNumCols();
        final ArrayList<R> results = new ArrayList<>(first.items.size());
        for (int index = 0; index < first.items.size(); index += 1) {
            results.add(combiner.apply(first.items.get(index), second.items.get(index)));
        }
        return new Grid<>(results, first.getNumRows(), first.getNumCols());
    }

    public static Grid<Cell> ofCells(final int numRows, final int numCols) {
        final ArrayList<Cell> cells = new ArrayList<>(numRows * numCols);
        for (int row = 0; row < numRows; row += 1) {
            for (int col = 0; col < numCols; col += 1) {
                cells.add(new Cell(row, col));
            }
        }
        return new Grid<>(cells, numRows, numCols);
    }

    /**
        Get the neighbors of the given cell on this grid in unspecified order.
     */
    public ArrayList<Cell> getNeighbors(final Cell cell) {
        return getNeighborsURDL(cell);
    }

    /**
        Get neighbors of the given cell on this grid in up, right, down, left order.
     */
    public ArrayList<Cell> getNeighborsURDL(final Cell cell) {
        // PERF: VisualVM says this method is using 10% of total compute with
        // assertions disabled on Complex under RandomSolver.
        assert inBounds(cell);
        final ArrayList<Cell> neighbors = new ArrayList<>(4);
        if (cell.row - 1 >= 0) {
            neighbors.add(new Cell(cell.row - 1, cell.col)); // Up.
        }
        if (cell.col + 1 < getNumCols()) {
            neighbors.add(new Cell(cell.row, cell.col + 1)); // Right.
        }
        if (cell.row + 1 < getNumRows()) {
            neighbors.add(new Cell(cell.row + 1, cell.col)); // Down.
        }
        if (cell.col - 1 >= 0) {
            neighbors.add(new Cell(cell.row, cell.col - 1)); // Left.
        }
        assert neighbors.stream().allMatch(cell::isNeighbor);
        return neighbors;
    }
}
