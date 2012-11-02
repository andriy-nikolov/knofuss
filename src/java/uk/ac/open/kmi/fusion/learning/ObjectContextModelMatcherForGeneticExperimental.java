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
package uk.ac.open.kmi.fusion.learning;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.fusion.api.IObjectContextWrapper;
import uk.ac.open.kmi.fusion.api.impl.ComparisonPair;
import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;
import uk.ac.open.kmi.fusion.learning.cache.CachedPair;
import uk.ac.open.kmi.fusion.learning.cache.MemoryInstanceCache;
import uk.ac.open.kmi.fusion.objectidentification.LuceneBackedObjectContextWrapper;
// import uk.ac.open.kmi.fusion.index.LuceneDiskIndexer;
// import uk.ac.open.kmi.fusion.index.LuceneMemoryIndexer;
// import uk.ac.open.kmi.fusion.index.LuceneMemoryIndexerAllFields;

public class ObjectContextModelMatcherForGeneticExperimental {
	ObjectContextModel instanceModel;
	Set<LuceneBackedObjectContextWrapper> sourceResources;
	Map<String, LuceneBackedObjectContextWrapper> sourceResourcesTable;
	List<ComparisonPair> selectedPairs;

	MemoryInstanceCache cache;
	
	double threshold;
	//List<AtomicMapping> mappings;
	boolean multiOntologyCase = false;
	
	Map<Integer, Double> results;
	
	String sourceQuery = null;
	
	String notFoundFile = "not-found.txt";
	
	// boolean useSampling = false;
	
	private static Logger log = Logger.getLogger(ObjectContextModelMatcherForGeneticExperimental.class);
	


	private void init() {
		sourceResources = new HashSet<LuceneBackedObjectContextWrapper>();
		sourceResourcesTable = new HashMap<String, LuceneBackedObjectContextWrapper>();
		selectedPairs = new ArrayList<ComparisonPair>();
		//indexer = FusionEnvironment.getIndexer();
		results = new HashMap<Integer, Double>();
		
	}
		
	public ObjectContextModelMatcherForGeneticExperimental() {
		init();
	}
	
	private int calculateSimilarities(boolean useSample) {
		int i;//, j;
		IObjectContextWrapper resTarget;
		
		double similarity;
		
		int comparisons = cache.getSize();
		ComparisonPair pair;
				
		log.info(instanceModel.toString());
		
		Map<Integer, Double> preliminaryResults = new HashMap<Integer, Double>();
		
		Map<Integer, Set<Integer>> compsBySourceInstance = new HashMap<Integer, Set<Integer>>();
		// Map<Integer, Set<Integer>> compsByTargetInstance = new HashMap<Integer, Set<Integer>>();
		
		
		
		try {

			i=0;
			// j=0;
			long currentTime;
			// long totalTimeSearch = 0;
			long totalTimeComparison = 0;
			long totalTimeRetrieval = 0;
			
			double epsilon = 0.0000001;
			// long initTime = System.currentTimeMillis();
			// String type = instanceModel.getRestrictedTypesTarget().get(0).toString();
			//log.info("Concept type : "+type);
			
			//log.info("Cache size : "+FusionEnvironment.getInstance().getMemoryInstanceCache().getSize());
			
			// Map<String, List<String>> valuesByPropertyUri = new HashMap<String, List<String>>();

			Iterator<CachedPair> iterator = cache.getComparablePairsIterator(useSample);
			
			IObjectContextWrapper resSource;
			
			double totalSimilarity = 0;
			double averageSimilarity = 0;
			double maxSimilarity = 0;
			
			CachedPair cachedPair;
			
			while(iterator.hasNext()) {
				
				cachedPair = iterator.next();
				currentTime = System.currentTimeMillis();
				resSource = cachedPair.getCandidateInstance().getObjectContextWrapper(instanceModel, true);
				resTarget = cachedPair.getTargetInstance().getObjectContextWrapper(instanceModel, false);
				totalTimeRetrieval+=(System.currentTimeMillis()-currentTime);
			
				pair = new ComparisonPair(resSource, resTarget);
				// System.out.print("Comparison ... ");
				currentTime = System.currentTimeMillis();
				similarity = instanceModel.getSimilarity(pair);
				pair.setSimilarity(similarity);
				// System.out.println("done");
				/*if(resSource.getIndividual().toString().endsWith("Person990")&&
						resTarget.getIndividual().toString().endsWith("Person991")) {
					log.info("Person990 vs Person991 similarity: " + similarity);
				}*/
				
				totalSimilarity+=similarity;
				if(similarity>maxSimilarity) {
					maxSimilarity = similarity;
				}
				
				totalTimeComparison+=(System.currentTimeMillis()-currentTime);
					
				// if(similarity>=threshold) {
				preliminaryResults.put(cachedPair.getId(), similarity);
				Utils.addToSetMap(cachedPair.getCandidateInstance().getId(), cachedPair.getId(), compsBySourceInstance);
					// Utils.addToSetMap(cachedPair.getTargetInstance().getId(), cachedPair.getId(), compsByTargetInstance);
					// results.put(cachedPair.getId(), similarity);	
				// }
				i++;
			}
			
			averageSimilarity = totalSimilarity/comparisons;
			if(Double.toString(averageSimilarity).equals("NaN")) {
				log.error("NaN");
			}
			log.info("similarity: average: "+averageSimilarity+" max: "+maxSimilarity);
			
			SimilarityComparator comparator = new SimilarityComparator(preliminaryResults);
			List<Integer> sortedPairIds;
			Set<Integer> pairIds;
			Integer selectedPairId;
			double sim, bestSim;
			
			CachedPair testPair;
			
			for(Integer sourceId : compsBySourceInstance.keySet()) {
				
				pairIds = compsBySourceInstance.get(sourceId);
				sortedPairIds = new ArrayList<Integer>(pairIds);
				Collections.sort(sortedPairIds, comparator);
				selectedPairId = sortedPairIds.get(0);
				testPair = cache.getCachedPairById(selectedPairId);

				bestSim = preliminaryResults.get(selectedPairId);
				// epsilon = 1-sim;
				results.put(selectedPairId, bestSim);
				for(i = 1;i<sortedPairIds.size();i++) {
					selectedPairId = sortedPairIds.get(i);
					sim = preliminaryResults.get(selectedPairId);
					if((bestSim-sim)<=epsilon) {
						results.put(selectedPairId, sim);
					} else {
						testPair = cache.getCachedPairById(selectedPairId);
						if(testPair.getCandidateInstance().getUri().toString().equals("http://data.nytimes.com/N45527707190659418771")) {
							if(testPair.getTargetInstance().getUri().toString().equals("http://dbpedia.org/resource/Potomac_River")) {
								log.info("correct sim: "+preliminaryResults.get(selectedPairId));
								log.info("incorrect pair: "+preliminaryResults.get(selectedPairId));
								log.info(cache.getCachedPairById(sortedPairIds.get(0)).getCandidateInstance().getUri().toString());
								log.info(cache.getCachedPairById(sortedPairIds.get(0)).getTargetInstance().getUri().toString());
							}
						} 
						// break;
					}
				}
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			return -1;
		}
		return comparisons;
	}
	
	
	private int calculateNeighborhoodGrowth(Integer sourceId, Set<Integer> comparisonSet, Map<Integer, Double> preliminaryResults) {
		double bestSimilarity = 0;
		
		Map<Integer, Double> mapSimilarities = new HashMap<Integer, Double>();
		double currentSimilarity;
		for(Integer pairId : comparisonSet) {
			currentSimilarity = preliminaryResults.get(pairId);
			mapSimilarities.put(pairId, currentSimilarity);
			if(currentSimilarity >= bestSimilarity) {
				bestSimilarity = currentSimilarity;
			}
			
		}
		
		double threshold = 1-(1-bestSimilarity)*2;
		
		int growth = 0;
		
		for(Integer pairId : mapSimilarities.keySet()) {
			currentSimilarity = mapSimilarities.get(pairId);
			if(currentSimilarity>=threshold) {
				growth++;
			}
		}
		
		return growth;
		
	}
	
		
	public Map<Integer, Double> execute(double threshold, MemoryInstanceCache cache, boolean useSample) {
		this.cache = cache;
		this.threshold = threshold;
//		properties.clear();
		sourceResources.clear();
		//targetResources.clear();
		selectedPairs.clear();
		//candidatePairs.clear();
		
		
		//this.sourceQuery = instanceModel.serializeQuerySPARQLSource();
		
		List<String> searchFieldNames = new ArrayList<String>();
		
		searchFieldNames.addAll(instanceModel.getTargetAttributesByPath().keySet());
		
		// Map<String, String> variablePropertyMap = instanceModel.getVariablePathMapTarget();
		// for(String key : variablePropertyMap.keySet()) {
		//	searchFieldNames.add(variablePropertyMap.get(key));
		// }
				
		//PersistenceUtil.setDaoManager(FusionGlobals.fusionSession.getDaoManager());
		
//		log.info("Loading sources... "+this.instanceModel.getRestrictedTypesSource().get(0));
//		fillList(sourceResources, this.sourceResourcesTable, FusionEnvironment.getInstance().getFusionRepositoryConnection());
//		log.info("Loading targets... "+this.instanceModel.getRestrictedTypesTarget().get(0));
//		log.info("Indexing... "+this.instanceModel.getRestrictedTypesTarget().get(0));
//		log.info("Candidate individuals: "+sourceResources.size());
		
		//log.info("Calculating similarities... "+this.instanceModel.getRestrictedTypesTarget().get(0));
		Calendar calendarBefore = new GregorianCalendar();
		int comparisons = calculateSimilarities(useSample);
		Calendar calendarAfter = new GregorianCalendar();
		log.info("Comparisons: "+comparisons);
		log.info("Time cost: "+(calendarAfter.getTimeInMillis()-calendarBefore.getTimeInMillis()));
		//addToModel();
	
		return results; 
	}

	
	public ObjectContextModel getObjectContextModel() {
		return instanceModel;
	}

	public void setObjectContextModel(ObjectContextModel instanceModel) {
		this.instanceModel = instanceModel;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public boolean isMultiOntologyCase() {
		return multiOntologyCase;
	}

	public void setMultiOntologyCase(boolean multiOntologyCase) {
		this.multiOntologyCase = multiOntologyCase;
	}

	
	
	
}
