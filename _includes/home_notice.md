## Summary

Create [a graph backing store](http://wiki.opencog.org/w/Neo4j_Backing_Store) for OpenCog using [Neo4j graph database](http://neo4.org/). The GraphBackingStore API will extend the current BackingStore C++ API. It can take special queries that map naturally into Cypher queries and simple manipulations of Neo4j graph traversals. The Neo4j node-relationship structures and custom indices will be optimized for [AtomSpace](http://wiki.opencog.org/w/AtomSpace) usage and performance. [neo4c C/C++ library](https://github.com/wfreeman/neo4c) will be improved to allow OpenCog C/C++ code to execute Neo4j Cypher queries over REST.

See: [Google Summer of Code 2015 Proposal](https://www.google-melange.com/gsoc/proposal/review/student/google/gsoc2015/hendy/5629499534213120)

## Student Profile

Hendy Irawan ([ceefour666@gmail.com](mailto:ceefour666@gmail.com))

Graduate student in [Electrical Engineering](http://stei.itb.ac.id/), [Bandung Institute of Technology](http://www.itb.ac.id/), Indonesia

* Skype: ceefour666
* Mobile: [+6285624614466](tel:+6285624614466)
* GitHub profile: [ceefour](https://github.com/ceefour)
* [StackOverflow profile](http://stackoverflow.com/users/122441/hendy-irawan)
* FreeNode nickname: ceefour
* Website: [www.hendyirawan.com](http://www.hendyirawan.com/)
* [Lumen Social Robot thesis](http://lumen.hendyirawan.com/)

## Motivation

I have background experience working with Neo4j, in a social commerce app, and also adapting [Yago2s database](http:/www.mpi-inf.mpg.de/departments/databases-and-information-systems/research/yago-naga/yago/) into Neo4j (resulting about 14 GiB graph DB). I believe it would be practical to use Neo4j for [AtomSpace](http://wiki.opencog.org/w/AtomSpace) as well.

In my experience, Neo4j has excellent traversal performance. But before 2.0 it was hard to get good lookup performance. Fortunately since v2.0 Neo4j has easy-to-use indexes (based on Lucene) and I've found by using this feature carefully, lookup operations are fast too.

I also agree with Dr. Goertzel that the schema and general structure should be adaptable to [Hypergraph DB](http://www.hypergraphdb.org/index).

## Skills

I am capable in C/C++, Java, HTML, JavaScript, and CSS.
I have working knowledge and experience in installing and running Neo4j, designing graph databases, and executing Cypher question.
I have beginner knowledge in Python and Scheme.
I am prepared to learn Scheme further to complete this task.

## Past Contributions and Patches

* [Early proposal of using Neo4j for OpenCog Atomspace](http://lumen.hendyirawan.com/2014/07/neo4j-as-graph-database-for-opencog.html) (July 2014)
* [My patches and pull requests to OpenCog GitHub](https://github.com/opencog/opencog/pulls?q=is%3Apr+author%3Aceefour+is%3Aclosed)
* I have contributed bug reports, patches, and also a co-maintainer in several open source projects, including in:
    * [GitHub](https://github.com/ceefour)
    * [Apache Software Foundation](https://issues.apache.org/jira/issues/?filter=-2&jql=reporter%20in%20(%22ceefour666%40gmail.com%22%2C%20ceefour)%20ORDER%20BY%20createdDate%20DESC)
    * [Eclipse Foundation](https://bugs.eclipse.org/bugs/buglist.cgi?email1=hendy%40soluvas.com&emailassigned_to1=1&emailcc1=1&emaillongdesc1=1&emailqa_contact1=1&emailreporter1=1&emailtype1=substring&list_id=11362954&order=Importance&query_format=advanced)
    * [Codehaus](http://jira.codehaus.org/browse/GROOVY-7274?filter=-2&jql=reporter%20in%20(ceefour)%20ORDER%20BY%20createdDate%20DESC)

## About OpenCog

OpenCog is an umbrella project for several open source projects with a vision of realizing Artificial General Intelligence (AGI) using the integrative approach. With this approach, several narrow AI modules are integrated and work together to perform AGI tasks.

The core module of OpenCog is [AtomSpace](http://wiki.opencog.org/w/AtomSpace), used to represent various kinds of knowledge inside the OpenCog framework. Other OpenCog modules include [Probabilistic Logic Networks (PLN)](http://wiki.opencog.org/w/PLN), [MOSES](http://wiki.opencog.org/w/MOSES), and [RelEx](http://wiki.opencog.org/w/RelEx_Dependency_Relationship_Extractor).

## Goals & Benefits

The motivation for suggesting Neo4j is a combination of the following factors:

1. **Structure**. It's a graph DB so its internals match those of the AtomSpace reasonably well.
2. **Licensing**. It has reasonable [OSS license terms](http://neo4j.com/open-source-project/) (GPLv3 and AGPLv3).
3. **Ecosystem**. It has a robust software ecosystem around it (e.g. connection to web-services frameworks, plugins for spatial and temporal indexing, etc.) and a fairly large, active user community.
4. **Works well**. OpenCog team members, notably Rodas and Eskender, have used it before so we have some validation that there aren't weird, obvious gotchas in its usage.
5. **Potential customers**. As a side point: a couple potential customers for OpenCog work are already using Neo4j, so using it will help with these particular business relationships.
6. **Performant**. A specific analysis of the types of queries we probably want to execute against a backing store in the near future, and a comparison of this to Neo4j's querying and indexing methods, suggests that we should be able to execute these queries reasonably efficiently against Neo4j, via appropriate machinations as will be suggested below.
7. **Scalability**. Neo4j can be run distributed across multiple machines; currently this uses a master-slave architecture, but there is momentum behind scaling it further. Scalability requirements and issues are discussed in [Scaling OpenCog](http://wiki.opencog.org/w/Scaling_OpenCog). See also [Distributed AtomSpace Architecture](http://wiki.opencog.org/w/Distributed_AtomSpace_Architecture).
8. **Persistence**. The current in-memory AtomSpace and pattern matcher design can run with high performance, without bottlenecks, when the database fits in RAM. The goal is to provide a graph backing store which can perform at least on par or better than PostgreSQL backing store, when the database is sufficiently large or cannot fit in RAM.

OpenCog, in the medium-to-long term, is not going to commit to any particular backing store technology; the BackingStore API should remain storage-technology-independent. However, in the short-to-medium term, the choice of backing store technology may have some meaningful impact on development and utilization of the system; so the choice of which backing stores to utilize isn't a totally trivial choice even though it's not a "permanent" one. For example, it should possible to implement the GraphBackingStore API using [HypergraphDB](http://www.hypergraphdb.org/index).

## Challenges & Planned Approaches

**Challenge:** Sufficiently small datasets could be fully in performed in RAM, efficiently.

**Approach:** We'll use the [OpenCog Bio knowledge base dataset](https://github.com/opencog/agi-bio/tree/master/knowledge-import), a moderately sized dataset about 212 MiB in size, to test performance.

**Challenge:** The pattern matcher has a hefty setup overhead, and makes a number of worst-case, non-optimal assumptions about how to perform the query. In essence, its designed to work well for complex queries, not simple ones. 

**Approach:** It's planned to use the native Cypher query for queries, which is expected to perform better than running the OpenCog pattern matcher on top Neo4j. Neo4j is also expected to be performant when running a complex query, or a simple/complex query returning many results at once, compared to running many queries returning only single results.

## Design and Implementation

Currently OpenCog supports a [PostgreSQL](http://www.postgresql.org) backing store. It will be augmented with a graph backing store backed by Neo4j, accessible via the same API and via the extended graph API.

For comparison, the current API for accessing the (in-RAM) [AtomSpace](http://wiki.opencog.org/w/AtomSpace) can be seen in [opencog/atomspace/AtomSpace.h](https://github.com/opencog/opencog/blob/6fad61aca3fb7af0ccfb01bf2ca98b69b5ec6699/opencog/atomspace/AtomSpace.h)

### Pattern Matcher to Cypher Query Translator

As an illustration during initial discussion, suppose we have the query "find drugs whose active ingredients dock with the protein FKH1". This would be formulated as:

    (AND
      (Inheritance 
        $X 
        (ConceptNode "drug"))
      (Evaluation
        (PredicateNode "dock")
        (List
          $X
          (ProteinNode "FKH1"))))

The Neo4j backing store will need to translate such pattern matcher query into Cypher query as follows: (try this query online at [http://console.neo4j.org/r/wmrc6v](http://console.neo4j.org/r/wmrc6v))

    MATCH
      (i:Inheritance) <-[:OPERAND]- (:And) -[:OPERAND]-> (e:Evaluation),
      (i) -[:SUPER]-> (:Concept {id: "drug"}),
      (i) -[:SUB]-> (x:Concept),
      (e) -[:PREDICATE]-> (:Predicate {id: "dock"}),
      (e) -[:PARAMETER {position: 0}]-> (x),
      (e) -[:PARAMETER {position: 1}]-> (:ProteinNode {id: "protein_fkh1"})
    RETURN x;

Query Results

    +-------------------------------------------+
    | x                                         |
    +-------------------------------------------+
    | Node[17]{name:"Acme drug",id:"acme_drug"} |
    +-------------------------------------------+
    1 row

The above is only an illustration. The actual dataset that we plan to use for testing will be [Bio knowledge base dataset in Scheme format](https://github.com/opencog/agi-bio/tree/master/knowledge-import) which is a 212 MiB database which is more representative in measuring the performance of the Neo4j based backing store.

### Recursive Unification in Neo4j

Instead of performing unification (variable grounding) once, some situations may require multiple passes to fully ground the variables, if the unification candidates themselves contain variables.

I propose using Cypher's built-in support for multiple [`MATCH` clauses](http://neo4j.com/docs/stable/query-match.html) to implement this behavior. Each `MATCH` clause can both reuse previous variables and define new variables (to be reused by next `MATCH`es, or to be returned as values).

To illustrate, the following subpattern from the [naive Modus Ponens sample](http://wiki.opencog.org/w/Idea:_Recursive_Unification_using_the_Pattern_Matcher):

    (ImplicationLink
        (VariableNode "$A")
        (VariableNode "$B"))

may translate simply to:

    MATCH
      (a) <-[:WHEN]- (i:Implication) -[:THEN]-> (b)
    RETURN i, a, b;

However, we want to make sure we find `VariableNode`s:

    MATCH
      (a) <-[:WHEN]- (i:Implication) -[:THEN]-> (b)
    OPTIONAL MATCH
      (a) -[:*]-> (av:Variable),
      (b) -[:*]-> (bv:Variable)
    RETURN i, a, b, av, bv;

Those `VariableNode`s (`av` and `bv`) can then be used to translate the pattern to Cypher again in the next iteration.
This flattens the recursive algorithm into an iterative one. For a recursion 3 levels deep, we will need to execute 3 Cypher queries.

More information about recursive unification is available in [Recursive unification using the Pattern Matcher - OpenCog Wiki](http://wiki.opencog.org/w/Idea:_Recursive_Unification_using_the_Pattern_Matcher).
 
### Additional Details

In order to make this Google Summer of Code proposal concise, there are longer, more complete details about the plan available and will be updated during work at:
[Neo4j Backing Store - OpenCog Wiki](http://wiki.opencog.org/w/Neo4j_Backing_Store)

## Timeline and Administration

1. **Up to start of Google Summer of Code 2015:**

    * Initial [discussion with OpenCog mentors](https://groups.google.com/forum/#!topic/opencog/uePABY4OhNA) in [OpenCog Google group](https://groups.google.com/forum/#!forum/opencog) - done / continuous.
    * Early [proposal of using Neo4j for OpenCog Atomspace](http://lumen.hendyirawan.com/2014/07/neo4j-as-graph-database-for-opencog.html) (pre-GSoc 2015) - done in July 2014.
    * Prepare [OpenCog build environment](http://wiki.opencog.org/w/Building_OpenCog) in my Linux Mint 17.1 KDE 64bit notebook - done.
    * Submit [my patches and pull requests to OpenCog GitHub](https://github.com/opencog/opencog/pulls?q=is%3Apr+author%3Aceefour+is%3Aclosed) - done.
    * Get commit access to [neo4c Neo4j library for C/C++](https://github.com/wfreeman/neo4c) - done.
    * Initial analysis of OpenCog's [Bio knowledge base dataset in Scheme format](https://github.com/opencog/agi-bio/tree/master/knowledge-import) - done.

2. **May 25-31, 2015:**

    * Specify the Neo4j schema for Scheme dumps in general,
      and especially for OpenCog Bio knowledge base dataset

3. **June 1-7, 2015:**

    * Create the Scheme dump importer (using Java/Clojure)
    * Import the OpenCog bio knowledge base dataset Scheme dump into Neo4j

4. **June 8-14, 2015:**

    * Execute handcoded Cypher queries
    * Tweak indexes/schema/etc to optimize queries while retaining convenient graph schema both programmatically and ideally for human consumption as well
    
5. **June 15-21, 2015:**

    * Implement a pattern-matcher-to-Cypher transformer
    
6. **June 22-28, 2015:** (mid-term evaluation June 26-Jul 2)

    * Implement a pattern-matcher-to-Cypher transformer

7. **June 29-July 5, 2015:**

    * Update [neo4c Neo4j library for C/C++](https://github.com/wfreeman/neo4c) for accessing Neo4j graph DB from OpenCog C/C++ code

8. **July 6-July 26, 2015:**

    * Implement `Neo4jProxy` for `GraphBackingStore`
    * bug fixes
    * performance tweaks

9. **July 27-Aug 9, 2015:**

    * Test `Neo4jProxy` `GraphBackingStore` implementation
      using OpenCog Bio knowledge base dataset
    * bug fixes
    * performance tweaks

10. **Aug 10-16, 2015:**

    * buffer time if any of the previous tasks are late or pending bugs
    * stretch goal: research additional moderately sized graph dataset (100-500 MiB) for OpenCog Neo4j Backing Store testing

11. **Aug 17-23, 2015:**

    * buffer time if any of the previous tasks are late or pending bugs
    * stretch goal: initial performance comparison with [Hypergraph DB](http://www.hypergraphdb.org/index)

My master program study current term and exams end on May 20, 2015, and my term starts September 2015.

Planned time allocated for Google Summer of Code 2015 work during these timeline is 30 hours per week, in most cases also during weekends, when I also love to do my research.

I have ~8 Mbps internet connection whenever I'm on my campus, and I can also use HSPA connection using my mobile provider elsewhere.

I have [signed the OpenCog Individual Contributor License Agreement](https://www.dropbox.com/s/wvon6suu6bqfhae/OpenCog-ICLA-Hendy-Irawan.pdf?dl=0).

I will send weekly email reports to [OpenCog Google group](https://groups.google.com/forum/#!forum/opencog) and also update the [Neo4j Backing Store - OpenCog Wiki](http://wiki.opencog.org/w/Neo4j_Backing_Store).

## Future Considerations

* Neo4j, as a popular open source project with plenty of tutorials, documentation, resources in the Internet, makes it easy for anyone to learn and work with Neo4j
* [neo4c Neo4j library for C/C++](https://github.com/wfreeman/neo4c) is published on GitHub and anyone can contribute to it
* Other tools and languages, like C/C++, Scheme, Python, Java, are already the accepted stack for OpenCog projects
* I plan to continue working with OpenCog during my master's thesis topic: [semantic knowledge representation and reasoning in artificial intelligence robot](http://lumen.hendyirawan.com)
