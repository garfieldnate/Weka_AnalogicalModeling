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
	List<Concept> lattice = new ArrayList<>();

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

	// TODO: adding mid-lattice needs to trickle the new piece of extent down
	// the reset of the lattice.

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
			for (Concept parent : newParents) {
				if (parent.getIntent().isDescendantOf(candidate.getIntent())) {
					addParent = false;
					break;
				} else if (candidate.getIntent().isDescendantOf(
						parent.getIntent()))
					newParents.remove(parent);// assert(newParents.contains(parent))
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

			sb.append(current.hashCode() + " [label=\"" + getCount(current)
					+ "x" + current.getIntent() + ":" + current.getExtent()
					+ "\"];\n");
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
		// children. We also union the children together to see if they have
		// overlap, and adjust the count accordingly.
		BigInteger two = BigInteger.valueOf(2);
		BigInteger possiblePermutations = two.pow(concept.getIntent()
				.numMatches());
		// simply 2^matches if there are no parents
		if (concept.getParents().size() == 0)
			return possiblePermutations;

		Iterator<Concept> parentIterator = concept.getParents().iterator();
		Label current = parentIterator.next().getIntent();
		BigInteger parentPermutations = two.pow(current.numMatches());
		// for one parent, subtract 2^(parent matches) from 2^matches
		if (!parentIterator.hasNext())
			return possiblePermutations.subtract(parentPermutations);

		// for several parents, we need to keep track of their overlapping with
		// an intersection
		Label intersection = current;
		while (parentIterator.hasNext()) {
			current = parentIterator.next().getIntent();
			parentPermutations = parentPermutations.add(two.pow(current
					.numMatches()));
			intersection = intersection.intersect(current);
		}
		// the parents each blocked out parentOverlap of the same intents
		BigInteger parentOverlap = two.pow(intersection.numMatches());
		// to only count the overlaps once, we need to subtract
		// parentOverlap*(numParents - 1) from parentPermutations
		BigInteger extraCounts = parentOverlap.multiply(BigInteger
				.valueOf(concept.getParents().size() - 1));
		parentPermutations = parentPermutations.subtract(extraCounts);

		// finally, return the possible permutations minus the permutations that
		// happen in the parents.
		return possiblePermutations.subtract(parentPermutations);
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

		public void addToExtent(Set<Subcontext> newSubs) {
			// TODO: not needed. Is anything needed?
			// for (Subcontext sub : newSubs)
			// assert (!extent.contains(sub));
			extent.addAll(newSubs);
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
