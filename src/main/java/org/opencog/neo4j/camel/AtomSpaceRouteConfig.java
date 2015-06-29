package org.opencog.neo4j.camel;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.opencog.atomspace.AtomSpaceProtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.inject.Inject;
import java.util.UUID;

/**
 * Camel routes configuration.
 */
@Configuration
@Profile("camel")
public class AtomSpaceRouteConfig {

    private static final Logger log = LoggerFactory.getLogger(AtomSpaceRouteConfig.class);

    @Inject
    private Environment env;

    @Bean
    public RouteBuilder neo4jPullRouteBuilder() {
        final String zmqHost = env.getRequiredProperty("zeromq.host");
        final int zmqPort = env.getRequiredProperty("zeromq.port", Integer.class);
        final String zmqTopic = env.getRequiredProperty("zeromq.topic");
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("zeromq:tcp://" + zmqHost + ":" + zmqPort + "?socketType=PULL&topics=" + zmqTopic)
                        .to("log:atomspace-in?showAll=true&multiline=true")
                        .process((Exchange xc) -> {
                            final byte[] bytes = (byte[]) xc.getIn().getBody();
                            final AtomSpaceProtos.AtomsRequest reqs = AtomSpaceProtos.AtomsRequest.parseFrom(bytes);
                            xc.getIn().setBody(reqs);
                        }).to("log:atomspace-out?showAll=true&multiline=true");
            }
        };
    }

    //@Bean
    public RouteBuilder timerRouteBuilder() {
        final String zmqHost = env.getRequiredProperty("zeromq.host");
        final int zmqPort = env.getRequiredProperty("zeromq.port", Integer.class);
        final String zmqTopic = env.getRequiredProperty("zeromq.topic");
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
                        .to("zeromq:tcp://" + zmqHost + ":" + zmqPort + "?messageConvertor=org.opencog.atomspace.ProtoMessageConvertor&socketType=PUSH&topics=" + zmqTopic)
                        .to("log:timer-to-atomspace?showAll=true&multiline=true");
            }
        };
    }

}
