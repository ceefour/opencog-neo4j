package org.opencog.atomspace;

import java.io.Serializable;

/**
 * Created by ceefour on 7/1/15.
 */
public class NodeRequest implements Serializable {
    private AtomType type;
    private String name;

    public NodeRequest(AtomType type, String name) {
        this.type = type;
        this.name = name;
    }

    public AtomType getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
