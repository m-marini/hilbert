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

import java.util.Random;

import static java.lang.Math.exp;

/**
 * Add the generation of Poisson number
 */
public class ExtRandom extends Random {
    private static final double STEP = 500;
    private static final double E_STEP = exp(STEP);


    public ExtRandom() {
        super();
    }

    public ExtRandom(long seed) {
        super(seed);
    }

    /**
     * Returns a value with poisson distribution (mean = lambda)
     *
     * @param lambda the lambda parameter
     */
    public int nextPoisson(double lambda) {
        double l = exp(-lambda);
        int k = -1;
        double p = 1;
        do {
            ++k;
            double u = nextDouble();
            p *= u;
        } while (p > l);
        return k;
    }

    /**
     * Returns a value with poisson distribution (mean = lambda)
     *
     * @param lambda the lambda parameter
     */
    public int nextPoisson1(double lambda) {
        if (lambda < 10) {
            double l = exp(-lambda);
            int k = -1;
            double p = 1;
            do {
                ++k;
                double u = nextDouble();
                p *= u;
            } while (p > l);
            return k;
        } else {
            double lambdaLeft = lambda;
            int k = -1;
            double p = 1;
            do {
                k++;
                while (p < 1 && lambdaLeft > 0) {
                    if (lambdaLeft > STEP) {
                        p *= E_STEP;
                        lambdaLeft -= STEP;
                    } else {
                        p *= exp(lambdaLeft);
                        lambdaLeft = 0;
                    }
                }
            } while (p > 0);
            return k;
        }
    }
}
