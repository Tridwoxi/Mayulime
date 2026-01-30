package think.repr;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
    Rectangular two-dimensional grid. No minimum size. Grids store items of type "T",
    which have a "Cell" as their location.
 */
public final class Grid<T> {

    // Potential optimization: use a primitive boolean[] or int[] to avoid unboxing
    // costs. Since grids are used in hot loops, the speedup may be significant.
    private final ArrayList<T> items;
    private final int numRows;
    private final int numCols;

    public Grid(final ArrayList<T> items, final int numRows, final int numCols) {
        assert items.size() == numRows * numCols && numRows >= 0;
        this.items = new ArrayList<>(items);
        this.numRows = numRows;
        this.numCols = numCols;
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

    public boolean inBounds(final Cell cell) {
        return (
            cell.row() >= 0 &&
            cell.row() < numRows &&
            cell.col() >= 0 &&
            cell.col() < numCols
        );
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    public Stream<T> itemStream() {
        return items.stream();
    }

    public Stream<Cell> cellStream() {
        return IntStream.range(0, items.size()).mapToObj(index ->
            new Cell(index / numCols, index % numCols)
        );
    }

    public static <F, S, R> Grid<R> combine(
        final Grid<F> first,
        final Grid<S> second,
        final BiFunction<F, S, R> combiner
    ) {
        assert first.getNumRows() == second.getNumRows();
        assert first.getNumCols() == second.getNumCols();
        final ArrayList<R> results = new ArrayList<>(first.items.size());
        for (int index = 0; index < first.items.size(); index++) {
            results.add(combiner.apply(first.items.get(index), second.items.get(index)));
        }
        return new Grid<>(results, first.getNumRows(), first.getNumCols());
    }
}
