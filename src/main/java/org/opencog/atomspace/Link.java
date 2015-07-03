package org.opencog.atomspace;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Represents an AtomSpace <a href="http://wiki.opencog.org/w/Link">Link</a>.
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

    public ImmutableList<? extends Handle> getOutgoingSet() {
        return outgoingSet;
    }

    @Override
    public String toString() {
        return "Link{" +
                "type=" + getType() +
                ", outgoingSet=" + outgoingSet +
                '}';
    }
}
