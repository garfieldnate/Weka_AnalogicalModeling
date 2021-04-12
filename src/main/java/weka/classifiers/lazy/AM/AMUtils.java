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

import com.jakewharton.picnic.CellStyle;
import com.jakewharton.picnic.TextAlignment;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

import static java.math.MathContext.DECIMAL32;

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

    /**
     * Picnic library table style used for printing gangs and analogical sets
     */
    public static final CellStyle REPORT_TABLE_STYLE = new CellStyle.Builder()
        .setPaddingLeft(1).
        setPaddingRight(1).
        setBorderLeft(true).
        setBorderRight(true).
        setAlignment(TextAlignment.MiddleRight).build();

    // used for printing all decimal numbers
    private static final String DECIMAL_FORMAT = "%.5f";

    /**
     * Format a double into a string using {@link #DECIMAL_FORMAT}.
     *
     * @param d value to format
     * @return {@code d} formatted using {@link #DECIMAL_FORMAT}
     */
    public static String formatDouble(double d) {
        return String.format(DECIMAL_FORMAT, d);
    }

    public static final MathContext MATH_CONTEXT = DECIMAL32;

    /**
     * @return return a formatted percentage indicating the size of the analogical effect of {@code pointers}
     */
    public static String formatPointerPercentage(BigInteger pointers, BigDecimal totalPointers) {
        BigDecimal ratio = new BigDecimal(pointers).divide(totalPointers, MATH_CONTEXT);
        return formatPercentage(ratio);
    }

    /**
     * @return {@code val} formatted as a percentage with three decimal places
     */
    public static String formatPercentage(BigDecimal val) {
        float percentage = val.scaleByPowerOfTen(2).floatValue();
        return String.format("%%%.3f", percentage);
    }
}
