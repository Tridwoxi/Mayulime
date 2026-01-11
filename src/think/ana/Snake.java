package think.ana;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javafx.application.Platform;
import think.repr.Point;
import think.repr.Problem;

// TODO: what's even happening here??
public final class Snake {

    private final Problem board;
    private final HashSet<Point> rubbers;

    public Snake(final Problem board, final HashSet<Point> rubbers) {
        this.board = board;
        this.rubbers = rubbers;
    }

    public ArrayList<Point> bfs(final Point source, final HashSet<Point> destinations) {
        if (destinations.contains(source)) {
            System.err.println("Internal error: can't path to yourself.");
            Platform.exit();
        }
        final ArrayDeque<Point> queue = new ArrayDeque<>();
        final HashSet<Point> visited = new HashSet<>();
        final HashMap<Point, Point> prev = new HashMap<>();
        visited.add(source);
        queue.add(source);
        while (!queue.isEmpty()) {
            final Point current = queue.removeFirst();
            for (final Point neighbor : current.getNeighbors(board)) {
                if (!board.isOpen(neighbor) || visited.contains(neighbor)) {
                    continue;
                }
                visited.add(neighbor);
                prev.put(neighbor, current);
                if (destinations.contains(neighbor)) {
                    return buildPath(prev, source, neighbor);
                }
                queue.add(neighbor);
            }
        }
        return new ArrayList<>();
    }

    private ArrayList<Point> buildPath(
        final HashMap<Point, Point> prev,
        final Point source,
        final Point destination
    ) {
        final ArrayList<Point> reversed = new ArrayList<>();
        Point current = destination;
        while (!current.equals(source)) {
            reversed.add(current);
            current = prev.get(current);
        }
        final ArrayList<Point> path = new ArrayList<>(reversed.size());
        for (int i = reversed.size() - 1; i >= 0; i--) {
            path.add(reversed.get(i));
        }
        return path;
    }
}
