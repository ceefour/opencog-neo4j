package commands;

import org.crsh.cli.Argument;
import org.crsh.cli.Command;
import org.crsh.cli.Required;
import org.crsh.cli.Usage;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
import org.opencog.atomspace.Atom;
import org.opencog.atomspace.AtomRequest;
import org.opencog.atomspace.GraphBackingStore;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ceefour on 6/28/15.
 */
@Usage("Atom operations")
public class atom extends BaseCommand {

    @Usage("Get atom(s) by handle UUID")
    @Command
    public void get(@Usage("Atom handle UUID") @Required @Argument List<String> uuidStrs,
                     InvocationContext context) throws Exception {
        final ConfigurableListableBeanFactory appCtx = (ConfigurableListableBeanFactory) context.getAttributes().get("spring.beanfactory");
        final GraphBackingStore backingStore = appCtx.getBean("zmqBackingStore", GraphBackingStore.class);

        final List<Atom> atoms = backingStore.getAtomsAsync(uuidStrs.stream()
                .map(it -> new AtomRequest(Long.valueOf(it))).collect(Collectors.toList())).get();
        atoms.forEach(it -> out.println(it));
    }

}
