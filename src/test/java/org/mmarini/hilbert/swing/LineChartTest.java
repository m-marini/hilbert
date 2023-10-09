package org.mmarini.hilbert.swing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

class LineChartTest {

    @ParameterizedTest
    @CsvSource({
            "0,0, -1,1,1,0.2",
            "1,1,  0.99996,1.00002,0.0001,0.00002",
            "1.23456,1.23456,  1.23454,1.23458,0.0001,0.00002",
            "0,1,  -0.2,1.2,1,0.2",
            "0,1.1,  -0.2,1.4,1,0.2",
            "1000,1002, 999.8,1002.2,1,0.2",
            "1,11, -2,14,10,2",
            "123456.7,123456.8, 123456.68,123456.82,0.1,0.02",
    })
    void getAxisConfigs(double x1, double x2, double min, double max, double major, double minor) {
        // Given ...
        LineChart.AxixConfig config = LineChart.getAxisConfigs(List.of(DoubleVector.of(x1, x2)));

        assertThat(config.min, closeTo(min, 1e-10));
        assertThat(config.max, closeTo(max, 1e-10));
        assertThat(config.majorTick, closeTo(major, 1e-10));
        assertThat(config.minorTick, closeTo(minor, 1e-10));
    }
}