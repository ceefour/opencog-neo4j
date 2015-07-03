package org.opencog.atomspace;

import javax.annotation.Nullable;

/**
 * To get an atom if you just know the UUID.
 */
public class GenericHandle implements Handle {

    private long uuid;
    @Nullable
    private Atom atom;

    public GenericHandle(long uuid) {
        this.uuid = uuid;
    }

    @Override
    public long getUuid() {
        return this.uuid;
    }

    @Nullable
    public Atom getAtom() {
        return atom;
    }

    @Override
    public String toString() {
        return "GenericHandle{" +
                "uuid=" + uuid +
                '}';
    }
}
