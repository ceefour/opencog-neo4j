package org.opencog.neo4j;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.Node;
import org.opencog.atomspace.AtomType;
import org.opencog.atomspace.Handle;
import org.opencog.atomspace.Link;

import javax.annotation.Nullable;
import java.util.List;

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
     * This will iterate the Link node's outgoing relationships.
     * @param type
     * @param graphNode
     * @see Neo4jBackingStore#toAtom(Node)
     */
    public Neo4jLink(AtomType type, org.neo4j.graphdb.Node graphNode) {
        super((long) graphNode.getProperty(Neo4jNode.GID_PROPERTY),
                type, FluentIterable.from(graphNode.getRelationships(Direction.OUTGOING)).transform(it -> Neo4jBackingStore.forNode(it.getEndNode())).toList());
        this.graphNode = graphNode;
    }

    public Neo4jLink(AtomType type, org.neo4j.graphdb.Relationship graphRel) {
        super((long) graphRel.getProperty(Neo4jNode.GID_PROPERTY),
                type, ImmutableList.of(Neo4jBackingStore.forNode(graphRel.getStartNode()),
            Neo4jBackingStore.forNode(graphRel.getEndNode())));
        this.graphRel = graphRel;
    }

    /**
     * If you already have a {@link List} of {@link Handle}s, this will be more efficient than {@link #Neo4jLink(AtomType, Relationship)}.
     * @param type
     * @param outgoingSet
     * @param graphRel
     */
    public Neo4jLink(AtomType type, ImmutableList<Handle> outgoingSet, Relationship graphRel) {
        super((long) graphRel.getProperty(Neo4jNode.GID_PROPERTY), type, outgoingSet);
        this.graphRel = graphRel;
    }

    public Neo4jLink(AtomType type, ImmutableList<Handle> outgoingSet, Node graphNode) {
        super((long) graphNode.getProperty(Neo4jNode.GID_PROPERTY),
                type, outgoingSet);
        this.graphNode = graphNode;
    }

    @Override
    public String toString() {
        return "Neo4jLink{" + getUuid() + ":" + getType() +
                ", outgoingSet" + getOutgoingSet() +
                ", graphNode=" + graphNode +
                ", graphRel=" + graphRel +
                '}';
    }
}
