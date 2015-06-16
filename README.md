# opencog-neo4j

Google Summer of Code 2015 Proposal to implement Neo4j Graph Backing Store as described in http://wiki.opencog.org/w/Neo4j_Backing_Store

## Bio scheme files

We'll use the [OpenCog Bio knowledge base](https://github.com/opencog/agi-bio/tree/master/knowledge-import)
(dataset in separate repo at `git@gitlab.com:opencog-bio/bio-data.git`), a moderately sized dataset about 212 MiB in size, to test performance.

## mmc4.scm patch

`mmc4.scm` must be patched as follows due to typo and `ConceptNode` name mismatch:

    (define count (count-all))
    (define message (string-append " Atoms loaded " "\n"))
    (display count)
    (display message)
    (define start_time (current-time))
    (InheritanceLink 
         (ConceptNode "Aging-Mythelation_GeneSet")
         (ConceptNode "Geneset")
    )
    
    (EvaluationLink 
         (PredicateNode "organism_of"
         (ListLink 
            (ConceptNode "Aging-Mythelation_GeneSet")
            (ConceptNode "Homo sapiens"))
         )
    )
    
    (EvaluationLink 
         (PredicateNode "source_PubMedID"
         (ListLink 
            (ConceptNode "Aging-Mythelation_GeneSet")
            (NumberNode "#23177740"))
         )
    )
    
    (EvaluationLink 
         (PredicateNode "brief_description_of"
         (ListLink 
            (ConceptNode "Aging-Mythelation_GeneSet")
            (PhraseNode "Genes Associated with Aging in Both the Methylome and th Transcript."))
         )
    )
    
## Generating the Neo4j Database

After running `OpenCogNeo4jGraphBackingStoreApplication`, a Neo4j database will be created
at `~/tmp/opencog-neo4j`. You can inspect it by running:

    neo4j-shell -path ~/tmp/opencog-neo4j  

If you want to use the Neo4j Web UI, first make it writable by Neo4j:

    sudo adduser neo4j ceefour
    chmod -Rc ug+rw ~/tmp/opencog-neo4j

Edit `/etc/neo4j/neo4j-server.properties` and comment:

    org.neo4j.server.database.location=data/graph.db

then add:

    org.neo4j.server.database.location=/home/ceefour/tmp/opencog-neo4j

then restart:

    sudo service neo4j-service restart

Now you can access at http://localhost:7474/

Use this query:

    MATCH (g:opencog_Gene), (c:opencog_Concept), (l:opencog_EvaluationLink)--(x) RETURN g, c, l, x LIMIT 75

