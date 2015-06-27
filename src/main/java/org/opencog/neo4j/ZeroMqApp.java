package org.opencog.neo4j;

import org.opencog.neo4j.camel.AtomSpaceCamelConfiguration;
import org.opencog.neo4j.camel.AtomSpaceRouteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.inject.Inject;

/**
 * Runs {@link org.opencog.neo4j.camel.AtomSpaceRouteConfig}.
 */
@Configuration
@Import({AtomSpaceCamelConfiguration.class, AtomSpaceRouteConfig.class})
@Profile("zeromqapp")
public class ZeroMqApp implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ZeroMqApp.class);
    private static String[] args;

    public static void main(String[] args) {
        ZeroMqApp.args = args;
        new SpringApplicationBuilder(ZeroMqApp.class)
                .profiles("zeromqapp")
                .web(false)
                .run(args);
    }

    @Inject
    private Environment env;

    @Override
    public void run(String... args) throws Exception {
        log.info("Joining thread, you can press Ctrl+C to shutdown application");
        Thread.currentThread().join();
    }

}
