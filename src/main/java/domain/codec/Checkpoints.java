package domain.codec;

import domain.codec.Parser.BadMapCodeException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import think.graph.impl.GridGraph.Cell;

/**
    Keep track of checkpoint cells in traversal order.
 */
final class Checkpoints {

    private record OrderedCell(Cell cell, int order) {}

    private static final Cell SENTINEL = new Cell(-1, -1);
    private final SortedSet<OrderedCell> checkpoints;
    private final Set<Integer> observedOrders;
    private Cell start;
    private Cell finish;

    Checkpoints() {
        this.checkpoints = new TreeSet<>(Comparator.comparingInt(OrderedCell::order));
        this.observedOrders = new HashSet<>();
        this.start = SENTINEL;
        this.finish = SENTINEL;
    }

    void addCheckpoint(final Cell cell, final int order) throws BadMapCodeException {
        Safety.require(observedOrders.add(order));
        checkpoints.add(new OrderedCell(cell, order));
    }

    void setStart(final Cell cell) throws BadMapCodeException {
        Safety.require(start.equals(SENTINEL));
        start = cell;
    }

    void setFinish(final Cell cell) throws BadMapCodeException {
        Safety.require(finish.equals(SENTINEL));
        finish = cell;
    }

    List<Cell> toOrderedPath() throws BadMapCodeException {
        Safety.require(!start.equals(SENTINEL) && !finish.equals(SENTINEL));
        final List<Cell> ordered = new ArrayList<>(checkpoints.size() + 2);
        ordered.add(start);
        ordered.addAll(checkpoints.stream().map(OrderedCell::cell).toList());
        ordered.add(finish);
        return ordered;
    }
}
