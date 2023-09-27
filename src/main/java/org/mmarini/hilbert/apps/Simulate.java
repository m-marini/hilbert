/*
 * Copyright (c) 2023 Marco Marini, marco.marini@mmarini.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 *    END OF TERMS AND CONDITIONS
 */

package org.mmarini.hilbert.apps;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.mmarini.hilbert.model.RulesLoader;
import org.mmarini.hilbert.model.Status;
import org.mmarini.hilbert.model.StatusLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.UnaryOperator;

/**
 * Simulates the society
 */
public class Simulate {
    private static final Logger logger = LoggerFactory.getLogger(Simulate.class);

    private static ArgumentParser createParser() {
        ArgumentParser parser = ArgumentParsers.newFor(Simulate.class.getName()).build()
                .defaultHelp(true)
                .version("?")
                .description("Run a session of interaction between robot and environment.");
        parser.addArgument("-v", "--version")
                .action(Arguments.version())
                .help("show current version");
        parser.addArgument("-r", "--rules")
                .setDefault("rules.yml")
                .help("specify rules yaml file");
        parser.addArgument("-s", "--status")
                .setDefault("status.yml")
                .help("specify status yaml file");
        parser.addArgument("-n", "--number")
                .setDefault(10000L)
                .type(Long.class)
                .help("specify the maximum number of iterations");
        return parser;
    }

    /**
     * The application entry point
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ArgumentParser parser = createParser();
        try {
            Namespace parsedArgs = parser.parseArgs(args);
            String statusFile = parsedArgs.getString("status");
            logger.atInfo().log("Loading {} ...", statusFile);
            Status status = StatusLoader.fromFile(statusFile);

            String rulesFile = parsedArgs.getString("rules");
            logger.atInfo().log("Loading {} ...", rulesFile);
            UnaryOperator<Status> engine = RulesLoader.fromFile(rulesFile);
            long n = parsedArgs.getLong("number");
            logger.atInfo().log("Running {} iterations ...", n);
            for (long i = 0; i < n && status.getPopulation() > 0; i++) {
                logger.atInfo().log("Step {} Population {} Tecnology {}",
                        i,
                        status.getPopulation(),
                        status.getTechnology());
                status = engine.apply(status);
                logger.atDebug().log("Post engine {}", status);
            }
            if (status.getPopulation() == 0) {
                logger.atInfo().log("The population became extinct");
            }
            logger.atInfo().log("Completed");
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        } catch (IOException e) {
            logger.atError().setCause(e).log();
        }
    }
}
