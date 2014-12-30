package weka.classifiers.lazy.AM.lattice;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import weka.classifiers.lazy.AM.data.ClassifiedSupra;
import weka.classifiers.lazy.AM.data.Subcontext;
import weka.classifiers.lazy.AM.data.SubcontextList;
import weka.classifiers.lazy.AM.data.Supracontext;
import weka.classifiers.lazy.AM.label.Label;

public class SparseLattice implements Lattice {
	private final List<Concept> lattice = new ArrayList<>();
	private static BigInteger two = BigInteger.valueOf(2);

	public SparseLattice(SubcontextList subList) {
		Concept bottom = new Concept(subList.getLabeler().getAllMatchLabel());
		lattice.add(bottom);
		for (Subcontext sub : subList) {
			Set<Subcontext> extent = new HashSet<>();
			extent.add(sub);
			addIntent(extent, sub.getLabel(), bottom);
		}
		System.out.println(dumpLattice("lattice"));
	}

	Concept addIntent(Set<Subcontext> extent, Label intent,
			Concept generatorConcept) {
		generatorConcept = getMaximalConcept(intent, generatorConcept);
		if (generatorConcept.getIntent().equals(intent)) {
			// concept with given label is already present, so just add the subs
			generatorConcept.addToExtent(extent);
			// generatorConcept.combineExtent(generatorConcept);
			// markIfHetero(generatorConcept);
			return generatorConcept;
		}
		Set<Concept> newParents = new HashSet<>();
		for (Concept candidate : generatorConcept.getParents()) {
			if (!candidate.getIntent().isDescendantOf(intent)) {
				// this possible parent turned out not to be a parent, so
				// generate a parent by intersecting it with the new label, and
				// save the new parent
				Set<Subcontext> newExtent = new HashSet<>(extent);
				newExtent.addAll(candidate.getExtent());
				candidate = addIntent(newExtent,
						intent.intersect(candidate.getIntent()), candidate);
			}
			boolean addParent = true;
			Iterator<Concept> newParentIterator = newParents.iterator();
			while (newParentIterator.hasNext()) {
				Concept parent = newParentIterator.next();
				if (parent.getIntent().isDescendantOf(candidate.getIntent())) {
					addParent = false;
					break;
				} else if (candidate.getIntent().isDescendantOf(
						parent.getIntent()))
					newParentIterator.remove();// assert(newParents.contains(parent))
												// may be instructive here
			}
			if (addParent)
				newParents.add(candidate);
		}
		Set<Subcontext> newExtent = new HashSet<>(extent);
		newExtent.addAll(generatorConcept.getExtent());
		Concept newConcept = new Concept(intent, newExtent);
		lattice.add(newConcept);
		for (Concept parent : newParents) {
			generatorConcept.removeParent(parent);
			newConcept.addParent(parent);
			parent.addToExtent(extent);
		}
		generatorConcept.addParent(newConcept);
		return newConcept;
	}

	Concept getMaximalConcept(Label intent, Concept generatorConcept) {
		boolean parentIsMaximal = true;
		while (parentIsMaximal) {
			parentIsMaximal = false;
			for (Concept parent : generatorConcept.getParents()) {
				if (intent.isDescendantOf(parent.getIntent())) {
					generatorConcept = parent;
					parentIsMaximal = true;
					break;
				}
			}
		}
		return generatorConcept;
	}

	private String dumpLattice(String graphName) {
		StringBuilder sb = new StringBuilder();
		sb.append("digraph " + graphName + " {\n");
		Set<Concept> visited = new HashSet<>();
		Queue<Concept> queue = new LinkedList<>();
		queue.add(lattice.get(0));
		while (queue.size() != 0) {
			Concept current = queue.poll();
			if (visited.contains(current))
				continue;
			visited.add(current);
			String color = "\"black\"";

			ClassifiedSupra supra = new ClassifiedSupra();
			for (Subcontext sub : current.getExtent()) {
				supra.add(sub);
				if (supra.isHeterogeneous()) {
					color = "\"red\"";
					break;
				}
			}

			sb.append(current.hashCode() + " [color=" + color + ", label=\""
					+ getCount(current) + "x" + current.getIntent() + ":"
					+ current.getExtent() + "\"];\n");
			for (Concept parent : current.getParents())
				sb.append(current.hashCode() + " -> " + parent.hashCode()
						+ ";\n");
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
		concepts: for (int i = (lattice.get(0).getExtent().size() == 0) ? 1 : 0; i < lattice
				.size(); i++) {
			Concept concept = lattice.get(i);
			ClassifiedSupra supra = new ClassifiedSupra();
			for (Subcontext sub : concept.getExtent()) {
				supra.add(sub);
				if (supra.isHeterogeneous())
					continue concepts;
			}
			supra.setCount(getCount(concept));
			supras.add(supra);
		}
		// TODO how should we get the supracontexts?
		return supras;
	}

	private BigInteger getCount(Concept concept) {
		// if a supra has N matches, then those matches can be replaced with
		// mismatches in 2^N ways (including the possibility of replacing none
		// of them). That's the number a supra would have if it had no children.
		// However, the matches which any child also has cannot be counted
		// because the match/mismatch permutation will occur in the child
		// instead, so we must subtract the permutations that happen in the
		// children.

		// simply 2^matches if there are no parents
		BigInteger count = labelCount(concept.getIntent());
		if (concept.getParents().size() == 0)
			return count;

		// otherwise, we subtract the count headed by the parents
		List<Label> parentLabels = new ArrayList<>();
		for (Concept c : concept.getParents())
			parentLabels.add(c.getIntent());
		return count.subtract(countOfUnion(parentLabels));
	}

	/**
	 * Use the inclusion-exclusion principle to find the total count for the
	 * union of the given concepts. This principle states that to find the
	 * cardinality of the union of several sets, you include the cardinalities
	 * of the individual sets, exclude the cardinalities of the pair-wise
	 * intersections, include that of the triple-wise intersections, exclude
	 * quardruple-wise, etc. For example, |A&cup;B&cup;C| = |A| + |B| + |C| -
	 * |A&cap;B| - |A&cap;C| - |B&cap;C| + |A&cap;B&cap;C|. See <a href=
	 * "https://en.wikipedia.org/wiki/Inclusion%E2%80%93exclusion_principle"
	 * >Wikipedia</a> for more discussion.
	 * 
	 * @param parentLabels
	 *            Compute the count of the union of this set of concepts.
	 */
	private BigInteger countOfUnion(List<Label> parentLabels) {
		BigInteger unionCount = BigInteger.ZERO;
		boolean add = true;
		for (int i = 1; i <= parentLabels.size(); i++) {
			if (add)
				unionCount = unionCount.add(countOfIntersections(parentLabels,
						i));
			else
				unionCount = unionCount.subtract(countOfIntersections(
						parentLabels, i));
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
		recursiveCountOfIntesections(parentLabels, new Label[size], 0, 0);
		return intersectionCount;
	}

	// this recursively finds all subsets of size labels.length and adds their
	// intersections to intersectionCount
	private void recursiveCountOfIntesections(List<Label> parentLabels,
			Label[] labels, int subsetSize, int nextIndex) {
		if (subsetSize == labels.length) {
			Label intersection = labels[0];
			for (int i = 1; i < labels.length; i++)
				intersection = intersection.intersect(labels[i]);
			intersectionCount = intersectionCount.add(labelCount(intersection));
		} else {
			for (int j = nextIndex; j < parentLabels.size(); j++) {
				labels[subsetSize] = parentLabels.get(j);
				recursiveCountOfIntesections(parentLabels, labels,
						subsetSize + 1, j + 1);
			}
		}
	}

	private BigInteger labelCount(Label label) {
		return two.pow(label.numMatches());
	}

	private class Concept {
		Set<Subcontext> extent;
		Label intent;
		Set<Concept> parents;

		// TODO: track heterogeneity
		// boolean isHetero;
		// double outcome;

		public Concept(Label intent) {
			this.intent = intent;
			extent = new HashSet<Subcontext>();
			parents = new HashSet<Concept>();
		}

		public Concept(Label intent, Set<Subcontext> extent) {
			this.intent = intent;
			this.extent = new HashSet<>(extent);
			parents = new HashSet<Concept>();
		}

		public Set<Subcontext> getExtent() {
			return Collections.unmodifiableSet(extent);
		}

		/**
		 * Add subcontexts to the extent of this concept as well as to all of
		 * its ancestors.
		 * 
		 * @param newSubs
		 */
		public void addToExtent(Set<Subcontext> newSubs) {
			// TODO: not needed. Is anything needed?
			// for (Subcontext sub : newSubs)
			// assert (!extent.contains(sub));
			extent.addAll(newSubs);
			for (Concept parent : getParents())
				parent.addToExtent(newSubs);
			// markIfHetero
		}

		// public int getOutcome(){}

		public Label getIntent() {
			return intent;
		}

		public Set<Concept> getParents() {
			return Collections.unmodifiableSet(parents);
		}

		public void addParent(Concept newParent) {
			parents.add(newParent);
		}

		public void removeParent(Concept oldParent) {
			// TODO: this assert fails, but nothing seems to actually be wrong.
			// assert (parents.contains(oldParent));
			parents.remove(oldParent);
		}

		// public boolean isHeterogeneous() {
		// return isHetero;
		// }

		@Override
		public String toString() {
			return intent + "(" + extent + ")->[" + parents + "]";
		}
	}
}
