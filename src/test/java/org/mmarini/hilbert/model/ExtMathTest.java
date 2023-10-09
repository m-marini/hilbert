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

package org.mmarini.hilbert.model;

import org.junit.jupiter.api.Test;

import static java.lang.Math.log;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExtMathTest {

    @Test
    void digitFloor() {
        // Given ...
        // When ...
        double zero = ExtMath.digitFloor(0d, 4);
        double negZero = ExtMath.digitFloor(-0d, 4);
        double one = ExtMath.digitFloor(1d, 4);
        double negOne = ExtMath.digitFloor(-1d, 4);

        double num1_23456789 = ExtMath.digitFloor(1.23456789d, 4);
        double num1234_56789 = ExtMath.digitFloor(1234.56789d, 4);
        double num999_99999 = ExtMath.digitFloor(999.99999d, 4);
        double num1_23456789e29 = ExtMath.digitFloor(1.23456789e29, 4);


        assertEquals(0d, zero);
        assertEquals(-0d, negZero);
        assertEquals(1d, one);
        assertEquals(-1d, negOne);

        assertEquals(1.234d, num1_23456789);

        assertThat(num1234_56789, closeTo(1234d, 1e-10));

        assertEquals(999.9d, num999_99999);

        assertThat(num1_23456789e29, closeTo(1.234e29, 1e-10));
    }

    @Test
    void invSoftmax() {
        // Given ...
        // When ...
        double[] prefs = ExtMath.invSoftmax(0.5, 1, 2);

        // Then ...
        assertArrayEquals(new double[]{
                -log(2),
                0,
                log(2)
        }, prefs);
    }

    @Test
    void softmax() {
        // Given ...
        // When ...
        double[] prefs = ExtMath.softmax(-log(2), 0, log(2));

        // Then ...
        assertArrayEquals(new double[]{
                0.5 / 3.5,
                1 / 3.5,
                2 / 3.5
        }, prefs);

    }
}