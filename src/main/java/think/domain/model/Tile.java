package think.domain.model;

/**
    Things on a maze. This implementation contains only supported tiles; others like teleports
    and ice tiles are excluded.
 */
public enum Tile {
    BLANK(true),
    PLAYER_WALL(false),
    SYSTEM_WALL(false),
    WAYPOINT(true);

    private final boolean passable;

    Tile(final boolean passable) {
        this.passable = passable;
    }

    public boolean isPassable() {
        return passable;
    }

    public boolean isBlocked() {
        return !passable;
    }
}
