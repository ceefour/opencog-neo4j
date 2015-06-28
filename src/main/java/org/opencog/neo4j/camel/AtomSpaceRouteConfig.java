package org.opencog.neo4j.camel;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.opencog.atomspace.AtomSpaceProtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.UUID;

/**
 * Camel routes configuration.
 */
@Configuration
@Profile("camel")
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
                            final byte[] bytes = (byte[]) xc.getIn().getBody();
                            final AtomSpaceProtos.AtomsRequest reqs = AtomSpaceProtos.AtomsRequest.parseFrom(bytes);
                            xc.getIn().setBody(reqs);
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
                        .process((Exchange it) -> {
                            it.getIn().setHeader("comment", "Sending " + UUID.randomUUID());
                            final AtomSpaceProtos.AtomRequest req = AtomSpaceProtos.AtomRequest.newBuilder()
                                    .setKind(AtomSpaceProtos.AtomRequest.AtomRequestKind.NODE)
                                    .setAtomType("GeneNode")
                                    .setNodeName("something")
                                    .build();
                            final AtomSpaceProtos.AtomsRequest reqs = AtomSpaceProtos.AtomsRequest.newBuilder().addRequests(req).build();
                            it.getIn().setBody(reqs);
                        })
                        .to("zeromq:tcp://127.0.0.1:5555?messageConvertor=org.opencog.atomspace.ProtoMessageConvertor&socketType=PUSH&topics=atomspace.neo4j")
                        .to("log:timer-to-atomspace?showAll=true&multiline=true");
            }
        };
    }

}
