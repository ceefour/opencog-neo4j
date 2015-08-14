package org.opencog.atomspace;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Represents an AtomSpace <a href="http://wiki.opencog.org/w/Link">Link</a>.
 * The type and outgoing set of the link cannot be changed after the link has been inserted into the AtomSpace:
 * OpenCog atoms are <a href="http://en.wikipedia.org/wiki/Immutable_object">immutable objects.</a>
 * Inspired by <a href="https://github.com/opencog/atomspace/blob/master/opencog/atomspace/Link.h">opencog/atomspace/Link.h</a>.
 */
public class Link extends Atom {

    private ImmutableList<? extends Handle> outgoingSet;

//    public Link(AtomType type) {
//        super(type);
//    }

    public Link(AtomType type, List<? extends Handle> outgoingSet) {
        super(type);
        this.outgoingSet = ImmutableList.copyOf(outgoingSet);
    }

    public Link(long uuid, AtomType type, List<? extends Handle> outgoingSet) {
        super(uuid, type);
        this.outgoingSet = ImmutableList.copyOf(outgoingSet);
    }

    /**
     * The type and outgoing set of the link cannot be changed after the link has been inserted into the AtomSpace:
     * OpenCog atoms are <a href="http://en.wikipedia.org/wiki/Immutable_object">immutable objects.</a>
     * @return
     * @see http://wiki.opencog.org/w/Link
     */
    public ImmutableList<? extends Handle> getOutgoingSet() {
        return outgoingSet;
    }

    @Override
    public String toString() {
        return "(" + getType().toUpperCamel() +
                " " + Joiner.on(' ').join(outgoingSet.stream().map(Handle::getUuid).toArray()) +
                ") ; " + getUuid();
    }
}
