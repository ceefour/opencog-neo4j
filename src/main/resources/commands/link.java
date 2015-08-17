package commands;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import org.crsh.cli.Argument;
import org.crsh.cli.Command;
import org.crsh.cli.Required;
import org.crsh.cli.Usage;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
import org.opencog.atomspace.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ceefour on 6/28/15.
 */
@Usage("Link operations")
public class link extends BaseCommand {

    private static final Logger log = LoggerFactory.getLogger(link.class);

    @Usage("Store link")
    @Command
    public void store(@Usage("ATOM_TYPE OUTGOING1 OUTGOING2 ...") @Required @Argument List<String> nodePaths,
                    InvocationContext context) throws Exception {
        final ConfigurableListableBeanFactory appCtx = (ConfigurableListableBeanFactory) context.getAttributes().get("spring.beanfactory");
        final GraphBackingStore backingStore = appCtx.getBean("zmqBackingStore", GraphBackingStore.class);
        final AtomTable atomTable = appCtx.getBean(AtomTable.class);

        final AtomType atomType = AtomType.valueOf(CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, nodePaths.get(0)));
        final List<Handle> outgoingSet = nodePaths.stream().skip(1).map(nodePath -> new Handle(Long.valueOf(nodePath))).collect(Collectors.toList());
        final Link link = new Link(Atom.RANDOM.nextLong(), atomType, outgoingSet);
        final Handle linkHandle = atomTable.addAsync(link).get();

        final Integer storedCount = backingStore.storeAtomsAsync(ImmutableList.of(linkHandle)).get();
        out.println(link);
        out.println("Stored " + storedCount + " links");
    }

}
