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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;

import uk.ac.open.kmi.common.utils.Utils;

public class MemoryInstanceCache {

	private List<CacheEntry> sourceCachedEntries;
	private List<CacheEntry> targetCachedEntries;
	private Map<URI, Integer> sourceUriToIdMap;
	private Map<URI, Integer> targetUriToIdMap;

	private List<CachedPair> cachedPairs;
	
	private Map<String, Set<Integer>> targetCachedEntryIDsByType;
	
	private int sampleSize = 0;
	
	// Missed pairs are pairs of identical individuals (i.e., positive matches), which, 
	// however, should not be compared by the algorithm. They represent pairs which exist in the 
	// gold standard, but were missed by the blocker.
	
	// private List<CachedPair> missedPairs;
			
	public MemoryInstanceCache() {
		cachedPairs = new LinkedList<CachedPair>();
		// missedPairs = new LinkedList<CachedPair>();
		sourceCachedEntries = new LinkedList<CacheEntry>();
		targetCachedEntries = new LinkedList<CacheEntry>();
		sourceUriToIdMap = new HashMap<URI, Integer>();
		targetUriToIdMap = new HashMap<URI, Integer>();
		targetCachedEntryIDsByType = new HashMap<String, Set<Integer>>();
	}
	
		
	public Iterator<CachedPair> getAllPairsIterator() {
		return cachedPairs.iterator();
	}
	
	public Iterator<CachedPair> getComparablePairsIterator(boolean useSampling) {
		return new FilteredCachedPairIterator(this.cachedPairs, useSampling, false);
	}
	
	public CachedPair getCachedPairById(int id) {
		return cachedPairs.get(id);
	}

	public void addPairToCache(CachedPair pair) {
		cachedPairs.add(pair);
	}
	
	/*public void addMissedPairToCache(CachedPair pair) {
		missedPairs.add(pair);
	}*/
	
	public CachedPair addPairToCache(CacheEntry candidate, CacheEntry target, boolean goldStandard) {
		CachedPair pair = new CachedPair(candidate, target, getSize(), goldStandard); 
		addPairToCache(pair);
		return pair;
	}
	
	/*public CachedPair addMissedPairToCache(CacheEntry candidate, CacheEntry target) {
		CachedPair pair = new CachedPair(candidate, target, getSize()+getMissedSize()); 
		addMissedPairToCache(pair);
		return pair;
	}*/
			
	public int getSize() {
		return cachedPairs.size();
	}
	
	/*public int getMissedSize() {
		return missedPairs.size();
	}*/
	
	public boolean containsSourceCacheEntry(URI uri) {
		return this.sourceUriToIdMap.containsKey(uri);
	}
	
	public boolean containsTargetCacheEntry(URI uri) {
		return this.sourceUriToIdMap.containsKey(uri);
	}
	
	public CacheEntry getSourceCacheEntry(URI uri) {
		return getCacheEntry(uri, this.sourceCachedEntries, this.sourceUriToIdMap);
	}
	
	public CacheEntry getTargetCacheEntry(URI uri) {
		return getCacheEntry(uri, this.targetCachedEntries, this.targetUriToIdMap);
	}
	
	private CacheEntry getCacheEntry(URI uri, List<CacheEntry> cachedEntries, Map<URI, Integer> uriToIdMap) {
		if(uriToIdMap.containsKey(uri)) {
			return cachedEntries.get(uriToIdMap.get(uri));
		} else { 
			int id = cachedEntries.size();
			CacheEntry cacheEntry = new CacheEntry(this, uri, id);
			cachedEntries.add(cacheEntry);
			uriToIdMap.put(uri, id);
			return cacheEntry;
		}
	}

	public List<CacheEntry> getSourceCachedEntries() {
		return sourceCachedEntries;
	}

	public List<CacheEntry> getTargetCachedEntries() {
		return targetCachedEntries;
	}
	
	public Map<String, Set<Integer>> getTargetEntryIDsByType() {
		return this.targetCachedEntryIDsByType;
	}
	
	public void addCacheEntryIdToTargetTypeTable(CacheEntry entry, String type) {
		Utils.addToSetMap(type, entry.getId(), this.targetCachedEntryIDsByType);
	}
	
	public Set<Integer> selectRandomSample(int sampleSize) {
		this.sampleSize = sampleSize;
		List<CacheEntry> sourceCachedEntriesCopy = new ArrayList<CacheEntry>(sourceCachedEntries);
		Collections.shuffle(sourceCachedEntriesCopy);
		int threshold = Math.min(sampleSize, sourceCachedEntriesCopy.size());
		
		for(int i = 0; i < threshold; i++) {
			sourceCachedEntriesCopy.get(i).setSampled(true);
		}
		
		Iterator<CachedPair> iterator = this.getComparablePairsIterator(true);
		
		CachedPair pair;
		HashSet<Integer> sampledGoldStandard = new HashSet<Integer>();
		while(iterator.hasNext()) {
			pair = iterator.next();
			if(pair.getCandidateInstance().isSampled()&&pair.isGoldStandard()) {
				sampledGoldStandard.add(pair.getId());
			}
		}
		
		return sampledGoldStandard;
	}

	public int getSampleSize() {
		return sampleSize;
	}
	
	
	
}
