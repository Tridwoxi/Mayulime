package think.graph;

import java.util.ArrayList;
import java.util.Optional;

/**
    Interface for a weighted directed graph of vertices (V) connected by edges (E). The semantics
    of {@code add*}, {@code remove*}, and {@code contains*} are equivalent to those established in
    the Java Collections Framework. However, a Graph is not a Set, so simply because something was
    added and not explicitly removed does not imply it is contained.
 */
public interface Graph<V, E> {
    /**
        Add the vertex to this graph if it does not already exist. Return true iff the vertex was
        not present in this graph immediately prior to this operation.
     */
    boolean addVertex(V vertex);
    /**
        Remove the vertex from this graph if it exists. Remove all incoming and outgoing edges of
        the vertex. Return true iff the vertex was present in this graph immediately prior to this
        operation.
     */
    boolean removeVertex(V vertex);
    /**
        Return true iff the vertex is present in this graph.
     */
    boolean containsVertex(V vertex);
    /**
        Set the value of the directed edge from source to destination. Return true iff the given
        edge value is different from what was previously stored, or no value was previously stored.
        Throw a NoSuchElementException if source or destination is not present in this graph.
     */
    boolean setEdge(V source, V destination, E edge);
    /**
        Return the value of the directed edge from source to destination, or an empty Optional if
        no such edge exists. Throw a NoSuchElementException if source or destination is not
        present in this graph.
     */
    Optional<E> getEdge(V source, V destination);
    /**
        Remove the directed edge from source to destination if it exists. Return true iff the edge
        was present in this graph immediately prior to this operation. Do not remove the vertices
        this edge is incident upon. Throw a NoSuchElementException if source or destination is not
        present in this graph.
     */
    boolean removeEdge(V source, V destination);
    /**
        Get all vertices in this graph in an implementation-defined order.
     */
    ArrayList<V> getAllVertices();
    /**
        Get all vertices in this graph that have an edge from the given vertex; all verticies that
        the given one can reach. Throw a NoSuchElementException the given vertex is not present in
        this graph.
     */
    ArrayList<V> getChildren(V vertex);
    /**
        Get all vertices in this graph that have an edge to the given vertex; all verticies that
        can reach the given one. Throw a NoSuchElementException the given vertex is not present in
        this graph.
     */
    ArrayList<V> getParents(V vertex);
    /**
        Create a shallow copy of this graph. Implementations should declare the exact type of
        returned graph.
     */
    Graph<V, E> shallowCopy();

    /**
        Calculate the out-degree of the given vertex.
     */
    default int getNumChildren(final V vertex) {
        return getChildren(vertex).size();
    }

    /**
        Calculate the in-degree of the given vertex.
     */
    default int getNumParents(final V vertex) {
        return getParents(vertex).size();
    }
}
