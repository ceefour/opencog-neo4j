package org.opencog.atomspace;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.Optional;

/**
 * Inspired by <a href="https://github.com/opencog/atomspace/blob/master/opencog/atomspace/BackingStore.h">opencog/atomspace/BackingStore.h</a>
 */
public interface BackingStore {

    /**
     * Return a pointer to a link of the indicated type and outset,
     * if it exists.
     * @param type
     * @param handleSeq the outgoing set of the link.
     * @return
     */
    Optional<Link> getLink(AtomType type, List<Handle> handleSeq);

    /**
     * Return a pointer to a node of the indicated type and name,
     * if it exists.
     * @param type
     * @param name
     * @return
     */
    Optional<Node> getNode(AtomType type, String name);

    /**
     * Return a pointer to a node of the indicated type and name,
     * if it exists.
     * @param reqs
     * @return Warning: may contain null values for each not found node.
     */
    ListenableFuture<List<Atom>> getAtomsAsync(List<AtomRequest> reqs);

    /**
     * Return a pointer to an Atom associated with the given
     * handle, if it exists.
     * @param handle
     * @return
     */
    Optional<Atom> getAtom(Handle handle);

    /**
     * Return a vector containing the handles of the entire incoming
     * set of the indicated handle.
     * @param handle
     * @return
     */
    List<Handle> getIncomingSet(Handle handle);

    /**
     * Recursively store the atom and anything in it's outgoing set.
     * If the atom is already in {@link BackingStore} storage, this will update it's
     * truth value, etc.
     * @param handle Local {@link Node} or {@link Link} object must be provided
     *               inside the handle.
     * @return
     */
    Boolean storeAtom(Handle handle);

    /**
     * Recursively store the atom and anything in it's outgoing set.
     * If the atom is already in {@link BackingStore} storage, this will update it's
     * truth value, etc.
     * @param handle Local {@link Node} or {@link Link} object must be provided
     *               inside the handle.
     * @return
     */
    ListenableFuture<Boolean> storeAtomAsync(Handle handle);

    /**
     * Recursively store the atom(s) and anything in it's outgoing set.
     * If an atom is already in {@link BackingStore} storage, this will update it's
     * truth value, etc.
     * @param handles Local {@link Node} or {@link Link} object must be provided
     *               inside the handle.
     * @return Number of stored/updated atoms.
     */
    Integer storeAtoms(List<Handle> handles);

    /**
     * Recursively store the atom(s) and anything in it's outgoing set.
     * If an atom is already in {@link BackingStore} storage, this will update it's
     * truth value, etc.
     * @param handles Local {@link Node} or {@link Link} object must be provided
     *               inside the handle.
     * @return Number of stored/updated atoms.
     */
    ListenableFuture<Integer> storeAtomsAsync(List<Handle> handles);

    /**
     * Load <strong>all</strong> atoms of the given {@link AtomType}, but only if they are not
     * already in the AtomTable.  (This avoids truth value merges
     * between truth values stored in the {@link BackingStore} backend, and truth values
     * in the {@link AtomSpace}.)
     * @param atomTable
     * @param type
     * @return Number of atoms loaded.
     */
    Integer loadType(String atomTable, AtomType type);

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
    boolean isTypeIgnored(AtomType type);

    /**
     * Returns true if the backing store will ignore this atom,
     * either because it is of an ignorable type, or is a link
     * which contains an atom that is of an ignorable type.
     * @param handle
     * @return
     */
    boolean isAtomIgnored(Handle handle);

}
