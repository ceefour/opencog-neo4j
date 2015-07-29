package org.opencog.atomspace;

import java.io.Serializable;

/**
 * Represents an <a href="http://wiki.opencog.org/w/Atom">Atom</a> in the {@link AtomSpace}.
 * Inspired by <a href="https://github.com/opencog/atomspace/blob/master/opencog/atomspace/Atom.h">opencog/atomspace/Atom.h</a>.
 * @see Node
 * @see Link
 */
public class Atom implements Serializable {

    /**
     * Property used to store the 64-bit UUID in Neo4j Backing Store.
     * We don't name it "uuid" because the popular UUID is a 128-bit universally unique ID with known generation algorithm.
     * So for 64-bit ID, "UUID" is not a technically accurate name.
     * "gid" informally stands for "global ID".
     */
    public static final String GID_PROPERTY = "gid";

    private AtomType type;

    public Atom(AtomType type) {
        this.type = type;
    }

    public AtomType getType() {
        return type;
    }

}
