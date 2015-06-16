package org.opencog.neo4j;

import java.util.Optional;

/**
 * Inspired by <a href="https://github.com/opencog/atomspace/blob/master/opencog/atomspace/BackingStore.h">opencog/atomspace/BackingStore.h</a>
 */
public interface BackingStore {

    /**
     * Return a pointer to a link of the indicated type and outset,
     * if it exists.
     * @param type
     * @param handleSeq
     * @return
     */
    Optional<Link> getLink(String type, String handleSeq);

    /**
     * Return a pointer to a node of the indicated type and name,
     * if it exists.
     * @param type
     * @param name
     * @return
     */
    Optional<Node> getNode(String type, String name);

    /**
     * Return a pointer to an Atom associated with the given
     * handle, if it exists.
     * @param handle
     * @return
     */
    Optional<Atom> getAtom(String handle);

    /**
     * Return a vector containing the handles of the entire incoming
     * set of the indicated handle.
     * @param handle
     * @return
     */
    String getIncomingSet(String handle);

    /**
     * Recursively store the atom and anything in it's outgoing set.
     * If the atom is already in storage, this will update it's
     * truth value, etc.
     * @param handle
     * @return
     */
    String storeAtom(String handle);

    /**
     * Load <strong>all</strong> atoms of the given type, but only if they are not
     * already in the AtomTable.  (This avoids truth value merges
     * between truth values stored in the backend, and truth values
     * in the atomspace.)
     * @param atomTable
     * @param type
     * @return
     */
    String loadType(String atomTable, String type);

    /**
     * Read-write synchronization barrier.
     * All writes will be completed before this routine returns.
     * This allows the backend to implement asynchronous writes,
     * while still providing some control to those who need it.
     * (Mostly the unit tests, at this time.)
     */
    void barrier();

    /**
     * Returns true if the backing store will ignore this type.
     * This is used for performance optimization, as asking the
     * backend to retreive an atom can take a long time. If an atom
     * is of this given type, it will not be fetched.
     * @param type
     * @return
     */
    boolean isTypeIgnored(String type);

    /**
     * Returns true if the backing store will ignore this atom,
     * either because it is of an ignorable type, or is a link
     * which contains an atom that is of an ignorable type.
     * @param handle
     * @return
     */
    boolean isAtomIgnored(String handle);

}
