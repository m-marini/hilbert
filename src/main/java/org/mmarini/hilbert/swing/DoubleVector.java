package org.mmarini.hilbert.swing;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

/**
 * Represents a immutable double vector
 */
public class DoubleVector {

    /**
     * Returns the vector from sliced array
     *
     * @param values the values
     * @param offset the offset
     * @param size   the size
     */
    public static DoubleVector create(double[] values, int offset, int size) {
        requireNonNull(values);
        if (!(offset >= 0)) {
            throw new IllegalArgumentException("offset must be non negative");
        }
        if (!(size >= 0)) {
            throw new IllegalArgumentException("size must be non negative");
        }
        if (!(offset == 0 && size == 0 || offset < values.length)) {
            throw new IllegalArgumentException("offset exceeds the buffer size");
        }
        if (!(offset == 0 && size == 0 || offset + size <= values.length)) {
            throw new IllegalArgumentException("size exceeds the buffer size");
        }
        return new DoubleVector(values, offset, size);
    }

    /**
     * Returns the double vector from value stream
     *
     * @param stream the stream
     */
    public static DoubleVector from(DoubleStream stream) {
        return of(stream.toArray());
    }

    /**
     * Returns the vector from values
     *
     * @param values the values
     */
    public static DoubleVector of(double... values) {
        return new DoubleVector(requireNonNull(values), 0, values.length);
    }

    private final double[] buffer;
    private final int offset;
    private final int size;

    /**
     * Creates the double vector
     *
     * @param buffer the data buffer
     * @param offset the offset
     * @param size   the size
     */
    protected DoubleVector(double[] buffer, int offset, int size) {
        this.buffer = buffer;
        this.offset = offset;
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DoubleVector that = (DoubleVector) o;
        return size == that.size && Arrays.equals(
                buffer, offset, offset + size,
                that.buffer, that.offset, that.offset + that.size);
    }

    /**
     * Returns the i-th element
     *
     * @param i the element index
     */
    public double get(int i) {
        if (!(i >= 0 && i < size)) {
            throw new ArrayIndexOutOfBoundsException(i);
        }
        return buffer[i + offset];
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(size);
        for (int i = offset; i < offset + size; i++) {
            result = 31 * result + Double.hashCode(buffer[i]);
        }
        return result;
    }

    /**
     * Returns the size of vector
     */
    public int size() {
        return size;
    }

    /**
     * Returns the sliced vector
     *
     * @param from the start index
     * @param to   the end index exclusive
     */
    public DoubleVector slice(int from, int to) {
        return create(buffer, offset + from, to - from);
    }

    /**
     * Returns the stream of values
     */
    public DoubleStream stream() {
        return DoubleStream.of(buffer).skip(offset).limit(size);
    }

    @Override
    public String toString() {
        if (size == 0) {
            return "()";
        } else if (size <= 5) {
            return IntStream.range(offset, offset + size)
                    .mapToObj(i -> String.valueOf(buffer[i]))
                    .collect(Collectors.joining(", ", "(", ")"));
        } else {
            return "("
                    + IntStream.range(offset, offset + 5)
                    .mapToObj(i -> String.valueOf(buffer[i]))
                    .collect(Collectors.joining(", "))
                    + ", ...)";
        }
    }
}
