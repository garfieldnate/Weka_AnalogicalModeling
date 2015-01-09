package weka.classifiers.lazy.AM.data;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import weka.classifiers.lazy.AM.label.Label;
import weka.classifiers.lazy.AM.lattice.SparseLattice;

/**
 * This class is a decorator which wraps a {@link Supracontext} and adds the
 * functionality of a node (concept) used in the ImprovedAddIntent algorithm
 * (see {@link SparseLattice}.
 * 
 * @author Nathan Glenn
 * 
 * @param <T>
 *            The implementation of Supracontext to be stored in this node.
 */
// TODO: did this really need to be genericized? Won't you always use a
// classifiedSupra?
public class Concept<T extends Supracontext> implements Supracontext {
	// the wrapped supracontext
	private final T extent;
	private final Label intent;
	private Set<Concept<T>> parents;
	private boolean tag;
	private Concept<T> candidateParent;

	public Concept(Label intent, T extent) {
		this.intent = intent;
		this.extent = extent;
		parents = new HashSet<>();
		tag = false;
		candidateParent = null;
	}

	public Set<Subcontext> getExtent() {
		return extent.getData();
	}

	public T getSupra() {
		return extent;
	}

	/**
	 * Add the given subcontext to the extent of this concept.
	 * 
	 * @param newSub
	 */
	public void addToExtent(Subcontext newSub) {
		// TODO: not needed. Is anything needed?
		// for (Subcontext sub : newSubs)
		// assert (!extent.contains(sub));
		extent.add(newSub);
	}

	public Label getIntent() {
		return intent;
	}

	public Set<Concept<T>> getParents() {
		return Collections.unmodifiableSet(parents);
	}

	public void addParent(Concept<T> newParent) {
		parents.add(newParent);
	}

	public void removeParent(Concept<T> oldParent) {
		// this won't always be true; recursively grabbed ancestors may not be
		// the generating concept's parents.
		// assert (parents.contains(oldParent));
		parents.remove(oldParent);
	}

	@Override
	public String toString() {
		return intent + "(" + extent + ")->[" + parents + "]";
	}

	@Override
	public Supracontext copy() {
		// copy() contract states it must return same type as calling object, so
		// unchecked cast is okay
		@SuppressWarnings("unchecked")
		T newSupra = (T) extent.copy();
		Concept<T> newNode = new Concept<T>(intent, newSupra);
		newNode.parents = new HashSet<>(parents);
		return newNode;
	}

	/**
	 * This equals method differs from the specification in
	 * {@link Supracontext#equals(Object)}; it compares the concept intent
	 * (label), instead of the extent (contained subcontexts). {@inheritDoc}
	 */
	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!(other instanceof Concept))
			return false;
		// extent equals method will take care of any difference in
		// parameterized type
		@SuppressWarnings("rawtypes")
		Concept otherConcept = (Concept) other;
		return intent.equals(otherConcept.intent);
	}

	/**
	 * This implementation differs from the specification in
	 * {@link Supracontext#hashCode()}; it returns the hash of the intent
	 * (label), instead of the extent (contained subcontexts). {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return intent.hashCode();
	}

	// the following methods forward to the contained Supracontext

	@Override
	public Set<Subcontext> getData() {
		return extent.getData();
	}

	@Override
	public void add(Subcontext sub) {
		extent.add(sub);
	}

	@Override
	public boolean isEmpty() {
		return extent.isEmpty();
	}

	@Override
	public BigInteger getCount() {
		return extent.getCount();
	}

	@Override
	public void setCount(BigInteger count) {
		extent.setCount(count);
	}

	public boolean isTagged() {
		return tag;
	}

	public void setTagged(boolean tag) {
		this.tag = tag;
	}

	public Concept<T> getCandidateParent() {
		return candidateParent;
	}

	public void setCandidateParent(Concept<T> candidateParent) {
		this.candidateParent = candidateParent;
	}
}
