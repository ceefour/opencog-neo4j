package org.opencog.atomspace;

/**
 * To get an atom if you just know the UUID.
 */
public class GenericHandle implements Handle {

    private long uuid;

    public GenericHandle(long uuid) {
        this.uuid = uuid;
    }

    @Override
    public long getUuid() {
        return this.uuid;
    }

    @Override
    public String toString() {
        return "GenericHandle{" +
                "uuid=" + uuid +
                '}';
    }
}
