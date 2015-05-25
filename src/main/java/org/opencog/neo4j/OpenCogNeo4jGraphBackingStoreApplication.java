package org.opencog.neo4j;

import clojure.lang.LineNumberingPushbackReader;
import clojure.lang.LispReader;
import clojure.lang.Symbol;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.RandomUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
@Profile("cli")
public class OpenCogNeo4jGraphBackingStoreApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(OpenCogNeo4jGraphBackingStoreApplication.class);

    public static void main(String[] args) {
        new SpringApplicationBuilder(OpenCogNeo4jGraphBackingStoreApplication.class)
                .profiles("cli")
                .web(false)
                .run(args);
    }

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
        } catch (java.io.IOException e) {
            throw new RuntimeException("Cannot read " + schemeFile, e);
        }
    }

    public static class CypherPart {
        @Nullable
        public String varName;
        @Nullable
        public ImmutableMap<String, Object> param;
        public String create;

        public CypherPart(String varName, ImmutableMap<String, Object> param, String create) {
            this.varName = varName;
            this.param = param;
            this.create = create;
        }

        public CypherPart(String create) {
            this.varName = null;
            this.param = null;
            this.create = create;
        }

        @Override
        public String toString() {
            return create;
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

    public String varNameFor(String type, List<?> concept) {
        final String conceptName = (String) concept.get(1);
        final String varName = type + "_" + generateId(conceptName);
        return varName;
    }

    public String typeNameFor(List<?> concept) {
        final String typeName = ((Symbol) concept.get(0)).getName().replaceFirst("Node$", "");
        return typeName;
    }

    public CypherPart customNodeToCypher(List<?> concept) {
        final String typeName = ((Symbol) concept.get(0)).getName().replaceFirst("Node$", "");
        final String conceptName = (String) concept.get(1);
        final String varName = varNameFor(typeName, concept);
        final String create = String.format("MERGE (%s:opencog_%s {href: {%s_href}}) ON CREATE SET %s.prefLabel = {%s_prefLabel}",
                varName, typeName, varName, varName, varName);
        final ImmutableMap<String, Object> param = ImmutableMap.of(
                varName + "_href", "opencog:" + typeName + "_" + conceptName,
                varName + "_prefLabel", conceptName);
        return new CypherPart(varName, param, create);
    }

    @Deprecated
    public CypherPart geneToCypher(List<?> gene) {
        final String geneName = (String) gene.get(1);
        final String varName = varNameForGene(gene);
        final String create = String.format("MERGE (%s:opencog_Gene {href: {%s_href}}) ON CREATE SET %s.prefLabel = {%s_prefLabel}",
                varName, varName, varName, varName);
        final ImmutableMap<String, Object> param = ImmutableMap.of(
                varName + "_href", "opencog:Gene_" + geneName,
                varName + "_prefLabel", geneName);
        return new CypherPart(varName, param, create);
    }

    @Deprecated
    public CypherPart conceptToCypher(List<?> concept) {
        final String conceptName = (String) concept.get(1);
        final String varName = varNameForConcept(concept);
        final String create = String.format("MERGE (%s:opencog_Concept {href: {%s_href}}) ON CREATE SET %s.prefLabel = {%s_prefLabel}",
                varName, varName, varName, varName);
        final ImmutableMap<String, Object> param = ImmutableMap.of(
                varName + "_href", "opencog:Concept_" + conceptName,
                varName + "_prefLabel", conceptName);
        return new CypherPart(varName, param, create);
    }

    public CypherPart inheritanceToCypher(CypherParts parts, List<?> top) {
        // ensure a is prepared
        final List<?> a = (List<?>) top.get(1);
        final String aType = typeNameFor(a);
        final String aVarName = varNameFor(aType, a);
        if (!parts.nodes.containsKey(aVarName)) {
            final CypherPart aCypher = customNodeToCypher(a);
            parts.nodes.put(aVarName, aCypher);
        }

        // ensure b is prepared
        final List<?> b = (List<?>) top.get(2);
        final String bType = typeNameFor(b);
        final String bVarName = varNameFor(bType, b);
        if (!parts.nodes.containsKey(bVarName)) {
            final CypherPart bCypher = customNodeToCypher(b);
            parts.nodes.put(bVarName, bCypher);
        }

        final String create = String.format("CREATE UNIQUE (%s) -[:rdfs_subClassOf]-> (%s)", aVarName, bVarName);
        final CypherPart part = new CypherPart(create);
        parts.relationships.add(part);
        return part;
    }

    /**
     * note that mmc4.scm has incorrect EvaluationLink structure, it should be
     * (EvaluationLink (PredicateNode ...) (ListLink (...) (...)))
     *
     * i.e. PredicateNode should have no child ListLink
     *
     * @param parts
     * @param top
     * @return
     */
    public CypherPart evaluationToCypher(CypherParts parts, List<?> top) {
        // ensure predicate is prepared
        final List<?> predicate = (List<?>) top.get(1);
        final String predicateName = (String) predicate.get(1);
        final String predicateType = typeNameFor(predicate);
        final String predicateVarName = varNameFor(predicateType, predicate);
        if (!parts.nodes.containsKey(predicateVarName)) {
            final CypherPart predicateCypher = customNodeToCypher(predicate);
            parts.nodes.put(predicateVarName, predicateCypher);
        }

        // ensure all params are prepared
        final List<?> listLink = (List<?>) ((List<?>) top.get(1)).get(2);
        final List<List<?>> params = (List<List<?>>) listLink.subList(1, listLink.size());
        final List<String> paramNames = new ArrayList<>();
        for (List<?> param : params) {
            final String paramType = typeNameFor(param);
            final String paramVarName = varNameFor(paramType, param);
            if (!parts.nodes.containsKey(paramVarName)) {
                final CypherPart paramCypher = customNodeToCypher(param);
                parts.nodes.put(paramVarName, paramCypher);
            }
            paramNames.add(paramVarName);
        }

        final String varName = "EvaluationLink_" + predicateName + "_" + RandomUtils.nextInt(1000, 10000);
        String create = String.format("CREATE UNIQUE (%s:opencog_EvaluationLink) -[:opencog_predicate]-> (%s)",
                varName, predicateVarName);
        // http://schema.org/position
        for (int i = 0; i < paramNames.size(); i++) {
            create += String.format("\n  CREATE UNIQUE (%s) -[:opencog_parameter {position: %d}]-> (%s)",
                    varName, i, paramNames.get(i));
        }

        final CypherPart part = new CypherPart(create);
        parts.relationships.add(part);
        return part;
    }

    public CypherPart memberToCypher(CypherParts parts, List<?> top) {
        // ensure gene is prepared
        final List<?> gene = (List<?>) top.get(1);
        final String geneVarName = varNameForGene(gene);
        if (!parts.nodes.containsKey(geneVarName)) {
            final CypherPart geneCypher = geneToCypher(gene);
            parts.nodes.put(geneVarName, geneCypher);
        }

        // ensure concept is prepared
        final List<?> concept = (List<?>) top.get(2);
        final String conceptVarName = varNameForConcept(concept);
        if (!parts.nodes.containsKey(conceptVarName)) {
            final CypherPart conceptCypher = conceptToCypher(concept);
            parts.nodes.put(conceptVarName, conceptCypher);
        }

        final String create = String.format("CREATE UNIQUE (%s) -[:rdf_type]-> (%s)", geneVarName, conceptVarName);
        final CypherPart part = new CypherPart(create);
        parts.relationships.add(part);
        return part;
    }

    @Override
    public void run(String... args) throws Exception {
        final File schemeFile = new File("/data/project_netadm/opencog/neo4j/Bio_schemeFiles/mmc4.scm");
        final CypherParts parts = new CypherParts();
        final List<List<?>> lists = readScheme(schemeFile);
        for (final List<?> top : lists) {
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
                    log.info("INHERITANCE {}", top);
                    inheritanceToCypher(parts, top);
                    break;
                // EvaluationLink
                case "EvaluationLink":
                    log.info("EVALUATION {}", top);
                    evaluationToCypher(parts, top);
                    break;
                // MemberLink/2 gene:GeneNode concept:ConceptNode
                // MemberLink is mapped to "rdf:type" ("a" in TURTLE-speak)
                case "MemberLink":
                    log.info("MEMBER {}", top);
                    memberToCypher(parts, top);
                    break;
                default:
                    log.error("Unknown symbol in '{}'", top);
            }
        }
        log.info("Cypher:\n{}", parts);

        final GraphDatabaseService graphDb = graphDb();
        try (final Transaction tx = graphDb.beginTx()) {
            log.info("Ensuring constraints and indexes...");
            graphDb.execute("CREATE CONSTRAINT ON (n:opencog_Concept) ASSERT n.href IS UNIQUE");
            graphDb.execute("CREATE CONSTRAINT ON (n:opencog_Gene) ASSERT n.href IS UNIQUE");
            graphDb.execute("CREATE CONSTRAINT ON (n:opencog_Predicate) ASSERT n.href IS UNIQUE");
            graphDb.execute("CREATE CONSTRAINT ON (n:opencog_Phrase) ASSERT n.href IS UNIQUE");

            graphDb.execute("CREATE INDEX ON :opencog_Concept(prefLabel)");
            graphDb.execute("CREATE INDEX ON :opencog_Gene(prefLabel)");
            graphDb.execute("CREATE INDEX ON :opencog_Predicate(prefLabel)");
            graphDb.execute("CREATE INDEX ON :opencog_Phrase(prefLabel)");

            tx.success();
        }
        log.info("Ensured constraints and indexes.");

        try (final Transaction tx = graphDb.beginTx()) {
            log.info("Executing Cypher ...");
            graphDb.execute(parts.getAllCypher(), parts.getAllParams());
            tx.success();
        }
        log.info("Done");
    }

    @Bean(destroyMethod = "shutdown")
    public GraphDatabaseService graphDb() {
        return new GraphDatabaseFactory().newEmbeddedDatabase(System.getProperty("user.home") + "/tmp/opencog-neo4j");
    }

}
