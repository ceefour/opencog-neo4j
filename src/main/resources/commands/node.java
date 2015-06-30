package commands;

import com.google.common.base.CaseFormat;
import com.google.common.base.Splitter;
import org.crsh.cli.Argument;
import org.crsh.cli.Command;
import org.crsh.cli.Required;
import org.crsh.cli.Usage;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
import org.opencog.atomspace.AtomType;
import org.opencog.atomspace.GraphBackingStore;
import org.opencog.atomspace.Node;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Optional;

/**
 * Created by ceefour on 6/28/15.
 */
@Usage("Node operations")
public class node extends BaseCommand {

    @Usage("Get node(s) by type and name")
    @Command
    public void get(@Usage("ATOM_TYPE/NODE_NAME. e.g. 'ConceptNode/GO:0000024'") @Required @Argument List<String> nodePaths,
                     InvocationContext context) {
        final ConfigurableListableBeanFactory appCtx = (ConfigurableListableBeanFactory) context.getAttributes().get("spring.beanfactory");
        final GraphBackingStore backingStore = appCtx.getBean("zmqGraphBackingStore", GraphBackingStore.class);
        nodePaths.stream().forEach(nodePath -> {
            final List<String> segments = Splitter.on('/').splitToList(nodePath);
            final AtomType atomType = AtomType.valueOf(CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, segments.get(0)));
            final Optional<Node> node = backingStore.getNode(atomType, segments.get(1));
            node.ifPresent(it -> System.out.println(it));
        });
    }
}
