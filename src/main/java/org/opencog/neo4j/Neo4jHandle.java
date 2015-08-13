package org.opencog.neo4j;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.opencog.atomspace.Atom;
import org.opencog.atomspace.AtomType;
import org.opencog.atomspace.GraphMapping;
import org.opencog.atomspace.Handle;

/**
 * Positive value is {@link org.neo4j.graphdb.Node}/vertex ID.
 * Negative value is {@link org.neo4j.graphdb.Relationship}/edge ID.
 */
public class Neo4jHandle implements Handle {

    public enum IdKind {
        VERTEX,
        EDGE
    }
    private long uuid;
    protected int graphId;
    protected IdKind idKind;
    protected long vertexOrEdgeId;

    public Neo4jHandle() {
    }

    public Neo4jHandle(long uuid) {
        this.uuid = uuid;
//        // most significant bit (bit 63): 0 = VERTEX, 1 = EDGE
//        // bit 48-62: graph ID (reserved)
//        // bit 0-47: vertex/edge ID
//        this.idKind = (uuid & 0x80000000) == 0 ? IdKind.VERTEX : IdKind.EDGE;
//        this.graphId = (int) ((uuid & 0x7f000000) >> 48);
//        this.vertexOrEdgeId = uuid & 0x00ffffff;
    }

//    public Neo4jHandle(IdKind idKind, long vertexOrEdgeId) {
//        this.graphId = 0;
//        this.idKind = idKind;
//        this.vertexOrEdgeId = vertexOrEdgeId;
//    }

    @Override
    public long getUuid() {
        return this.uuid;
//        long uuid = vertexOrEdgeId |
//                (((long) graphId) << 48) |
//                (idKind == IdKind.VERTEX ? 0x80000000 : 0);
//        return uuid;
    }

//    public IdKind getIdKind() {
//        return idKind;
//    }
//
//    public long getVertexOrEdgeId() {
//        return vertexOrEdgeId;
//    }

    public static Neo4jHandle forNode(org.neo4j.graphdb.Node graphNode) {
//        final AtomType type = AtomType.forGraphLabel(graphNode.getLabels().iterator().next().name());
//        return new Neo4jHandle(IdKind.VERTEX, graphNode.getId());
        return new Neo4jHandle((long) graphNode.getProperty(org.opencog.atomspace.Node.GID_PROPERTY));
    }

    public static Neo4jHandle forLink(org.neo4j.graphdb.Node graphNode) {
//        final AtomType type = AtomType.forGraphLabel(graphNode.getLabels().iterator().next().name());
//        return new Neo4jHandle(IdKind.VERTEX, graphNode.getId());
        return new Neo4jHandle((long) graphNode.getProperty(org.opencog.atomspace.Node.GID_PROPERTY));
    }

    public static Neo4jHandle forLink(org.neo4j.graphdb.Relationship rel) {
//        final AtomType type = AtomType.forGraphLabel(graphNode.getLabels().iterator().next().name());
//        return new Neo4jHandle(IdKind.EDGE, rel.getId());
        return new Neo4jHandle((long) rel.getProperty(org.opencog.atomspace.Node.GID_PROPERTY));
    }

    /**
     * Since a graphNode can be either an AtomSpace {@link Neo4jNode} or {@link Neo4jLink},
     * it may return either one.
     * @param graphNode
     * @return
     */
    public Atom toAtom(org.neo4j.graphdb.Node graphNode) {
        final AtomType atomType = AtomType.forGraphLabel(graphNode.getLabels().iterator().next().name());
        if (atomType.getGraphMapping() == GraphMapping.VERTEX) {
            return new Neo4jNode(graphNode); //atomType, (String) graphNode.getProperty(Neo4jNode.NODE_NAME));
        } else {
            return new Neo4jLink(atomType, graphNode);
        }
    }

    public Neo4jLink toLink(Relationship graphRel) {
        final AtomType atomType = AtomType.forGraphLabel(graphRel.getType().name());
        return new Neo4jLink(atomType, graphRel);
    }

}
