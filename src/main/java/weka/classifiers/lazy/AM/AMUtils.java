/*
 * **************************************************************************
 * Copyright 2012 Nathan Glenn                                              * 
 * Licensed under the Apache License, Version 2.0 (the "License");          *
 * you may not use this file except in compliance with the License.         *
 * You may obtain a copy of the License at                                  *
 *                                                                          *
 * http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                          *
 * Unless required by applicable law or agreed to in writing, software      *
 * distributed under the License is distributed on an "AS IS" BASIS,        *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and      *
 * limitations under the License.                                           *
 ****************************************************************************/
package weka.classifiers.lazy.AM;

import java.math.MathContext;
import java.math.RoundingMode;

/**
 * This class holds constants and methods used in the AM classifier.
 *
 * @author nathan.glenn
 */
public class AMUtils {
    /**
     * An unknown class value.
     */
    public static final double UNKNOWN = Double.NaN;

    /**
     * A non-deterministic outcome, meaning that there is more than one
     * possibility.
     */
    public static final double NONDETERMINISTIC = -1;
    public static final String NONDETERMINISTIC_STRING = "&nondeterministic&";

    /**
     * A heterogeneous outcome, which means we don't bother with it.
     */
    public static final double HETEROGENEOUS = -2;

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    // used for printing all decimal numbers
    private static final String DECIMAL_FORMAT = "%.5f";

    /**
     * Format a double into a string using {@link #DECIMAL_FORMAT}.
     *
     * @param d
     * @return
     */
    public static String formatDouble(double d) {
        return String.format(DECIMAL_FORMAT, d);
    }

    /**
     * This is used by all of the BigDecimals. Precision is to 10 decimals.
     */
    public static final MathContext matchContext = new MathContext(10, RoundingMode.HALF_EVEN);

}
