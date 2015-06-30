package org.opencog.neo4j;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListenableFuture;
import org.neo4j.graphdb.*;
import org.opencog.atomspace.*;
import org.opencog.atomspace.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implements {@link GraphBackingStore} using Neo4j {@link org.neo4j.graphdb.GraphDatabaseService}.
 */
public class Neo4jGraphBackingStore implements GraphBackingStore {

    protected GraphDatabaseService db;

    @Override
    public Optional<Link> getLink(AtomType type, List<Handle> handleSeq) {
        if (type.getGraphMapping() == GraphMapping.EDGE) {
            Preconditions.checkArgument(handleSeq.size() == 2,
                    "Type " + type + " graph mapping is EDGE which supports only 2 outgoing set, but " + handleSeq.size() + " given");
            final String cypher = String.format("MATCH (a) -[r:%s]-> (b) WHERE id(a) = {a_id} AND id(b) = {b_id} RETURN r",
                    type.getGraphLabel());
            final long a_id = ((Neo4jHandle) handleSeq.get(0)).getVertexOrEdgeId();
            final long b_id = ((Neo4jHandle) handleSeq.get(1)).getVertexOrEdgeId();
            final Result result = db.execute(cypher, ImmutableMap.of("a_id", a_id,
                    "b_id", b_id));
            if (result.hasNext()) {
                final Map<String, Object> row = result.next();
//                final Neo4jHandle vertex1 = new Neo4jHandle(Neo4jHandle.IdKind.VERTEX, a_id);
//                final Neo4jHandle vertex2 = new Neo4jHandle(Neo4jHandle.IdKind.VERTEX, b_id);
                return Optional.of(new Neo4jLink(type, ImmutableList.copyOf(handleSeq), (Relationship) row.get("r")));
            } else {
                return Optional.empty();
            }
        } else if (type.getGraphMapping() == GraphMapping.HYPEREDGE) {
            final List<EdgeMapping> edgeMappings = type.getEdgeMappings(handleSeq.size());
            final List<String> matches = new ArrayList<>();
            final List<String> wheres = new ArrayList<>();
            final List<String> returns = new ArrayList<>();
            returns.add("l");
            for (int i = 0; i < handleSeq.size(); i++) {
                final Neo4jHandle handle = (Neo4jHandle) handleSeq.get(i);
                matches.add( String.format("(l) %s (o%d)", i, edgeMappings.get(i).toCypher(null), i) );
                wheres.add( String.format("id(o%d) = %d", i, handle.getVertexOrEdgeId()) );
//                returns.add("o" + i);
            }
            final String cypher = "MATCH " + Joiner.on(",\n  ").join(matches) + "\nWHERE " + Joiner.on(" AND ").join(wheres) +
                    "\nRETURN " + Joiner.on(", ").join(returns);
            final Result result = db.execute(cypher);
            if (result.hasNext()) {
                final Map<String, Object> row = result.next();
                return Optional.of(new Neo4jLink(type, ImmutableList.copyOf(handleSeq), (Relationship) row.get("l")));
            } else {
                return Optional.empty();
            }
        } else {
            throw new IllegalArgumentException("Cannot get link for type " + type + " with graph mapping " + type.getGraphMapping());
        }
    }

    @Override
    public Optional<org.opencog.atomspace.Node> getNode(AtomType type, String name) {
        final Optional<org.neo4j.graphdb.Node> graphNode = Optional.ofNullable(
                db.findNode(DynamicLabel.label(type.getGraphLabel()), Neo4jNode.NODE_NAME, name));
        return graphNode.map(it -> new Neo4jNode(type, name));
    }

    @Override
    public Optional<Atom> getAtom(Handle handle) {
        Preconditions.checkArgument(handle instanceof Neo4jHandle, "handle must be a Neo4jHandle");
        final Neo4jHandle graphHandle = (Neo4jHandle) handle;
        switch (graphHandle.getIdKind()) {
            case VERTEX:
                try {
                    final org.neo4j.graphdb.Node graphNode = db.getNodeById(graphHandle.getVertexOrEdgeId());
                    final Label graphLabel = graphNode.getLabels().iterator().next();
                    if (graphLabel.name().endsWith("Link")) {
                        final AtomType type = AtomType.forGraphLabel(graphLabel.name());
                        return Optional.of(new Neo4jLink(type, graphNode));
                    } else {
                        final AtomType type = AtomType.forGraphLabel(graphLabel.name());
                        return Optional.of(new Neo4jNode(type, (String) graphNode.getProperty(Neo4jNode.NODE_NAME)));
                    }
                } catch (NotFoundException e) {
                    return Optional.empty();
                }
            case EDGE:
                try {
                    final Relationship rel = db.getRelationshipById(graphHandle.getVertexOrEdgeId());
                    final AtomType type = AtomType.forGraphLabel(rel.getType().name());
                    return Optional.of(new Neo4jLink(type, rel));
                } catch (NotFoundException e) {
                    return Optional.empty();
                }

        }
        return null;
    }

    @Override
    public List<Handle> getIncomingSet(Handle handle) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String storeAtom(Handle handle) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String loadType(String atomTable, AtomType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void barrier() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isTypeIgnored(AtomType type) {
        return false;
    }

    @Override
    public boolean isAtomIgnored(Handle handle) {
        return !(handle instanceof Neo4jHandle);
    }

    @Override
    public ListenableFuture<Optional<Node>> getNodeAsync(AtomType type, String name) {
        throw new UnsupportedOperationException();
    }
}
