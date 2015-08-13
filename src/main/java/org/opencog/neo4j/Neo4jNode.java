package org.opencog.neo4j;

import org.opencog.atomspace.AtomType;
import org.opencog.atomspace.Node;

/**
 * Created by ceefour on 6/23/15.
 */
public class Neo4jNode extends Node {

    /**
     * Abbreviated to save space, because in majority of nodes this will be
     * the only property.
     */
    public static final String NODE_NAME = "nn";

    @Deprecated
    public Neo4jNode(AtomType type, String name) {
        super(type, name);
    }

    public Neo4jNode(long uuid, AtomType type, String name) {
        super(uuid, type, name);
    }

    public Neo4jNode(org.neo4j.graphdb.Node graphNode) {
        super((long) graphNode.getProperty(GID_PROPERTY),
                AtomType.forGraphLabel(graphNode.getLabels().iterator().next().name()),
                (String) graphNode.getProperty(NODE_NAME));
    }
}
