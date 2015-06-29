package org.opencog.atomspace;

import com.google.common.collect.ImmutableMap;
import org.opencog.neo4j.camel.AtomSpaceRouteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.inject.Inject;

/**
 * Runs {@link AtomSpaceRouteConfig}.
 */
@SpringBootApplication
//@Configuration
//@Import({AtomSpaceCamelConfiguration.class, AtomSpaceRouteConfig.class})
@Profile("shellapp")
public class ShellApp implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ShellApp.class);
    private static String[] args;

    public static void main(String[] args) {
        ShellApp.args = args;
        new SpringApplicationBuilder(ShellApp.class)
                .properties(ImmutableMap.of("org.neo4j.server.database.location", args[0]))
                .profiles("shellapp", "camel")
                .web(false)
                .run(args);
    }

    @Inject
    private Environment env;

    @Override
    public void run(String... args) throws Exception {
        log.info("Args: {}", args);
        //TODO: clojure.tools.nrepl.?
    }

}
