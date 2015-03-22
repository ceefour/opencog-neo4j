## Student Profile

Hendy Irawan (ceefour666@gmail.com)

Graduate student in [Electrical Engineering](http://stei.itb.ac.id/), [Bandung Institute of Technology](http://www.itb.ac.id/), Indonesia

* Skype: ceefour666
* Mobile: [+6285624614466](tel:+6285624614466)
* GitHub profile: [ceefour](https://github.com/ceefour)
* Website: [www.hendyirawan.com](http://www.hendyirawan.com/)
* [Lumen Social Robot thesis](http://lumen.hendyirawan.com/)

neo4c

## Summary

(Define the problem + Goals & benefits, 500 characters, no formatting)

Create an alternative backing store for OpenCog, using the [Neo4j graph database](http://neo4.org/).
Specialize the current BackingStore API into a GraphBackingStore API, capable of taking special queries that map naturally into simple manipulations of Neo4J graph traversals.
Create custom indices within Neo4j, appropriate for OpenCog.
Use Neo4j as a backing store for OpenCog (accessible via the GraphBackingStore API).

## Why Me

[Neo4j Backing Store - OpenCog Wiki](http://wiki.opencog.org/w/Neo4j_Backing_Store)

I have some background experience working with Neo4j, in a social commerce app, also implementing Yago2s database inside Neo4j (resulting about 14 GiB graph DB). I believe it would be practical to use Neo4j work OpenCog/AtomSpace as well, and I'm curious to find out how it performs in real load.

In my experience, Neo4j has excellent traversal performance. But before 2.0 it was hard to get good lookup performance. Fortunately since v2.0 they have easy-to-use indexes (based on Lucene) and I've found by using this feature carefully, lookup operations are fast too.

I also agree with Dr. Goertzel that the schema and general structure should be adaptable to HGDB.

## Problem

## Goals & Benefits

The motivation for suggesting Neo4j is a combination of the following factors

1. It's a graph DB so its internals match those of the AtomSpace reasonably well.
2. It has reasonable [OSS license terms](http://neo4j.com/open-source-project/) (GPLv3 and AGPLv3).
3. It has a robust software ecosystem around it (e.g. connection to web-services frameworks, plugins for spatial and temporal indexing, etc.) and a fairly large, active user community.
4. We've used it before (well, Rodas and Eskender have) so we have some validation that there aren't weird, obvious gotchas in its usage.
5. As a side point: a couple potential customers for OpenCog work are already using Neo4j, so using it will help with these particular business relationships.
6. A specific analysis of the types of queries we probably want to execute against a backing store in the near future, and a comparison of this to Neo4j's querying and indexing methods, suggests that we should be able to execute these queries reasonably efficiently against Neo4j, via appropriate machinations as will be suggested below.
7. Neo4j can be run distributed across multiple machines; currently this uses a master-slave architecture, but there is momentum behind scaling it further. Scalability requirements and issues are discussed in [Scaling OpenCog](http://wiki.opencog.org/w/Scaling_OpenCog). See also [Distributed AtomSpace Architecture](http://wiki.opencog.org/w/Distributed_AtomSpace_Architecture).

OpenCog, in the medium-to-long term, is not going to commit to any particular backing store technology; the BackingStore API should remain storage-technology-independent. However, in the short-to-medium term, the choice of backing store technology may have some meaningful impact on development and utilization of the system; so the choice of which backing stores to utilize isn't a totally trivial choice even though it's not a "permanent" one.

A warning to the reader is that, to really grok what I'm getting at here, you'll need to at least lightly familiarize yourself with the nature of Neo4j traversals and paths.

While this page mainly references Neo4j, most of the discussion would actually apply to using any reasonably functional graph DB as a Backing Store; the final section discusses this point in the context of comparing Neo4j to [HGDB (HypergraphDB)](http://www.hypergraphdb.org/index) as a Backing Store.

## Challenges & Planned Approaches

## Timeline

1. **Up to start of Google Summer of Code 2015:**

    * Initial [discussion with OpenCog mentors](https://groups.google.com/forum/#!topic/opencog/uePABY4OhNA) in [OpenCog Google group](https://groups.google.com/forum/#!forum/opencog) - done / continuous.
    * Initial [proposal of using Neo4j for OpenCog Atomspace](http://lumen.hendyirawan.com/2014/07/neo4j-as-graph-database-for-opencog.html) (pre-GSoc 2015) - done in July 2014.
    * Prepare [OpenCog build environment](http://wiki.opencog.org/w/Building_OpenCog) in my Linux Mint 17.1 KDE 64bit notebook - done.
    * Submit [my patches and pull requests to OpenCog GitHub](https://github.com/opencog/opencog/pulls?q=is%3Apr+author%3Aceefour+is%3Aclosed) - done.
    * Get commit access to [neo4c Neo4j library for C/C++](https://github.com/wfreeman/neo4c) - done.
    * Initial analysis of OpenCog's [Bio knowledge base dataset in Scheme format](https://github.com/opencog/agi-bio/tree/master/knowledge-import) - done.

2. **May 25-31, 2015:**

3. **June 1-7, 2015:**

4. **June 8-14, 2015:**

5. **June 15-21, 2015:**

6. **June 22-28, 2015:**

7. **June 29-July 5, 2015:**

8. **July 6-July 12, 2015:**

9. **July 13-July 19, 2015:**

10. **July 20-July 26, 2015:**

11. **July 27-Aug 2, 2015:**

12. **Aug 3-Aug 9, 2015:**

13. **Aug 10-16, 2015:**

    * buffer time if any of the previous tasks are late
    * stretch goal: research additional moderately sized graph dataset (100-500 MiB) for OpenCog Neo4j Backing Store testing

14. **Aug 17-23, 2015:**

    * buffer time if any of the previous tasks are late
    * stretch goal: initial performance comparison with [Hypergraph DB](http://www.hypergraphdb.org/index)

Planned time allocated for Google Summer of Code 2015 work during these timeline is 30 hours per week, in most cases also during weekends, when I also love to do my research.

During Google Summer of Code 2015 timeline, there are no scheduled classes for my master's program study.

## Bugs

## Past Contributions

