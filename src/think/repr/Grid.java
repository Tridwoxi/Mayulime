package think.repr;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import think.ana.Tools;

/**
    Rectangular two-dimensional grid. No minimum size. Grids store "T"s, which have a
    "Cell" as their location.
 */
public final class Grid<T> {

    // Potential optimization: use a primitive boolean[] or int[] to avoid unboxing
    // costs. Since grids are used in hot loops, the speedup may be significant.
    private final ArrayList<T> cells;
    private final int rowBound;
    private final int colBound;

    public Grid(final ArrayList<T> cells, final int rowBound, final int colBound) {
        assert cells.size() == rowBound * colBound;
        this.cells = new ArrayList<>(cells);
        this.rowBound = rowBound;
        this.colBound = colBound;
    }

    public Grid(final ArrayList<ArrayList<T>> cells) {
        this(
            Tools.flatten(cells),
            cells.size(),
            cells.isEmpty() ? 0 : cells.getFirst().size()
        );
        assert Tools.rectangular(cells);
    }

    public T getCell(final Cell cell) {
        assert containsCell(cell);
        return cells.get(cell.row() * colBound + cell.col());
    }

    public void setCell(final Cell cell, final T value) {
        assert containsCell(cell);
        cells.set(cell.row() * colBound + cell.col(), value);
    }

    public void swapCells(final Cell first, final Cell second) {
        assert containsCell(first) && containsCell(second);
        final int firstIndex = first.row() * colBound + first.col();
        final int secondIndex = second.row() * colBound + second.col();
        final T firstHolder = cells.get(firstIndex);
        cells.set(firstIndex, cells.get(secondIndex));
        cells.set(secondIndex, firstHolder);
    }

    public boolean containsCell(final Cell cell) {
        return (
            cell.row() >= 0 &&
            cell.row() < rowBound &&
            cell.col() >= 0 &&
            cell.col() < colBound
        );
    }

    public int getRowBound() {
        return rowBound;
    }

    public int getColBound() {
        return colBound;
    }

    public Stream<T> itemStream() {
        return cells.stream();
    }

    public Stream<Cell> cellStream() {
        return IntStream.range(0, cells.size()).mapToObj(index ->
            new Cell(index / colBound, index % colBound)
        );
    }

    public static <F, S, R> Grid<R> combine(
        final Grid<F> first,
        final Grid<S> second,
        final BiFunction<F, S, R> combiner
    ) {
        assert first.getRowBound() == second.getRowBound();
        assert first.getColBound() == second.getColBound();
        final ArrayList<R> results = new ArrayList<>(first.cells.size());
        for (int index = 0; index < first.cells.size(); index++) {
            results.add(combiner.apply(first.cells.get(index), second.cells.get(index)));
        }
        return new Grid<>(results, first.getRowBound(), first.getColBound());
    }
}
