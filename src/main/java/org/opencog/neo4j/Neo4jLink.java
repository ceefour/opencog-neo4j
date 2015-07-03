package org.opencog.neo4j;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.Node;
import org.opencog.atomspace.AtomType;
import org.opencog.atomspace.Handle;
import org.opencog.atomspace.Link;

import javax.annotation.Nullable;

/**
 * Usually backed by Neo4j {@link org.neo4j.graphdb.Node} (hypernode),
 * except for {@link AtomType#INHERITANCE_LINK} and {@link AtomType#MEMBER_LINK}.
 */
public class Neo4jLink extends Link {
    @Nullable
    private Node graphNode;
    @Nullable
    private Relationship graphRel;

//    public Neo4jLink(AtomType type) {
//        super(type);
//    }

    /**
     *
     * @param type
     * @param graphNode
     * @see Neo4jHandle#toAtom(Node)
     */
    public Neo4jLink(AtomType type, org.neo4j.graphdb.Node graphNode) {
        super(type, FluentIterable.from(graphNode.getRelationships(Direction.OUTGOING)).transform(it -> Neo4jHandle.forNode(it.getEndNode())).toList());
        this.graphNode = graphNode;
    }

    public Neo4jLink(AtomType type, org.neo4j.graphdb.Relationship graphRel) {
        super(type, ImmutableList.of(Neo4jHandle.forNode(graphRel.getStartNode()),
            Neo4jHandle.forNode(graphRel.getEndNode())));
        this.graphRel = graphRel;
    }

    public Neo4jLink(AtomType type, ImmutableList<Handle> outgoingSet, Relationship graphRel) {
        super(type, outgoingSet);
        this.graphRel = graphRel;
    }

    public Neo4jLink(AtomType type, ImmutableList<Handle> outgoingSet, Node graphNode) {
        super(type, outgoingSet);
        this.graphNode = graphNode;
    }

    @Override
    public String toString() {
        return "Neo4jLink{" + getType() +
                ", outgoingSet" + getOutgoingSet() +
                ", graphNode=" + graphNode +
                ", graphRel=" + graphRel +
                '}';
    }
}
