package org.opencog.neo4j;

/**
 * Created by ceefour on 6/23/15.
 */
public class Neo4jNode extends Node {

    /**
     * Abbreviated to save space, because in majority of nodes this will be
     * the only property.
     */
    public static final String NODE_NAME = "nn";

    public Neo4jNode(AtomType type, String name) {
        super(type, name);
    }
}
