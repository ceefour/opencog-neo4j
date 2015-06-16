package org.opencog.neo4j;

/**
 * <p>i.e. {@code ConceptNode}, {@code GeneNode}, {@code PredicateNode}, {@code PhraseNode}, {@code WordNode}, etc.</p>
 *
 * <p>Hendy's proposal: The AtomType is a Neo4j label of a {@link Node} or {@link Link}.
 * Most {@link Link}s .</p>
 *
 * @todo Determine what's the best way to implement Atom types, in a way that is performant
 * (both Java-wise and DB-wise) and is relatively portable in the Neo4j database.
 * (i.e. autogenerated numeric identifiers may be problematic)
 */
public enum AtomType {
    ATOM,
    NODE,
    LINK,
    WORD_NODE,
    CONCEPT_NODE,
    ASSOCIATIVE_LINK,
    EVALUATION_LINK,
    MULTIPARENT_LINK
}
