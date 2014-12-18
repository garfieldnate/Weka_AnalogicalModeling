package weka.classifiers.lazy.AM.data;

import java.util.Set;

import weka.classifiers.lazy.AM.label.Label;

/**
 * A supracontext contains a set of {@link Subcontext Subcontexts} which have
 * certain commonalities in their {@link Label Labels}.
 * 
 * @author Nathan Glenn
 * 
 */
public abstract class Supracontext {
	/**
	 * @return an unmodifiable view of the set of subcontexts contained in this
	 *         supracontext.
	 */
	public abstract Set<Subcontext> getData();

	/**
	 * @return true if this supracontext contains no subcontexts; false
	 *         otherwise.
	 */
	public boolean isEmpty() {
		return getData().isEmpty();
	}

	/**
	 * {@inheritDoc} Two Supracontexts are equal if they contain the same
	 * subcontexts.
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Supracontext))
			return false;
		Supracontext otherSupra = (Supracontext) other;
		return getData().equals(otherSupra.getData());
	}

	@Override
	public int hashCode() {
		return getData().hashCode();
	}
}
