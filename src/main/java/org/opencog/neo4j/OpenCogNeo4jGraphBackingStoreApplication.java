package org.opencog.neo4j;

import clojure.lang.LineNumberingPushbackReader;
import clojure.lang.LispReader;
import clojure.lang.Symbol;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Profile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public void run(String... args) throws Exception {
        final File schemeFile = new File("/data/project_netadm/opencog/neo4j/Bio_schemeFiles/mmc4.scm");
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
                // TODO: implement InheritanceLink
                case "InheritanceLink":
                    log.info("INHERITANCE {}", top);
                    break;
                // TODO: implement ConceptNode
                case "ConceptNode":
                    log.info("CONCEPT {}", top);
                    break;
                // TODO: implement EvaluationLink
                case "EvaluationLink":
                    log.info("EVALUATION {}", top);
                    break;
                // TODO: implement PredicateNode
                case "PredicateNode":
                    log.info("PREDICATE {}", top);
                    break;
                // TODO: implement ListLink
                case "ListLink":
                    log.info("LIST {}", top);
                    break;
                // TODO: implement (MemberLink gene:GeneNode concept:ConceptNode)
                case "MemberLink":
                    log.info("MEMBER {}", top);
                    break;
                // TODO: implement GeneNode
                case "GeneNode":
                    log.info("GENE {}", top);
                    break;
                default:
                    log.error("Unknown symbol in '{}'", top);
            }
        }
    }



}
