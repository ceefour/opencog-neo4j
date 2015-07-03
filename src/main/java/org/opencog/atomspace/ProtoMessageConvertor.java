package org.opencog.atomspace;

import com.google.common.base.Preconditions;
import com.google.protobuf.GeneratedMessage;
import org.apache.camel.Exchange;
import org.apacheextras.camel.component.zeromq.MessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Created by ceefour on 6/28/15.
 */
public class ProtoMessageConvertor implements MessageConverter {
    private static final Logger log = LoggerFactory.getLogger(ProtoMessageConvertor.class);

    @Override
    public byte[] convert(Exchange xc) {
        final GeneratedMessage msg = Preconditions.checkNotNull((xc.hasOut() ? xc.getOut() : xc.getIn()).getBody(GeneratedMessage.class),
                "Invalid message body: Body cannot be null and must be protobuf GeneratedMessage");
        final byte[] bytes = msg.toByteArray();
//        if (msg instanceof AtomSpaceProtos.AtomsRequest) {
//            log.debug("Converting AtomsRequest correlationId={} to {} bytes: {}",
//                    UuidUtils.fromByteArray(((AtomSpaceProtos.AtomsRequest) msg).getCorrelationId().toByteArray()),
//                    bytes.length, StandardCharsets.ISO_8859_1.decode(ByteBuffer.wrap(bytes)));
//        }
        return bytes;
    }
}
