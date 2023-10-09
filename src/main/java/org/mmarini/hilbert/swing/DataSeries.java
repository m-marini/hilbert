package org.mmarini.hilbert.swing;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Objects.requireNonNull;

/**
 * Keeps the last n data series
 */
public class DataSeries {
    /**
     * Returns the series
     *
     * @param size   the series size
     * @param extent the extent
     */
    public static DataSeries create(int size, int extent) {
        return new DataSeries(new double[size + extent], size);
    }

    /**
     * Returns the series
     *
     * @param size the series size
     */
    public static DataSeries create(int size) {
        return create(size, max(10, size / 2));
    }

    private final int maxSize;
    private double[] buffer;
    private int capacity;

    /**
     * Creates the data serie
     *
     * @param buffer  the buffer
     * @param maxSize the maximum data size
     */
    protected DataSeries(double[] buffer, int maxSize) {
        this.buffer = requireNonNull(buffer);
        this.maxSize = maxSize;
    }

    /**
     * Add a value to the series
     *
     * @param value the values
     */
    public DataSeries add(double value) {
        if (capacity >= buffer.length) {
            double[] newBuffer = new double[buffer.length];
            System.arraycopy(buffer, buffer.length - maxSize + 1, newBuffer, 0, maxSize - 1);
            capacity = maxSize - 1;
            buffer = newBuffer;
        }
        buffer[capacity++] = value;
        return this;
    }

    /**
     * Returns the data series
     */
    public DoubleVector getData() {
        int size = getSize();
        return DoubleVector.create(buffer, capacity - size, size);
    }

    /**
     * Returns the maximum data series size
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Returns the current data series size
     */
    public int getSize() {
        return min(capacity, maxSize);
    }
}
