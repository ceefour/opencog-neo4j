package org.opencog.atomspace;

/**
 */
public enum GraphMapping {
    /**
     * OpenCog {@link Node} mapped as Neo4j vertex.
     */
    VERTEX,
    /**
     * OpenCog {@link Link} mapped as Neo4j hyperedge with two
     * relationships: {@code rdf_subject} and {@code rdf_object}.
     * For read support of <a href="http://lumen.lskk.ee.itb.ac.id/">Lumen database</a>,
     * mapping from Neo4j {@link org.neo4j.graphdb.Relationship}
     * is also supported, but without support for {@link Atom#getUuid()}.
     */
    BINARY_HYPEREDGE,
    /**
     * OpenCog {@link Link} mapped as Neo4j hyperedge with
     * zero or more {@code opencog_parameter} relationships,
     * and optionally other relationships (e.g. {@link AtomType#EVALUATION_LINK}.
     */
    HYPEREDGE
}
