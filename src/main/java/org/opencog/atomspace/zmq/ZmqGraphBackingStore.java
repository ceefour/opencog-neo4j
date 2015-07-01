package org.opencog.atomspace.zmq;

import com.google.common.util.concurrent.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.camel.*;
import org.apache.camel.spi.Synchronization;
import org.apache.camel.support.SynchronizationAdapter;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Proxies {@link GraphBackingStore} calls through ZeroMQ.
 */
@Service
@Profile({"clientapp", "zeromqstore"})
public class ZmqGraphBackingStore implements GraphBackingStore, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ZmqGraphBackingStore.class);

    @Inject
    private Environment env;
    @Inject
    private CamelContext camelContext;
    private ProducerTemplate producerTemplate;

    private Map<UUID, SettableFuture<GeneratedMessage>> pendings = new ConcurrentHashMap<>();

    private ListeningExecutorService executorService;

    @PostConstruct
    public void init() throws Exception {
        executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        producerTemplate = camelContext.createProducerTemplate();
        final String zmqHost = env.getRequiredProperty("zeromq.host");
        final int zmqPort = env.getRequiredProperty("zeromq.port", Integer.class);
        final String zmqTopic = env.getRequiredProperty("zeromq.topic");
        producerTemplate.setDefaultEndpointUri("zeromq:tcp://" + zmqHost + ":" + zmqPort + "?messageConvertor=org.opencog.atomspace.ProtoMessageConvertor&socketType=REQ&topics=" + zmqTopic);
    }

    @PreDestroy
    public void close() throws Exception {
        producerTemplate.stop();
        executorService.shutdown();
        pendings.forEach((key, value) -> value.setException(new AtomSpaceException("Request " + key + " abandoned because ZeroMQ Backing Store is shutting down.")));
        pendings.clear();
    }

    @Handler
    public void handleMessage(@Body AtomSpaceProtos.AtomsResult atomsResult) {
        final UUID correlationId = UuidUtils.fromByteArray(atomsResult.getCorrelationId().toByteArray());
        final SettableFuture<GeneratedMessage> pending = pendings.get(correlationId);
        if (pending != null) {
            pendings.remove(correlationId);
            pending.set(atomsResult);
        } else {
            log.warn("Cannot find pending AtomsRequest with correlation id '{}'", correlationId);
        }
    }

    @Override
    public Optional<Link> getLink(AtomType type, List<Handle> handleSeq) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Node> getNode(AtomType type, String name) {
        try {
            return getNodeAsync(type, name).get();
        } catch (Exception e) {
            throw new AtomSpaceException(e, "Cannot get node %s/%s", type, name);
        }
    }

    @Override
    public ListenableFuture<List<Node>> getNodesAsync(List<NodeRequest> reqs) {
        return null;
    }

    @Override
    public ListenableFuture<Optional<Node>> getNodeAsync(AtomType type, String name) {
        final SettableFuture<Optional<Node>> nodeFuture = SettableFuture.create();
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
        final AtomSpaceProtos.AtomRequest req = AtomSpaceProtos.AtomRequest.newBuilder()
                .setKind(AtomSpaceProtos.AtomRequest.AtomRequestKind.NODE)
                .setAtomType(type.toUpperCamel())
                .setNodeName(name)
                .build();
        final UUID correlationId = UUID.randomUUID();
//        pendings.put(correlationId, (SettableFuture) msgFuture);
        final AtomSpaceProtos.AtomsRequest reqs = AtomSpaceProtos.AtomsRequest.newBuilder()
                .setCorrelationId(ByteString.copyFrom(UuidUtils.toByteArray(correlationId)))
                .addRequests(req)
                .build();
        producerTemplate.asyncCallbackRequestBody(producerTemplate.getDefaultEndpoint(),
                reqs, new Synchronization() {
                    @Override
                    public void onComplete(Exchange exchange) {
                        final AtomSpaceProtos.AtomsResult atomsResult;
                        try {
                            atomsResult = AtomSpaceProtos.AtomsResult.parseFrom(exchange.getIn().getBody(byte[].class));
                            final AtomSpaceProtos.AtomResult first = atomsResult.getResults(0);
                            switch (first.getKind()) {
                                case NOT_FOUND:
                                    nodeFuture.set(Optional.empty());
                                    break;
                                case NODE:
                                    nodeFuture.set(Optional.of(new Node(AtomType.forUpperCamel(first.getAtomType()), first.getNodeName())));
                                    break;
                                case LINK:
                                    final List<GenericHandle> outgoingSet = first.getOutgoingSetList().stream().map(it -> new GenericHandle(it))
                                            .collect(Collectors.toList());
                                    final Link link = new Link(AtomType.forUpperCamel(first.getAtomType()), outgoingSet);
                                    throw new IllegalStateException("Expected node, but got link " + link);
                                default:
                                    throw new IllegalArgumentException("Unknown AtomResult kind: " + first.getKind());
                            }
                        } catch (InvalidProtocolBufferException e) {
                            nodeFuture.setException(e);
                        }
                    }

                    @Override
                    public void onFailure(Exchange exchange) {
                        nodeFuture.setException(exchange.getException());
                    }
                });
//        log.info("Request: {}", producerTemplate.requestBody(reqs));
        return nodeFuture;
    }

    @Override
    public Optional<Atom> getAtom(Handle handle) {
        try {
            return getAtomAsync(handle).get();
        } catch (Exception e) {
            throw new AtomSpaceException(e, "Cannot get atom %s", handle);
        }
    }

    public ListenableFuture<Optional<Atom>> getAtomAsync(Handle handle) {
        final SettableFuture<Optional<Atom>> atomFuture = SettableFuture.create();
        final SettableFuture<AtomSpaceProtos.AtomsResult> msgFuture = SettableFuture.create();
        Futures.addCallback(msgFuture, new FutureCallback<AtomSpaceProtos.AtomsResult>() {
            @Override
            public void onSuccess(AtomSpaceProtos.AtomsResult result) {
                final AtomSpaceProtos.AtomResult first = result.getResults(0);
                switch (first.getKind()) {
                    case NOT_FOUND:
                        atomFuture.set(Optional.empty());
                        break;
                    case NODE:
                        atomFuture.set(Optional.of(new Atom(AtomType.forUpperCamel(first.getAtomType()))));
                        break;
                    case LINK:
                        final List<GenericHandle> outgoingSet = first.getOutgoingSetList().stream().map(it -> new GenericHandle(it))
                                .collect(Collectors.toList());
                        atomFuture.set(Optional.of(new Link(AtomType.forUpperCamel(first.getAtomType()), outgoingSet)));
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown AtomResult kind: " + first.getKind());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                atomFuture.setException(t);
            }
        });
        final AtomSpaceProtos.AtomRequest req = AtomSpaceProtos.AtomRequest.newBuilder()
                .setKind(AtomSpaceProtos.AtomRequest.AtomRequestKind.UUID)
                .setUuid(handle.getUuid())
                .build();
        final UUID correlationId = UUID.randomUUID();
        pendings.put(correlationId, (SettableFuture) msgFuture);
        final AtomSpaceProtos.AtomsRequest reqs = AtomSpaceProtos.AtomsRequest.newBuilder()
                .setCorrelationId(ByteString.copyFrom(UuidUtils.toByteArray(correlationId)))
                .addRequests(req)
                .build();
        producerTemplate.sendBody(reqs);
        return atomFuture;
    }

    @Override
    public List<Handle> getIncomingSet(Handle handle) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String storeAtom(Handle handle) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String loadType(String atomTable, AtomType type) {
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
}
