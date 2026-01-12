package think.repr;

/**
    2D grid of cells represented in 1D for performance. Points are packed longs.
 */
public final class Grid {

    private final boolean[] cells;
    private final int boundI;
    private final int boundJ;

    public Grid(boolean[] cells, int boundI, int boundJ) {
        assert cells.length == boundI * boundJ;
        this.cells = cells.clone();
        this.boundI = boundI;
        this.boundJ = boundJ;
    }

    public boolean getCell(int i, int j) {
        assert i >= 0 && i < boundI && j >= 0 && j < boundJ;
        return cells[i * boundJ + j];
    }

    public boolean getCell(long packed) {
        return getCell(unpackI(packed), unpackJ(packed));
    }

    public int getBoundI() {
        return boundI;
    }

    public int getBoundJ() {
        return boundJ;
    }

    public static long pack(int i, int j) {
        return ((long) i << 32) | j;
    }

    public static int unpackI(long packed) {
        return (int) (packed >> 32);
    }

    public static int unpackJ(long packed) {
        return (int) packed;
    }

    public static boolean isNeighbor(long a, long b) {
        return (
            Math.abs(unpackI(a) - unpackI(b)) + Math.abs(unpackJ(a) - unpackJ(b)) == 1
        );
    }
}
