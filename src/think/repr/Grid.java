package think.repr;

import java.util.ArrayList;

/**
    2D grid of cells represented in 1D for performance. Points are packed longs.
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

    public boolean getCell(final long packed) {
        return getCell(unpackI(packed), unpackJ(packed));
    }

    public int getBoundI() {
        return boundI;
    }

    public int getBoundJ() {
        return boundJ;
    }

    public static long pack(final int i, final int j) {
        return ((long) i << 32) | j;
    }

    public static int unpackI(final long packed) {
        return (int) (packed >> 32);
    }

    public static int unpackJ(final long packed) {
        return (int) packed;
    }

    public static boolean isNeighbor(final long a, final long b) {
        return (
            Math.abs(unpackI(a) - unpackI(b)) + Math.abs(unpackJ(a) - unpackJ(b)) == 1
        );
    }
}
