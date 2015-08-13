# AtomSpace Protocol for ZeroMQ+Protobuf

## Generating Java Proxies

Requirements:
 
1. [Protobuf Compiler](https://developers.google.com/protocol-buffers/docs/downloads).
    In Ubuntu: `sudo apt-get install protobuf-compiler`

To generate, run:

    protoc -I=protobuf --java_out=src/main/java protobuf/ZMQMessages.proto

## ZeroMQ Server Application

Launch `ZeroMqApp_go` which will run `org.opencog.neo4j.ZeroMqApp` class using database in `~/opencog-data/go.neo4j`.

Connect via SSH: `ssh -p2000 user@localhost`

Password: `opencog`

More info about shell commands like `node get`, etc. see `SHELL.md`
