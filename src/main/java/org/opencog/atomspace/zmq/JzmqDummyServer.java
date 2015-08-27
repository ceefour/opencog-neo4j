package org.opencog.atomspace.zmq;

import org.apache.commons.io.HexDump;
import org.zeromq.ZMQ;

/**
 * Created by ceefour on 8/27/15.
 */
public class JzmqDummyServer {

    public static void main(String... args) throws Exception {
        try (final ZMQ.Context context = ZMQ.context(1)) {
            try (final ZMQ.Socket socket = context.socket(ZMQ.REP)) {
                socket.bind("tcp://127.0.0.1:5555");
                while (!Thread.currentThread ().isInterrupted()) {
                    byte[] request = socket.recv(0);
                    HexDump.dump(request, 0, System.out, 0);

                    String response = "World";
                    socket.send(response.getBytes(), 0);
                    Thread.sleep(1000); // Do some 'work'
                }
            }
        }
    }
}
