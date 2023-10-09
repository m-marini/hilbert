package org.mmarini.hilbert.swing;

import hu.akarnokd.rxjava3.swing.SwingObservable;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.PublishProcessor;
import org.mmarini.hilbert.Messages;
import org.mmarini.hilbert.model.Status;
import org.mmarini.swing.GridLayoutHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.util.Arrays;
import java.util.stream.Stream;

import static java.lang.Math.max;
import static java.lang.Math.round;
import static org.mmarini.hilbert.model.ExtMath.invSoftmax;

/**
 * Manages the simulation parameters
 */
public class ParamsPanel extends JPanel {
    public static final int MAXIMUM_TIME_INTERVAL = 5000;
    public static final int TIME_INTERVAL_STEP = 50;
    private static final long DEFAULT_TIME_INTERVAL = 250;
    private static final Logger logger = LoggerFactory.getLogger(ParamsPanel.class);

    /**
     * Returns the normalized spin values
     *
     * @param args the spin values
     */
    private static int[] normalizeSpin(int... args) {
        int min = Arrays.stream(args).min().orElse(0);
        if (min <= 0) {
            return Arrays.stream(args).map(x -> x - min + 1).toArray();
        } else {
            return args;
        }
    }

    /**
     * Returns the preferences
     *
     * @param args the spin values
     */
    private static double[] toPrefs(int... args) {
        double[] values = Arrays.stream(args)
                .mapToDouble(x -> x)
                .toArray();
        return invSoftmax(values);
    }

    /**
     * Returns the spin values from coefficients
     *
     * @param args the coefficients
     */
    private static int[] toSpin(double... args) {
        double min = Arrays.stream(args).min().orElse(0);
        double max = Arrays.stream(args).max().orElse(0);
        double scale = max(100 / max, 1 / min);
        return Arrays.stream(args)
                .mapToInt(x -> (int) round(x * scale))
                .toArray();
    }

    private final JSpinner farmersSpinner;
    private final JSpinner researchersSpinner;
    private final JSpinner educatorsSpinner;
    private final JSpinner doctorsSpinner;
    private final JSpinner inactiveSpinner;
    private final JSpinner foodSpinner;
    private final JSpinner researchSpinner;
    private final JSpinner educationSpinner;
    private final JSpinner healthSpinner;
    private final JSpinner settlementSpinner;
    private final JSpinner timeIntervalSpinner;
    private final PublishProcessor<Status> statusProcessor;
    private final Flowable<Number> timeIntervalFlow;
    private boolean changing;

    /**
     * Creates the parameters panel
     */
    public ParamsPanel() {
        this.farmersSpinner = new JSpinner();
        this.researchersSpinner = new JSpinner();
        this.educatorsSpinner = new JSpinner();
        this.doctorsSpinner = new JSpinner();
        this.inactiveSpinner = new JSpinner();
        this.foodSpinner = new JSpinner();
        this.researchSpinner = new JSpinner();
        this.educationSpinner = new JSpinner();
        this.healthSpinner = new JSpinner();
        this.settlementSpinner = new JSpinner();
        this.timeIntervalSpinner = new JSpinner(new SpinnerNumberModel());
        this.statusProcessor = PublishProcessor.create();
        SpinnerNumberModel model = (SpinnerNumberModel) timeIntervalSpinner.getModel();
        model.setMinimum(0);
        model.setMaximum(MAXIMUM_TIME_INTERVAL);
        model.setStepSize(TIME_INTERVAL_STEP);
        timeIntervalFlow = SwingObservable.change(timeIntervalSpinner)
                .toFlowable(BackpressureStrategy.LATEST)
                .map(ev -> getTimeInterval());
        timeIntervalSpinner.setValue(DEFAULT_TIME_INTERVAL);
        init();

        Stream.of(farmersSpinner, researchersSpinner, educatorsSpinner, doctorsSpinner, inactiveSpinner)
                .forEach(spinner -> spinner.addChangeListener((this::handlePopParamsChange)));
        Stream.of(foodSpinner, researchSpinner, educationSpinner, healthSpinner, settlementSpinner)
                .forEach(spinner -> spinner.addChangeListener((this::handleResParamsChange)));

        logger.atDebug().log("Created");
    }

    /**
     * Returns the status preferences
     */
    public Status getStatus() {
        double[] popPrefs = toPrefs(
                ((Number) farmersSpinner.getValue()).intValue(),
                ((Number) researchersSpinner.getValue()).intValue(),
                ((Number) educatorsSpinner.getValue()).intValue(),
                ((Number) doctorsSpinner.getValue()).intValue(),
                ((Number) inactiveSpinner.getValue()).intValue()
        );
        double[] resPrefs = toPrefs(
                ((Number) foodSpinner.getValue()).intValue(),
                ((Number) researchSpinner.getValue()).intValue(),
                ((Number) educationSpinner.getValue()).intValue(),
                ((Number) healthSpinner.getValue()).intValue(),
                ((Number) settlementSpinner.getValue()).intValue()
        );
        return new Status(0, popPrefs[0], popPrefs[1], popPrefs[2], popPrefs[3], popPrefs[4],
                resPrefs[0], resPrefs[1], resPrefs[2], resPrefs[3], resPrefs[4], 0);
    }

    /**
     * Sets the status
     *
     * @param status the status
     */
    public void setStatus(Status status) {
        changing = true;
        int[] alphasSpin = toSpin(status.getFarmers() / status.getPopulation(),
                status.getResearchers() / status.getPopulation(),
                status.getEducators() / status.getPopulation(),
                status.getDoctors() / status.getPopulation(),
                status.getInactives() / status.getPopulation());
        farmersSpinner.setValue(alphasSpin[0]);
        researchersSpinner.setValue(alphasSpin[1]);
        educatorsSpinner.setValue(alphasSpin[2]);
        doctorsSpinner.setValue(alphasSpin[3]);
        inactiveSpinner.setValue(alphasSpin[4]);

        int[] betasSpin = toSpin(
                status.getFoodRatio(),
                status.getResearchRatio(),
                status.getEducationRatio(),
                status.getHealthRatio(),
                status.getSettlementRatio());
        foodSpinner.setValue(betasSpin[0]);
        researchSpinner.setValue(betasSpin[1]);
        educationSpinner.setValue(betasSpin[2]);
        healthSpinner.setValue(betasSpin[3]);
        settlementSpinner.setValue(betasSpin[4]);
        changing = false;
    }

    /**
     * Returns the speed
     */
    public long getTimeInterval() {
        return ((Number) timeIntervalSpinner.getValue()).longValue();
    }

    /**
     * Handles population change event
     *
     * @param changeEvent the event
     */
    private void handlePopParamsChange(ChangeEvent changeEvent) {
        if (!changing) {
            changing = true;
            int[] norm = normalizeSpin(
                    ((Number) farmersSpinner.getValue()).intValue(),
                    ((Number) researchersSpinner.getValue()).intValue(),
                    ((Number) educatorsSpinner.getValue()).intValue(),
                    ((Number) doctorsSpinner.getValue()).intValue(),
                    ((Number) inactiveSpinner.getValue()).intValue());
            farmersSpinner.setValue(norm[0]);
            researchersSpinner.setValue(norm[1]);
            educatorsSpinner.setValue(norm[2]);
            doctorsSpinner.setValue(norm[3]);
            inactiveSpinner.setValue(norm[4]);
            changing = false;
            statusProcessor.onNext(getStatus());
        }
    }

    /**
     * Handles resources change event
     *
     * @param changeEvent the event
     */
    private void handleResParamsChange(ChangeEvent changeEvent) {
        if (!changing) {
            changing = true;
            int[] norm = normalizeSpin(
                    ((Number) foodSpinner.getValue()).intValue(),
                    ((Number) researchSpinner.getValue()).intValue(),
                    ((Number) educationSpinner.getValue()).intValue(),
                    ((Number) healthSpinner.getValue()).intValue(),
                    ((Number) settlementSpinner.getValue()).intValue());
            foodSpinner.setValue(norm[0]);
            researchSpinner.setValue(norm[1]);
            educationSpinner.setValue(norm[2]);
            healthSpinner.setValue(norm[3]);
            settlementSpinner.setValue(norm[4]);
            changing = false;
            statusProcessor.onNext(getStatus());
        }
    }

    /**
     * Initializes the panel
     */
    void init() {
        JPanel simParamsPanel = new GridLayoutHelper<>(Messages.RESOURCE_BUNDLE, new JPanel())
                .modify("insets,5 hw,1 at,0,0").add("Simulate.timeIntervalLabel.text")
                .modify("hfill at,0,1").add(timeIntervalSpinner)
                .getContainer();
        JPanel popParamsPanel = new GridLayoutHelper<>(Messages.RESOURCE_BUNDLE, new JPanel())
                .modify("insets,5 hw,1 at,0,0").add("Simulate.farmersLabel.text")
                .modify("at,1,0").add("Simulate.researchersLabel.text")
                .modify("at,2,0").add("Simulate.educatorsLabel.text")
                .modify("at,3,0").add("Simulate.doctorsLabel.text")
                .modify("at,4,0").add("Simulate.inactivesLabel.text")
                .modify("hfill at,0,1").add(farmersSpinner)
                .modify("at,1,1").add(researchersSpinner)
                .modify("at,2,1").add(educatorsSpinner)
                .modify("at,3,1").add(doctorsSpinner)
                .modify("at,4,1").add(inactiveSpinner)
                .getContainer();
        JPanel resParamsPanel = new GridLayoutHelper<>(Messages.RESOURCE_BUNDLE, new JPanel())
                .modify("insets,5 hw,1 at,0,0").add("Simulate.foodLabel.text")
                .modify("at,1,0").add("Simulate.researchLabel.text")
                .modify("at,2,0").add("Simulate.educationLabel.text")
                .modify("at,3,0").add("Simulate.healthLabel.text")
                .modify("at,4,0").add("Simulate.settlementLabel.text")
                .modify("hfill at,0,1").add(foodSpinner)
                .modify("at,1,1").add(researchSpinner)
                .modify("at,2,1").add(educationSpinner)
                .modify("at,3,1").add(healthSpinner)
                .modify("at,4,1").add(settlementSpinner)
                .getContainer();
        simParamsPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("Simulate.simParams.title")));
        popParamsPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("Simulate.popParams.title")));
        resParamsPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("Simulate.resParams.title")));

        new GridLayoutHelper<>(this)
                .modify("insets,2 weight,1,1 fill at,0,0").add(simParamsPanel)
                .modify("at,1,0").add(popParamsPanel)
                .modify("at,2,0").add(resParamsPanel);
    }

    /**
     * Returns the status event flow
     */
    public Flowable<Status> readStatus() {
        return statusProcessor;
    }

    /**
     * Returns the speed flow
     */
    public Flowable<Number> readTimeInterval() {
        return timeIntervalFlow;
    }
}
