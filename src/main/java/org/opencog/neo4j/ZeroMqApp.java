package org.opencog.neo4j;

import com.google.common.collect.ImmutableMap;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.opencog.neo4j.camel.AtomSpaceCamelConfiguration;
import org.opencog.neo4j.camel.AtomSpaceRouteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.inject.Inject;

/**
 * Runs {@link org.opencog.neo4j.camel.AtomSpaceRouteConfig}.
 */
@SpringBootApplication
//@Configuration
//@Import({AtomSpaceCamelConfiguration.class, AtomSpaceRouteConfig.class})
@Profile("zeromqapp")
public class ZeroMqApp implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ZeroMqApp.class);
    private static String[] args;

    public static void main(String[] args) {
        ZeroMqApp.args = args;
        new SpringApplicationBuilder(ZeroMqApp.class)
                .properties(ImmutableMap.of("org.neo4j.server.database.location", args[1]))
                .profiles("zeromqapp", "camel")
                .web(false)
                .run(args);
    }

    @Inject
    private Environment env;

    @Override
    public void run(String... args) throws Exception {
        log.info("Args: {}", args);
        log.info("Joining thread, you can press Ctrl+C to shutdown application");
        Thread.currentThread().join();
    }

    @Bean(destroyMethod = "shutdown")
    public GraphDatabaseService graphDb() {
        final String dbDir = env.getRequiredProperty("org.neo4j.server.database.location");
        log.info("Opening Neo4j database '{}'", dbDir);
        return new GraphDatabaseFactory().newEmbeddedDatabase(dbDir);//System.getProperty("user.home") + "/tmp/opencog-neo4j");
    }

}
