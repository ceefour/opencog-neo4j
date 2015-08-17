package org.opencog.atomspace;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.Optional;

/**
 * {@link BackingStore} API with graph-optimized operations.
 */
public interface GraphBackingStore extends BackingStore {
    ListenableFuture<Optional<Atom>> getAtomAsync(Handle handle);

    ListenableFuture<Optional<Node>> getNodeAsync(AtomType type, String name);

    ListenableFuture<Optional<Link>> getLinkAsync(AtomType type, List<Handle> handleSeq);

    ListenableFuture<Integer> storeAtomsAsyncFromAtomList(List<Atom> atoms);
}
