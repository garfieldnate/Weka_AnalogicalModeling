package weka.classifiers.lazy.AM.lattice;

import weka.classifiers.lazy.AM.data.ClassifiedSupra;
import weka.classifiers.lazy.AM.data.Concept;
import weka.classifiers.lazy.AM.data.Subcontext;
import weka.classifiers.lazy.AM.data.SubcontextList;
import weka.classifiers.lazy.AM.data.Supracontext;
import weka.classifiers.lazy.AM.label.Label;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Warning: this class is currently experimental, slow, and not correct. Do not
 * try to use in production.
 *
 * Fill a sparse lattice structure which stores unique references to unique
 * Supracontexts. The lattice filling algorithm is based on an improved version
 * of AddIntent, described in
 * "An Improved AddIntent Algorithm for Building Concept Lattice" by Lv
 * Lingling, et. al., 2011.
 *
 * @author Nate Glenn
 */
// TODO: next: further short-circuit processing of heteros, and fix counting
public class SparseLattice implements Lattice {
    private final List<Concept<ClassifiedSupra>> lattice = new ArrayList<>();
    private static final BigInteger two = BigInteger.valueOf(2);
    final List<Concept<ClassifiedSupra>> tagList;

	SparseLattice(SubcontextList subList) {
        tagList = new LinkedList<>();
        Concept<ClassifiedSupra> bottom = new Concept<>(subList.getLabeler().getLatticeTop(), new ClassifiedSupra());
        lattice.add(bottom);
        // int i = 0;
        for (Subcontext sub : subList) {
            // System.err.println(i++);
            Concept<ClassifiedSupra> generatorConcept = getMaximalConcept(sub.getLabel(), bottom);
            // ignore concepts already declared hetero
            if (generatorConcept.getSupra().isHeterogeneous()) continue;
            Concept<ClassifiedSupra> newConcept = addIntent(sub.getLabel(), generatorConcept);
            addExtent(newConcept, sub);
            resetTags();
        }
        System.out.println(dumpLattice("lattice"));
    }

    private void resetTags() {
        Iterator<Concept<ClassifiedSupra>> iter = tagList.iterator();
        while (iter.hasNext()) {
            Concept<ClassifiedSupra> concept = iter.next();
            concept.setTagged(false);
            concept.setCandidateParent(null);
            iter.remove();
        }
        assert (tagList.isEmpty());
    }

    private Concept<ClassifiedSupra> addIntent(Label intent, Concept<ClassifiedSupra> generatorConcept) {
        Label intersection = intent.intersect(generatorConcept.getIntent());
        generatorConcept = getMaximalConcept(intersection, generatorConcept);
        if (generatorConcept.getIntent().equals(intersection)) {
            if (generatorConcept.notTagged()) {
                tagList.add(generatorConcept);
                generatorConcept.setTagged(true);
                generatorConcept.setCandidateParent(generatorConcept);
            }
            // concept with given label is already present
            return generatorConcept;
        }
        Set<Concept<ClassifiedSupra>> newParents = new HashSet<>();
        for (Concept<ClassifiedSupra> candidate : generatorConcept.getParents()) {
            if (candidate.notTagged()) {
                Concept<ClassifiedSupra> tempConcept = candidate;
                if (!candidate.getIntent().isDescendantOf(intersection)) {
                    // this possible parent turned out not to be a parent, so
                    // generate a parent by intersecting it with the new label,
                    // and save the new parent
                    candidate = addIntent(intent, candidate);
                    tempConcept.setCandidateParent(candidate);
                }
                tagList.add(tempConcept);
                tempConcept.setTagged(true);
            } else {
                if (candidate.getCandidateParent() != null) candidate = candidate.getCandidateParent();
            }
            boolean addParent = true;
            Iterator<Concept<ClassifiedSupra>> newParentIterator = newParents.iterator();
            while (newParentIterator.hasNext()) {
                Concept<ClassifiedSupra> parent = newParentIterator.next();
                if (candidate.getIntent().isDescendantOf(parent.getIntent())) {
                    addParent = false;
                    break;
                } else if (parent.getIntent().isDescendantOf(candidate.getIntent())) newParentIterator.remove();
            }
            if (addParent) newParents.add(candidate);
        }

        Concept<ClassifiedSupra> newConcept = new Concept<>(intersection,
                                                            new ClassifiedSupra(generatorConcept.getExtent(),
                                                                                BigInteger.ONE
                                                            )
        );
        newConcept.setTagged(true);
        tagList.add(newConcept);
        newConcept.setCandidateParent(newConcept);

        tagList.add(generatorConcept);
        generatorConcept.setTagged(true);
        generatorConcept.setCandidateParent(newConcept);

        lattice.add(newConcept);

        for (Concept<ClassifiedSupra> parent : newParents) {
            // may not actually contain the given parent if it was returned from
            // a recursive call; remove it if it does, though
            generatorConcept.removeParent(parent);
            newConcept.addParent(parent);
        }
        generatorConcept.addParent(newConcept);
        return newConcept;
    }

    Concept<ClassifiedSupra> getMaximalConcept(Label intent, Concept<ClassifiedSupra> generatorConcept) {
        boolean parentIsMaximal = true;
        while (parentIsMaximal) {
            parentIsMaximal = false;
            for (Concept<ClassifiedSupra> parent : generatorConcept.getParents()) {
                if (intent.isDescendantOf(parent.getIntent())) {
                    generatorConcept = parent;
                    parentIsMaximal = true;
                    break;
                }
            }
        }
        return generatorConcept;
    }

    // add the given subcontext to the extent of concept and all of its
    // ancestors
    private void addExtent(Concept<ClassifiedSupra> concept, Subcontext extent) {
        Set<Concept<ClassifiedSupra>> visited = new HashSet<>();
        Queue<Concept<ClassifiedSupra>> queue = new LinkedList<>();
        queue.add(concept);
        while (queue.size() != 0) {
            Concept<ClassifiedSupra> current = queue.poll();
            if (visited.contains(current)) continue;
            visited.add(current);
            queue.addAll(current.getParents());

            current.addToExtent(extent);
        }
    }

    private String dumpLattice(String graphName) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph ").append(graphName).append(" {\nnode [shape=box]\n");
        Set<Label> visited = new HashSet<>();
        Queue<Concept<ClassifiedSupra>> queue = new LinkedList<>();
        queue.add(lattice.get(0));
        while (queue.size() != 0) {
            Concept<ClassifiedSupra> current = queue.poll();
            if (visited.contains(current.getIntent())) continue;
            visited.add(current.getIntent());
            String color = "";

            ClassifiedSupra supra = new ClassifiedSupra();
            for (Subcontext sub : current.getExtent()) {
                supra.add(sub);
                if (supra.isHeterogeneous()) {
                    color = "color=red, ";
                    break;
                }
            }

            sb.append(current.getIntent())
              .append(" [")
              .append(color)
              .append("label=\"")
              .append(getCount(current))
              .append("x")
              .append(current.getIntent())
              .append(":")
              .append(current.getExtent())
              .append("\"];\n");
            for (Concept<ClassifiedSupra> parent : current.getParents())
                sb.append(current.getIntent()).append(" -> ").append(parent.getIntent()).append(";\n");
            queue.addAll(current.getParents());
        }
        sb.append("}\n");
        return sb.toString();
    }

    @Override
    public Set<Supracontext> getSupracontexts() {
        Set<Supracontext> supras = new HashSet<>();
        // i is 1 if we skip "bottom", which may just be a dummy root with no
        // data
        for (int i = (lattice.get(0).getExtent().size() == 0) ? 1 : 0; i < lattice.size(); i++) {
            ClassifiedSupra supra = lattice.get(i).getSupra();
            if (supra.isHeterogeneous()) continue;
            supra.setCount(getCount(lattice.get(i)));
            supras.add(supra);
        }
        return supras;
    }

    private BigInteger getCount(Concept<ClassifiedSupra> concept) {
        // if a supra has N matches, then those matches can be replaced with
        // mismatches in 2^N ways (including the possibility of replacing none
        // of them). That's the number a supra would have if it had no children.
        // However, the matches which any child also has cannot be counted
        // because the match/mismatch permutation will occur in the child
        // instead, so we must subtract the permutations that happen in the
        // children.

        // simply 2^matches if there are no parents
        BigInteger count = labelCount(concept.getIntent());
        if (concept.getParents().size() == 0) return count;

        // otherwise, we subtract the count headed by the parents
        List<Label> parentLabels = new ArrayList<>();
        for (Concept<ClassifiedSupra> c : concept.getParents())
            parentLabels.add(c.getIntent());
        return count.subtract(countOfUnion(parentLabels));
    }

    /**
     * Use the inclusion-exclusion principle to find the total count for the
     * union of the given concepts. This principle states that to find the
     * cardinality of the union of several sets, you include the cardinalities
     * of the individual sets, exclude the cardinalities of the pair-wise
     * intersections, include that of the triple-wise intersections, exclude
     * quadruple-wise, etc. For example, |A&cup;B&cup;C| = |A| + |B| + |C| -
     * |A&cap;B| - |A&cap;C| - |B&cap;C| + |A&cap;B&cap;C|. See <a href=
     * "https://en.wikipedia.org/wiki/Inclusion%E2%80%93exclusion_principle"
     * >Wikipedia</a> for more discussion.
     *
     * @param parentLabels Compute the count of the union of this set of concepts.
     */
    private BigInteger countOfUnion(List<Label> parentLabels) {
        BigInteger unionCount = BigInteger.ZERO;
        boolean add = true;
        for (int i = 1; i <= parentLabels.size(); i++) {
            if (add) unionCount = unionCount.add(countOfIntersections(parentLabels, i));
            else unionCount = unionCount.subtract(countOfIntersections(parentLabels, i));
            add = !add;
        }
        return unionCount;
    }

    private BigInteger intersectionCount;

    /**
     * Find all size-wise intersections of the parentLabels and return their
     * total count.
     *
     * @param parentLabels
     * @param size
     * @return count of all size-wise intersections of parentLabels
     */
    private BigInteger countOfIntersections(List<Label> parentLabels, int size) {
        intersectionCount = BigInteger.ZERO;
        recursiveCountOfIntersections(parentLabels, new Label[size], 0, 0);
        return intersectionCount;
    }

    // this recursively finds all subsets of size labels.length and adds their
    // intersections to intersectionCount
    private void recursiveCountOfIntersections(List<Label> parentLabels, Label[] labels, int subsetSize, int nextIndex) {
        if (subsetSize == labels.length) {
            Label intersection = labels[0];
            for (int i = 1; i < labels.length; i++)
                intersection = intersection.intersect(labels[i]);
            intersectionCount = intersectionCount.add(labelCount(intersection));
        } else {
            for (int j = nextIndex; j < parentLabels.size(); j++) {
                labels[subsetSize] = parentLabels.get(j);
                recursiveCountOfIntersections(parentLabels, labels, subsetSize + 1, j + 1);
            }
        }
    }

    private BigInteger labelCount(Label label) {
        return two.pow(label.numMatches());
    }

    // private class Concept {
    // Set<Subcontext> extent;
    // Label intent;
    // Set<Concept> parents;
    //
    // public Concept(Label intent) {
    // this.intent = intent;
    // extent = new HashSet<Subcontext>();
    // parents = new HashSet<Concept>();
    // }
    //
    // public Concept(Label intent, Set<Subcontext> extent) {
    // this.intent = intent;
    // this.extent = new HashSet<>(extent);
    // parents = new HashSet<Concept>();
    // }
    //
    // public Set<Subcontext> getExtent() {
    // return Collections.unmodifiableSet(extent);
    // }
    //
    // /**
    // * Add subcontexts to the extent of this concept as well as to all of
    // * its ancestors.
    // *
    // * @param newSubs
    // */
    // public void addToExtent(Set<Subcontext> newSubs) {
    // // TODO: not needed. Is anything needed?
    // // for (Subcontext sub : newSubs)
    // // assert (!extent.contains(sub));
    // extent.addAll(newSubs);
    // for (Concept parent : getParents())
    // parent.addToExtent(newSubs);
    // }
    //
    // public Label getIntent() {
    // return intent;
    // }
    //
    // public Set<Concept> getParents() {
    // return Collections.unmodifiableSet(parents);
    // }
    //
    // public void addParent(Concept newParent) {
    // parents.add(newParent);
    // }
    //
    // public void removeParent(Concept oldParent) {
    // // TODO: this assert fails, but nothing seems to actually be wrong.
    // // assert (parents.contains(oldParent));
    // parents.remove(oldParent);
    // }
    //
    // @Override
    // public String toString() {
    // return intent + "(" + extent + ")->[" + parents + "]";
    // }
    // }
}
