package org.opencog.atomspace;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Created by ceefour on 7/4/15.
 */
public abstract class GraphBackingStoreBase implements GraphBackingStore, AutoCloseable {

    @PreDestroy
    public void close() throws Exception {
    }

    @Override
    public final Optional<Node> getNode(AtomType type, String name) {
        try {
            return getNodeAsync(type, name).get();
        } catch (Exception e) {
            throw new AtomSpaceException(e, "Cannot get node %s/%s", type, name);
        }
    }

    @Override
    public final Optional<Link> getLink(AtomType type, List<Handle> handleSeq) {
        try {
            return getLinkAsync(type, handleSeq).get();
        } catch (Exception e) {
            throw new AtomSpaceException(e, "Cannot get link %s %s",
                    type, handleSeq.stream().map(Handle::getUuid).toArray());
        }
    }

    @Override
    public final Optional<Atom> getAtom(Handle handle) {
        try {
            return getAtomAsync(handle).get();
        } catch (Exception e) {
            throw new AtomSpaceException(e, "Cannot get atom %s", handle);
        }
    }

    @Override
    public final ListenableFuture<Optional<Atom>> getAtomAsync(Handle handle) {
        return Futures.transform(getAtomsAsync(ImmutableList.of(new AtomRequest(handle.getUuid()))),
                (List<Atom> it) -> !it.isEmpty() ? Optional.of(it.get(0)) : Optional.empty());
    }

    @Override
    public final ListenableFuture<Optional<Node>> getNodeAsync(AtomType type, String name) {
        return Futures.transform(getAtomsAsync(ImmutableList.of(new AtomRequest(type, name))),
                (List<Atom> it) -> !it.isEmpty() ? Optional.of((Node) it.get(0)) : Optional.empty());
    }

    @Override
    public final ListenableFuture<Optional<Link>> getLinkAsync(AtomType type, List<Handle> handleSeq) {
        final AtomRequest req = new AtomRequest(type, handleSeq.stream().map(Handle::getUuid).collect(Collectors.toList()));
        return Futures.transform(getAtomsAsync(ImmutableList.of(req)),
                (List<Atom> it) -> !it.isEmpty() ? Optional.of((Link) it.get(0)) : Optional.empty());
    }

    @Override
    public final Integer storeAtoms(List<Handle> handles) {
        try {
            return storeAtomsAsync(handles).get();
        } catch (Exception e) {
            throw new AtomSpaceException(e, "Cannot store %d atoms: %s",
                    handles.size(), handles.stream().map(Handle::getUuid).toArray());
        }
    }

    @Override
    public final ListenableFuture<Boolean> storeAtomAsync(Handle handle) {
        return Futures.transform(storeAtomsAsync(ImmutableList.of(handle)),
                (Integer it) -> it >= 1);
    }

    @Override
    public final Boolean storeAtom(Handle handle) {
        try {
            return storeAtomAsync(handle).get();
        } catch (Exception e) {
            throw new AtomSpaceException(e, "Cannot store atom %s", handle);
        }
    }
}
