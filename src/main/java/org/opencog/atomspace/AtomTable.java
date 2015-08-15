package org.opencog.atomspace;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ceefour on 8/15/15.
 */
public class AtomTable {

    private final Map<Long, Atom> atoms = new ConcurrentHashMap<>();

    public Optional<Atom> getAtom(Handle handle) {
        return Optional.ofNullable(atoms.get(handle.getUuid()));
    }

}
