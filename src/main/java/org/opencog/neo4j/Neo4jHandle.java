package org.opencog.neo4j;

import org.neo4j.graphdb.*;

/**
 * Positive value is {@link org.neo4j.graphdb.Node}/vertex ID.
 * Negative value is {@link org.neo4j.graphdb.Relationship}/edge ID.
 */
public class Neo4jHandle implements Handle {

    public enum IdKind {
        VERTEX,
        EDGE
    }
    protected IdKind idKind;
    protected long vertexOrEdgeId;

    public Neo4jHandle() {
    }

    public Neo4jHandle(IdKind idKind, long vertexOrEdgeId) {
        this.idKind = idKind;
        this.vertexOrEdgeId = vertexOrEdgeId;
    }

    @Override
    public long getUuid() {
        return idKind == IdKind.VERTEX ? vertexOrEdgeId : -vertexOrEdgeId;
    }

    public IdKind getIdKind() {
        return idKind;
    }

    public long getVertexOrEdgeId() {
        return vertexOrEdgeId;
    }

    public static Neo4jHandle forNode(org.neo4j.graphdb.Node graphNode) {
//        final AtomType type = AtomType.forGraphLabel(graphNode.getLabels().iterator().next().name());
        return new Neo4jHandle(IdKind.VERTEX, graphNode.getId());
    }

    public static Neo4jHandle forLink(org.neo4j.graphdb.Node graphNode) {
//        final AtomType type = AtomType.forGraphLabel(graphNode.getLabels().iterator().next().name());
        return new Neo4jHandle(IdKind.VERTEX, graphNode.getId());
    }

    public static Neo4jHandle forLink(org.neo4j.graphdb.Relationship rel) {
//        final AtomType type = AtomType.forGraphLabel(graphNode.getLabels().iterator().next().name());
        return new Neo4jHandle(IdKind.EDGE, rel.getId());
    }

}
