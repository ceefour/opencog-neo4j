package org.opencog.atomspace;

import com.fasterxml.jackson.databind.node.TextNode;

import java.io.Serializable;

/**
 * Nodes are Atoms with names.
 * The name and the type of a Node together determine a unique key.
 * It means there exist only one Node with a given (name,type) pair.
 * If a second Node with the same key is inserted, it is merged with the previously inserted Node.
 * The name and type of the node cannot be changed after the node has been inserted into the AtomSpace: OpenCog atoms are immutable objects.
 * Represents an AtomSpace <a href="http://wiki.opencog.org/w/Node">Node</a>.
 * Inspired by <a href="https://github.com/opencog/atomspace/blob/master/opencog/atomspace/Node.h">opencog/atomspace/Node.h</a>.
 */
public class Node extends Atom implements Serializable {

    protected String name;

    public Node(AtomType type, String name) {
        super(type);
        this.name = name;
    }

    public Node(long uuid, AtomType type, String name) {
        super(uuid, type);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "(" + getType().toUpperCamel() + " " +
                new TextNode(name) + ") ; " + getUuid();
    }
}
