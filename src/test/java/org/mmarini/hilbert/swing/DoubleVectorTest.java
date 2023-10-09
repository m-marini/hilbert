package org.mmarini.hilbert.swing;

import org.junit.jupiter.api.Test;

import java.util.stream.DoubleStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.matchesRegex;
import static org.junit.jupiter.api.Assertions.*;

class DoubleVectorTest {
    @Test
    void createTest() {
        DoubleVector v = DoubleVector.create(new double[]{1, 2, 3, 4}, 1, 2);
        DoubleVector v0 = DoubleVector.create(new double[0], 0, 0);
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> {
            DoubleVector.create(new double[]{1, 2, 3, 4}, -1, 2);
        });
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> {
            DoubleVector.create(new double[]{1, 2, 3, 4}, 0, -1);
        });
        IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class, () -> {
            DoubleVector.create(new double[]{1, 2, 3, 4}, 4, 1);
        });
        IllegalArgumentException ex4 = assertThrows(IllegalArgumentException.class, () -> {
            DoubleVector.create(new double[]{1, 2, 3, 4}, 3, 2);
        });

        assertEquals(0, v0.size());
        assertEquals(2, v.size());
        assertEquals(2d, v.get(0));
        assertEquals(3d, v.get(1));

        assertThat(ex1.getMessage(), matchesRegex("offset must be non negative"));
        assertThat(ex2.getMessage(), matchesRegex("size must be non negative"));
        assertThat(ex3.getMessage(), matchesRegex("offset exceeds the buffer size"));
        assertThat(ex4.getMessage(), matchesRegex("size exceeds the buffer size"));
    }

    @Test
    void equalsHashTest() {
        DoubleVector v0 = DoubleVector.of();
        DoubleVector v01 = DoubleVector.create(new double[]{1}, 0, 0);

        DoubleVector v2 = DoubleVector.of(0, 1);
        DoubleVector v21 = DoubleVector.create(new double[]{2, 0, 1, 4}, 1, 2);
        DoubleVector v22 = DoubleVector.of(0, 2);

        assertTrue(v0.equals(v0));
        assertTrue(v0.equals(v01));
        assertTrue(v0.hashCode() == v01.hashCode());

        assertTrue(v2.equals(v21));
        assertTrue(v21.equals(v2));
        assertTrue(v2.hashCode() == v21.hashCode());

        assertFalse(v0.equals(null));
        assertFalse(v01.equals(null));
        assertFalse(v2.equals(null));
        assertFalse(v21.equals(null));

        assertFalse(v0.equals(new Object()));
        assertFalse(v01.equals(new Object()));
        assertFalse(v2.equals(new Object()));
        assertFalse(v21.equals(new Object()));

        assertFalse(v0.equals(v2));
        assertFalse(v0.equals(v21));
        assertFalse(v01.equals(v2));
        assertFalse(v01.equals(v21));
        assertFalse(v2.equals(v0));
        assertFalse(v2.equals(v01));
        assertFalse(v2.equals(v22));
        assertFalse(v21.equals(v0));
        assertFalse(v21.equals(v01));
        assertFalse(v21.equals(v22));
    }

    @Test
    void ofTest() {
        DoubleVector v0 = DoubleVector.of();
        DoubleVector v1 = DoubleVector.of(1);
        DoubleVector v2 = DoubleVector.of(1, 2);

        assertEquals(0, v0.size());
        assertEquals(1, v1.size());
        assertEquals(2, v2.size());
        assertEquals(1d, v1.get(0));
        assertEquals(1d, v2.get(0));
        assertEquals(2d, v2.get(1));
    }

    @Test
    void sliceTest() {
        DoubleVector v0 = DoubleVector.of(0, 1, 2, 3);
        DoubleVector v = v0.slice(1, 3);
        assertEquals(2, v.size());
        assertEquals(1d, v.get(0));
        assertEquals(2d, v.get(1));
    }

    @Test
    void streamTest() {
        DoubleVector v = DoubleVector.create(new double[]{0, 1, 2, 3}, 1, 2);

        DoubleStream s = v.stream();
        double[] ary = s.toArray();

        assertArrayEquals(new double[]{1, 2}, ary);
    }

    @Test
    void toStringTest() {
        DoubleVector v0 = DoubleVector.of();
        DoubleVector v1 = DoubleVector.of(0);
        DoubleVector v5 = DoubleVector.of(0, 1, 2, 3, 4);
        DoubleVector v6 = DoubleVector.of(0, 1, 2, 3, 4, 5);

        assertThat(v0, hasToString("()"));
        assertThat(v1, hasToString("(0.0)"));
        assertThat(v5, hasToString("(0.0, 1.0, 2.0, 3.0, 4.0)"));
        assertThat(v6, hasToString("(0.0, 1.0, 2.0, 3.0, 4.0, ...)"));
    }
}