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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mmarini.ArgumentsGenerator;

import java.util.stream.Stream;

import static java.lang.Math.E;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StatusTest {
    public static Stream<Arguments> statusSum() {
        return ArgumentsGenerator.createStream(10, 1234,
                ArgumentsGenerator.uniform(-10, 10), // pop
                ArgumentsGenerator.gaussian(0d, 4d), // farmers
                ArgumentsGenerator.gaussian(0d, 4d), // researchers
                ArgumentsGenerator.gaussian(0d, 4d), // educators
                ArgumentsGenerator.gaussian(0d, 4d), // doctors
                ArgumentsGenerator.gaussian(0d, 4d), // inactive
                ArgumentsGenerator.gaussian(0d, 4d), // food
                ArgumentsGenerator.gaussian(0d, 4d), // research
                ArgumentsGenerator.gaussian(0d, 4d), // education
                ArgumentsGenerator.gaussian(0d, 4d), // health
                ArgumentsGenerator.gaussian(0d, 4d), // settlement
                ArgumentsGenerator.uniform(0d, 4d), // technology

                ArgumentsGenerator.uniform(-10, 10), // pop
                ArgumentsGenerator.gaussian(0d, 4d), // farmers
                ArgumentsGenerator.gaussian(0d, 4d), // researchers
                ArgumentsGenerator.gaussian(0d, 4d), // educators
                ArgumentsGenerator.gaussian(0d, 4d), // doctors
                ArgumentsGenerator.gaussian(0d, 4d), // inactive
                ArgumentsGenerator.gaussian(0d, 4d), // food
                ArgumentsGenerator.gaussian(0d, 4d), // research
                ArgumentsGenerator.gaussian(0d, 4d), // education
                ArgumentsGenerator.gaussian(0d, 4d), // health
                ArgumentsGenerator.gaussian(0d, 4d), // settlement
                ArgumentsGenerator.uniform(0d, 4d) // technology
        ).map(arguments -> {
            Object[] args = arguments.get();

            int i = 0;
            int pop1 = ((Number) args[i++]).intValue();
            double pf1 = ((Number) args[i++]).doubleValue();
            double pr1 = ((Number) args[i++]).doubleValue();
            double pe1 = ((Number) args[i++]).doubleValue();
            double ph1 = ((Number) args[i++]).doubleValue();
            double pi1 = ((Number) args[i++]).doubleValue();
            double rf1 = ((Number) args[i++]).doubleValue();
            double rr1 = ((Number) args[i++]).doubleValue();
            double re1 = ((Number) args[i++]).doubleValue();
            double rh1 = ((Number) args[i++]).doubleValue();
            double rs1 = ((Number) args[i++]).doubleValue();
            double tec1 = ((Number) args[i++]).doubleValue();

            int pop2 = ((Number) args[i++]).intValue();
            double pf2 = ((Number) args[i++]).doubleValue();
            double pr2 = ((Number) args[i++]).doubleValue();
            double pe2 = ((Number) args[i++]).doubleValue();
            double ph2 = ((Number) args[i++]).doubleValue();
            double pi2 = ((Number) args[i++]).doubleValue();
            double rf2 = ((Number) args[i++]).doubleValue();
            double rr2 = ((Number) args[i++]).doubleValue();
            double re2 = ((Number) args[i++]).doubleValue();
            double rh2 = ((Number) args[i++]).doubleValue();
            double rs2 = ((Number) args[i++]).doubleValue();
            double tec2 = ((Number) args[i++]).doubleValue();

            return Arguments.of(
                    new Status(pop1, pf1, pr1, pe1, ph1, pi1, rf1, rr1, re1, rh1, rs1, tec1),
                    new Status(pop2, pf2, pr2, pe2, ph2, pi2, rf2, rr2, re2, rh2, rs2, tec2),
                    new Status(pop1 + pop2,
                            pf1 + pf2,
                            pr1 + pr2,
                            pe1 + pe2,
                            ph1 + ph2,
                            pi1 + pi2,
                            rf1 + rf2,
                            rr1 + rr2,
                            re1 + re2,
                            rh1 + rh2,
                            rs1 + rs2,
                            tec1 + tec2));
        });
    }

    @Test
    void getEfficiency() {
        // Given ...
        Status demography = Status.technology(1);

        // When ...
        double ka = demography.getEfficiency();

        // Then ...
        assertEquals(1 - 1 / E, ka);
    }

    @ParameterizedTest
    @MethodSource("statusSum")
    void sum(Status a, Status b, Status expected) {
        assertEquals(expected, Status.sum(a, b));
    }

}