package org.opencog.atomspace;

import java.io.Serializable;

/**
 * Inspired by <a href="https://github.com/opencog/atomspace/blob/master/opencog/atomspace/AttentionValue.h">opencog/atomspace/AttentionValue.h</a>
 */
public class AttentionValue implements Serializable {
    short sti;
    short lti;
    short vlti;

    public short getSti() {
        return sti;
    }

    public void setSti(short sti) {
        this.sti = sti;
    }

    public short getLti() {
        return lti;
    }

    public void setLti(short lti) {
        this.lti = lti;
    }

    public short getVlti() {
        return vlti;
    }

    public void setVlti(short vlti) {
        this.vlti = vlti;
    }
}
