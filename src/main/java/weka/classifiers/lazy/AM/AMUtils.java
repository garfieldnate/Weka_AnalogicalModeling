/*
 * **************************************************************************
 * Copyright 2021 Nathan Glenn                                              *
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
import java.util.*;
import java.util.stream.Collectors;

import static java.math.RoundingMode.HALF_EVEN;

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

    /**
     * @param pointers         the number of pointers for the current context
     * @param totalPointers    the number of pointers for all contexts
     * @param numDecimals      the number of digits to output after the decimal point.
     * @param addPercentPrefix true if a percent sign (%) should be prefixed
     * @return return a formatted percentage indicating the size of the analogical effect of {@code pointers}
     */
    public static String formatPointerPercentage(BigInteger pointers, BigDecimal totalPointers, int numDecimals, boolean addPercentPrefix) {
        BigDecimal ratio = new BigDecimal(pointers).divide(totalPointers, new MathContext(numDecimals + 2, HALF_EVEN));
        return formatPercentage(ratio, numDecimals, addPercentPrefix);
    }

    /**
     * @param numDecimals      the number of digits to output after the decimal point.
     * @param addPercentPrefix true if a percent sign (%) should be prefixed
     * @return {@code val} formatted as a percentage with three decimal places
     */
    public static String formatPercentage(BigDecimal val, int numDecimals, boolean addPercentPrefix) {
        float percentage = val.scaleByPowerOfTen(2).floatValue();
        String prefix = "";
        if (addPercentPrefix) {
            prefix = "%%";
        }
        return String.format(prefix + "%." + numDecimals + "f", percentage);
    }

    public static class CsvDoc {
        public final List<String> headers;
        public final List<List<String>> entries;

        public CsvDoc(List<String> headers, List<List<String>> entries) {
            this.headers = headers;
            this.entries = entries;
        }
    }

    /**
     * Simplify CSV printing by allowing specifying rows as Maps and converting automatically to lists of column values
     * in the {@link #build()} method.
     */
    public static class CsvBuilder {
        private final Set<String> header = new LinkedHashSet<>();
        private final List<Map<String, String>> entries = new ArrayList<>();

        private final Map<String, String> defaultValues = new HashMap<>();

        public void addEntry(Map<String, String> entry) {
            Map<String, String> safeCopy = new HashMap<>(entry);
            entries.add(safeCopy);
            header.addAll(entry.keySet());
        }

        public CsvDoc build() {
            List<String> sortedHeader = header.stream().sorted().collect(Collectors.toList());
            List<List<String>> rows = new ArrayList<>();
            for (Map<String, String> entry : entries) {
                List<String> row = new ArrayList<>();
                rows.add(row);
                for (String h : sortedHeader) {
                    row.add(entry.getOrDefault(h, defaultValues.getOrDefault(h, "")));
                }
            }
            return new CsvDoc(sortedHeader, rows);
        }

        public void setDefault(String columnName, String value) {
            defaultValues.put(columnName, value);
        }
    }
}
