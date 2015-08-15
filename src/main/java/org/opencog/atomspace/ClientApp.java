package org.opencog.atomspace;

import org.crsh.console.jline.JLineProcessor;
import org.crsh.console.jline.Terminal;
import org.crsh.console.jline.TerminalFactory;
import org.crsh.console.jline.console.ConsoleReader;
import org.crsh.console.jline.internal.Configuration;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellFactory;
import org.crsh.spring.SpringBootstrap;
import org.crsh.util.InterruptHandler;
import org.fusesource.jansi.AnsiConsole;
import org.opencog.neo4j.camel.AtomSpaceRouteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.inject.Inject;
import java.io.*;

/**
 * Runs {@link AtomSpaceRouteConfig}.
 */
@SpringBootApplication
//@Configuration
//@Import({AtomSpaceCamelConfiguration.class, AtomSpaceRouteConfig.class})
@Profile("clientapp")
public class ClientApp implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ClientApp.class);
    private static String[] args;

    public static void main(String[] args) {
        ClientApp.args = args;
        new SpringApplicationBuilder(ClientApp.class)
                .profiles("clientapp", "camel")
                .web(false)
                //.properties("zeromq.host=127.0.0.1", "zeromq.port=5555", "zeromq.topic=atomspace.neo4j")
                .run(args);
    }

    @Inject
    private Environment env;

    @Bean
    public SpringBootstrap springBootstrap() {
        return new SpringBootstrap();
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Args: {}", args);
        //TODO: clojure.tools.nrepl.?
//        CRaSH.main(args);
        final ShellFactory factory = springBootstrap().getContext().getPlugin(ShellFactory.class);
        final Shell shell = factory.create(null);

        final Terminal term = TerminalFactory.create();

        //
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    term.restore();
                }
                catch (Exception ignore) {
                }
            }
        });

        //
        String encoding = Configuration.getEncoding();

        // Use AnsiConsole only if term doesn't support Ansi
        PrintStream out;
        PrintStream err;
        boolean ansi;
        if (term.isAnsiSupported()) {
            out = new PrintStream(new BufferedOutputStream(term.wrapOutIfNeeded(new FileOutputStream(FileDescriptor.out)), 16384), false, encoding);
            err = new PrintStream(new BufferedOutputStream(term.wrapOutIfNeeded(new FileOutputStream(FileDescriptor.err)), 16384), false, encoding);
            ansi = true;
        } else {
            out = AnsiConsole.out;
            err = AnsiConsole.err;
            ansi = false;
        }

        //
        FileInputStream in = new FileInputStream(FileDescriptor.in);
        ConsoleReader reader = new ConsoleReader(null, in, out, term);

        //
        final JLineProcessor processor = new JLineProcessor(ansi, shell, reader, out);

        //
        InterruptHandler interruptHandler = new InterruptHandler(new Runnable() {
            @Override
            public void run() {
                processor.interrupt();
            }
        });
        interruptHandler.install();

        //
        Thread thread = new Thread(processor);
        thread.setDaemon(true);
        thread.start();

        //
        try {
            processor.closed();
        }
        catch (Throwable t) {
            log.error("Cannot close processor", t);
        }
    }

}
