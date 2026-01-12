package think.repr;

import java.util.ArrayList;

/**
    2D grid of cells represented in 1D for performance.
 */
public final class Grid {

    private final ArrayList<Boolean> cells;
    private final int boundI;
    private final int boundJ;

    public Grid(final ArrayList<Boolean> cells, final int boundI, final int boundJ) {
        assert cells.size() == boundI * boundJ;
        this.cells = new ArrayList<>(cells);
        this.boundI = boundI;
        this.boundJ = boundJ;
    }

    public boolean getCell(final int i, final int j) {
        assert i >= 0 && i < boundI && j >= 0 && j < boundJ;
        return cells.get(i * boundJ + j);
    }

    public boolean getCell(final Point point) {
        return getCell(point.i(), point.j());
    }

    public int getBoundI() {
        return boundI;
    }

    public int getBoundJ() {
        return boundJ;
    }
}
