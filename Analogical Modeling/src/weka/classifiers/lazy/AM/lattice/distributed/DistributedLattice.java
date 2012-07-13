package weka.classifiers.lazy.AM.lattice.distributed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import weka.classifiers.lazy.AM.lattice.SubcontextList;

/**
 * This  lass manages several smaller, heterogeneous lattices.
 * @author nate
 *
 */
public class DistributedLattice {
	
	List<HeterogeneousLattice> hlattice;
	public DistributedLattice(SubcontextList subList){
//		Collection<SubcontextList> sublistList = sslist.getSublistList();
		
		//TODO: find split points; 
		//TODO: make hlattice list;
		//TODO: add subcontexts to each list, using masked label
		//TODO: combine all into one AnalogicalSet
//		hlattice = new ArrayList<HeterogeneousLattice>(sublistList.size());
//		
//		SubcontextList sl;
//		for(Iterator<SubcontextList> iter = sublistList.iterator(); iter.hasNext();){
//			sl = iter.next();
//			hlattice.add(new HeterogeneousLattice(sl));
//		}
	}

}
