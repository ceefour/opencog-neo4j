package org.opencog.neo4j.camel;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

/**
 * Camel routes configuration.
 */
@Configuration
public class AtomSpaceRouteConfig {

    private static final Logger log = LoggerFactory.getLogger(AtomSpaceRouteConfig.class);

    @Bean
    public RouteBuilder neo4jPullRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("zeromq:tcp://127.0.0.1:5555?socketType=PULL&topics=atomspace.neo4j")
                        .to("log:atomspace-in?showAll=true&multiline=true")
                        .process((Exchange xc) -> {


                        }).to("log:atomspace-out?showAll=true&multiline=true");
            }
        };
    }

    @Bean
    public RouteBuilder timerRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("timer:5")
                        .process((Exchange it) -> { it.getIn().setBody("Sending " + UUID.randomUUID()); })
                        .to("zeromq:tcp://127.0.0.1:5555?socketType=PUSH&topics=atomspace.neo4j")
                        .to("log:timer-to-atomspace?showAll=true&multiline=true");
            }
        };
    }

}
