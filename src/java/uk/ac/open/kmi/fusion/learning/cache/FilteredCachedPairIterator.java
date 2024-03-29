/* Copyright (c) 2012, Knowledge Media Institute
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   * Neither the name of the <organization> nor the
 *     names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
				} 
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
