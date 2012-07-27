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
package weka.classifiers.lazy.AM.lattice;

import weka.core.SelectedTag;
import weka.core.Tag;

public enum MissingDataCompare {
	/**
	 * Always returns 0, considering the missing data to match anything
	 */
	MATCH("match", "Consider the missing attribute value to match anything") {
		@Override
		public int outcome(int var1, int var2) {
			return 0;
		}

	},

	/**
	 * Always returns 1, considering the missing data to be a mismatch
	 */
	MISMATCH("mismatch",
			"Consider the missing attribute value to be a mismatch") {
		@Override
		public int outcome(int var1, int var2) {
			return 1;
		}

	},

	/**
	 * Treats missing value as like any other value; two missing values match,
	 * but a missing value matches nothing else.
	 */
	VARIABLE(
			"variable",
			"Treat the the missing attribute value as an attribute value of its own; "
					+ "a missing value will match another missing value, but nothing else.") {
		@Override
		public int outcome(int var1, int var2) {
			return var1 != var2 ? 1 : 0;
		}

	};

	// string used on command line to indicate the use of this strategy
	private final String optionString;
	// string which describes comparison strategy for a given entry
	private String description;

	/**
	 * 
	 * @param optionString
	 *            The string required to choose this comparison strategy from
	 *            the command line
	 * @param description
	 *            A description of the comparison strategy for the given value
	 */
	MissingDataCompare(String optionString, String description) {
		this.optionString = optionString;
		this.description = description;
	};

	/**
	 * 
	 * @return string used on command line to indicate the use of this strategy
	 */
	public String getOptionString() {
		return optionString;
	}

	/**
	 * 
	 * @return string which describes comparison strategy for a given
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * 
	 * @return Array of tags to allow Weka user to choose one of the comparison
	 *         strategies as an option.
	 */
	public static Tag[] getTags() {
		MissingDataCompare[] values = values();
		Tag[] tags = new Tag[values.length];
		for (int i = 0; i < tags.length; i++)
			tags[i] = new Tag(values[i].ordinal(), values[i].getDescription());
		return tags;
	}

	/**
	 * 
	 * @param Selection
	 *            specifying which enum element to return. The id of this tag
	 *            must match the desired element's ordinal() value.
	 * @return The selected element of this enum
	 */
	public static MissingDataCompare getElement(SelectedTag tag) {
		int id = tag.getSelectedTag().getID();
		for (MissingDataCompare mdc : MissingDataCompare.values())
			if (mdc.ordinal() == id)
				return mdc;
		throw new IllegalArgumentException(
				"There is no element with the specified value");
	}

	/**
	 * 
	 * @param first
	 *            value
	 * @param second
	 *            value
	 * @return 0 or 1, depending on how the comparison is done.
	 */
	public abstract int outcome(int var1, int var2);
}