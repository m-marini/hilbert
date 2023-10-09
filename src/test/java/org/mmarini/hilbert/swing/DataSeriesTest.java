package org.mmarini.hilbert.swing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataSeriesTest {
    @Test
    void add1() {
        // Given ...
        DataSeries series = DataSeries.create(2, 1);
        // When ...
        series.add(1);
        // Then ...
        assertEquals(1, series.getSize());
        assertEquals(DoubleVector.of(1), series.getData());
    }

    @Test
    void add2() {
        // Given ...
        DataSeries series = DataSeries.create(2, 1);

        // When ...
        series.add(1);
        series.add(2);

        // Then ...
        assertEquals(2, series.getSize());
        assertEquals(DoubleVector.of(1, 2), series.getData());
    }

    @Test
    void add3() {
        // Given ...
        DataSeries series = DataSeries.create(2, 1);

        // When ...
        series.add(1);
        series.add(2);
        series.add(3);

        // Then ...
        assertEquals(2, series.getSize());
        assertEquals(DoubleVector.of(2, 3), series.getData());
    }


    @Test
    void add4() {
        // Given ...
        DataSeries series = DataSeries.create(2, 1);

        // When ...
        series.add(1);
        series.add(2);
        series.add(3);
        series.add(4);

        // Then ...
        assertEquals(2, series.getSize());
        assertEquals(DoubleVector.of(3, 4), series.getData());
    }

    @Test
    void create() {
        // Given ...
        // When ...
        DataSeries series = DataSeries.create(1, 1);
        // Then ...
        assertEquals(0, series.getSize());
        assertEquals(DoubleVector.of(), series.getData());
    }
}