package org.opencog.atomspace.zmq;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.inject.Inject;

/**
 * Configures Camel to route incoming ZeroMQ messages (replies) to {@link ZmqBackingStore}.
 */
@Configuration
@Profile({"clientapp", "zeromqstore"})
public class ZmqGraphBackingStoreConfig {

    @Inject
    private Environment env;

//    @Bean
//    public RouteBuilder neo4jPullRouteBuilder() {
//        final String zmqHost = env.getRequiredProperty("zeromq.host");
//        final int zmqPort = env.getRequiredProperty("zeromq.port", Integer.class);
//        final String zmqTopic = env.getRequiredProperty("zeromq.topic");
//        return new RouteBuilder() {
//            @Override
//            public void configure() throws Exception {
//                from("zeromq:tcp://" + zmqHost + ":" + zmqPort + "?socketType=PULL&topics=" + zmqTopic)
//                        .to("log:atomspace-in?showAll=true&multiline=true")
//                        .process((Exchange xc) -> {
//                            final byte[] bytes = (byte[]) xc.getIn().getBody();
//                            log.debug("Received {} bytes", bytes.length);
//                            final AtomSpaceProtos.AtomsRequest reqs = AtomSpaceProtos.AtomsRequest.parseFrom(bytes);
//                            xc.getIn().setBody(reqs);
//                        }).to("log:atomspace-out?showAll=true&multiline=true");
//            }
//        };
//    }

}
