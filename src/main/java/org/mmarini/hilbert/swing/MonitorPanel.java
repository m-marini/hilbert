package org.mmarini.hilbert.swing;

import org.mmarini.hilbert.Messages;
import org.mmarini.hilbert.model.Status;
import org.mmarini.swing.GridLayoutHelper;

import javax.swing.*;
import java.util.Map;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * Shows the kpis of simulation
 * The population, technology
 */
public class MonitorPanel extends JPanel {

    private static final int SERIES_SIZE = 400;
    private final JFormattedTextField popField;
    private final JFormattedTextField techField;
    private final JFormattedTextField lifeExpectancyField;
    private final JFormattedTextField kfField;
    private final JFormattedTextField keField;
    private final JFormattedTextField khField;
    private final PieChart popChart;
    private final PieChart resChart;
    private final LineChart popHistory;
    private final LineChart techHistory;
    private final DataSeries techSeries;
    private final DataSeries popSeries;
    private final DataSeries farmersSeries;
    private final DataSeries researchersSeries;
    private final DataSeries educatorsSeries;
    private final DataSeries doctorsSeries;
    private final DataSeries inactivesSeries;

    /**
     * Creates the monitor panel
     */
    public MonitorPanel() {
        this.popField = new JFormattedTextField();
        this.techField = new JFormattedTextField();
        this.lifeExpectancyField = new JFormattedTextField();
        this.kfField = new JFormattedTextField();
        this.keField = new JFormattedTextField();
        this.khField = new JFormattedTextField();
        this.popChart = new PieChart();
        this.resChart = new PieChart();
        this.popHistory = new LineChart();
        this.techHistory = new LineChart();
        this.popSeries = DataSeries.create(SERIES_SIZE);
        this.farmersSeries = DataSeries.create(SERIES_SIZE);
        this.researchersSeries = DataSeries.create(SERIES_SIZE);
        this.educatorsSeries = DataSeries.create(SERIES_SIZE);
        this.doctorsSeries = DataSeries.create(SERIES_SIZE);
        this.inactivesSeries = DataSeries.create(SERIES_SIZE);
        this.techSeries = DataSeries.create(SERIES_SIZE);

        init();
        createContent();
    }

    /**
     * Creates the panel content
     */
    private void createContent() {
        JPanel chartsPanel = new GridLayoutHelper<>(new JPanel())
                .modify("insets,5")
                .modify("fill weight,1,1 at,0,0").add(popChart)
                .modify("at,1,0").add(resChart)
                .modify("at,0,1").add(popHistory)
                .modify("at,1,1").add(techHistory)
                .getContainer();
        JPanel fieldsPanel = new GridLayoutHelper<>(Messages.RESOURCE_BUNDLE, new JPanel())
                .modify("insets,5 e at,0,0").add("MonitorPanel.popLabel")
                .modify("at,0,1").add("MonitorPanel.techLabel")
                .modify("at,0,2").add("MonitorPanel.kfLabel")
                .modify("at,0,3").add("MonitorPanel.keLabel")
                .modify("at,0,4").add("MonitorPanel.khLabel")
                .modify("at,0,5").add("MonitorPanel.lifeExpectancyLabel")
                .modify("w hfill at,1,0").add(popField)
                .modify("at,1,1").add(techField)
                .modify("at,1,2").add(kfField)
                .modify("at,1,3").add(keField)
                .modify("at,1,4").add(khField)
                .modify("at,1,5").add(lifeExpectancyField)
                .getContainer();
        new GridLayoutHelper<>(Messages.RESOURCE_BUNDLE, this)
                .modify("insets,5 fill weight,1,1 at,0,0").add(chartsPanel)
                .modify("noweight nofill nw at,1,0").add(fieldsPanel);
    }

    /**
     * Initializes the panel
     */
    private void init() {
        popField.setValue(0);
        techField.setValue(0d);
        kfField.setValue(0d);
        keField.setValue(0d);
        khField.setValue(0d);
        lifeExpectancyField.setValue(0d);

        popHistory.setTitle(Messages.getString("MonitorPanel.popHistory.title"));
        popHistory.setLegend(Messages.getString("MonitorPanel.popHistory.legend").split(","));

        techHistory.setTitle(Messages.getString("MonitorPanel.techHistory.title"));

        popChart.setTitle(Messages.getString("MonitorPanel.popChart.title"));
        popChart.setLegend(Messages.getString("MonitorPanel.popChart.legend").split(","));

        resChart.setTitle(Messages.getString("MonitorPanel.resChart.title"));
        resChart.setLegend(Messages.getString("MonitorPanel.resChart.legend").split(","));

        Stream.of(popField, kfField, keField, khField, lifeExpectancyField,
                        techField)
                .forEach(field -> {
                    field.setColumns(10);
                    field.setHorizontalAlignment(JTextField.RIGHT);
                    field.setEditable(false);
                });
    }

    /**
     * Sets the kpis
     *
     * @param kpis the kpis
     */
    public void setKpis(Map<String, Number> kpis) {
        if (!kpis.isEmpty()) {
            kfField.setValue(kpis.get("kf"));
            keField.setValue(kpis.get("ke"));
            khField.setValue(kpis.get("kh"));
            lifeExpectancyField.setValue(kpis.get("lifeExpectancy"));
        }
    }

    /**
     * Sets the status info
     *
     * @param status the status
     */
    public void setStatus(Status status) {
        popField.setValue(status.getPopulation());
        techField.setValue(status.getTechnology());

        popSeries.add(status.getPopulation());
        farmersSeries.add(status.getFarmers());
        researchersSeries.add(status.getResearchers());
        educatorsSeries.add(status.getEducators());
        doctorsSeries.add(status.getDoctors());
        inactivesSeries.add(status.getInactives());
        techSeries.add(status.getTechnology());

        popHistory.setyData(
                farmersSeries.getData(),
                researchersSeries.getData(),
                educatorsSeries.getData(),
                doctorsSeries.getData(),
                inactivesSeries.getData(),
                popSeries.getData());
        techHistory.setyData(techSeries.getData());

        popChart.setData(
                status.getFarmers(),
                status.getResearchers(),
                status.getEducators(),
                status.getDoctors(),
                status.getInactives());
        resChart.setData(DoubleStream.of(
                        status.getFoodRatio(),
                        status.getResearchRatio(),
                        status.getEducationRatio(),
                        status.getHealthRatio(),
                        status.getSettlementRatio())
                .map(x -> x * 100)
                .toArray()
        );
    }
}
