package org.opencog.atomspace;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ceefour on 8/15/15.
 */
@Repository
public class AtomTable {

    private final Map<Long, Atom> atoms = new ConcurrentHashMap<>();

    public Optional<Atom> getAtom(Handle handle) {
        return Optional.ofNullable(atoms.get(handle.getUuid()));
    }

    public ListenableFuture<Handle> addAsync(Atom atom) {
        Preconditions.checkArgument(atom.getUuid() != Handle.UNDEFINED,
                "Atom %s must have UUID", atom);
        atoms.put(atom.getUuid(), atom);
        final Handle handle = new Handle(atom.getUuid(), this, atom);
        return Futures.immediateFuture(handle);
    }

}
