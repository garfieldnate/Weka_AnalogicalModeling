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
package weka.classifiers.lazy.AM.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import weka.classifiers.lazy.AM.label.Label;
import weka.classifiers.lazy.AM.label.Labeler;
import weka.core.Instance;

/**
 * This class creates and manages a list of {@link Subcontext subcontexts} from
 * a set of previously classified exemplars and an exemplar to be classified.
 * 
 * Create a list of subcontexts by calling
 * {@link #SubcontextList(Exemplar, List, int)}. Iterate through the created
 * subcontexts using the {@link Iterator} returned by {@link #iterator()}.
 * 
 * @author Nate Glenn
 * 
 */
// TODO: why use an iterator, instead of just returning a list?
public class SubcontextList implements Iterable<Subcontext> {

	private final HashMap<Label, Subcontext> labelToSubcontext = new HashMap<>();

	private final Labeler labeler;

	/**
	 * 
	 * @return the number of attributes used to predict an outcome
	 */
	public int getCardinality() {
		return labeler.getCardinality();
	}

	/**
	 * If you use this constructor, you will have to call the {@link #add}
	 * method repeatedly in order to fill the contexts.
	 * 
	 * @param testEx
	 *            Exemplar which is being classified and assigns contexts
	 * @param the
	 *            number of attributes being used to classify the instance
	 */
	SubcontextList(Labeler labeler) {
		this.labeler = labeler;
	}

	/**
	 * This is the easiest to use constructor. It creates and stores a list of
	 * subcontexts given classified exemplars and an exemplar to be classified.
	 * 
	 * @param testEx
	 *            Exemplar which is being classified
	 * @param data
	 *            Exemplars used to classify testEx
	 * @param cardinality
	 *            the number of attributes used to predict an Instance's class
	 */
	public SubcontextList(Labeler labeler, List<Instance> data) {
		this.labeler = labeler;
		for (Instance se : data)
			add(se);
	}

	/**
	 * Adds the exemplar to the correct subcontext.
	 * 
	 * @param data
	 */
	void add(Instance data) {
		Label label = labeler.label(data);
		if (!labelToSubcontext.containsKey(label))
			labelToSubcontext.put(label, new Subcontext(label));
		labelToSubcontext.get(label).add(data);
	}

	/**
	 * Adds the exemplars to the correct subcontexts.
	 * 
	 * @param data
	 *            Exemplars to add
	 */
	void addAll(Iterable<Instance> data) {
		for (Instance d : data)
			add(d);
	}

	/**
	 * This method is not particularly speedy, since it sorts the contained
	 * subcontexts by label. It is meant for test purposes only; do not rely on
	 * exact output being the same in the future.
	 */
	@Override
	public String toString() {
		List<Label> sortedLabels = new ArrayList<>(labelToSubcontext.keySet());
		// sort the labels by hashcode so that output is consistent for testing
		// purposes
		Collections.sort(sortedLabels, new Comparator<Label>() {
			@Override
			public int compare(Label firstLabel, Label secondLabel) {
				return Integer.compare(firstLabel.hashCode(),
						secondLabel.hashCode());
			}
		});

		StringBuilder s = new StringBuilder();
		for (Label label : sortedLabels) {
			s.append(labelToSubcontext.get(label));
			s.append(',');
		}
		// remove last comma
		s.deleteCharAt(s.length() - 1);
		return s.toString();
	}

	/**
	 * Returns equals if both lists contain the same data in the same
	 * subcontexts. Does not compare the Labeler object.
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof SubcontextList))
			return false;
		SubcontextList otherList = (SubcontextList) other;
		return labelToSubcontext.equals(otherList.labelToSubcontext);
	}

	/**
	 * 
	 * @return An iterator which returns each of the contained subcontexts.
	 */
	@Override
	public Iterator<Subcontext> iterator() {

		return new Iterator<Subcontext>() {

			Iterator<Label> keyIterator = labelToSubcontext.keySet().iterator();

			@Override
			public boolean hasNext() {
				return keyIterator.hasNext();
			}

			@Override
			public Subcontext next() {
				return labelToSubcontext.get(keyIterator.next());
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * @return The labeler object used to assign incoming data to subcontexts.
	 */
	public Labeler getLabeler() {
		return labeler;
	}
}
