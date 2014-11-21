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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import weka.core.Instance;

/**
 * This class creates and manages a list of {@link Subcontext subcontexts}
 * from a set of previously classified exemplars and an exemplar to be classified.
 * 
 * Create a list of subcontexts by calling {@link #SubcontextList(Exemplar, List, int)}.
 * Iterate through the created subcontexts using the {@link Iterator} returned by {@link #iterator()}.
 * 
 * @author Nate Glenn
 * 
 */
public class SubcontextList implements Iterable<Subcontext> {

	private HashMap<Integer, Subcontext> labelToSubcontext = new HashMap<Integer, Subcontext>();

	private Labeler labeler;
	
	/**
	 * 
	 * @return the number of attributes used to predict an outcome
	 */
	public int getCardinality() {
		return labeler.getCardinality();
	}

	/**
	 * If you use this constructor, you will have to call the {@link #add} method
	 * repeatedly in order to fill the contexts.
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
		int label = labeler.getContextLabel(data);
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

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		Iterator<Subcontext> iter = iterator();
		while (iter.hasNext()) {
			s.append(iter.next());
			s.append(',');
		}
		return s.toString();
	}

	/**
	 * 
	 * @return An iterator which returns each of the contained subcontexts.
	 */
	@Override
	public Iterator<Subcontext> iterator() {

		return new Iterator<Subcontext>() {

			Iterator<Integer> keyIterator = labelToSubcontext.keySet()
					.iterator();

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
}
