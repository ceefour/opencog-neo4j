# AtomSpace Protocol for ZeroMQ+Protobuf

## Generating Java Proxies

Requirements:
 
1. [Protobuf Compiler](https://developers.google.com/protocol-buffers/docs/downloads).
    In Ubuntu: `sudo apt-get install protobuf-compiler`

To generate, run:

    protoc -I=protobuf --java_out=src/main/java protobuf/ZMQMessages.proto
