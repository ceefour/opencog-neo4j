package org.opencog.neo4j;

import clojure.lang.Symbol;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.Node;
import org.opencog.atomspace.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements {@link GraphBackingStore} using Neo4j {@link org.neo4j.graphdb.GraphDatabaseService}.
 */
@Transactional
public class Neo4jBackingStore extends GraphBackingStoreBase {

    private static final Logger log = LoggerFactory.getLogger(Neo4jBackingStore.class);

    @Inject
    private PlatformTransactionManager txMgr;
    protected GraphDatabaseService db;
    private TransactionTemplate txTemplate;

    public Neo4jBackingStore(GraphDatabaseService db) {
        this.db = db;
    }

    public static Handle forNode(Node graphNode) {
//        final AtomType type = AtomType.forGraphLabel(graphNode.getLabels().iterator().next().name());
//        return new Neo4jHandle(IdKind.VERTEX, graphNode.getId());
        return new Handle((long) graphNode.getProperty(org.opencog.atomspace.Node.GID_PROPERTY));
    }

    public static Handle forLink(Node graphNode) {
//        final AtomType type = AtomType.forGraphLabel(graphNode.getLabels().iterator().next().name());
//        return new Neo4jHandle(IdKind.VERTEX, graphNode.getId());
        return new Handle((long) graphNode.getProperty(org.opencog.atomspace.Node.GID_PROPERTY));
    }

    public static Handle forLink(Relationship rel) {
//        final AtomType type = AtomType.forGraphLabel(graphNode.getLabels().iterator().next().name());
//        return new Neo4jHandle(IdKind.EDGE, rel.getId());
        return new Handle((long) rel.getProperty(org.opencog.atomspace.Node.GID_PROPERTY));
    }

    /**
     * Since a graphNode can be either an AtomSpace {@link Neo4jNode} or {@link Neo4jLink},
     * it may return either one.
     * @param graphNode
     * @return
     */
    public static Atom toAtom(Node graphNode) {
        final AtomType atomType = AtomType.forGraphLabel(graphNode.getLabels().iterator().next().name());
        if (atomType.getGraphMapping() == GraphMapping.VERTEX) {
            return new Neo4jNode(graphNode); //atomType, (String) graphNode.getProperty(Neo4jNode.NODE_NAME));
        } else {
            return new Neo4jLink(atomType, graphNode);
        }
    }

    public static Neo4jLink toLink(Relationship graphRel) {
        final AtomType atomType = AtomType.forGraphLabel(graphRel.getType().name());
        return new Neo4jLink(atomType, graphRel);
    }

    @PostConstruct
    public void init() {
        txTemplate = new TransactionTemplate(txMgr);
    }

    protected Optional<Link> doGetLink(AtomType type, List<Handle> handleSeq) {
        if (type.getGraphMapping() == GraphMapping.BINARY_HYPEREDGE) {
            Preconditions.checkArgument(handleSeq.size() == 2,
                    "Type " + type + " graph mapping is BINARY_HYPEREDGE which supports only 2 outgoing set, but " + handleSeq.size() + " given");
            final String cypher = String.format(
                    "MATCH (a {gid: {a_gid}}), (b {gid: {b_gid}})\n" +
                    "OPTIONAL MATCH (a) -[r:%s]-> (b),\n" +
                    "(a) <-[:rdf_subject]- (l:%s) -[:rdf_object]-> (b)\n" +
                    "RETURN r, l",
                    type.getGraphLabel(), type.getGraphLabel());
            final long a_gid = handleSeq.get(0).getUuid();
            final long b_gid = handleSeq.get(1).getUuid();
            try (final Result result = db.execute(cypher, ImmutableMap.of("a_gid", a_gid,
                    "b_gid", b_gid))) {
                if (result.hasNext()) {
                    final Map<String, Object> row = result.next();
                    if (row.get("r") != null) { // binary edge
                        //                final Neo4jHandle vertex1 = new Neo4jHandle(Neo4jHandle.IdKind.VERTEX, a_id);
                        //                final Neo4jHandle vertex2 = new Neo4jHandle(Neo4jHandle.IdKind.VERTEX, b_id);
                        return Optional.of(new Neo4jLink(type, ImmutableList.copyOf(handleSeq), (Relationship) row.get("r")));
                    } else if (row.get("l") != null) { // binary hyperedge
                        //                final Neo4jHandle vertex1 = new Neo4jHandle(Neo4jHandle.IdKind.VERTEX, a_id);
                        //                final Neo4jHandle vertex2 = new Neo4jHandle(Neo4jHandle.IdKind.VERTEX, b_id);
                        return Optional.of(new Neo4jLink(type, ImmutableList.copyOf(handleSeq), (Node) row.get("l")));
                    } else {
                        return Optional.empty();
                    }
                } else {
                    return Optional.empty();
                }
            }
        } else if (type.getGraphMapping() == GraphMapping.HYPEREDGE) {
            final List<EdgeMapping> edgeMappings = type.getEdgeMappings(handleSeq.size());
            final List<String> matches = new ArrayList<>();
            final List<String> wheres = new ArrayList<>();
            final List<String> returns = new ArrayList<>();
            returns.add("l");
            for (int i = 0; i < handleSeq.size(); i++) {
                final Handle handle = handleSeq.get(i);
                matches.add( String.format("(l) %s (o%d)", i, edgeMappings.get(i).toCypher(null), i) );
                wheres.add( String.format("o%d.gid = %d", i, handle.getUuid()) );
//                returns.add("o" + i);
            }
            final String cypher = "MATCH " + Joiner.on(",\n  ").join(matches) + "\nWHERE " + Joiner.on(" AND ").join(wheres) +
                    "\nRETURN " + Joiner.on(", ").join(returns);
            try (final Result result = db.execute(cypher)) {
                if (result.hasNext()) {
                    final Map<String, Object> row = result.next();
                    return Optional.of(new Neo4jLink(type, ImmutableList.copyOf(handleSeq), (Relationship) row.get("l")));
                } else {
                    return Optional.empty();
                }
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
                        // for Neo4j Backing Store, links with UUIDs are always vertices (hyper-edge), due to indexing requirement
                        final String cypher = "OPTIONAL MATCH (n {gid: {gid}}) RETURN n";
                        try (final Result uuidResult = db.execute(cypher, ImmutableMap.of("gid", req.getUuid()))) {
                            final Map<String, Object> mapResult = uuidResult.hasNext() ? uuidResult.next() : ImmutableMap.of();
                            if (mapResult.get("n") != null) {
                                final Node graphNode = (Node) mapResult.get("n");
                                final AtomType atomType = AtomType.forGraphLabel(graphNode.getLabels().iterator().next().name());
                                if (GraphMapping.VERTEX == atomType.getGraphMapping()) {
                                    final Atom atom = new Neo4jNode(graphNode);
                                    log.debug("Converted {} to {}", graphNode, atom);
                                    result.add(atom);
                                } else {
                                    final Atom atom = new Neo4jLink(atomType, graphNode);
                                    log.debug("Converted {} to {}", graphNode, atom);
                                    result.add(atom);
                                }
                            } else {
                                log.debug("Node/Link {} not found for request {}", req.getUuid(), req);
                                result.add(null);                            }
                        }
                        break;
                    case NODE:
                        final Optional<org.neo4j.graphdb.Node> graphNode = Optional.ofNullable(
                                db.findNode(DynamicLabel.label(req.getType().getGraphLabel()), Neo4jNode.NODE_NAME, req.getName()));
                        final Optional<Neo4jNode> openCogNode = graphNode.map(it -> new Neo4jNode(it));
                        log.debug("Converted {} to {}", graphNode, openCogNode);
                        result.add(openCogNode.orElse(null));
                        break;
                    case LINK:
                        final Optional<Link> foundLink = doGetLink(req.getType(), req.getHandleSeq().stream()
                                .map(Handle::new).collect(Collectors.toList()));
                        result.add(foundLink.orElse(null));
                    default:
                        throw new IllegalArgumentException("Unsupported request kind: " + req.getKind());
                }
            });
            tx.success();
            return Futures.immediateFuture(Collections.unmodifiableList(result));
        }
    }

    protected static class CypherPart {
        @Nullable
        public String varName;
        @Nullable
        public ImmutableMap<String, Object> param;
        public String create;
        /**
         * List of MATCH dependencies.
         */
        public final ImmutableList<String> matchDependencies;

        public CypherPart(@Nullable String varName, ImmutableMap<String, Object> param, String create) {
            this.varName = varName;
            this.param = param;
            this.create = create;
            this.matchDependencies = ImmutableList.of();
        }

        public CypherPart(@Nullable String varName, ImmutableMap<String, Object> param, String create,
                          ImmutableList<String> matchDependencies) {
            this.varName = varName;
            this.param = param;
            this.create = create;
            this.matchDependencies = matchDependencies;
        }

        public CypherPart(String create) {
            this.varName = null;
            this.param = ImmutableMap.of();
            this.create = create;
            this.matchDependencies = ImmutableList.of();
        }

        @Override
        public String toString() {
            return create;
        }
    }

    protected static class CypherParts {
        public Map<String, CypherPart> nodes = new LinkedHashMap<>();
        public List<CypherPart> relationships = new ArrayList<>();

        @Override
        public String toString() {
            return getAllCypher();
        }

        public String getAllCypher() {
            return nodes.values().stream().map(CypherPart::toString).collect(Collectors.joining("\n")) + "\n\n" +
                    relationships.stream().map(CypherPart::toString).collect(Collectors.joining("\n"));
        }

        public ImmutableMap<String, Object> getAllParams() {
            final ImmutableMap.Builder<String, Object> paramb = ImmutableMap.builder();
            for (CypherPart part : nodes.values()) {
                if (part.param != null) {
                    paramb.putAll(part.param);
                }
            }
            for (CypherPart part : relationships) {
                if (part.param != null) {
                    paramb.putAll(part.param);
                }
            }
            return paramb.build();
        }
    }

    public static String generateId(String name) {
        return name.replaceAll("[^A-Za-z0-9]+", "_");
    }

    public String varNameForGene(List<?> gene) {
        final String geneName = (String) gene.get(1);
        final String varName = "Gene_" + generateId(geneName);
        return varName;
    }

    public String varNameForConcept(List<?> concept) {
        final String conceptName = (String) concept.get(1);
        final String varName = "Concept_" + generateId(conceptName);
        return varName;
    }

    public String varNameFor(String type, org.opencog.atomspace.Node node) {
        final String conceptName = node.getName();
        final String varName = type + "_" + generateId(conceptName);
        return varName;
    }

    protected CypherPart customNodeToCypher(org.opencog.atomspace.Node node) {
        Preconditions.checkArgument(node.getUuid() != Handle.UNDEFINED,
                "Node (%s \"%s\") must have UUID", node.getType().toUpperCamel(), node.getName());
        final String typeName = node.getType().toUpperCamel();// maintain 1:1 with OpenCog terms - .replaceFirst("Node$", "");
        final String conceptName = node.getName();
        final String varName = varNameFor(typeName, node);
        final String create = String.format("" +
                        "MERGE (%s:%s {%s: {%s_gid}, %s: {%s_nodeName}}) ON MATCH SET %s.tv = {%s_tv}",
                varName, node.getType().getGraphLabel(),
                Atom.GID_PROPERTY, varName,
                Neo4jNode.NODE_NAME, varName,
                varName, varName);
        final ImmutableMap<String, Object> param = ImmutableMap.of(
                varName + "_gid", node.getUuid(),
                varName + "_nodeName", conceptName,
                varName + "_tv", node.getTruthValue().toArray());
        return new CypherPart(varName, param, create);
    }

    @Override
    public ListenableFuture<Integer> storeAtomsAsync(List<Handle> handles) {
        try (final Transaction tx = db.beginTx()) {
            for (final Handle handle : handles) {
                final Atom atom = handle.resolve().get();
                final CypherParts parts = new CypherParts();
                if (atom instanceof org.opencog.atomspace.Node) {
                    final org.opencog.atomspace.Node node = (org.opencog.atomspace.Node) atom;
                    final String varName = varNameFor(node.getType().toUpperCamel(), node);
                    final CypherPart part = customNodeToCypher(node);
                    parts.nodes.put(varName, part);
                    // TODO: insert node
                } else if (atom instanceof Link) {
                    final Link link = (org.opencog.atomspace.Link) atom;
                    // TODO: link
                    throw new UnsupportedOperationException();
                } else {
                    throw new IllegalArgumentException("Cannot determine atom is Node or Link: " + atom);
                }
                db.execute(parts.getAllCypher(), parts.getAllParams());
            }
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Handle> getIncomingSet(Handle handle) {
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
        return false;
    }

}
