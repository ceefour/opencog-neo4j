package org.opencog.neo4j;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.opencog.atomspace.Atom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.neo4j.config.JtaTransactionManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.inject.Inject;

/**
 * Runs {@link org.opencog.neo4j.camel.AtomSpaceRouteConfig}.
 */
@SpringBootApplication
@EnableTransactionManagement
@ComponentScan(basePackageClasses = {ZeroMqApp.class, Atom.class})
//@Configuration
//@Import({AtomSpaceCamelConfiguration.class, AtomSpaceRouteConfig.class})
@Profile("zeromqapp")
public class ZeroMqApp implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ZeroMqApp.class);
    private static String[] args;

    public static void main(String[] args) {
        ZeroMqApp.args = args;
        Preconditions.checkArgument(args.length >= 1, "Argument required: NEO4J_DB_LOCATION");
        final String dbLocation = args[0];
        new SpringApplicationBuilder(ZeroMqApp.class)
                .properties(ImmutableMap.of("org.neo4j.server.database.location", dbLocation))
                .profiles("zeromqapp", "camel", "zeromqstore")
                .web(false)
                .run(args);
    }

    @Inject
    private Environment env;

    @Override
    public void run(String... args) throws Exception {
        log.info("Args: {}", (Object) args);
        log.info("Joining thread, you can press Ctrl+C to shutdown application");
        Thread.currentThread().join();
    }

    @Bean(destroyMethod = "shutdown")
    public GraphDatabaseService graphDb() {
        final String dbDir = env.getRequiredProperty("org.neo4j.server.database.location");
        log.info("Opening Neo4j database '{}'", dbDir);
        return new GraphDatabaseFactory().newEmbeddedDatabase(dbDir);//System.getProperty("user.home") + "/tmp/opencog-neo4j");
    }

    @Bean
    public Neo4jBackingStore neo4jGraphBackingStore() {
        return new Neo4jBackingStore(graphDb());
    }

    @Bean
    public JtaTransactionManagerFactoryBean transactionManager() {
        return new JtaTransactionManagerFactoryBean(graphDb());
    }


}
