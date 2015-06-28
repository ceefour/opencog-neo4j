package org.opencog.atomspace;

/**
 * Any AtomSpace error.
 */
public class AtomSpaceException extends RuntimeException {

    public AtomSpaceException() {
        super();
    }

    public AtomSpaceException(String message) {
        super(message);
    }

    public AtomSpaceException(String message, Throwable cause) {
        super(message, cause);
    }

    public AtomSpaceException(Throwable cause) {
        super(cause);
    }

    public AtomSpaceException(Throwable cause, String format, Object... args) {
        super(String.format(format, args), cause);
    }

    protected AtomSpaceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
