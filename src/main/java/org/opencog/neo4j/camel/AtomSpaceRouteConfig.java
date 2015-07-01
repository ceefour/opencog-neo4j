package org.opencog.neo4j.camel;

import com.google.protobuf.ByteString;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.opencog.atomspace.*;
import org.opencog.neo4j.Neo4jGraphBackingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Camel routes configuration.
 */
@Configuration
@Profile("camel")
public class AtomSpaceRouteConfig {

    private static final Logger log = LoggerFactory.getLogger(AtomSpaceRouteConfig.class);

    @Inject
    private Environment env;
    @Inject @Named("neo4jGraphBackingStore")
    private GraphBackingStore neo4jBs;

    @Bean
    public RouteBuilder neo4jRepRouteBuilder() {
        final String zmqHost = env.getRequiredProperty("zeromq.host");
        final int zmqPort = env.getRequiredProperty("zeromq.port", Integer.class);
        final String zmqTopic = env.getRequiredProperty("zeromq.topic");
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("zeromq:tcp://" + zmqHost + ":" + zmqPort + "?messageConvertor=org.opencog.atomspace.ProtoMessageConvertor&socketType=REP&topics=" + zmqTopic)
                        .to("log:atomspace-in?showAll=true&multiline=true")
                        .process((Exchange xc) -> {
                            final byte[] bytes = xc.getIn().getBody(byte[].class);
                            log.debug("Received {} bytes", bytes.length);
                            final AtomSpaceProtos.AtomsRequest reqs = AtomSpaceProtos.AtomsRequest.parseFrom(bytes);
                            xc.getIn().setBody(reqs);
                        }).to("log:atomspace-in-parsed?showAll=true&multiline=true")
                        .process((Exchange xc) -> {
                            final AtomSpaceProtos.AtomsRequest inp = xc.getIn().getBody(AtomSpaceProtos.AtomsRequest.class);
                            final AtomSpaceProtos.AtomsResult.Builder atomsResultb = AtomSpaceProtos.AtomsResult.newBuilder()
                                    .setCorrelationId(inp.getCorrelationId());
                            // FIXME: catch exception and put into protobuf
                            final List<NodeRequest> nodeRequests = inp.getRequestsList().stream().map(it -> new NodeRequest(AtomType.forUpperCamel(it.getAtomType()), it.getNodeName()))
                                    .collect(Collectors.toList());
                            final List<Node> nodes = neo4jBs.getNodesAsync(nodeRequests).get();
                            nodes.forEach(node -> {
                                //final Optional<Node> node = neo4jBs.getNode(AtomType.forUpperCamel(req.getAtomType()), req.getNodeName());
                                if (node != null) {
                                    atomsResultb.addResults(AtomSpaceProtos.AtomResult.newBuilder()
                                            .setKind(AtomSpaceProtos.AtomResult.ResultKind.NODE)
                                            .setAtomType(node.getType().toUpperCamel())
                                            .setNodeName(node.getName()).build());
                                } else {
                                    atomsResultb.addResults(AtomSpaceProtos.AtomResult.newBuilder()
                                            .setKind(AtomSpaceProtos.AtomResult.ResultKind.NOT_FOUND).build());
                                }
                            });
                            xc.getIn().setBody(atomsResultb.build());
                        }).to("log:atomspace-replied?showAll=true&multiline=true");
            }
        };
    }

//    @Bean
    public RouteBuilder timerRouteBuilder() {
        final String zmqHost = env.getRequiredProperty("zeromq.host");
        final int zmqPort = env.getRequiredProperty("zeromq.port", Integer.class);
        final String zmqTopic = env.getRequiredProperty("zeromq.topic");
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("timer:?period=5s")
                        .process(it -> {
                            final UUID correlationId = UUID.randomUUID();
                            it.getIn().setHeader("comment", "Sending " + correlationId);
                            final AtomSpaceProtos.AtomRequest req1 = AtomSpaceProtos.AtomRequest.newBuilder()
                                    .setKind(AtomSpaceProtos.AtomRequest.AtomRequestKind.NODE)
                                    .setAtomType("ConceptNode")
                                    .setNodeName("GO:0000024")
                                    .build();
                            final AtomSpaceProtos.AtomRequest req2 = AtomSpaceProtos.AtomRequest.newBuilder()
                                    .setKind(AtomSpaceProtos.AtomRequest.AtomRequestKind.NODE)
                                    .setAtomType("ConceptNode")
                                    .setNodeName("GO:0000025")
                                    .build();
                            final AtomSpaceProtos.AtomsRequest reqs = AtomSpaceProtos.AtomsRequest.newBuilder()
                                    .setCorrelationId(ByteString.copyFrom(UuidUtils.toByteArray(correlationId)))
                                    .addRequests(req1).addRequests(req2).build();
                            it.getIn().setBody(reqs);
                        })
                        .to("zeromq:tcp://" + zmqHost + ":" + zmqPort + "?messageConvertor=org.opencog.atomspace.ProtoMessageConvertor&socketType=REQ&topics=" + zmqTopic)
//                        .to("zeromq:tcp://" + zmqHost + ":" + zmqPort + "?socketType=PUSH&topics=" + zmqTopic);
                        .process(it -> it.getIn().setBody(AtomSpaceProtos.AtomsResult.parseFrom(it.getIn().getBody(byte[].class))))
                        .to("log:timer-to-atomspace?showAll=true&multiline=true");
            }
        };
    }

}
