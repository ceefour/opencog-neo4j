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

## Bugs

## Past Contributions

