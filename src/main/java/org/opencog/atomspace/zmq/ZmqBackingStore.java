package org.opencog.atomspace.zmq;

import com.google.common.base.Verify;
import com.google.common.util.concurrent.*;
import org.apache.camel.*;
import org.apache.camel.spi.Synchronization;
import org.opencog.atomspace.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Proxies {@link GraphBackingStore} calls through ZeroMQ.
 */
@Service
@Profile({"clientapp", "zeromqstore"})
public class ZmqBackingStore extends GraphBackingStoreBase {

    private static final Logger log = LoggerFactory.getLogger(ZmqBackingStore.class);
    public static final AtomSpaceProtos.ZMQAttentionValueHolderMessage ATTENTIONVALUE_BLANK =
            AtomSpaceProtos.ZMQAttentionValueHolderMessage.newBuilder()
                    .setSTI(0)
                    .setLTI(0)
                    .setVLTI(0).build();

    @Inject
    private Environment env;
    @Inject
    private CamelContext camelContext;
    private ProducerTemplate producerTemplate;

//    private Map<UUID, SettableFuture<GeneratedMessage>> pendings = new ConcurrentHashMap<>();

//    private ListeningExecutorService executorService;

    @PostConstruct
    public void init() throws Exception {
//        executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        producerTemplate = camelContext.createProducerTemplate();
        final String zmqHost = env.getRequiredProperty("zeromq.host");
        final int zmqPort = env.getRequiredProperty("zeromq.port", Integer.class);
        final String zmqTopic = env.getRequiredProperty("zeromq.topic");
        producerTemplate.setDefaultEndpointUri("zeromq:tcp://" + zmqHost + ":" + zmqPort + "?messageConvertor=org.opencog.atomspace.ProtoMessageConvertor&socketType=REQ&topics=" + zmqTopic);
    }

    @PreDestroy
    public void close() throws Exception {
        producerTemplate.stop();
//        executorService.shutdown();
//        pendings.forEach((key, value) -> value.setException(new AtomSpaceException("Request " + key + " abandoned because ZeroMQ Backing Store is shutting down.")));
//        pendings.clear();
    }

//    @Handler
//    public void handleMessage(@Body AtomSpaceProtos.AtomsResult atomsResult) {
//        final UUID correlationId = UuidUtils.fromByteArray(atomsResult.getCorrelationId().toByteArray());
//        final SettableFuture<GeneratedMessage> pending = pendings.get(correlationId);
//        if (pending != null) {
//            pendings.remove(correlationId);
//            pending.set(atomsResult);
//        } else {
//            log.warn("Cannot find pending AtomsRequest with correlation id '{}'", correlationId);
//        }
//    }

    @Override
    public ListenableFuture<List<Atom>> getAtomsAsync(List<AtomRequest> reqs) {
        final SettableFuture<List<Atom>> atomsFuture = SettableFuture.create();
//        final SettableFuture<AtomSpaceProtos.AtomsResult> msgFuture = SettableFuture.create();
//        Futures.addCallback(msgFuture, new FutureCallback<AtomSpaceProtos.AtomsResult>() {
//            @Override
//            public void onSuccess(AtomSpaceProtos.AtomsResult result) {
//                final AtomSpaceProtos.AtomResult first = result.getResults(0);
//                switch (first.getKind()) {
//                    case NOT_FOUND:
//                        nodeFuture.set(Optional.empty());
//                        break;
//                    case NODE:
//                        nodeFuture.set(Optional.of(new Node(AtomType.forUpperCamel(first.getAtomType()), first.getNodeName())));
//                        break;
//                    case LINK:
//                        final List<GenericHandle> outgoingSet = first.getOutgoingSetList().stream().map(it -> new GenericHandle(it))
//                                .collect(Collectors.toList());
//                        final Link link = new Link(AtomType.forUpperCamel(first.getAtomType()), outgoingSet);
//                        throw new IllegalStateException("Expected node, but got link " + link);
//                    default:
//                        throw new IllegalArgumentException("Unknown AtomResult kind: " + first.getKind());
//                }
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
//                nodeFuture.setException(t);
//            }
//        });

//        final HashMap<Integer, String> atomTypeInfob = new HashMap<>();
//        reqs.stream().filter(it -> it.getKind() == AtomRequest.AtomRequestKind.NODE || it.getKind() == AtomRequest.AtomRequestKind.LINK)
//                .map(it -> it.getType().toUpperCamel())
//                .distinct()
//                .forEach(it -> atomTypeInfob.put(atomTypeInfob.size() + 1, it)); // must NOT be concurrent!
//        final ImmutableBiMap<Integer, String> atomTypeInfos = ImmutableBiMap.copyOf(atomTypeInfob);
        final AtomSpaceProtos.ZMQRequestMessage protoReqs = AtomSpaceProtos.ZMQRequestMessage.newBuilder()
                .setFunction(AtomSpaceProtos.ZMQFunctionType.ZMQgetAtoms)
//                .addAllAtomType(atomTypeInfos.entrySet().stream().map(it ->
//                        AtomSpaceProtos.ZMQAtomTypeInfo.newBuilder()
//                            .setId()))
                .addAllFetch(reqs.stream().map(javaReq -> {
                    final AtomSpaceProtos.ZMQAtomFetch.Builder b = AtomSpaceProtos.ZMQAtomFetch.newBuilder()
                            .setKind(javaReq.getKind().toProto())
                            .setHandle(javaReq.getUuid());
                    if (javaReq.getType() != null) {
                        b.setType(javaReq.getType().getId());
                    }
                    if (javaReq.getName() != null) {
                        b.setName(javaReq.getName());
                    }
                    if (javaReq.getHandleSeq() != null) {
                        b.addAllOutgoing(javaReq.getHandleSeq());
                    }
                    return b.build();
                }).collect(Collectors.toList()))
                .build();
        producerTemplate.asyncCallbackRequestBody(producerTemplate.getDefaultEndpoint(),
                protoReqs, new Synchronization() {
                    @Override
                    public void onComplete(Exchange exchange) {
                        final AtomSpaceProtos.ZMQReplyMessage atomsResult;
                        try {
                            atomsResult = AtomSpaceProtos.ZMQReplyMessage.parseFrom(exchange.getIn().getBody(byte[].class));
                            log.debug("Received {}", atomsResult);
                            final List<Atom> atoms = atomsResult.getAtomList().stream().map(res -> {
                                switch (res.getAtomtype()) {
                                    case ZMQAtomTypeNotFound:
                                        return null;
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
                            log.debug("Got {} atoms: {}", atoms.size(), atoms.stream().limit(10).toArray());
                            atomsFuture.set(atoms);
                        } catch (Exception e) {
                            atomsFuture.setException(e);
                        }
                    }

                    @Override
                    public void onFailure(Exchange exchange) {
                        atomsFuture.setException(exchange.getException());
                    }
                });
//        log.info("Request: {}", producerTemplate.requestBody(reqs));
        return atomsFuture;
    }

    @Override
    public List<Handle> getIncomingSet(Handle handle) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListenableFuture<Integer> storeAtomsAsync(List<Handle> handles) {
        final AtomSpaceProtos.ZMQRequestMessage.Builder reqMsgb = AtomSpaceProtos.ZMQRequestMessage.newBuilder();
        reqMsgb.setFunction(AtomSpaceProtos.ZMQFunctionType.ZMQstoreAtoms);
        handles.forEach(handle -> {
            final Atom atom = handle.resolve().get();
            //final Optional<Node> node = neo4jBs.getNode(AtomType.forUpperCamel(req.getAtomType()), req.getNodeName());
            if (atom instanceof Node) {
                reqMsgb.addAtom(AtomSpaceProtos.ZMQAtomMessage.newBuilder()
                        .setHandle(atom.getUuid())
                        .setAtomtype(AtomSpaceProtos.ZMQAtomType.ZMQAtomTypeNode)
                                //.setAtomTypeStr(atom.getType().toUpperCamel()) // no longer needed
                        .setType(atom.getType().getId())
                        .setTruthValue(AtomSpaceProtos.ZMQTruthValueMessage.newBuilder()
                                .addSingleTruthValue(AtomSpaceProtos.ZMQSingleTruthValueMessage.newBuilder()
                                        .setTruthvaluetype(AtomSpaceProtos.ZMQTruthValueType.ZMQTruthValueTypeSimple)
                                        .setMean((float) atom.getTruthValue().getFuzzyStrength())
                                        .setConfidence((float) atom.getTruthValue().getConfidence())
                                        .setCount((float) atom.getTruthValue().getCount())
                                        .build()))
                        .setAttentionvalueholder(ATTENTIONVALUE_BLANK) // no need to send this anyway
                        .setName(((Node) atom).getName())
                        .build());
            } else if (atom instanceof Link) {
                Verify.verify(((Link) atom).getOutgoingSet() != null,
                        "Link %s outgoingSet cannot be null", atom.getType());
                reqMsgb.addAtom(AtomSpaceProtos.ZMQAtomMessage.newBuilder()
                        .setHandle(atom.getUuid())
                        .setAtomtype(AtomSpaceProtos.ZMQAtomType.ZMQAtomTypeLink)
                                //.setAtomTypeStr(atom.getType().toUpperCamel()) // no longer needed
                        .setType(atom.getType().getId())
                        .setTruthValue(AtomSpaceProtos.ZMQTruthValueMessage.newBuilder()
                                .addSingleTruthValue(AtomSpaceProtos.ZMQSingleTruthValueMessage.newBuilder()
                                        .setTruthvaluetype(AtomSpaceProtos.ZMQTruthValueType.ZMQTruthValueTypeSimple)
                                        .setMean((float) atom.getTruthValue().getFuzzyStrength())
                                        .setConfidence((float) atom.getTruthValue().getConfidence())
                                        .setCount((float) atom.getTruthValue().getCount())
                                        .build()))
                        .setAttentionvalueholder(ATTENTIONVALUE_BLANK) // no need to send this anyway
                        .addAllOutgoing(((Link) atom).getOutgoingSet().stream().map(Handle::getUuid).collect(Collectors.toList()))
                        .build());
            } else {
                reqMsgb.addAtom(AtomSpaceProtos.ZMQAtomMessage.newBuilder()
                        .setAtomtype(AtomSpaceProtos.ZMQAtomType.ZMQAtomTypeNotFound)
                        .setHandle(Handle.UNDEFINED)
                        .setType(0)
                        .setAttentionvalueholder(ATTENTIONVALUE_BLANK) // unfortunately we have to fill this :(
                        .build());
            }
        });

        final AtomSpaceProtos.ZMQRequestMessage reqMsg = reqMsgb.build();
        final SettableFuture<Integer> future = SettableFuture.create();
//        log.info("Request: {}", producerTemplate.requestBody(reqMsg));
        producerTemplate.asyncCallbackRequestBody(producerTemplate.getDefaultEndpoint(),
                reqMsg, new Synchronization() {
                    @Override
                    public void onComplete(Exchange exchange) {
                        final AtomSpaceProtos.ZMQReplyMessage replyMsg;
                        try {
                            replyMsg = AtomSpaceProtos.ZMQReplyMessage.parseFrom(exchange.getIn().getBody(byte[].class));
                            log.debug("Received {}", replyMsg);
                            future.set(handles.size());
                        } catch (Exception e) {
                            future.setException(e);
                        }
                    }

                    @Override
                    public void onFailure(Exchange exchange) {
                        future.setException(exchange.getException());
                    }
                });
        return future;
    }

    @Override
    public Integer loadType(String atomTable, AtomType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void barrier() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isTypeIgnored(AtomType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAtomIgnored(Handle handle) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListenableFuture<Integer> storeAtomsAsyncFromAtomList(List<Atom> atoms) {
        throw new UnsupportedOperationException();
    }
}
