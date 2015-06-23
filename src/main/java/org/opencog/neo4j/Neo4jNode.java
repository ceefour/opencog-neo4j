package org.opencog.neo4j;

/**
 * Created by ceefour on 6/23/15.
 */
public class Neo4jNode extends Node {

    public static final String NODE_NAME = "nodeName";

    public Neo4jNode(AtomType type, String name) {
        super(type, name);
    }
}
