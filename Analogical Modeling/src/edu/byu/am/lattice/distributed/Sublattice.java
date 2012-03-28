package edu.byu.am.lattice.distributed;


/**
 * Represents a lattice which is to be combined with other sublattices to determine predictions
 * later on. When a sublattice is filled, there are two main differences:
 * <list>
 * 	<li>Only a part of a an exemplar's features are used to assign lattice locations; this is taken
 * 	care of by {@link Subsubcontextlist}.</li>
 * 	<li>No supracontext is ever determined to be heterogeneous. This is, of course, less efficient
 * 	in some ways.</li>
 * </list>
 * Inefficiencies brought about by not eliminating heterogeneous supracontexts and by having to
 * combine sublattices are a compromise to the alternative, using a single lattice for any size
 * exemplars. Remember that the underlying structure of a lattice is an array of size 2^n, n being
 * the size of the exemplars contained. So if the exemplars are 20 features long, a signle lattice 
 * would be 2^20 or 1M elements long. On the other hand, if the exemplars are split in 4, then 4
 * sublattices of size 2^5, or 32, can be used instead, making for close to 100,000 times less
 * memory used.<p>
 * In terms of processing power, more is required to use sublattices. However, using threads the
 * processing of each can be done in parallel.
 * @author Nate Glenn
 *
 */
public class Sublattice {

}
