package org.opencog.neo4j;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.neo4j.graphdb.*;
import org.opencog.atomspace.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements {@link GraphBackingStore} using Neo4j {@link org.neo4j.graphdb.GraphDatabaseService}.
 */
@Transactional
public class Neo4jGraphBackingStore extends GraphBackingStoreBase {

    private static final Logger log = LoggerFactory.getLogger(Neo4jGraphBackingStore.class);

    @Inject
    private PlatformTransactionManager txMgr;
    protected GraphDatabaseService db;
    private TransactionTemplate txTemplate;

    public Neo4jGraphBackingStore(GraphDatabaseService db) {
        this.db = db;
    }

    @PostConstruct
    public void init() {
        txTemplate = new TransactionTemplate(txMgr);
    }

    protected Optional<Link> doGetLink(AtomType type, List<Handle> handleSeq) {
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

    /**
     * The real workhorse for all other primitive methods.
     * @param reqs
     * @return
     */
    @Override
    public ListenableFuture<List<Atom>> getAtomsAsync(List<AtomRequest> reqs) {
        try (final Transaction tx = db.beginTx()) {
            final ArrayList<Atom> result = new ArrayList<>();
            // TODO: use cypher to do bulk find
            reqs.forEach(req -> {
                switch (req.getKind()) {
                    case UUID:
                        final Neo4jHandle handle = new Neo4jHandle(req.getUuid());
                        switch (handle.getIdKind()) {
                            case VERTEX:
                                try {
                                    final org.neo4j.graphdb.Node graphNode = db.getNodeById(handle.getVertexOrEdgeId());
                                    final Atom atom = handle.toAtom(graphNode);
                                    log.debug("Converted {} to {}", handle, atom);
                                    result.add(atom);
                                } catch (NotFoundException e) {
                                    log.trace("Node {} not found for request {}", handle.getVertexOrEdgeId(), req);
                                    result.add(null);
                                }
                                break;
                            case EDGE:
                                try {
                                    final Relationship graphRel = db.getRelationshipById(handle.getVertexOrEdgeId());
                                    final Neo4jLink link = handle.toLink(graphRel);
                                    log.debug("Converted {} to {}", handle, link);
                                    result.add(link);
                                } catch (NotFoundException e) {
                                    log.trace("Relationship {} not found for request {}", handle.getVertexOrEdgeId(), req);
                                    result.add(null);
                                }
                                break;
                            default:
                                throw new IllegalArgumentException("Unsupported ID kind: " + handle.getIdKind());
                        }
                        break;
                    case NODE:
                        final Optional<org.neo4j.graphdb.Node> graphNode = Optional.ofNullable(
                                db.findNode(DynamicLabel.label(req.getType().getGraphLabel()), Neo4jNode.NODE_NAME, req.getName()));
                        result.add(graphNode.map(it -> new Neo4jNode(req.getType(), req.getName())).orElse(null));
                        break;
                    case LINK:
                        final Optional<Link> foundLink = doGetLink(req.getType(), req.getHandleSeq().stream()
                                .map(Neo4jHandle::new).collect(Collectors.toList()));
                        result.add(foundLink.orElse(null));
                    default:
                        throw new IllegalArgumentException("Unsupported request kind: " + req.getKind());
                }
            });
            tx.success();
            return Futures.immediateFuture(Collections.unmodifiableList(result));
        }
    }

    @Override
    public List<Handle> getIncomingSet(Handle handle) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean storeAtom(Handle handle) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListenableFuture<Integer> storeAtomsAsync(List<Handle> handles) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer loadType(String atomTable, AtomType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void barrier() {
        // do nothing?
    }

    @Override
    public boolean isTypeIgnored(AtomType type) {
        return false;
    }

    @Override
    public boolean isAtomIgnored(Handle handle) {
        return !(handle instanceof Neo4jHandle);
    }

}
