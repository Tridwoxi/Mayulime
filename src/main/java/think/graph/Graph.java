package think.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
    A graph is a collection of vertices (V) connected by edges (E). Graphs are always directed.

    All methods that accept a vertex except for {@link #containsVertex(V)}, {@link
    MutableVertexGraph#addVertex(V)}, and {@link MutableVertexGraph#removeVertex(V)} throw {@link
    NoSuchElementException} if the given vertex is not contained in the graph.

    All methods that mutate the graph return if the graph changed as a result of the operation
    (this is consistent with the Java Collections Framework). If the graph is unable to change in
    the requested manner because doing so would violate an internal invariant, the method throws
    an {@link IllegalArgumentException}.

    All methods are required; no method will ever throw {@link UnsupportedOperationException}.
 */
public interface Graph<V, E> {
    boolean containsVertex(V vertex);
    Optional<E> getEdge(V source, V destination);
    List<V> getChildren(V vertex);
    ArrayList<V> getParents(V vertex);
    /**
        The return order of this method is implementation-defined. The implementation may leave
        the return order unspecified.
     */
    List<V> getAllVertices();
    /**
        Implementations should declare the most specific return type they are able. For example,
        if "A implements Graph", then A::shallowCopy(void) should return "A", not "Graph".
     */
    Graph<V, E> shallowCopy();

    default int getNumChildren(final V vertex) {
        return getChildren(vertex).size();
    }

    default int getNumParents(final V vertex) {
        return getParents(vertex).size();
    }

    interface MutableVertexGraph<V, E> extends Graph<V, E> {
        boolean addVertex(V vertex);
        /**
            In addition to ensuring this graph does not contain the given vertex, this method will
            ensure there are no incoming or outgoing edges of the given vertex. Hence adding an
            edge using {@link MutableEdgeGraph#setEdge(V, V, E)} and not removing it with {@link
            MutableEdgeGraph#removeEdge(V, V)} is not sufficient to ensure it exists. This method
            must be supported even if the graph is not an {@link MutableEdgeGraph}.
         */
        boolean removeVertex(V vertex);

        /**
            Replace the given vertex. If the graph already contains the replacement and the
            replacement is not equal to the placement, it is an IllegalArgumentException.
         */
        boolean replaceVertex(V previous, V replacement);
    }

    interface MutableEdgeGraph<V, E> extends Graph<V, E> {
        /**
            Ensure the referenced edge has the given value. If no edge exists, it will be added.
         */
        boolean setEdge(V source, V destination, E edge);
        boolean removeEdge(V source, V destination);
    }

    interface MutableGraph<V, E> extends MutableVertexGraph<V, E>, MutableEdgeGraph<V, E> {}
}
