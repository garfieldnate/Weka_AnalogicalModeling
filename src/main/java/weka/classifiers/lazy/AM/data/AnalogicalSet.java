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

import weka.classifiers.lazy.AM.AMUtils;
import weka.classifiers.lazy.AM.lattice.Lattice;
import weka.core.Instance;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class holds a list of the exemplars that influenced the predicted
 * outcome of a certain test item, along with the analogical effect of each.
 *
 * @author Nate Glenn
 */
public class AnalogicalSet {

    /**
     * Mapping of an exemplar to its analogical effect
     */
    private final Map<Instance, BigDecimal> exEffectMap = new HashMap<>();

    /**
     * Mapping of exemplar to the number of pointers to it
     */
    private final Map<Instance, BigInteger> exPointerMap;

    private final Map<String, BigInteger> classPointerMap = new HashMap<>();

    private final Map<String, BigDecimal> classLikelihoodMap = new HashMap<>();

    private final Set<Supracontext> supraList;

    private BigInteger totalPointers = BigInteger.ZERO;

    private Set<String> predictedClasses = null;
    private BigDecimal classProbability = BigDecimal.valueOf(-1);

    /**
     * The exemplar whose class is being predicted by this set
     */
    private final Instance classifiedExemplar;

    private static final String newline = System.getProperty("line.separator");

    // these are used for sorting items to be printed
    private static final Comparator<Map.Entry<Instance, BigInteger>> entryComparator1 = (arg1, arg2) -> {
        // compare all attribute string values and then the number of
        // pointers
        int compare;
        for (int i = 0; i < arg1.getKey().numAttributes(); i++) {
            compare = arg1.getKey().stringValue(i).compareTo(arg2.getKey().stringValue(i));
            if (compare != 0) {
                return compare;
            }
        }
        return arg1.getValue().compareTo(arg2.getValue());
    };

    private static final Comparator<Entry<String, BigInteger>> entryComparator2 = Comparator.comparing(Entry::getValue);

    /**
     * @param lattice  filled lattice, which contains the data for calculating the analogical set
     * @param testItem Exemplar being classified
     * @param linear   True if counting of pointers should be done linearly; false if quadratically.
     */
    public AnalogicalSet(Lattice lattice, Instance testItem, boolean linear) {

        Set<Supracontext> set = lattice.getSupracontexts();

        this.classifiedExemplar = testItem;
        this.supraList = set;

        // find numbers of pointers to individual exemplars
        exPointerMap = getPointers(set, linear);

        // find the total number of pointers
        for (Instance e : exPointerMap.keySet())
            totalPointers = totalPointers.add(exPointerMap.get(e));

        // find the analogical effect of an exemplar by dividing its pointer
        // count by the total pointer count
        for (Instance e : exPointerMap.keySet())
            exEffectMap.put(
                e,
                new BigDecimal(exPointerMap.get(e)).divide(
                    new BigDecimal(getTotalPointers()),
                    AMUtils.matchContext
                )
            );

        // find the likelihood for a given outcome based on the pointers
        for (Instance e : exPointerMap.keySet()) {
            String className = e.stringValue(e.classAttribute());
            if (classPointerMap.containsKey(className))
                classPointerMap.put(className, classPointerMap.get(className).add(exPointerMap.get(e)));
            else classPointerMap.put(className, exPointerMap.get(e));
        }

        // set the likelihood of each possible class index to be its share of
        // the total pointers
        for (String className : classPointerMap.keySet())
            classLikelihoodMap.put(className,
                                   new BigDecimal(classPointerMap.get(className)).divide(new BigDecimal(totalPointers),
                                                                                         AMUtils.matchContext
                                   )
            );
        // Find the classes with the highest likelihood (there may be a tie)
        BigDecimal temp;
        for (String className : classLikelihoodMap.keySet()) {
            temp = classLikelihoodMap.get(className);
            int comp = temp.compareTo(getClassProbability());
            if (comp > 0) {
                classProbability = temp;
                predictedClasses = new HashSet<>();
                predictedClasses.add(className);
            } else if (comp == 0) {
                predictedClasses.add(className);
            }
        }
    }

    /**
     * See page 392 of the red book.
     *
     * @param set    List of Supracontexts created by filling the supracontextual lattice.
     * @param linear True if pointer counting should be done linearly; false if it should be done quadratically
     * @return A mapping of each exemplar to the number of pointers pointing to it.
     */
    private Map<Instance, BigInteger> getPointers(Set<Supracontext> set, boolean linear) {
        Map<Instance, BigInteger> pointers = new HashMap<>();

        // number of pointers in a supracontext,
        // that is the number of exemplars in the whole thing
        BigInteger pointersInList = BigInteger.ZERO;
        // iterate all supracontext
        for (Supracontext supra : set) {
            if (!linear) {
                pointersInList = BigInteger.ZERO;
                // sum number of exemplars for each subcontext
                for (Subcontext sub : supra.getData())
                    pointersInList = pointersInList.add(BigInteger.valueOf(sub.getExemplars().size()));
            }
            // iterate subcontexts in supracontext
            for (Subcontext sub : supra.getData()) {
                // number of supras containing this subcontext
                BigInteger pointersToSupra = supra.getCount();
                // iterate exemplars in subcontext
                for (Instance e : sub.getExemplars()) {
                    // pointers to exemplar = pointersToSupra * pointers in list
                    // add together if already in the map
                    if (pointers.get(e) != null) pointers.put(e,
                                                              pointers.get(e)
                                                                      .add((linear ? BigInteger.ONE : pointersInList).multiply(
                                                                          pointersToSupra))
                    );
                    else pointers.put(e, (linear ? BigInteger.ONE : pointersInList).multiply(pointersToSupra));
                }
            }
        }
        return pointers;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("classifying: ");
        sb.append(getClassifiedEx());
        sb.append(newline);

        sb.append("outcome: ");
        sb.append(predictedClasses);
        sb.append(" (");
        sb.append(classProbability);
        sb.append(")");
        sb.append(newline);

        Set<Entry<Instance, BigInteger>> sortedEntries1 = new TreeSet<>(entryComparator1);
        sortedEntries1.addAll(getExemplarPointers().entrySet());
        sb.append("Exemplar effects:");
        sb.append(AMUtils.LINE_SEPARATOR);
        for (Entry<Instance, BigInteger> e : sortedEntries1) {
            sb.append(e.getKey())
              .append(" : ")
              .append(e.getValue())
              .append(" (")
              .append(new BigDecimal(e.getValue()).divide(new BigDecimal(totalPointers), AMUtils.matchContext))
              .append(")")
              .append(AMUtils.LINE_SEPARATOR);
        }

        Set<Entry<String, BigInteger>> sortedEntries2 = new TreeSet<>(entryComparator2);
        sortedEntries2.addAll(getClassPointers().entrySet());
        sb.append("Outcome likelihoods:").append(newline);
        for (Entry<String, BigInteger> e : sortedEntries2)
            sb.append(e.getKey())
              .append(" : ")
              .append(e.getValue())
              .append(" (")
              .append(new BigDecimal(e.getValue()).divide(new BigDecimal(totalPointers), AMUtils.matchContext))
              .append(")")
              .append(AMUtils.LINE_SEPARATOR);

        return sb.toString();
    }

    /**
     * @return A mapping between exemplars and their analogical effect (decimal percentage)
     */
    public Map<Instance, BigDecimal> getExemplarEffectMap() {
        return exEffectMap;
    }

    /**
     * @return Mapping of exemplars in the analogical set to the number of pointers to it
     */
    public Map<Instance, BigInteger> getExemplarPointers() {
        return exPointerMap;
    }

    /**
     * @return A mapping between a possible class index and its likelihood (decimal probability)
     */
    public Map<String, BigDecimal> getClassLikelihoodMap() {
        return classLikelihoodMap;
    }

    /**
     * @return The total number of pointers in this analogical set
     */
    public BigInteger getTotalPointers() {
        return totalPointers;
    }

    /**
     * @return A mapping between a class value index the number of pointers pointing to it
     */
    public Map<String, BigInteger> getClassPointers() {
        return classPointerMap;
    }

    /**
     * @return A mapping between the class value index and its selection probability
     */
    public Map<String, BigDecimal> getClassLikelihood() {
        return classLikelihoodMap;
    }

    /**
     * @return The exemplar which was classified
     */
    public Instance getClassifiedEx() {
        return classifiedExemplar;
    }

    /**
     * @return Probability of the predicted class
     */
    public BigDecimal getClassProbability() {
        return classProbability;
    }

    /**
     * @return Index of the predicted class attribute value
     */
    // TODO: this could actually be a tie, so it should return multiple
    public Set<String> getPredictedClasses() {
        return predictedClasses;
    }

    /**
     * @return The Supracontexts that comprise the analogical set.
     */
    public Set<Supracontext> getSupraList() {
        return Collections.unmodifiableSet(supraList);
    }
}
