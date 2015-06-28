package org.opencog.atomspace;

import com.google.protobuf.GeneratedMessage;
import org.apache.camel.Exchange;
import org.apacheextras.camel.component.zeromq.MessageConverter;

/**
 * Created by ceefour on 6/28/15.
 */
public class ProtoMessageConvertor implements MessageConverter {
    @Override
    public byte[] convert(Exchange xc) {
        final GeneratedMessage msg = (GeneratedMessage) xc.getIn().getBody();
        return msg.toByteArray();
    }
}
