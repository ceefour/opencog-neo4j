package org.opencog.neo4j;

import com.google.common.collect.ImmutableList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an AtomSpace <a href="http://wiki.opencog.org/w/Link">Link</a>.
 * Inspired by <a href="https://github.com/opencog/atomspace/blob/master/opencog/atomspace/Link.h">opencog/atomspace/Link.h</a>.
 */
public class Link extends Atom {

    private ImmutableList<Handle> outgoingSet;

    public Link(AtomType type) {
        super(type);
    }

    public Link(AtomType type, ImmutableList<Handle> outgoingSet) {
        super(type);
        this.outgoingSet = outgoingSet;
    }

    public ImmutableList<Handle> getOutgoingSet() {
        return outgoingSet;
    }

}
