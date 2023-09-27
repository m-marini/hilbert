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

import java.util.Arrays;

/**
 * ExtMath functions
 */
public interface ExtMath {
    /**
     * Returns the softmax preferences of arguments
     *
     * @param ratios the ratios
     */
    static double[] invSoftmax(double... ratios) {
        double[] prefs = Arrays.stream(ratios).map(Math::log).toArray();
        double offset = (Arrays.stream(prefs).max().orElseThrow() + Arrays.stream(prefs).min().orElseThrow()) / 2;
        for (int i = 0; i < ratios.length; i++) {
            prefs[i] -= offset;
        }
        return prefs;
    }

    /**
     * Returns the softmax ratios of arguments
     *
     * @param prefs the preferences
     */
    static double[] softmax(double... prefs) {
        double[] ratios = Arrays.stream(prefs).map(Math::exp).toArray();
        double tot = Arrays.stream(ratios).sum();
        for (int i = 0; i < ratios.length; i++) {
            ratios[i] /= tot;
        }
        return ratios;
    }
}
