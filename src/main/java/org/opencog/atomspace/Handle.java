package org.opencog.atomspace;

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents an <a href="http://wiki.opencog.org/w/Atom">Atom</a> handle in the {@link AtomSpace}.
 * Inspired by <a href="https://github.com/opencog/atomspace/blob/master/opencog/atomspace/Handle.h">opencog/atomspace/Handle.h</a>.
 * @see Atom
 * @see Node
 * @see Link
 */
public class Handle implements Serializable {

    public static final long UNDEFINED = Long.MIN_VALUE; // == ULONG_MAX

    private long uuid;
    private transient List<AtomTable> resolvers;
    private transient Atom atom;

    public Handle() {
        this.uuid = Handle.UNDEFINED;
    }

    public Handle(long uuid) {
        this.uuid = uuid;
    }

    /**
     * 64-bit numeric value.
     * @return
     */
    public long getUuid() {
        return uuid;
    }

    public Optional<Atom> resolve() {
        if (atom != null) {
            return Optional.of(atom);
        }

        for (AtomTable resolver : resolvers) {
            final Optional<Atom> found = resolver.getAtom(this);
            if (found.isPresent()) {
                atom = found.get();
                return found;
            }
        }
        return Optional.empty();
    }

    public synchronized void addResolver(AtomTable resolver) {
        if (resolvers == null) {
            resolvers = new CopyOnWriteArrayList<>();
        }
        resolvers.add(resolver);
    }
}
