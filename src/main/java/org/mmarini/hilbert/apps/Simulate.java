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
import org.mmarini.Tuple2;
import org.mmarini.hilbert.model.CSVWriter;
import org.mmarini.hilbert.model.RulesSerde;
import org.mmarini.hilbert.model.Status;
import org.mmarini.hilbert.model.StatusSerde;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Simulates the society
 */
public class Simulate {
    public static final List<String> KPI_NAMES = List.of(
            "population",
            "technology",
            "deathsO",
            "lambdaO",
            "maxPopO",
            "popO",
            "deathsS",
            "births",
            "kf",
            "kfPop",
            "kfRes",
            "lambdaS",
            "lambdaB",
            "deltaTR",
            "lambdaR",
            "deltaTE",
            "lambdaE",
            "ke"
    );
    private static final Logger logger = LoggerFactory.getLogger(Simulate.class);

    private static ArgumentParser createParser() {
        ArgumentParser parser = ArgumentParsers.newFor(Simulate.class.getName()).build()
                .defaultHelp(true)
                .version("?")
                .description("Run a session of simulation.");
        parser.addArgument("-v", "--version")
                .action(Arguments.version())
                .help("show current version");
        parser.addArgument("-r", "--rules")
                .setDefault("rules.yml")
                .help("specify rules yaml file");
        parser.addArgument("-s", "--status")
                .setDefault("status.yml")
                .help("specify status yaml file");
        parser.addArgument("-k", "--kpis")
                .required(false)
                .help("specify kpis csv file");
        parser.addArgument("-o", "--output")
                .setDefault("output.yml")
                .help("specify output yaml file");
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
            Status status = StatusSerde.fromFile(statusFile);

            String rulesFile = parsedArgs.getString("rules");
            logger.atInfo().log("Loading {} ...", rulesFile);
            Function<Status, Tuple2<Status, Supplier<Map<String, Number>>>> engine = RulesSerde.fromFile(rulesFile);
            long n = parsedArgs.getLong("number");
            Optional<String> kpisFilename = Optional.ofNullable(parsedArgs.getString("kpis"));
            kpisFilename.ifPresent(file -> logger.atInfo().log("Writing kpi on {}", file));
            Optional<CSVWriter> kpiWriter = kpisFilename.flatMap(file -> {
                try {
                    return Optional.of(CSVWriter.create(file, KPI_NAMES));
                } catch (FileNotFoundException e) {
                    logger.atError().setCause(e).log();
                    return Optional.empty();
                }
            });
            logger.atInfo().log("Running {} iterations ...", n);
            for (long i = 0; i < n && status.getPopulation() > 0; i++) {
                logger.atInfo().log("Step {} Population {} Technology {}",
                        i,
                        status.getPopulation(),
                        status.getTechnology());
                Tuple2<Status, Supplier<Map<String, Number>>> next = engine.apply(status);
                kpiWriter.ifPresent(writer -> writer.write(next._2.get()));
                status = next._1;
                logger.atDebug().log("Post engine {}", status);
            }
            if (status.getPopulation() == 0) {
                logger.atInfo().log("The population became extinct");
            }
            StatusSerde.write(new File(parsedArgs.getString("output")), status);
            kpiWriter.ifPresent(writer -> {
                try {
                    writer.close();
                } catch (IOException e) {
                    logger.atError().setCause(e).log();
                }
            });
            logger.atInfo().log("Completed");
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        } catch (IOException e) {
            logger.atError().setCause(e).log();
        }
    }
}
