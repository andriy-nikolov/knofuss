package uk.ac.open.kmi.fusion.learning;

import java.util.*;
import java.io.*;


import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.*;
import org.openrdf.model.*;
import org.openrdf.model.vocabulary.RDF;


import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.common.utils.sparql.MySPARQLParser;
import uk.ac.open.kmi.fusion.api.*;
import uk.ac.open.kmi.fusion.api.impl.*;
// import uk.ac.open.kmi.fusion.index.LuceneDiskIndexer;
// import uk.ac.open.kmi.fusion.index.LuceneMemoryIndexer;
// import uk.ac.open.kmi.fusion.index.LuceneMemoryIndexerAllFields;
import uk.ac.open.kmi.fusion.learning.cache.CachedPair;
import uk.ac.open.kmi.fusion.learning.cache.MemoryInstanceCache;
import uk.ac.open.kmi.fusion.objectidentification.*;
import uk.ac.open.kmi.fusion.objectidentification.standard.*;
import uk.ac.open.kmi.fusion.util.*;

public class ContextModelMatcherForGeneticNeighborhoodGrowth {
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
	
	boolean selectionAll = true;
	
	// boolean useSampling = false;
	
	private static Logger log = Logger.getLogger(ContextModelMatcherForGeneticNeighborhoodGrowth.class);
	
	

	private void init() {
		sourceResources = new HashSet<LuceneBackedObjectContextWrapper>();
		sourceResourcesTable = new HashMap<String, LuceneBackedObjectContextWrapper>();
		selectedPairs = new ArrayList<ComparisonPair>();
		//indexer = FusionEnvironment.getIndexer();
		results = new HashMap<Integer, Double>();
		
	}
		
	public ContextModelMatcherForGeneticNeighborhoodGrowth() {
		init();
	}
	
	private int calculateSimilarities(boolean useSample, boolean isFinal) {
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
			
			double epsilon = 0.0000001;
			
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
				if(resSource.getIndividual().toString().equals("http://data.nytimes.com/N78390312302609901431")&&
						resTarget.getIndividual().toString().equals("http://sws.geonames.org/1275004/")) {
					log.info("Person990 vs Person991 similarity: " + similarity);
				}
				
				totalSimilarity+=similarity;
				if(similarity>maxSimilarity) {
					maxSimilarity = similarity;
				}
				
				totalTimeComparison+=(System.currentTimeMillis()-currentTime);
					
				//if(similarity>=epsilon) {
				
					preliminaryResults.put(cachedPair.getId(), similarity);
					Utils.addToSetMap(cachedPair.getCandidateInstance().getId(), cachedPair.getId(), compsBySourceInstance);
					// Utils.addToSetMap(cachedPair.getTargetInstance().getId(), cachedPair.getId(), compsByTargetInstance);
					// results.put(cachedPair.getId(), similarity);	
				// }
				//}
				i++;
			}
			
			averageSimilarity = totalSimilarity/comparisons;
			if(Double.toString(averageSimilarity).equals("NaN")) {
				log.error("NaN");
			}
			log.info("similarity: average: "+averageSimilarity+" max: "+maxSimilarity);
			
			if(isFinal) {
				
				if(selectionAll) {
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
						if(testPair.getCandidateInstance().getUri().toString().equals("http://data.nytimes.com/N45527707190659418771")) {
							log.info("here");
						}
						
						bestSim = preliminaryResults.get(selectedPairId);
						if(bestSim > epsilon) {
							if(isValid(instanceModel, testPair)) {
								results.put(selectedPairId, bestSim);
							}
							for(i = 1;i<sortedPairIds.size();i++) {
								selectedPairId = sortedPairIds.get(i);
								sim = preliminaryResults.get(selectedPairId);
								testPair = cache.getCachedPairById(selectedPairId);
								
								if(Math.abs(sim-bestSim)<=epsilon) {
									if(isValid(instanceModel, testPair)) {
										results.put(selectedPairId, sim);
									} 
								} else {
									
									break;
								}
							}
						}
					}
				} else {
					// Select by pseudo precision/recall 
					
					SimilarityComparator comparator = new SimilarityComparator(preliminaryResults);
					
					Map<Integer, Double> topPairs = new HashMap<Integer, Double>();
					List<Integer> topPairsSorted = new ArrayList<Integer>();
					
					List<Integer> pairsSorted = new ArrayList<Integer>(preliminaryResults.keySet());
					List<Integer> wrongPairsSorted = new ArrayList<Integer>();
					
					Collections.sort(pairsSorted, comparator);
					
					double runningPseudoPrecision = 0.0;
					
					double runningPseudoRecall = 0.0;
					
					List<Integer> sortedPairIds;
					Set<Integer> pairIds;
					Integer selectedPairId;
					double sim, bestSim;
					
					CachedPair testPair;
					
					double alpha = 0.01;
					
					for(Integer sourceId : compsBySourceInstance.keySet()) {
						
						pairIds = compsBySourceInstance.get(sourceId);
						sortedPairIds = new ArrayList<Integer>(pairIds);
						Collections.sort(sortedPairIds, comparator);
						selectedPairId = sortedPairIds.get(0);
						testPair = cache.getCachedPairById(selectedPairId);
						if(testPair.getCandidateInstance().getUri().toString().equals("http://data.nytimes.com/N45527707190659418771")) {
							log.info("here");
						}
						
						bestSim = preliminaryResults.get(selectedPairId);
						if(bestSim > epsilon) {
							if(isValid(instanceModel, testPair)) {
							//	results.put(selectedPairId, bestSim);
								topPairs.put(selectedPairId, bestSim);
							}
						}
					}
					
					// Now, sort everything
					topPairsSorted.addAll(topPairs.keySet());
					Collections.sort(topPairsSorted, comparator);
					
					int currentTop = 0; // current index in the main list
					double threshold, bestThreshold = 0;
					int pairId;
					double tp, p = 0, fp, fn, total = compsBySourceInstance.size();
					double pseudoPrecision, pseudoRecall, pseudoF, maxPseudoF = -1, maxPseudoP = -1, maxPseudoR = -1.0;
					int bestI = 0;
					int j = 0;
					i = 0;
					while(i<topPairsSorted.size()) {
						threshold = topPairs.get(topPairsSorted.get(i));
						while(topPairs.get(topPairsSorted.get(i))>=threshold) {
							i++;
							if(i>=topPairsSorted.size()) break;
						}
						tp = i;
						while(preliminaryResults.get(pairsSorted.get(j))>=threshold) {
							j++;
							if(j>=pairsSorted.size()) break;
						}
						p = j;
						pseudoPrecision = tp/p;
						pseudoRecall = tp/total;
						pseudoF = (1+alpha)*pseudoPrecision*pseudoRecall/(alpha*pseudoPrecision+pseudoRecall);
						// log.info("pp: "+pseudoPrecision+", pr: "+pseudoRecall+", pf: "+pseudoF);
						if(pseudoF>=maxPseudoF) {
							maxPseudoF = pseudoF;
							maxPseudoP = pseudoPrecision;
							maxPseudoR = pseudoRecall;
							bestThreshold = threshold;
							bestI = i;
							
						}
						
					}
					
					/*for(i = 0; i<topPairsSorted.size();i++) {
						threshold = topPairs.get(topPairsSorted.get(i));
						tp = i+1;
						for(int j = currentTop;j<pairsSorted.size();j++) {
							pairId = pairsSorted.get(j);
							sim = preliminaryResults.get(pairId);
							currentTop++;
							if(sim>=threshold) {
								p++;
							} else {
								break;
							}
						}
						
						log.info("tp: "+tp+", p: "+p+", total: "+total);
						
						
						pseudoPrecision = tp/p;
						pseudoRecall = tp/total;
						pseudoF = (1+alpha)*pseudoPrecision*pseudoRecall/(alpha*pseudoPrecision+pseudoRecall);
						log.info("pp: "+pseudoPrecision+", pr: "+pseudoRecall+", pf: "+pseudoF);
						if(pseudoF>=maxPseudoF) {
							maxPseudoF = pseudoF;
							maxPseudoP = pseudoPrecision;
							maxPseudoR = pseudoRecall;
							bestThreshold = threshold;
							bestI = i;
							
						}
						
					}*/
					
					log.info("Best pseudo F: "+maxPseudoF+", p: "+maxPseudoP+", r: "+maxPseudoR);
					log.info("Best threshold: "+bestThreshold);
					
					
					for(Integer key : preliminaryResults.keySet()) {
						if(preliminaryResults.get(key)>=bestThreshold) {
							results.put(key, preliminaryResults.get(key));
						}
					}
					
					/*for(Integer sourceId : compsBySourceInstance.keySet()) {
						
						pairIds = compsBySourceInstance.get(sourceId);
						sortedPairIds = new ArrayList<Integer>(pairIds);
						Collections.sort(sortedPairIds, comparator);
						selectedPairId = sortedPairIds.get(0);
						testPair = cache.getCachedPairById(selectedPairId);
						if(testPair.getCandidateInstance().getUri().toString().equals("http://data.nytimes.com/N45527707190659418771")) {
							log.info("here");
						}
						
						bestSim = preliminaryResults.get(selectedPairId);
						if((bestSim > epsilon)&&(bestSim>=bestThreshold)) {
							if(isValid(instanceModel, testPair)) {
								results.put(selectedPairId, bestSim);
								
							}
							for(i = 1;i<sortedPairIds.size();i++) {
								selectedPairId = sortedPairIds.get(i);
								sim = preliminaryResults.get(selectedPairId);
								testPair = cache.getCachedPairById(selectedPairId);
								
								if((Math.abs(sim-bestSim)<=epsilon)) {
									if(isValid(instanceModel, testPair)) {
										results.put(selectedPairId, sim);
									} 
								} else {
									
									break;
								}
							}
						}
					}*/
					writeNeighborhoodGrowthsToFile(preliminaryResults, compsBySourceInstance, comparator);
					
				}
			} else {
				results.putAll(preliminaryResults);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			return -1;
		}
		return comparisons;
	}
	
	private void writeNeighborhoodGrowthsToFile(Map<Integer, Double> preliminaryResults, Map<Integer, Set<Integer>> compsBySourceInstance, SimilarityComparator comparator) {
		
		Set<Integer> pairIds;
		List<Integer> sortedPairIds = new ArrayList<Integer>();
		CachedPair pair;
		
		String fileName = "neighborhoodGrowths.csv";
		
		int neighbourhoodGrowth;
		int selectedPairId;
		
		double sim, epsilon;
		Map<Integer, Integer> mapNeighbourhoodGrowthBySourceId = new HashMap<Integer, Integer>();
		Map<Integer, Double> bestSimBySourceId = new HashMap<Integer, Double>();
		try {
			PrintWriter writer = Utils.openPrintFileWriter(fileName);
			
			for(Integer sourceId : compsBySourceInstance.keySet()) {
				
				
				
				pairIds = compsBySourceInstance.get(sourceId);
				sortedPairIds.clear();
				sortedPairIds.addAll(pairIds);
				Collections.sort(sortedPairIds, comparator);
				selectedPairId = sortedPairIds.get(0);
				pair = cache.getCachedPairById(selectedPairId);
				sim = preliminaryResults.get(selectedPairId);
				writer.print(sourceId+",");
				writer.print(pair.getCandidateInstance().getUri().toString()+":"+pair.getTargetInstance().getUri().toString()+",");
				bestSimBySourceId.put(sourceId, sim);
				writer.print(sim+",");
				epsilon = 1-sim;
				neighbourhoodGrowth = 1;
				for(int i = 1;i<sortedPairIds.size();i++) {
					selectedPairId = sortedPairIds.get(i);
					sim = preliminaryResults.get(selectedPairId);
					if((1-sim)<=2*epsilon) {
						neighbourhoodGrowth++;
						// results.put(selectedPairId, sim);
					} else {
						
						break;
					}
				}
				writer.print(neighbourhoodGrowth+",");
				writer.println(pair.isGoldStandard());
				mapNeighbourhoodGrowthBySourceId.put(sourceId, neighbourhoodGrowth);
				
			}
			
			writer.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	private boolean isValid(ObjectContextModel model, CachedPair pair) {
		
		List<VariableComparisonSpecification> specs = model.getVariableComparisonSpecifications();
		IObjectContextWrapper resSource = pair.getCandidateInstance().getObjectContextWrapper(instanceModel, true);
		IObjectContextWrapper resTarget = pair.getTargetInstance().getObjectContextWrapper(instanceModel, false);
		for(VariableComparisonSpecification spec : specs) {
			List<? extends Object> sourceValues = resSource.getValuesByAttribute(spec.getSourceAttribute());
			List<? extends Object> targetValues = resTarget.getValuesByAttribute(spec.getTargetAttribute());
			
			if((sourceValues==null)||(targetValues==null)) {
				return false;
			}
		}
		
		return true;
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
	
		
	public Map<Integer, Double> execute(double threshold, MemoryInstanceCache cache, boolean useSample, boolean isFinal) {
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
		int comparisons = calculateSimilarities(useSample, isFinal);
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
