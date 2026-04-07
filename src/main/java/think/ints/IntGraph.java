package think.ints;

import java.util.function.IntConsumer;

/**
    Adjacency list-backed weighted graph of primitive ints. The vertex set is 0-indexed and fixed
    at construction time. Talking about vertices that are not present throws an IOOBE. The field
    {@link #NO_EDGE_EXISTS} has value {@code Integer.MAX_VALUE} and setting an edge to have that as
    its weight is equivalent to removing it. If you have a bad habit of mixing directed and
    undirected operations, the reverse edge will be silently discarded.
 */
public final class IntGraph {

    public static final int NO_EDGE_EXISTS = Integer.MAX_VALUE;
    // adjacencyList[i] contains outgoing edges for vertex i at its even indices and weights for
    // those edges at the immediately following odd indices. This interleaved list representation
    // was chosen to take advantage of IntList.removeRange.
    private final IntList[] adjacencyList;

    public IntGraph(final int finalNumVertices) {
        // Adjacency list instead of adjacency matrix because grid graphs are sparse and have low
        // average degree.
        this.adjacencyList = new IntList[finalNumVertices];
        for (int index = 0; index < finalNumVertices; index += 1) {
            adjacencyList[index] = new IntList(0);
        }
    }

    public void setEdgeDirected(final int from, final int to, final int weight) {
        if (weight == NO_EDGE_EXISTS) {
            removeEdgeDirected(from, to);
            return;
        }
        final IntList adjacent = adjacencyList[from];
        final int index = searchEvenIndices(adjacent, to);
        if (index == IntList.NOT_FOUND) {
            adjacent.add(to);
            adjacent.add(weight);
        } else {
            adjacent.set(index + 1, weight);
        }
    }

    public void setEdgeUndirected(final int from, final int to, final int weight) {
        setEdgeDirected(from, to, weight);
        setEdgeDirected(to, from, weight);
    }

    public int getEdgeDirected(final int from, final int to) {
        final IntList adjacent = adjacencyList[from];
        final int index = searchEvenIndices(adjacent, to);
        if (index == IntList.NOT_FOUND) {
            return NO_EDGE_EXISTS;
        }
        return adjacent.get(index + 1);
    }

    public int getEdgeUndirected(final int from, final int to) {
        return getEdgeDirected(to, from);
    }

    public int removeEdgeDirected(final int from, final int to) {
        final IntList adjacent = adjacencyList[from];
        final int index = searchEvenIndices(adjacent, to);
        if (index == IntList.NOT_FOUND) {
            return NO_EDGE_EXISTS;
        }
        final int weight = adjacent.get(index + 1);
        adjacent.removeRange(index, index + 2);
        return weight;
    }

    public int removeEdgeUndirected(final int from, final int to) {
        final int weight = removeEdgeDirected(from, to);
        removeEdgeDirected(to, from);
        return weight;
    }

    public int[] getVertexChildren(final int vertex) {
        final IntList adjacent = adjacencyList[vertex];
        final int[] result = new int[adjacent.size() / 2];
        for (int neighbor = 0; neighbor < adjacent.size(); neighbor += 1) {
            result[neighbor] = adjacent.get(neighbor * 2);
        }
        return result;
    }

    public void forEachVertexChild(final int vertex, final IntConsumer action) {
        final IntList adjacent = adjacencyList[vertex];
        for (int neighbor = 0; neighbor < adjacent.size(); neighbor += 1) {
            action.accept(adjacent.get(neighbor * 2));
        }
    }

    public int getVertexDegree(final int vertex) {
        return adjacencyList[vertex].size() / 2;
    }

    public int getGraphNumVertices() {
        return adjacencyList.length;
    }

    private int searchEvenIndices(final IntList list, final int value) {
        for (int index = 0; index < list.size(); index += 2) {
            if (list.get(index) == value) {
                return index;
            }
        }
        return IntList.NOT_FOUND;
    }
}
