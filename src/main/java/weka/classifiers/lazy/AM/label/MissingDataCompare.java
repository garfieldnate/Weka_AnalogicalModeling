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
package weka.classifiers.lazy.AM.label;

import weka.classifiers.lazy.AM.Enum2TagUtils.TagInfo;
import weka.core.Attribute;
import weka.core.Instance;

public enum MissingDataCompare implements TagInfo {
    /**
     * Always returns 0, considering the missing data to match anything
     */
    MATCH("match", "Consider the missing attribute value to match anything") {
        @Override
        public boolean matches(Instance i1, Instance i2, Attribute att) {
            return true;
        }

    },

    /**
     * Always returns 1, considering the missing data to be a mismatch
     */
    MISMATCH("mismatch", "Consider the missing attribute value to be a mismatch") {
        @Override
        public boolean matches(Instance i1, Instance i2, Attribute att) {
            return false;
        }

    },

    /**
     * Treats missing value as like any other value; two missing values match,
     * but a missing value matches nothing else.
     */
    VARIABLE("variable",
             "Treat the the missing attribute value as an attribute value of its own; "
             + "a missing value will match another missing value, but nothing else."
    ) {
        @Override
        public boolean matches(Instance i1, Instance i2, Attribute att) {
            return i1.isMissing(att) && i2.isMissing(att);
        }

    };

    // string used on command line to indicate the use of this strategy
    private final String optionString;
    // string which describes comparison strategy for a given entry
    private final String description;

    /**
     * @param optionString The string required to choose this comparison strategy from the command line
     * @param description  A description of the comparison strategy for the given value
     */
    MissingDataCompare(String optionString, String description) {
        this.optionString = optionString;
        this.description = description;
    }

    /**
     * @return string used on command line to indicate the use of this strategy
     */
    @Override
    public String getOptionString() {
        return optionString;
    }

    /**
     * @return string which describes comparison strategy for a given
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Compare the two instances and return the comparison result. It is assumed
     * that has a missing value for the given attribute.
     *
     * @param i1  First instance
     * @param i2  Second instance
     * @param att Attribute to be compared between the two instances
     * @return true if the attributes match, false if they do not; the matching mechanism depends on the chosen
     * algorithm.
     */
    public abstract boolean matches(Instance i1, Instance i2, Attribute att);
}
