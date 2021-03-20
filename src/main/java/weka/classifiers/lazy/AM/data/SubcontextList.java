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

import weka.classifiers.lazy.AM.label.Label;
import weka.classifiers.lazy.AM.label.Labeler;
import weka.core.Instance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This class creates and manages a list of {@link Subcontext subcontexts} from
 * a set of previously classified exemplars and an exemplar to be classified.
 *
 * After creating a list of subcontexts, iterate through the subcontexts using
 * the {@link Iterator} returned by {@link #iterator()}.
 *
 * @author Nate Glenn
 */
// TODO: why use an iterator, instead of just returning a list?
public class SubcontextList implements Iterable<Subcontext> {

    private final HashMap<Label, Subcontext> labelToSubcontext = new HashMap<>();

    private final Labeler labeler;

    /**
     * @return the number of attributes used to predict an outcome
     */
    public int getCardinality() {
        return labeler.getCardinality();
    }

    /**
     * This is the easiest to use constructor. It creates and stores a list of
     * subcontexts given classified exemplars and an exemplar to be classified.
     *
     * @param labeler Labeler for assigning labels to items in data
     * @param data    Training data (exemplars)
     */
    public SubcontextList(Labeler labeler, List<Instance> data) {
        this.labeler = labeler;
        for (Instance se : data)
            add(se);
    }

    /**
     * Adds {@code exemplar} to the correct subcontext.
     */
    void add(Instance exemplar) {
        Label label = labeler.label(exemplar);
        if (!labelToSubcontext.containsKey(label)) labelToSubcontext.put(label, new Subcontext(label));
        labelToSubcontext.get(label).add(exemplar);
    }

    /**
     * Adds the exemplars to the correct subcontexts.
     *
     * @param data Exemplars to add
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
        sortedLabels.sort(Comparator.comparingInt(Object::hashCode));

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
        if (this == other) return true;
        if (other == null) return false;
        if (!(other instanceof SubcontextList)) return false;
        SubcontextList otherList = (SubcontextList) other;
        return labelToSubcontext.equals(otherList.labelToSubcontext);
    }

    /**
     * @return An iterator which returns each of the contained subcontexts.
     */
    @Override
    public Iterator<Subcontext> iterator() {

        return new Iterator<>() {

			final Iterator<Label> keyIterator = labelToSubcontext.keySet().iterator();

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

    public int size() {
        return labelToSubcontext.size();
    }

    /**
     * @return The labeler object used to assign incoming data to subcontexts.
     */
    public Labeler getLabeler() {
        return labeler;
    }
}
