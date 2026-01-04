package think.repr;

/**
    What is at a position in a pathery board? The association is to pair teleports and
    to order checkpoints, and need not be dense, but must be uniquely identifying.
 */
public record Cell(CellType type, int association) {
    public static enum CellType {
        BRICK,
        CHECKPOINT,
        NOTHING,
        TELEPORT_IN,
        TELEPORT_OUT,
    }
}
