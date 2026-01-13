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
    private final int boundI;
    private final int boundJ;

    public Grid(final ArrayList<T> cells, final int boundI, final int boundJ) {
        assert cells.size() == boundI * boundJ;
        this.cells = new ArrayList<>(cells);
        this.boundI = boundI;
        this.boundJ = boundJ;
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
        final int i = cell.i();
        final int j = cell.j();
        assert i >= 0 && i < boundI && j >= 0 && j < boundJ;
        return cells.get(i * boundJ + j);
    }

    public void setCell(final Cell cell, final T value) {
        final int i = cell.i();
        final int j = cell.j();
        assert i >= 0 && i < boundI && j >= 0 && j < boundJ;
        cells.set(i * boundJ + j, value);
    }

    public boolean containsCell(final Cell cell) {
        return cell.i() >= 0 && cell.i() < boundI && cell.j() >= 0 && cell.j() < boundJ;
    }

    public int getBoundI() {
        return boundI;
    }

    public int getBoundJ() {
        return boundJ;
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

    public Stream<T> elemStream() {
        return cells.stream();
    }

    public Stream<Cell> cellStream() {
        return IntStream.range(0, cells.size()).mapToObj(k ->
            new Cell(k / boundJ, k % boundJ)
        );
    }

    public static <A, B, C> Grid<C> combine(
        Grid<A> a,
        Grid<B> b,
        BiFunction<A, B, C> combiner
    ) {
        assert a.getBoundI() == b.getBoundI();
        assert a.getBoundJ() == b.getBoundJ();
        assert a.getSize() == b.getSize();
        final ArrayList<C> cells = new ArrayList<>(a.getSize());
        for (int i = 0; i < a.getSize(); i++) {
            cells.add(combiner.apply(a.getNth(i), b.getNth(i)));
        }
        return new Grid<>(cells, a.getBoundI(), a.getBoundJ());
    }
}
