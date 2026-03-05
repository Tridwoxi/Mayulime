package think.domain.model;

/**
    Things on a maze. This implementation contains only supported features; others like teleports
    and ice tiles are excluded.
 */
public enum Feature {
    BLANK(true),
    PLAYER_WALL(false),
    SYSTEM_WALL(false),
    CHECKPOINT(true);

    private final boolean passable;

    Feature(final boolean passable) {
        this.passable = passable;
    }

    public boolean isPassable() {
        return passable;
    }

    public boolean isBlocked() {
        return !passable;
    }
}
