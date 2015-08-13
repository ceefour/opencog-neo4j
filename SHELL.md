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
    (ConceptNode "GO:0000024") ; 5434235504711159139
    (ConceptNode "GO:0000025") ; -6750899309375792365
    (ConceptNode "GO:0000026") ; -6794468188338365771
    
### atom get

    > atom get 5434235504711159139 -8495518123530697371 8785363810015061186 6978320295718623985 -6794468188338365771
