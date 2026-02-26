package think.graph;

import java.util.NoSuchElementException;
import java.util.SequencedSet;

/**
    A graph is a collection of vertex keys (K) which hold the vertex values (V) of the vertices
    they represent and are connected by edges (E). Graphs must not have duplicate keys or edges.

    All methods that accept a vertex key except for {@link Graph#containsVertexKey(K)}, {@link
    MutableVertexGraph#putVertex(K, V)}, and {@link MutableVertexGraph#removeVertex(K)} throw {@link
    NoSuchElementException} if the given vertex key is not contained in the graph.

    All methods that mutate the graph return if the graph changed as a result of the operation
    (this is consistent with the Java Collections Framework). If the graph is unable to change in
    the requested manner because doing so would violate an internal invariant, the method throws
    an {@link IllegalArgumentException}.

    All methods are required; no method will ever throw {@link UnsupportedOperationException}.

    The iteration order of returned collections is implementation-defined. It must not be left
    unspecified: implementations are to treat iteration order as part of the interface contract.
 */
public interface Graph<K, V, E> {
    boolean containsVertexKey(K vertexKey);
    V getVertexValue(K vertexKey);
    boolean containsEdge(K sourceKey, K destinationKey);
    E getEdge(K sourceKey, K destinationKey);
    SequencedSet<K> getChildren(K parentKey);
    SequencedSet<K> getParents(K childKey);
    SequencedSet<K> getAllVertexKeys();
    /**
        It would be nice (but it is not required) for implementations to declare the most specific
        return type they are able. For example, if "A implements Graph", then A::shallowCopy(void)
        should return "A", not "Graph", but returning "Graph" is acceptable.
     */
    Graph<K, V, E> shallowCopy();
    int getNumVertices();

    default int getNumChildren(final K parentKey) {
        return getChildren(parentKey).size();
    }

    default int getNumParents(final K childKey) {
        return getParents(childKey).size();
    }

    interface MutableVertexGraph<K, V, E> extends Graph<K, V, E> {
        boolean putVertex(K vertexKey, V vertexValue);
        /**
            In addition to ensuring this graph does not contain the given vertex, this method will
            ensure there are no incoming or outgoing edges of the given vertex. Hence adding an
            edge using {@link MutableEdgeGraph#putEdge(K, K, E)} and not removing it with {@link
            MutableEdgeGraph#removeEdge(K, K)} is not sufficient to ensure it is contained. This
            method must be supported even if the graph is not an {@link MutableEdgeGraph}.
         */
        boolean removeVertex(K vertexKey);
    }

    interface MutableEdgeGraph<K, V, E> extends Graph<K, V, E> {
        boolean putEdge(K sourceKey, K destinationKey, E edge);
        boolean removeEdge(K sourceKey, K destinationKey);
    }

    // @formatter:off
    interface MutableGraph<K, V, E> extends
        MutableVertexGraph<K, V, E>,
        MutableEdgeGraph<K, V, E> {}
}
