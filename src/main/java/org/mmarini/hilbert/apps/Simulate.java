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

import hu.akarnokd.rxjava3.swing.SwingObservable;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.mmarini.Tuple2;
import org.mmarini.hilbert.Messages;
import org.mmarini.hilbert.model.*;
import org.mmarini.hilbert.swing.MonitorPanel;
import org.mmarini.hilbert.swing.ParamsPanel;
import org.mmarini.swing.GridLayoutHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.mmarini.swing.SwingUtils.*;

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
            "ke",
            "deathsH",
            "kh",
            "lifeExpectancy",
            "lambdaH"
    );
    public static final int WIDTH = 1200;
    public static final int HEIGHT = 800;
    private static final Logger logger = LoggerFactory.getLogger(Simulate.class);

    private static ArgumentParser createParser() {
        ArgumentParser parser = ArgumentParsers.newFor(Simulate.class.getName()).build()
                .defaultHelp(true)
                .version(Messages.getString("Hilbert.version"))
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
        parser.addArgument("-b", "--batch")
                .action(Arguments.storeTrue())
                .help("specify batch mode");
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
            new Simulate().run(parsedArgs);
        } catch (ArgumentParserException ex) {
            parser.handleError(ex);
            System.exit(1);
        }
    }

    private final JFrame frame;
    private final JMenuItem exitMenu;
    private final JButton startBtn;
    private final JButton stopBtn;
    private final JMenuItem startMenu;
    private final JMenuItem stopMenu;
    private final ParamsPanel paramsPanel;
    private final SimulatorEngine<Tuple2<Status, Supplier<Map<String, Number>>>, Tuple2<Status, Supplier<Map<String, Number>>>> simulator;
    private final MonitorPanel monitorPanel;
    private final JMenuItem loadStatusMenu;
    private final JMenuItem loadRulesMenu;
    private final JFileChooser loadStatusPanel;
    private final JFileChooser loadRulesPanel;
    private Status status;
    private Function<Status, Tuple2<Status, Supplier<Map<String, Number>>>> engine;
    private CSVWriter kpiWriter;

    /**
     * Create the simulation app
     */
    public Simulate() {
        this.frame = new JFrame(Messages.getString("Hilbert.title"));
        this.loadStatusMenu = createMenuItem("Simulate.loadStatusMenu");
        this.loadRulesMenu = createMenuItem("Simulate.loadRulesMenu");
        this.exitMenu = createMenuItem("Simulate.exitMenu");
        this.startMenu = createMenuItem("Simulate.startMenu");
        this.stopMenu = createMenuItem("Simulate.stopMenu");
        this.startBtn = createButton("Simulate.startButton");
        this.stopBtn = createButton("Simulate.stopButton");
        this.paramsPanel = new ParamsPanel();
        this.monitorPanel = new MonitorPanel();
        this.loadStatusPanel = new JFileChooser();
        this.loadRulesPanel = new JFileChooser();
        this.simulator = SimulatorEngineImpl.create(
                this::nextSeed,
                Function.identity());
    }

    /**
     * Creates the frame content
     */
    private void createContent() {
        JPanel panel = new GridLayoutHelper<>(Messages.RESOURCE_BUNDLE, new JPanel())
                .modify("insets,5 weight,1,1 fill at,0,0")
                .add(monitorPanel)
                .modify("noweight at,0,1")
                .add(paramsPanel)
                .getContainer();
        frame.getContentPane().add(panel, BorderLayout.CENTER);
    }

    /**
     * Creates the event flows
     */
    private void createFlows() {
        SwingObservable.actions(exitMenu).toFlowable(BackpressureStrategy.LATEST)
                .doOnNext(ev -> frame.dispose())
                .subscribe();
        SwingObservable.actions(loadStatusMenu).toFlowable(BackpressureStrategy.LATEST)
                .doOnNext(this::handleLoadStatus)
                .subscribe();
        SwingObservable.actions(loadRulesMenu).toFlowable(BackpressureStrategy.LATEST)
                .doOnNext(this::handleLoadRules)
                .subscribe();
        paramsPanel.readStatus()
                .doOnNext(this::handleStatusParamsChange)
                .doOnError(err -> logger.atError().setCause(err).log())
                .subscribe();
        paramsPanel.readTimeInterval()
                .doOnNext(this::handleSpeedChange)
                .subscribe();
        Stream.of(SwingObservable.actions(startBtn), SwingObservable.actions(startMenu))
                .map(obs -> obs.toFlowable(BackpressureStrategy.LATEST))
                .reduce(Flowable::mergeWith)
                .orElseThrow()
                .doOnNext(this::handleStart)
                .subscribe();
        Stream.of(SwingObservable.actions(stopBtn), SwingObservable.actions(stopMenu))
                .map(obs -> obs.toFlowable(BackpressureStrategy.LATEST))
                .reduce(Flowable::mergeWith)
                .orElseThrow()
                .doOnNext(this::handleStop)
                .subscribe();
        simulator.setOnEvent(this::handleSeedChange);
    }

    /**
     * Creates the menu bar
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu file = createMenu("Simulate.fileMenu");
        file.add(loadStatusMenu);
        file.add(loadRulesMenu);
        file.add(new JSeparator());
        file.add(exitMenu);

        JMenu run = createMenu("Simulate.runMenu");
        run.add(startMenu);
        run.add(stopMenu);
        menuBar.add(file);
        menuBar.add(run);
        frame.setJMenuBar(menuBar);
    }

    /**
     * Creates the toolbar
     */
    private void createToolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.add(this.startBtn);
        toolbar.add(this.stopBtn);
        frame.getContentPane().add(toolbar, BorderLayout.NORTH);
    }

    /**
     * Handles load rules event
     *
     * @param actionEvent the event
     */
    private void handleLoadRules(ActionEvent actionEvent) {
        if (simulator.isActive()) {
            simulator.stop()
                    .doOnSuccess(s0 -> {
                        engine = loadRules().orElse(engine);
                        simulator.start(s0);
                    })
                    .subscribe();
        } else {
            engine = loadRules().orElse(engine);
        }
    }

    /**
     * Handles load status event
     *
     * @param actionEvent the event
     */
    private void handleLoadStatus(ActionEvent actionEvent) {
        if (simulator.isActive()) {
            simulator.stop()
                    .doOnSuccess(seed -> {
                        Status status = loadStatus().orElse(seed._1);
                        simulator.start(Tuple2.of(status, Map::of));
                    })
                    .subscribe();
        } else {
            this.status = loadStatus().orElse(status);
        }
    }

    /**
     * Handles the simulation seed change
     *
     * @param seed the seed
     */
    private void handleSeedChange(Tuple2<Status, Supplier<Map<String, Number>>> seed) {
        this.status = seed._1;
        monitorPanel.setStatus(status);
        monitorPanel.setKpis(seed._2.get());
    }

    /**
     * Handles time interval change
     *
     * @param timeInterval the time interval
     */
    private void handleSpeedChange(Number timeInterval) {
        simulator.setEventInterval(Duration.ofMillis(timeInterval.longValue()));
        logger.atDebug().log("Time interval {} ms", timeInterval);
    }

    /**
     * Handles start event
     *
     * @param actionEvent the event
     */
    private void handleStart(ActionEvent actionEvent) {
        logger.atDebug().log("Start");
        startBtn.setEnabled(false);
        startMenu.setEnabled(false);
        simulator.start(Tuple2.of(status, Map::of))
                .doOnSuccess(ev -> {
                    stopMenu.setEnabled(true);
                    stopBtn.setEnabled(true);
                })
                .subscribe();
    }

    /**
     * Handles change of status parameters
     *
     * @param params the parameters
     */
    private void handleStatusParamsChange(Status params) {
        if (simulator.isActive()) {
            simulator.request(seed -> Tuple2.of(new Status(
                            seed._1.getPopulation(),
                            params.getFarmerPrefs(), params.getResearchPrefs(), params.getEducatorPrefs(), params.getDoctorPrefs(), params.getInactivePrefs(),
                            params.getFoodPrefs(), params.getResearchPrefs(), params.getEducationPrefs(), params.getHealthPrefs(), params.getSettlementPrefs(),
                            seed._1.getTechnology()),
                    Map::of));
        } else {
            status = new Status(
                    status.getPopulation(),
                    params.getFarmerPrefs(), params.getResearcherPrefs(), params.getEducatorPrefs(), params.getDoctorPrefs(), params.getInactivePrefs(),
                    params.getFoodPrefs(), params.getResearchPrefs(), params.getEducationPrefs(), params.getHealthPrefs(), params.getSettlementPrefs(),
                    status.getTechnology());
            handleSeedChange(Tuple2.of(status, Map::of));
        }
    }

    /**
     * Handles stop event
     *
     * @param actionEvent the event
     */
    private void handleStop(ActionEvent actionEvent) {
        logger.atDebug().log("Stop");
        stopBtn.setEnabled(false);
        stopMenu.setEnabled(false);
        simulator.stop()
                .doOnSuccess(seed -> {
                    startBtn.setEnabled(true);
                    startMenu.setEnabled(true);
                    this.status = seed._1;
                })
                .subscribe();
    }

    /**
     * Initializes simulator
     *
     * @param parsedArgs the parsed argument
     */
    private void init(Namespace parsedArgs) {
        try {
            String statusFile = parsedArgs.getString("status");
            logger.atInfo().log("Loading {} ...", statusFile);
            this.status = StatusSerde.fromFile(statusFile);

            String rulesFile = parsedArgs.getString("rules");
            logger.atInfo().log("Loading {} ...", rulesFile);
            this.engine = RulesSerde.fromFile(rulesFile);
            this.kpiWriter = null;
            String kpisFilename = parsedArgs.getString("kpis");
            if (kpisFilename != null) {
                logger.atInfo().log("Writing kpi on {}", kpisFilename);
                try {
                    kpiWriter = CSVWriter.create(kpisFilename, KPI_NAMES);
                } catch (FileNotFoundException e) {
                    logger.atError().setCause(e).log();
                }
            }
        } catch (IOException e) {
            logger.atError().setCause(e).log();
            throw new RuntimeException(e);
        }
        frame.getContentPane().setLayout(new BorderLayout());
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screenSize);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        stopBtn.setEnabled(false);
        stopMenu.setEnabled(false);

        loadStatusPanel.setCurrentDirectory(new File("."));
        loadStatusPanel.setFileFilter(new FileNameExtensionFilter(Messages.getString("Simulate.statusFile.text"), "yml", "yaml"));

        loadRulesPanel.setCurrentDirectory(new File("."));
        loadRulesPanel.setFileFilter(new FileNameExtensionFilter(Messages.getString("Simulate.rulesFile.text"), "yml", "yaml"));

        createMenuBar();
        createToolBar();
        createContent();
        createFlows();
    }

    /**
     * Shows the load rules dialog and returns the selected engine
     */
    private Optional<Function<Status, Tuple2<Status, Supplier<Map<String, Number>>>>> loadRules() {
        if (loadRulesPanel.showDialog(frame, Messages.getString("Simulate.loadRules.text")) == JFileChooser.APPROVE_OPTION) {
            try {
                return Optional.of(RulesSerde.fromFile(loadRulesPanel.getSelectedFile()));
            } catch (Throwable e) {
                logger.atError().setCause(e).log("Error loading rules");
                showMessageKey("Simulate.loadRules.error", new JLabel(e.getMessage()));
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    /**
     * show load file panel and return the status
     */
    private Optional<Status> loadStatus() {
        if (loadStatusPanel.showDialog(frame, Messages.getString("Simulate.loadStatus.text")) == JFileChooser.APPROVE_OPTION) {
            try {
                return Optional.of(StatusSerde.fromFile(loadStatusPanel.getSelectedFile()));
            } catch (Throwable e) {
                logger.atError().setCause(e).log("Error loading status");
                showMessageKey("Simulate.loadStatus.error", new JLabel(e.getMessage()));
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    /**
     * Returns the next status and the simulated time interval
     *
     * @param seed the seed
     * @param dt   the time interval
     */
    private Tuple2<Tuple2<Status, Supplier<Map<String, Number>>>, Double> nextSeed(Tuple2<Status, Supplier<Map<String, Number>>> seed, double dt) {
        Tuple2<Status, Supplier<Map<String, Number>>> next = engine.apply(seed._1);
        return Tuple2.of(next, dt);
    }

    /**
     * Runs the simulator
     *
     * @param parsedArgs the parsed argument
     */
    private void run(Namespace parsedArgs) {
        init(parsedArgs);
        if (parsedArgs.getBoolean("batch")) {
            runBatch(parsedArgs);
        } else {
            runInteractive();
        }
    }

    /**
     * Runs batch simulator
     *
     * @param parsedArgs the parsed argument
     */
    private void runBatch(Namespace parsedArgs) {
        try {
            long n = parsedArgs.getLong("number");
            logger.atInfo().log("Running {} iterations ...", n);
            for (long i = 0; i < n && status.getPopulation() > 0; i++) {
                logger.atInfo().log("Step {} Population {} Technology {}",
                        i,
                        status.getPopulation(),
                        status.getTechnology());
                Tuple2<Status, Supplier<Map<String, Number>>> next = engine.apply(status);
                if (kpiWriter != null) {
                    kpiWriter.write(next._2.get());
                }
                status = next._1;
                logger.atDebug().log("Post engine {}", status);
            }
            if (status.getPopulation() == 0) {
                logger.atInfo().log("The population became extinct");
            }
            StatusSerde.write(new File(parsedArgs.getString("output")), status);
            if (kpiWriter != null) {
                try {
                    kpiWriter.close();
                } catch (IOException e) {
                    logger.atError().setCause(e).log();
                }
            }
            logger.atInfo().log("Completed");
        } catch (IOException e) {
            logger.atError().setCause(e).log();
        }
    }

    /**
     * Runs interactive simulator
     */
    private void runInteractive() {
        paramsPanel.setStatus(status);
        monitorPanel.setStatus(status);
        simulator.pushSeed(Tuple2.of(status, Map::of));
        simulator.setEventInterval(Duration.ofMillis(paramsPanel.getTimeInterval()));
        frame.setVisible(true);
    }
}
