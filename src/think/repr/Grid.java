package think.repr;

import java.util.ArrayList;
import think.ana.Tools;

/**
    Rectangular 2D grid of cells represented in 1D for performance. Useless, really,
    since we're boxing things anyway, but it is nice to have a grid of sorts.
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

    public T getCell(final Point point) {
        final int i = point.i();
        final int j = point.j();
        assert i >= 0 && i < boundI && j >= 0 && j < boundJ;
        return cells.get(i * boundJ + j);
    }

    public int getBoundI() {
        return boundI;
    }

    public int getBoundJ() {
        return boundJ;
    }
}
