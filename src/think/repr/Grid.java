package think.repr;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import think.ana.Tools;

/**
    Rectangular 2D grid of cells. Implemented as 1D grid for less pointer chasing
    (silly micro-optimization). Potential performance improvement: turn everything into
    arrays to avoid cost of boxing primitive types.
 */
public final class Grid<T> {

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
        final int row = cell.row();
        final int col = cell.col();
        assert row >= 0 && row < rowBound && col >= 0 && col < colBound;
        return cells.get(row * colBound + col);
    }

    public void setCell(final Cell cell, final T value) {
        final int row = cell.row();
        final int col = cell.col();
        assert row >= 0 && row < rowBound && col >= 0 && col < colBound;
        cells.set(row * colBound + col, value);
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

    public T getNth(final int index) {
        assert index >= 0 && index < cells.size();
        return cells.get(index);
    }

    public void setNth(final int index, final T value) {
        assert index >= 0 && index < cells.size();
        cells.set(index, value);
    }

    public int getSize() {
        return cells.size();
    }

    public Stream<T> itemSteam() {
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
        assert first.getSize() == second.getSize();
        final ArrayList<R> cells = new ArrayList<>(first.getSize());
        for (int index = 0; index < first.getSize(); index++) {
            cells.add(combiner.apply(first.getNth(index), second.getNth(index)));
        }
        return new Grid<>(cells, first.getRowBound(), first.getColBound());
    }
}
