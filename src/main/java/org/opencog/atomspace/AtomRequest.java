package org.opencog.atomspace;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

/**
 * Created by ceefour on 7/1/15.
 */
public class AtomRequest implements Serializable {
    public enum AtomRequestKind {
        /**
         * get {@link Node} or {@link Link} by handle UUID
         */
        UUID(AtomSpaceProtos.ZMQAtomFetchKind.UUID),
        /**
         * get node by atom_type and node_name
         */
        NODE(AtomSpaceProtos.ZMQAtomFetchKind.NODE),
        /**
         * get link by atom_type and handle_seq
         */
        LINK(AtomSpaceProtos.ZMQAtomFetchKind.LINK);

        private AtomSpaceProtos.ZMQAtomFetchKind protoKind;

        AtomRequestKind(AtomSpaceProtos.ZMQAtomFetchKind protoKind) {
            this.protoKind = protoKind;
        }

        public AtomSpaceProtos.ZMQAtomFetchKind toProto() {
            return protoKind;
        }
    }
    private final AtomRequestKind kind;
    private final long uuid;
    @Nullable
    private final AtomType type;
    @Nullable
    private final String name;
    @Nullable
    private final ImmutableList<Long> handleSeq;

    public AtomRequest(long uuid) {
        this.kind = AtomRequestKind.UUID;
        this.uuid = uuid;
        this.type = null;
        this.name = null;
        this.handleSeq = null;
    }

    public AtomRequest(AtomType type, String name) {
        this.kind = AtomRequestKind.NODE;
        this.uuid = 0;
        this.type = type;
        this.name = name;
        this.handleSeq = null;
    }

    public AtomRequest(AtomType type, List<Long> handleSeq) {
        this.kind = AtomRequestKind.LINK;
        this.uuid = 0;
        this.type = type;
        this.name = null;
        this.handleSeq = ImmutableList.copyOf(handleSeq);
    }

    public AtomRequestKind getKind() {
        return kind;
    }

    public long getUuid() {
        return uuid;
    }

    public AtomType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public ImmutableList<Long> getHandleSeq() {
        return handleSeq;
    }
}
