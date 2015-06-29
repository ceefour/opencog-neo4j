package commands;

import com.google.common.base.CaseFormat;
import org.crsh.cli.Argument;
import org.crsh.cli.Command;
import org.crsh.cli.Required;
import org.crsh.cli.Usage;
import org.crsh.command.InvocationContext;
import org.opencog.atomspace.AtomType;
import org.opencog.atomspace.GraphBackingStore;
import org.opencog.atomspace.Node;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

/**
 * Created by ceefour on 6/28/15.
 */
public class node {
    @Usage("Get a single node by type and name")
    @Command
    public void get(@Usage("Atom type") @Required @Argument String atomTypeName,
                    @Usage("Node name") @Required @Argument String nodeName,
                     InvocationContext context) {
        final ApplicationContext appCtx = (ApplicationContext) context.getAttributes().get("spring.beanfactory");
        final GraphBackingStore backingStore = appCtx.getBean("zmqGraphBackingStore", GraphBackingStore.class);
        final AtomType atomType = AtomType.valueOf(CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, atomTypeName));
        final Optional<Node> node = backingStore.getNode(atomType, nodeName);
        node.ifPresent(it -> System.out.println(it));
    }
}
