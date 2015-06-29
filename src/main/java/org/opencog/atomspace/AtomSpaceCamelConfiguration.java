package org.opencog.atomspace;

import org.apache.camel.spring.javaconfig.CamelConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Camel configuration.
 */
@Configuration
@Profile("camel")
public class AtomSpaceCamelConfiguration extends CamelConfiguration {

}
