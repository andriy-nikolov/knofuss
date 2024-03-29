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
package uk.ac.open.kmi.fusion.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;

import uk.ac.open.kmi.common.utils.OIComparison;
import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.ILuceneBlocker;
import uk.ac.open.kmi.fusion.api.IObjectIdentificationMethod;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.AtomicMapping;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.api.impl.FusionMethodWrapper;
import uk.ac.open.kmi.fusion.learning.cache.CacheEntry;
import uk.ac.open.kmi.fusion.learning.cache.CachedPair;
import uk.ac.open.kmi.fusion.learning.cache.MemoryInstanceCache;
import uk.ac.open.kmi.fusion.util.FusionException;

/**
 * A dummy object identification method for debugging purposes. Merely copies the gold standard mappings as its output.
 * 
 * @author Andriy Nikolov
 *
 */
public class DummyGoldStandardCopyObjectIdentificationMethod implements
		IObjectIdentificationMethod {

	private FusionMethodWrapper descriptor;
	private static Logger log = Logger.getLogger(DummyGoldStandardCopyObjectIdentificationMethod.class);
	
	private int depth = 1;
	
	private boolean addMissing = false;
	
	public DummyGoldStandardCopyObjectIdentificationMethod() {
		
	}

	@Override
	public void setMethodDescriptor(FusionMethodWrapper descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public FusionMethodWrapper getDescriptor() {
		return descriptor;
	}
	
	
	private void initProperties() {
		
		if(descriptor.getProperties().containsKey(FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"depth")) {
			this.depth = Integer.parseInt((descriptor.getProperties().get(FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"depth")));
			if(this.depth>2) {
				log.error("Does not support depth greater than 2");
				depth = 2;
			}
		}
		
	}

	@Override
	public List<AtomicMapping> getAllPossibleMappings(ApplicationContext context) throws FusionException {

		try {
			
//			initProperties();
			
			MemoryInstanceCache cache = new MemoryInstanceCache();
			
			this.initializeSourceInstances(context, cache);
//			List<Attribute> targetPropertiesPool = new ArrayList<Attribute>();
			Map<Integer, String> goldStandardEncoded = new HashMap<Integer, String>();
			this.doBlocking(context, cache, goldStandardEncoded);
			
			Map<Integer, Double> finalResults = this.generateFinalResultsFromBlocker(cache);
			
			
			/*CandidateSolutionPool candidateSolutionPool = new CandidateSolutionPool(context, sourcePropertiesPool, targetPropertiesPool, cache);
			candidateSolutionPool.setMaxIterations(maxIterations);
			candidateSolutionPool.setPopulationSize(populationSize);
			candidateSolutionPool.setUseUnsupervisedFitness(useUnsupervisedFitness);
			candidateSolutionPool.setCrossoverRate(crossoverRate);
			candidateSolutionPool.setMutationRate(mutationRate);
			
			candidateSolutionPool.setGoldStandardSet(goldStandardEncoded.keySet());
			Map<Integer, Double> finalResults = candidateSolutionPool.run();
			CandidateSolution finalSolution = candidateSolutionPool.getFinalSolution();
			doFilteringByClasses(finalResults, cache, goldStandardEncoded.keySet(), finalSolution.getFitness(), context.getRestrictedTypesTarget().get(0)); */
			
			return createAtomicMappings(finalResults, cache);
			
		} catch(Exception e) {
			throw new FusionException("Could not perform coreference resolution: ", e);
		}
		
	}

	private void doBlocking(ApplicationContext context, MemoryInstanceCache cache, Map<Integer, String> goldStandardEncoded) throws FusionException {
		
		try {
		ILuceneBlocker blocker = context.getBlocker();

		// Set<String> targetPropertiesSet = new HashSet<String>();

		Map<String, Document> docs;
		int cacheSize = 0;
		CacheEntry targetEntry;
		
		int tp = 0;
		int i = 0;
		String signature;
		
		boolean goldStandardAvailable = context.isGoldStandardAvailable();
		Map<String, OIComparison> goldStandard;
		Set<String> missedKeys;
		if(goldStandardAvailable) {
			goldStandard = context.getGoldStandard();
			missedKeys = new HashSet<String>(goldStandard.keySet());
		} else {
			missedKeys = new HashSet<String>(0);
		}
		String type = context.getRestrictedTypesTarget().get(0);
		
		// targetPropertyArray = targetPropertiesPool.toArray(targetPropertyArray);
	
		//if(useBlocking) {
		
		log.info("Caching starts");
		i=0;
		
		CachedPair pair;
		String[] uris;
			
			// Add the pairs which were missed by the blocker to the goldStandard
			if(goldStandardAvailable) {
				CacheEntry sourceEntry;
				Document doc;
				int missed = 0;
				int relevant = 0;
				URI sourceEntryURI;
				for(String sign : missedKeys) {
					uris = sign.split(" : ");
					sourceEntryURI = FusionEnvironment.getInstance().getFusionKbValueFactory().createURI(uris[0].trim());
					if(cache.containsSourceCacheEntry(sourceEntryURI)) {
						relevant++;
						sourceEntry = cache.getSourceCacheEntry(FusionEnvironment.getInstance().getFusionKbValueFactory().createURI(uris[0].trim()));
						doc = blocker.findByURI(uris[1].trim());
						if(doc==null) {
							// System.out.println(uris[1].trim());
							missed ++;
						} else {
							targetEntry = cache.getTargetCacheEntry(FusionEnvironment.getInstance().getMainKbValueFactory().createURI(uris[1].trim()));
							targetEntry.readPropertiesFromLuceneDocument(doc);
							pair = cache.addPairToCache(sourceEntry, targetEntry, true);
							pair.setMissing(false);
							goldStandardEncoded.put(pair.getId(), sign);
						}
					}
					
				}
				
				log.info("Missed comparison pairs added: "+(relevant-missed)+", could not find: "+missed);
			}
			
			log.info("Gold standard size: "+goldStandardEncoded.size());
		} catch(Exception e) {
			throw new FusionException("Could not create the cache of instance pairs for comparison: ", e);
		}
	}
	
	
	private Map<Integer, Double> generateFinalResultsFromBlocker(MemoryInstanceCache cache) {
		Map<Integer, Double> results = new HashMap<Integer, Double>();
		
		Iterator<CachedPair> iterator = cache.getComparablePairsIterator(false);
		CachedPair currentPair;
		while(iterator.hasNext()) {
			currentPair = iterator.next();
			results.put(currentPair.getId(), 1.0);
		}
		
		return results;
		
	}

	private void initializeSourceInstances(ApplicationContext context, MemoryInstanceCache cache) throws OpenRDFException {
				
		CacheEntry currentCacheEntry = null;
		String currentUri = "";
		String previousUri = "";
		
		log.info("Initialize source property pool...");
		
		// Retrieve all direct paths
		String tmpQuery = context.serializeQuerySPARQLSource();
		log.info("Query: "+tmpQuery);
		TupleQuery query = FusionEnvironment.getInstance().getFusionRepositoryConnection().prepareTupleQuery(QueryLanguage.SPARQL, tmpQuery);
		TupleQueryResult res = query.evaluate();

		BindingSet bs;
		URI propertyURI;
		Set<String> tmpSet = new HashSet<String>();
		String val;
		List<String> tokens;
		try {
			while(res.hasNext()) {
				bs = res.next();
				if(!(bs.getValue("uri") instanceof URI)) continue;
				currentUri = bs.getValue("uri").toString();
				if(!currentUri.equals(previousUri)) {
					
					currentCacheEntry = cache.getSourceCacheEntry((URI)bs.getValue("uri"));
					tmpSet.clear();
				}
				previousUri = currentUri;
				
			}
			log.info("Collected source instances ");
		} finally {
			res.close();
		}
		
		
		
		log.info("Initialize source property pool... finished");
		log.info("Source dataset size: "+cache.getSourceCachedEntries().size());
		
	}
	
	private List<AtomicMapping> createAtomicMappings(Map<Integer, Double> resultsEncoded, MemoryInstanceCache cache) {
		List<AtomicMapping> mappings = new ArrayList<AtomicMapping>(resultsEncoded.size());
		
		CachedPair pair;
		for(Integer id : resultsEncoded.keySet()) {
			pair = cache.getCachedPairById(id);
			mappings.add(pair.convertToAtomicMapping(1.0));
		}
		
		return mappings;
		
	}

}
