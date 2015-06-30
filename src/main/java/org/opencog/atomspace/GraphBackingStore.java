package org.opencog.atomspace;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Optional;

/**
 * {@link BackingStore} API with graph-optimized operations.
 */
public interface GraphBackingStore extends BackingStore {
    ListenableFuture<Optional<Node>> getNodeAsync(AtomType type, String name);
}
