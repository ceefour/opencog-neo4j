package org.opencog.atomspace;

import java.util.List;
import java.util.Map;

/**
 * Inspired by <a href="https://github.com/opencog/atomspace/blob/master/opencog/atomspace/AtomSpace.h">opencog/atomspace/AtomSpace.h</a>
 */
public interface AtomSpace {

    /**
     * Add atom(s) to the Atom Table. If the atom already exists
     * then new truth value is ignored, and the existing atom is
     * returned.
     *
     * <p>Using bulk add API may optimize addition of many atoms at once.
     * If there's an error with one atom, the whole transaction should be rolled back.</p>
     */
    Map<String, Atom> addAtoms(List<Atom> atoms);
}
