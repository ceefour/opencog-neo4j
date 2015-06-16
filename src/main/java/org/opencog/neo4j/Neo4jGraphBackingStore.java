package org.opencog.neo4j;

import java.util.Optional;

/**
 * Implements {@link GraphBackingStore} using Neo4j {@link org.neo4j.graphdb.GraphDatabaseService}.
 */
public class Neo4jGraphBackingStore implements GraphBackingStore {
    @Override
    public Optional<Link> getLink(String type, String handleSeq) {
        return null;
    }

    @Override
    public Optional<Node> getNode(String type, String name) {
        return null;
    }

    @Override
    public Optional<Atom> getAtom(String handle) {
        return null;
    }

    @Override
    public String getIncomingSet(String handle) {
        return null;
    }

    @Override
    public String storeAtom(String handle) {
        return null;
    }

    @Override
    public String loadType(String atomTable, String type) {
        return null;
    }

    @Override
    public void barrier() {

    }

    @Override
    public boolean isTypeIgnored(String type) {
        return false;
    }

    @Override
    public boolean isAtomIgnored(String handle) {
        return false;
    }
}
