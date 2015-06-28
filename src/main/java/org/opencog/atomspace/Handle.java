package org.opencog.atomspace;

/**
 * Represents an <a href="http://wiki.opencog.org/w/Atom">Atom</a> handle in the {@link AtomSpace}.
 * Inspired by <a href="https://github.com/opencog/atomspace/blob/master/opencog/atomspace/Handle.h">opencog/atomspace/Handle.h</a>.
 * @see Atom
 * @see Node
 * @see Link
 */
public interface Handle {
    /**
     * 64-bit numeric value.
     * @return
     */
    long getUuid();
}
