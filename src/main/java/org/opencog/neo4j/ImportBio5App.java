package org.opencog.neo4j;

import clojure.lang.LineNumberingPushbackReader;
import clojure.lang.LispReader;
import clojure.lang.Symbol;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.RandomUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.QueryExecutionException;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.opencog.atomspace.Atom;
import org.opencog.atomspace.AtomType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.actuate.autoconfigure.CrshAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Removes {@code href} and {@code prefLabel} support, and only uses AtomSpace concept {@link Neo4jNode#NODE_NAME}
 * as {@code UNIQUE} index. To save space and improve import time.
 *
 * <p>Importer that should still work with:</p>
 *
 * <ol>
 *  <li>Bio_schemeFiles/mmc4.scm</li>
 *  <li>Bio_schemeFiles/Lifespan-observations_2015-02-21.scm</li>
 * </ol>
 *
 * and targeted to work with:
 *
 * <ol>
 *     <li>Bio_schemeFiles/GO_annotation.scm</li>
 *     <li>Bio_schemeFiles/GO_new.scm</li>
 * </ol>
 */
@SpringBootApplication(exclude = CrshAutoConfiguration.class)
@Profile("importbio5app")
public class ImportBio5App implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ImportBio5App.class);
    private static String[] args;
    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        Preconditions.checkArgument(args.length >= 2, "Required arguments: input-scm output-neo4j");
        ImportBio5App.args = args;
        new SpringApplicationBuilder(ImportBio5App.class)
                .profiles("cli", "importbio5app")
                .web(false)
                .run(args);
    }

    @Inject
    private Environment env;

    public List<List<?>> readScheme(File schemeFile) {
        // http://www.programcreek.com/java-api-examples/index.php?api=clojure.lang.LispReader
        log.info("Reading scheme file '{}'", schemeFile);
        final List<List<?>> lists = new ArrayList<>();
        try (FileReader reader = new FileReader(schemeFile)) {
            final LineNumberingPushbackReader pushbackReader = new LineNumberingPushbackReader(reader);
            final Object EOF = new Object();
            while (true) {
                final Object obj = LispReader.read(pushbackReader, false, EOF, false);
                if (obj == EOF) {
                    break;
                }
                log.trace("{}", obj);
                // Each is either a class clojure.lang.Symbol, a java.lang.String, or nested clojure.lang.PersistentList
                //log.info("{}", (Object) ((List) obj).stream().map(x -> x.getClass()).toArray());
                Preconditions.checkState(obj instanceof List,
                        "Not a List in '%s' line %s column %s",
                        schemeFile, pushbackReader.getLineNumber(), pushbackReader.getColumnNumber());
                lists.add((List<?>) obj);
            }
            return lists;
        } catch (IOException e) {
            throw new RuntimeException("Cannot read " + schemeFile, e);
        }
    }

    public static class CypherPart {
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

        public String getMatchDependenciesAsCypher() {
            return Joiner.on('\n').join(matchDependencies);
        }

    }

    public static class CypherParts {
        public Map<String, CypherPart> nodes = new LinkedHashMap<>();
        public List<CypherPart> relationships = new ArrayList<>();

        @Override
        public String toString() {
            return getAllCypher();
        }

        public String getAllCypher() {
            return nodes.values().stream().map(CypherPart::getMatchDependenciesAsCypher).collect(Collectors.joining("\n")) + "\n\n" +
                    relationships.stream().map(CypherPart::getMatchDependenciesAsCypher).collect(Collectors.joining("\n")) + "\n\n" +
                    nodes.values().stream().map(CypherPart::toString).collect(Collectors.joining("\n")) + "\n\n" +
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

    public String varNameFor(String type, List<?> concept) {
        final String conceptName = (String) concept.get(1);
        final String varName = type + "_" + generateId(conceptName);
        return varName;
    }

    public String typeNameFor(List<?> concept) {
        final String typeName = ((Symbol) concept.get(0)).getName();// maintain 1:1 with OpenCog terms - .replaceFirst("Node$", "");
        return typeName;
    }

    public CypherPart customNodeToCypher(List<?> concept) {
        final String typeName = ((Symbol) concept.get(0)).getName();// maintain 1:1 with OpenCog terms - .replaceFirst("Node$", "");
        final String conceptName = (String) concept.get(1);
        final String varName = varNameFor(typeName, concept);
        final String create = String.format("" +
                        "MERGE (%s:opencog_%s {%s: {%s_nodeName}}) ON CREATE SET %s.%s = {%s_gid}",
                varName, typeName, Neo4jNode.NODE_NAME, varName, varName, Atom.GID_PROPERTY, varName);
        final ImmutableMap<String, Object> param = ImmutableMap.of(
                varName + "_gid", RANDOM.nextLong(),
                varName + "_nodeName", conceptName);
        return new CypherPart(varName, param, create);
    }

    public void customNodeToMatch(List<?> concept,
                                  Map<String, Object> outParams, List<String> outDependencies) {
        final String typeName = ((Symbol) concept.get(0)).getName();// maintain 1:1 with OpenCog terms - .replaceFirst("Node$", "");
        final String conceptName = (String) concept.get(1);
        final String varName = varNameFor(typeName, concept);
        final String nodeNameVar = varName + "_match_nodeName";
        final String create = String.format("MATCH (%s:opencog_%s {%s: {%s}})",
                varName, typeName, varName, varName, Neo4jNode.NODE_NAME, nodeNameVar);
        outDependencies.add(create);
        outParams.put(nodeNameVar, conceptName);
    }

    /**
     *
     * @param relationshipType e.g. {@code rdfs_subClassOf}
     * @param parts
     * @param stvStrength
     * @param stvConfidence
     * @param stvCount
     * @param aVarName varName for subject (must be prepared)
     * @param bVarName varName for subject (must be prepared)
     * @param outParams
     * @param outDependencies
     * @return
     * @throws Exception
     */
    protected CypherPart createBinaryLinkVertex(final String relationshipType,
                                                CypherParts parts,
                                                    Double stvStrength,
                                                    Double stvConfidence,
                                                    Double stvCount,
                                                    String aVarName, String bVarName,
                                                    Map<String, Object> outParams, List<String> outDependencies) throws Exception {
        final String varName = relationshipType + "_" + RandomUtils.nextInt(1000, 10000);
        String create;
        if (stvStrength != null) {
            create = String.format("CREATE (%s:%s {gid: {gid}, tv: {tv}})",
                    varName, relationshipType);
        } else {
            create = String.format("CREATE (%s:%s {gid: {gid}})", varName, relationshipType);
        }
        outParams.put("gid", RANDOM.nextLong());
        outParams.put("tv", new double[] {
                Optional.ofNullable(stvStrength).orElse(0d), Optional.ofNullable(stvConfidence).orElse(0d),
                Optional.ofNullable(stvCount).orElse(0d)});
        // RDF reification, rdf:subject and rdf:object
        create += String.format("\n  CREATE (%s) -[:rdf_subject]-> (%s)", varName, aVarName);
        create += String.format("\n  CREATE (%s) -[:rdf_object]-> (%s)", varName, bVarName);

        final CypherPart part = new CypherPart(null, ImmutableMap.copyOf(outParams), create, ImmutableList.copyOf(outDependencies));
        parts.relationships.add(part);
        return part;
    }

    /**
     *
     * @param parts
     * @param stvStrength
     * @param stvConfidence
     * @param stvCount
     * @param predicateName
     * @param predicateType
     * @param predicateVarName
     * @param outParams
     * @param outDependencies
     * @return
     * @throws Exception
     */
    protected CypherPart createEvaluationLinkVertex(CypherParts parts,
                                                    Double stvStrength,
                                                    Double stvConfidence,
                                                    Double stvCount,
                                                    String predicateName,
                                                    String predicateType,
                                                    String predicateVarName,
                                                    List<String> paramNames,
                                                    Map<String, Object> outParams, List<String> outDependencies) throws Exception {
        final String relationshipType = "opencog_EvaluationLink";
        final String varName = relationshipType + "_" + predicateName + "_" + RandomUtils.nextInt(1000, 10000);
        String create;
        if (stvStrength != null) {
            create = String.format("CREATE (%s:%s {gid: {gid}, tv: {tv}})",
                    varName, relationshipType);
        } else {
            create = String.format("CREATE (%s:%s {gid: {gid}})", varName, relationshipType);
        }
        outParams.put("gid", RANDOM.nextLong());
        outParams.put("tv", new double[] {
                Optional.ofNullable(stvStrength).orElse(0d), Optional.ofNullable(stvConfidence).orElse(0d),
                Optional.ofNullable(stvCount).orElse(0d)});
        create += String.format("\n  CREATE (%s) -[:opencog_predicate]-> (%s)", varName, predicateVarName);
        // http://schema.org/position
        for (int i = 0; i < paramNames.size(); i++) {
            create += String.format("\n  CREATE (%s) -[:opencog_parameter {position: %d}]-> (%s)",
                    varName, i, paramNames.get(i));
        }

        final CypherPart part = new CypherPart(null, ImmutableMap.copyOf(outParams), create, ImmutableList.copyOf(outDependencies));
        parts.relationships.add(part);
        return part;
    }

    /**
     *
     * @param parts
     * @param top
     * @return
     * @deprecated For OpenCog, all Links are using hyperedges now.
     */
    @Deprecated
    public CypherPart inheritanceToCypherRelationship(CypherParts parts, List<?> top) {
        final ArrayList<String> outDependencies = new ArrayList<>();
        final Map<String, Object> outParams = new HashMap<>();

        // ensure a is prepared
        final List<?> a = (List<?>) top.get(1);
        final String aType = typeNameFor(a);
        final String aVarName = varNameFor(aType, a);
        if (!parts.nodes.containsKey(aVarName)) {
            final CypherPart aCypher = customNodeToCypher(a);
            parts.nodes.put(aVarName, aCypher);
        }
        //customNodeToMatch(a, outParams, outDependencies);

        // ensure b is prepared
        final List<?> b = (List<?>) top.get(2);
        final String bType = typeNameFor(b);
        final String bVarName = varNameFor(bType, b);
        if (!parts.nodes.containsKey(bVarName)) {
            final CypherPart bCypher = customNodeToCypher(b);
            parts.nodes.put(bVarName, bCypher);
        }
        //customNodeToMatch(b, outParams, outDependencies);

        final String create = String.format("CREATE (%s) -[:rdfs_subClassOf {%s: {gid}}]-> (%s)",
                aVarName, Atom.GID_PROPERTY, bVarName);
        outParams.put("gid", RANDOM.nextLong());
        final CypherPart part = new CypherPart(null, ImmutableMap.copyOf(outParams), create,
                ImmutableList.copyOf(outDependencies));
        parts.relationships.add(part);
        return part;
    }

    /**
     * (InheritanceLink a b)
     * means: a rdfs:subClassOf b
     * @param parts
     * @param top
     * @return
     */
    public CypherPart inheritanceToCypher(CypherParts parts, List<?> top) {
        final ArrayList<String> outDependencies = new ArrayList<>();
        final Map<String, Object> outParams = new HashMap<>();

        // ensure a is prepared
        final List<?> a = (List<?>) top.get(1);
        final String aType = typeNameFor(a);
        final String aVarName = varNameFor(aType, a);
        if (!parts.nodes.containsKey(aVarName)) {
            final CypherPart aCypher = customNodeToCypher(a);
            parts.nodes.put(aVarName, aCypher);
        }
        //customNodeToMatch(a, outParams, outDependencies);

        // ensure b is prepared
        final List<?> b = (List<?>) top.get(2);
        final String bType = typeNameFor(b);
        final String bVarName = varNameFor(bType, b);
        if (!parts.nodes.containsKey(bVarName)) {
            final CypherPart bCypher = customNodeToCypher(b);
            parts.nodes.put(bVarName, bCypher);
        }
        //customNodeToMatch(b, outParams, outDependencies);

        try {
            return createBinaryLinkVertex("rdfs_subClassOf", parts, null, null, null,
                    aVarName, bVarName, outParams, outDependencies);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create CypherPart for " + top, e);
        }
    }

    /**
     * note that mmc4.scm + GO_annotation.scm has incorrect EvaluationLink structure, it should be
     * either:
     *
     * <ol>
     *    <li>(EvaluationLink (PredicateNode ...) (ListLink (...) (...)))</li>
     *    <li>(EvaluationLink (stv 0.0 0.0) (PredicateNode ...) (ListLink (...) (...)))</li>
     * </ol>
     *
     * i.e. PredicateNode should have no child ListLink
     *
     * @param parts
     * @param top
     * @return
     */
    public CypherPart evaluationToCypher(CypherParts parts, List<?> top) {
        final ArrayList<String> outDependencies = new ArrayList<>();
        final LinkedHashMap<String, Object> outParams = new LinkedHashMap<>();

        @Nullable
        Double stvStrength = null;
        @Nullable
        Double stvConfidence = null;
        final List<?> stvMaybe = (List<?>) top.get(1);
        final int predicateOffset;
        if ("stv".equals(((Symbol) stvMaybe.get(0)).getName())) {
            predicateOffset = 2;
            stvStrength = (Double) stvMaybe.get(1);
            stvConfidence = (Double) stvMaybe.get(2);
        } else {
            predicateOffset = 1;
        }

        // ensure predicate is prepared
        final List<?> predicate = (List<?>) top.get(predicateOffset);
        final String predicateName;
        try {
            predicateName = (String) predicate.get(1);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid predicate: " + predicate + " inside " + top, e);
        }
        final String predicateType = typeNameFor(predicate);
        final String predicateVarName = varNameFor(predicateType, predicate);
        if (!parts.nodes.containsKey(predicateVarName)) {
            final CypherPart predicateCypher = customNodeToCypher(predicate);
            parts.nodes.put(predicateVarName, predicateCypher);
        }
        //customNodeToMatch(predicate, outParams, outDependencies);

        // ensure all params are prepared
        try {
            final List<?> listLink = (List<?>) ((List<?>) top.get(predicateOffset + 1));
            final List<List<?>> params = (List<List<?>>) listLink.subList(1, listLink.size());
            final List<String> paramNames = new ArrayList<>();
            for (List<?> param : params) {
                final String paramType = typeNameFor(param);
                final String paramVarName = varNameFor(paramType, param);
                if (!parts.nodes.containsKey(paramVarName)) {
                    final CypherPart paramCypher = customNodeToCypher(param);
                    parts.nodes.put(paramVarName, paramCypher);
                }
                //customNodeToMatch(param, outParams, outDependencies);

                paramNames.add(paramVarName);
            }

            return createEvaluationLinkVertex(parts, stvStrength, stvConfidence, null,
                    predicateName, predicateType, predicateVarName,
                    paramNames, outParams, outDependencies);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create CypherPart for " + top, e);
        }
    }

    /**
     * Either:
     *
     * <ol>
     *     <li><code>(MemberLink (GeneNode "A0A087WZ62") (ConceptNode "GO:0004826"))</code></li>
     *     <li><code>(MemberLink (stv 0.0 1.0) (GeneNode "A0A087WZ62") (ConceptNode "GO:0004826"))</code></li>
     * </ol>
     *
     * @param parts
     * @param top
     * @return
     * @deprecated For OpenCog, all {@link org.opencog.atomspace.Link}s now use hyperedge.
     */
    @Deprecated
    public CypherPart memberToCypherAsRelationship(CypherParts parts, List<?> top) {
        try {
            final ArrayList<String> outDependencies = new ArrayList<>();
            final LinkedHashMap<String, Object> outParams = new LinkedHashMap<>();

            @Nullable
            Double stvStrength = null;
            @Nullable
            Double stvConfidence = null;
            final List<?> stvMaybe = (List<?>) top.get(1);
            final int memberOffset;
            if ("stv".equals(((Symbol) stvMaybe.get(0)).getName())) {
                memberOffset = 2;
                stvStrength = (Double) stvMaybe.get(1);
                stvConfidence = (Double) stvMaybe.get(2);
            } else {
                memberOffset = 1;
            }

            // ensure gene is prepared
            final List<?> gene = (List<?>) top.get(memberOffset);
            final String geneVarName = varNameForGene(gene);
            if (!parts.nodes.containsKey(geneVarName)) {
                final CypherPart geneCypher = customNodeToCypher(gene);
                parts.nodes.put(geneVarName, geneCypher);
            }
            //customNodeToMatch(gene, outParams, outDependencies);

            // ensure concept is prepared
            final List<?> concept = (List<?>) top.get(memberOffset + 1);
            final String conceptVarName = varNameForConcept(concept);
            if (!parts.nodes.containsKey(conceptVarName)) {
                final CypherPart conceptCypher = customNodeToCypher(concept);
                parts.nodes.put(conceptVarName, conceptCypher);
            }
            //customNodeToMatch(concept, outParams, outDependencies);

            final String create;
            if (stvStrength != null) {
                create = String.format("CREATE (%s) -[:rdf_type {gid: {gid}, stv_strength: %f, stv_confidence: %f}]-> (%s)",
                        geneVarName, stvStrength, stvConfidence, conceptVarName);
            } else {
                create = String.format("CREATE (%s) -[:rdf_type {gid: {gid}}]-> (%s)", geneVarName, conceptVarName);
            }
            outParams.put("gid", RANDOM.nextLong());
            final CypherPart part = new CypherPart(null, ImmutableMap.copyOf(outParams), create,
                    ImmutableList.copyOf(outDependencies));
            parts.relationships.add(part);
            return part;
        } catch (Exception e) {
            throw new RuntimeException("Cannot create CypherParts for MemberLink " + top, e);
        }
    }

    /**
     * Either:
     *
     * <ol>
     *     <li><code>(MemberLink (GeneNode "A0A087WZ62") (ConceptNode "GO:0004826"))</code></li>
     *     <li><code>(MemberLink (stv 0.0 1.0) (GeneNode "A0A087WZ62") (ConceptNode "GO:0004826"))</code></li>
     * </ol>
     *
     * @param parts
     * @param top
     * @return
     */
    public CypherPart memberToCypher(CypherParts parts, List<?> top) {
        try {
            final ArrayList<String> outDependencies = new ArrayList<>();
            final LinkedHashMap<String, Object> outParams = new LinkedHashMap<>();

            @Nullable
            Double stvStrength = null;
            @Nullable
            Double stvConfidence = null;
            final List<?> stvMaybe = (List<?>) top.get(1);
            final int memberOffset;
            if ("stv".equals(((Symbol) stvMaybe.get(0)).getName())) {
                memberOffset = 2;
                stvStrength = (Double) stvMaybe.get(1);
                stvConfidence = (Double) stvMaybe.get(2);
            } else {
                memberOffset = 1;
            }

            // ensure gene is prepared
            final List<?> gene = (List<?>) top.get(memberOffset);
            final String geneVarName = varNameForGene(gene);
            if (!parts.nodes.containsKey(geneVarName)) {
                final CypherPart geneCypher = customNodeToCypher(gene);
                parts.nodes.put(geneVarName, geneCypher);
            }
            //customNodeToMatch(gene, outParams, outDependencies);

            // ensure concept is prepared
            final List<?> concept = (List<?>) top.get(memberOffset + 1);
            final String conceptVarName = varNameForConcept(concept);
            if (!parts.nodes.containsKey(conceptVarName)) {
                final CypherPart conceptCypher = customNodeToCypher(concept);
                parts.nodes.put(conceptVarName, conceptCypher);
            }
            //customNodeToMatch(concept, outParams, outDependencies);

            return createBinaryLinkVertex("rdf_type", parts,
                    stvStrength, stvConfidence, null,
                    geneVarName, conceptVarName, outParams, outDependencies);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create CypherParts for MemberLink " + top, e);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        final GraphDatabaseService graphDb = graphDb();

        try (final Transaction tx = graphDb.beginTx()) {
            log.info("Ensuring constraints and indexes...");
            for (AtomType atomType : AtomType.values()) {
                graphDb.execute(String.format("CREATE CONSTRAINT ON (n:%s) ASSERT n.%s IS UNIQUE",
                        atomType.getGraphLabel(), Atom.GID_PROPERTY));
            }

            graphDb.execute(String.format("CREATE CONSTRAINT ON (n:%s) ASSERT n.%s IS UNIQUE",
                    AtomType.CONCEPT_NODE.getGraphLabel(), Neo4jNode.NODE_NAME));
            graphDb.execute(String.format("CREATE CONSTRAINT ON (n:%s) ASSERT n.%s IS UNIQUE",
                    AtomType.GENE_NODE.getGraphLabel(), Neo4jNode.NODE_NAME));
            graphDb.execute(String.format("CREATE CONSTRAINT ON (n:%s) ASSERT n.%s IS UNIQUE",
                    AtomType.PREDICATE_NODE.getGraphLabel(), Neo4jNode.NODE_NAME));
            graphDb.execute(String.format("CREATE CONSTRAINT ON (n:%s) ASSERT n.%s IS UNIQUE",
                    AtomType.PHRASE_NODE.getGraphLabel(), Neo4jNode.NODE_NAME));

//            graphDb.execute("CREATE INDEX ON :opencog_ConceptNode(prefLabel)");
//            graphDb.execute("CREATE INDEX ON :opencog_GeneNode(prefLabel)");
//            graphDb.execute("CREATE INDEX ON :opencog_PredicateNode(prefLabel)");
//            graphDb.execute("CREATE INDEX ON :opencog_PhraseNode(prefLabel)");

            tx.success();
        }
        log.info("Ensured constraints and indexes.");

        try (final Transaction tx = graphDb.beginTx()) {
            final File schemeFile = new File(args[0]);
            final List<List<?>> lists = readScheme(schemeFile);
            for (int i = 0; i < lists.size(); i++) {
                if (i % 1000 == 0) {
                    log.info("List {} of {} ({}%)", i, lists.size(), 100 * i / lists.size());
                }
                final List<?> top = lists.get(i);
                final CypherParts parts = new CypherParts();

                final Symbol symbol = (Symbol) top.get(0);
                switch (symbol.getName()) {
                    // ignore 'define'
                    // ignore 'display'
                    // ignore 'set!'
                    case "define":
                    case "display":
                    case "set!":
                        break;
                    // InheritanceLink is mapped to "rdfs:subClassOf"
                    case "InheritanceLink":
                        log.trace("INHERITANCE {}", top);
                        inheritanceToCypher(parts, top);
                        break;
                    // (EvaluationLink (PredicateNode "") (ListLink (GeneNode "") (ConceptNode "")) )
                    // workaround for incorrect (EvaluationLink (PredicateNode "" (ListLink (GeneNode "") (ConceptNode ""))) )
                    case "EvaluationLink":
                        log.trace("EVALUATION {}", top);
                        evaluationToCypher(parts, top);
                        break;
                    // MemberLink/2 gene:GeneNode concept:ConceptNode
                    // MemberLink is mapped to "rdf:type" ("a" in TURTLE-speak)
                    case "MemberLink":
                        log.trace("MEMBER {}", top);
                        memberToCypher(parts, top);
                        break;
                    default:
                        log.error("Unknown symbol in '{}'", top);
                }

                if (!parts.nodes.isEmpty() || !parts.relationships.isEmpty()) {
                    log.trace("Parts:\n{}", parts);
                    try {
                        graphDb.execute(parts.getAllCypher(), parts.getAllParams());
                    } catch (QueryExecutionException e) {
                        throw new RuntimeException("Cannot import Scheme list: " + top + " using: " + parts.getAllCypher(), e);
                    } catch (Exception e) {
                        throw new RuntimeException("Cannot import Scheme list: " + top + " using: " + parts, e);
                    }
                }

//                for (CypherPart node : parts.nodes.values()) {
//                    String cypher = "";
//                    for (String matchDependency : node.matchDependencies) {
//                        cypher += matchDependency + "\n";
//                    }
//                    cypher += node.toString();
//                    log.info("Creating node {}: {}", node.varName, cypher);
//                    graphDb.execute(cypher, node.param);
//                }
//                for (CypherPart relationship : parts.relationships) {
//                    String cypher = "";
//                    for (String matchDependency : relationship.matchDependencies) {
//                        cypher += matchDependency + "\n";
//                    }
//                    cypher += relationship.toString();
//                    log.info("Creating relationship: {}", cypher);
//                    graphDb.execute(cypher, relationship.param);
//                }

            }

            log.info("Committing transaction...");
            tx.success();
        }
        log.info("Done");
    }

    @Bean(destroyMethod = "shutdown")
    public GraphDatabaseService graphDb() {
        final String dbDir = args[1];
        log.info("Opening Neo4j database '{}'", dbDir);
        return new GraphDatabaseFactory().newEmbeddedDatabase(dbDir);//System.getProperty("user.home") + "/tmp/opencog-neo4j");
    }

}
