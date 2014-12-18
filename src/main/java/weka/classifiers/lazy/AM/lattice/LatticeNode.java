package weka.classifiers.lazy.AM.lattice;

import java.math.BigInteger;

/**
 * This node is used to create circular linked lists while filling a lattice. A
 * count is provided to keep track of the number of occurrences of nodes with
 * equivalent contents (it should start out as {@link BigInteger#ONE} and never
 * be less than 0). This should be used instead of saving multiple nodes to save
 * space and time when filling a lattice.
 * 
 * @author Nathan Glenn
 * 
 * @param <T>
 *            This should be another type that implements LatticeNode so that a
 *            linked list can be created.
 */
public interface LatticeNode<T extends LatticeNode<T>> {
	/**
	 * @return the next object linked to by this node
	 */
	public T getNext();

	/**
	 * Set the next object linked to by this node
	 * 
	 * @param next
	 *            the object to link to
	 */
	public void setNext(T next);

	/**
	 * @return The index of this node. The index of a node should be -1 by
	 *         default unless set in a constructor, and it should never change
	 *         after object construction.
	 */
	// should be -1 if not set in a constructor or something
	public int getIndex();

	/**
	 * @return the count of this node
	 */
	public BigInteger getCount();

	/**
	 * Increment the count of this node by one.
	 */
	public void incrementCount();

	/**
	 * Decrement the count of this node by one.
	 * 
	 * @throws IllegalStateException
	 *             if the resulting count is less than 0.
	 */
	public void decrementCount();

	/**
	 * @param c
	 *            the count of this node.
	 * @throws IllegalArgumentException
	 *             if c is null or less than 0
	 */
	public void setCount(BigInteger c);

}
