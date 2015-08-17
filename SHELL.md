# Shell

`atomspace-neo4j` is the Neo4j AtomSpace Backing Store daemon which serves AtomSpace via ZeroMQ, and also provides SSH shell.

`atomspace` is the AtomSpace ZeroMQ client, providing a shell to connect to an AtomSpace Server such as `atomspace-neo4j`.

## atomspace-neo4j

Running:

    ./atomspace-neo4j NEO4J_DB_LOCATION
    
e.g. 

    ./atomspace-neo4j $HOME/opencog-data/go.neo4j

## Connecting via SSH

    ssh -p2000 user@localhost
    
Password is `opencog`

## Commands

### node get

    node get ConceptNode/GO:0000024
    
    > node get ConceptNode/GO:0000024 ConceptNode/GO:0000025 ConceptNode/GO:0000026
    (ConceptNode "GO:0000024") ; 3084893020920648735
    (ConceptNode "GO:0000025") ; 4992395876887606960
    (ConceptNode "GO:0000026") ; -1838318155284644836
    
### atom get

    > atom get 3084893020920648735 -1824963123531192610 8076551278925386278 -1838318155284644836

### node/link store

    node store ConceptNode/human ConceptNode/mammal
    # outputs UUIDs
    link store InheritanceLink UUID1 UUID2 
