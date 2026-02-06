package think.repr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import think.tools.Iteration;
import think.tools.Structures.Pair;

/**
    Rectangular two-dimensional grid. No minimum size. Grids store items of type "T",
    which have a "Cell" as their location.
 */
public final class Grid<T> {

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
        this(Iteration.filledArray(supplier, numRows * numCols), numRows, numCols);
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

    public <C extends Collection<Cell>> C where(
        final Predicate<T> predicate,
        final C emptyCollection
    ) {
        assert emptyCollection.isEmpty();
        where(predicate).forEachOrdered(emptyCollection::add);
        return emptyCollection;
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
}
