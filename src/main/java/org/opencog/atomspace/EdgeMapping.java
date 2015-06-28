package org.opencog.atomspace;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import java.io.Serializable;

/**
 * Describes how to map a {@link Link}'s outgoing to a graph edge,
 * useful for Cypher MATCH clause.
 */
public class EdgeMapping implements Serializable {

    private String name;
    private ImmutableMap<String, Object> properties;

    public EdgeMapping(String name, ImmutableMap<String, Object> properties) {
        this.name = name;
        this.properties = properties;
    }

    public EdgeMapping(String name) {
        this.name = name;
        this.properties = ImmutableMap.of();
    }

    /**
     * Edge/{@link org.neo4j.graphdb.Relationship} name, e.g. {@code opencog_predicate} or {@code opencog_parameter}.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Edge/{@link org.neo4j.graphdb.Relationship} properties, e.g. {@code position=0}.
     * @return
     */
    public ImmutableMap<String, Object> getProperties() {
        return properties;
    }

    public String toCypher(String varName) {
        String cypher = "-[" + Strings.nullToEmpty(varName) + ":" + name;
        if (!getProperties().isEmpty()) {
            final String[] propStrs = getProperties().entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).toArray(String[]::new);
            cypher += " {" + Joiner.on(", ").join(propStrs) + "}";
        }
        cypher += "]->";
        return cypher;
    }
}
