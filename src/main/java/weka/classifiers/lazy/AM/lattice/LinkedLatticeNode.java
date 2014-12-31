package weka.classifiers.lazy.AM.lattice;

import java.math.BigInteger;
import java.util.Set;

import weka.classifiers.lazy.AM.data.Subcontext;
import weka.classifiers.lazy.AM.data.Supracontext;

/**
 * This class is a decorator which wraps a {@link Supracontext} and adds the
 * functionality of a linked node.
 * 
 * @author Nathan Glenn
 * 
 * @param <T>
 *            This should be another type that implements LatticeNode so that a
 *            linked list can be created.
 */
public class LinkedLatticeNode<T extends Supracontext> implements Supracontext {
	T supra;
	// number representing when this supracontext was created
	private int index;
	// pointer which makes a circular linked list out of the lists of
	// subcontext. Using a circular linked list allows optimizations that we
	// will see later.
	private LinkedLatticeNode<T> next;

	public LinkedLatticeNode(T supra) {
		this.supra = supra;
		index = -1;
	}

	/**
	 * Creates a new supracontext from an old one and another exemplar,
	 * inserting the new after the old. Assumes that the addition of the new
	 * subcontext does not make the supracontext heterogeneous.
	 * 
	 * @param other
	 *            Supracontext to place this one after
	 * @param sub
	 *            Subcontext to insert in the new Supracontext
	 * @param ind
	 *            index of new Supracontext
	 */
	// it's okay to cast to T here because of the constraint that
	// Supracontext.copy() return its own type
	@SuppressWarnings("unchecked")
	public LinkedLatticeNode<T> insertAfter(Subcontext sub, int ind) {
		T newSupra = (T) getSupracontext().copy();
		newSupra.setCount(BigInteger.ONE);
		newSupra.add(sub);
		LinkedLatticeNode<T> newNode = new LinkedLatticeNode<T>(newSupra);
		newNode.index = ind;
		newNode.setNext(getNext());
		setNext(newNode);
		return newNode;
	}

	/**
	 * @return the next object linked to by this node
	 */
	public LinkedLatticeNode<T> getNext() {
		return next;
	}

	/**
	 * Set the next object linked to by this node
	 * 
	 * @param next
	 *            the object to link to
	 */
	public void setNext(LinkedLatticeNode<T> next) {
		this.next = next;
	}

	/**
	 * @return The index of this node. The index of a node should be -1 by
	 *         default unless set in a constructor, and it should never change
	 *         after object construction.
	 */
	// should be -1 if not set in a constructor or something
	public int getIndex() {
		return index;
	}

	/**
	 * Increases count by one.
	 */
	public void incrementCount() {
		supra.setCount(supra.getCount().add(BigInteger.ONE));
	}

	/**
	 * Decreases the count by one; if this reaches 0, then this Supracontext
	 * should be destroyed (by the caller).
	 * 
	 * @throws IllegalStateException
	 *             if the count is already zero.
	 */
	public void decrementCount() {
		if (supra.getCount().equals(BigInteger.ZERO))
			throw new IllegalStateException("Count cannot be less than zero");
		supra.setCount(supra.getCount().subtract(BigInteger.ONE));
	}

	public T getSupracontext() {
		return supra;
	}

	/**
	 * This method is unimplemented and will throw an
	 * {@link UnsupportedOperationException} if called. {@inheritDoc}
	 */
	@Override
	public Supracontext copy() {
		throw new UnsupportedOperationException();
	}

	// Below methods are delegated to the contained supracontext

	@Override
	public void add(Subcontext sub) {
		supra.add(sub);
	}

	@Override
	public Set<Subcontext> getData() {
		return supra.getData();
	}

	@Override
	public boolean isEmpty() {
		return supra.isEmpty();
	}

	@Override
	public BigInteger getCount() {
		return supra.getCount();
	}

	@Override
	public void setCount(BigInteger count) {
		supra.setCount(count);
	}

	@Override
	public boolean equals(Object other) {
		return supra.equals(other);
	}

	@Override
	public int hashCode() {
		return supra.hashCode();
	}

	@Override
	public String toString() {
		return supra.toString();
	}
}
