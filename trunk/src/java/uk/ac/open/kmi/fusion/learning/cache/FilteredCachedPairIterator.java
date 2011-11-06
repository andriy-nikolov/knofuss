package uk.ac.open.kmi.fusion.learning.cache;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class FilteredCachedPairIterator implements Iterator<CachedPair> {

	int currentIndex = 0;
	Iterator<CachedPair> iterator;
	boolean includeMissedGoldStandardMappings;
	boolean sampledOnly;
	CachedPair next = null;
	boolean hasNext = false;
	
	FilteredCachedPairIterator(List<CachedPair> list) {
		this(list, false, false);
	}
	
	FilteredCachedPairIterator(List<CachedPair> list, boolean sampledOnly, boolean includeMissedGoldStandardMappings) {
		this.iterator = list.iterator();
		this.includeMissedGoldStandardMappings = includeMissedGoldStandardMappings;
		this.sampledOnly = sampledOnly;
	}

	@Override
	public boolean hasNext() {
		while(iterator.hasNext()) {
			next = iterator.next();
			if((next.getCandidateInstance().isSampled()||(!sampledOnly))) {
				if((!next.isMissing())||includeMissedGoldStandardMappings) {
					hasNext = true;
					return hasNext;
				} /*else {
					System.out.println("here");
				}*/
			}
		}
		hasNext = false;
		return hasNext;
	}

	@Override
	public CachedPair next() {
		if(hasNext) {
			return next;
		} else {
			throw new NoSuchElementException("No more suitable elements: isMissing()=="+includeMissedGoldStandardMappings+", isSampledOnly()=="+this.sampledOnly);
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove() not supported");
	}
	
	

}
