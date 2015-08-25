package org.opencog.neo4j.camel;

import com.google.common.base.Verify;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.opencog.atomspace.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Camel routes configuration.
 */
@Configuration
@Profile("camel")
public class AtomSpaceRouteConfig {

    private static final Logger log = LoggerFactory.getLogger(AtomSpaceRouteConfig.class);
    public static final AtomSpaceProtos.ZMQAttentionValueHolderMessage ATTENTIONVALUE_BLANK =
            AtomSpaceProtos.ZMQAttentionValueHolderMessage.newBuilder()
                .setSTI(0)
                .setLTI(0)
                .setVLTI(0).build();

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
                            final AtomSpaceProtos.ZMQRequestMessage reqs = AtomSpaceProtos.ZMQRequestMessage.parseFrom(bytes);
                            xc.getIn().setBody(reqs);
                        }).to("log:atomspace-in-parsed?showAll=true&multiline=true")
                        .process((Exchange xc) -> {
                            final AtomSpaceProtos.ZMQRequestMessage inp = xc.getIn().getBody(AtomSpaceProtos.ZMQRequestMessage.class);
                            final AtomSpaceProtos.ZMQReplyMessage.Builder atomsResultb = AtomSpaceProtos.ZMQReplyMessage.newBuilder();
                            switch (inp.getFunction()) {
                                case ZMQgetAtoms:
                                    final List<AtomRequest> nodeRequests = inp.getFetchList().stream()
                                            .map(it -> {
                                                switch (it.getKind()) {
                                                    case UUID:
                                                        return new AtomRequest(it.getHandle());
                                                    case NODE:
                                                        return new AtomRequest(AtomType.forId(it.getType()), it.getName());
                                                    case LINK:
                                                        return new AtomRequest(AtomType.forId(it.getType()), it.getOutgoingList());
                                                    default:
                                                        throw new IllegalArgumentException("Unknown request kind: " + it.getKind());
                                                }
                                            }).collect(Collectors.toList());
                                    final List<Atom> atoms = neo4jBs.getAtomsAsync(nodeRequests).get();
                                    atoms.forEach(atom -> {
                                        //final Optional<Node> node = neo4jBs.getNode(AtomType.forUpperCamel(req.getAtomType()), req.getNodeName());
                                        if (atom instanceof Node) {
                                            atomsResultb.addAtom(AtomSpaceProtos.ZMQAtomMessage.newBuilder()
                                                    .setHandle(atom.getUuid())
                                                    .setAtomtype(AtomSpaceProtos.ZMQAtomType.ZMQAtomTypeNode)
                                                            //.setAtomTypeStr(atom.getType().toUpperCamel()) // no longer needed
                                                    .setType(atom.getType().getId())
                                                    //.setAttentionvalueholder(ATTENTIONVALUE_BLANK) // FIXME: get from atom
                                                    .setName(((Node) atom).getName())
                                                    .build());
                                        } else if (atom instanceof Link) {
                                            Verify.verify(((Link) atom).getOutgoingSet() != null,
                                                    "Link %s outgoingSet cannot be null", atom.getType());
                                            atomsResultb.addAtom(AtomSpaceProtos.ZMQAtomMessage.newBuilder()
                                                    .setHandle(atom.getUuid())
                                                    .setAtomtype(AtomSpaceProtos.ZMQAtomType.ZMQAtomTypeLink)
                                                            //.setAtomTypeStr(atom.getType().toUpperCamel()) // no longer needed
                                                    .setType(atom.getType().getId())
                                                    //.setAttentionvalueholder(ATTENTIONVALUE_BLANK) // FIXME: get from atom
                                                    .addAllOutgoing(((Link) atom).getOutgoingSet().stream().map(Handle::getUuid).collect(Collectors.toList()))
                                                    .build());
                                        } else {
                                            atomsResultb.addAtom(AtomSpaceProtos.ZMQAtomMessage.newBuilder()
                                                    .setAtomtype(AtomSpaceProtos.ZMQAtomType.ZMQAtomTypeNotFound)
                                                    //.setHandle(Handle.UNDEFINED)
                                                    //.setType(0)
                                                    //.setAttentionvalueholder(ATTENTIONVALUE_BLANK) // unfortunately we have to fill this :(
                                                    .build());
                                        }
                                    });
                                    xc.getIn().setBody(atomsResultb.build());
                                    break;
                                case ZMQstoreAtoms:
                                    final List<Atom> atomsToStore = inp.getAtomList().stream().map(res -> {
                                        switch (res.getAtomtype()) {
                                            case ZMQAtomTypeNode:
                                                return new Node(res.getHandle(), AtomType.forId(res.getType()), res.getName());
                                            case ZMQAtomTypeLink:
                                                final List<Handle> outgoingSet = res.getOutgoingList().stream()
                                                        .map(it -> new Handle(it))
                                                        .collect(Collectors.toList());
                                                final Link link = new Link(res.getHandle(), AtomType.forId(res.getType()), outgoingSet);
                                                return link;
                                            default:
                                                throw new IllegalArgumentException("Unknown AtomResult kind: " + res.getAtomtype());
                                        }
                                    }).collect(Collectors.toList());
                                    final Integer storedCount = neo4jBs.storeAtomsAsyncFromAtomList(atomsToStore).get();
                                    xc.getIn().setBody(atomsResultb.build());
                                    break;
                                default:
                                    throw new UnsupportedOperationException("Function not supported: " + inp.getFunction());
                            }
                        }).to("log:atomspace-replied?showAll=true&multiline=true")
                        .onException(Exception.class)
                        .process(xc -> {
                            log.error("Error processing", xc.getException());
                            // FIXME: catch exception and put into protobuf
                            xc.getIn().setBody(AtomSpaceProtos.ZMQReplyMessage.newBuilder().build());
                        });
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
                            final AtomSpaceProtos.ZMQAtomFetch req1 = AtomSpaceProtos.ZMQAtomFetch.newBuilder()
                                    .setKind(AtomSpaceProtos.ZMQAtomFetchKind.NODE)
                                    // FIXME: use the int<->UUID mapping
//                                    .setAtomType("ConceptNode")
                                    .setName("GO:0000024")
                                    .build();
                            final AtomSpaceProtos.ZMQAtomFetch req2 = AtomSpaceProtos.ZMQAtomFetch.newBuilder()
                                    .setKind(AtomSpaceProtos.ZMQAtomFetchKind.NODE)
                                    // FIXME: use the int<->UUID mapping
//                                    .setAtomType("ConceptNode")
                                    .setName("GO:0000025")
                                    .build();
                            final AtomSpaceProtos.ZMQRequestMessage reqs = AtomSpaceProtos.ZMQRequestMessage.newBuilder()
                                    .addFetch(req1).addFetch(req2).build();
                            it.getIn().setBody(reqs);
                        })
                        .to("zeromq:tcp://" + zmqHost + ":" + zmqPort + "?messageConvertor=org.opencog.atomspace.ProtoMessageConvertor&socketType=REQ&topics=" + zmqTopic)
//                        .to("zeromq:tcp://" + zmqHost + ":" + zmqPort + "?socketType=PUSH&topics=" + zmqTopic);
                        .process(it -> it.getIn().setBody(AtomSpaceProtos.ZMQReplyMessage.parseFrom(it.getIn().getBody(byte[].class))))
                        .to("log:timer-to-atomspace?showAll=true&multiline=true");
            }
        };
    }

}
