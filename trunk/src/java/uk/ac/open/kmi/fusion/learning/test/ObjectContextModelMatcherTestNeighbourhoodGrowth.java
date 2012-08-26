package uk.ac.open.kmi.fusion.learning.test;

import java.util.*;
import java.io.*;


import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

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
// import uk.ac.open.kmi.fusion.index.LuceneDiskIndexerAllFields;
import uk.ac.open.kmi.fusion.learning.CacheEntryBackedObjectContextWrapper;
import uk.ac.open.kmi.fusion.learning.CandidateSolution;
import uk.ac.open.kmi.fusion.learning.GeneticAlgorithmObjectIdentificationMethod;
import uk.ac.open.kmi.fusion.learning.SimilarityComparator;
import uk.ac.open.kmi.fusion.learning.cache.CacheEntry;
import uk.ac.open.kmi.fusion.learning.cache.CachedPair;
import uk.ac.open.kmi.fusion.learning.cache.MemoryInstanceCache;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.UnsupervisedFitness;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.UnsupervisedFitnessNeighbourhoodGrowth;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.UnsupervisedFitnessNeighbourhoodGrowthWholeSet;
import uk.ac.open.kmi.fusion.objectidentification.*;
import uk.ac.open.kmi.fusion.objectidentification.standard.*;
import uk.ac.open.kmi.fusion.util.*;

public class ObjectContextModelMatcherTestNeighbourhoodGrowth {
	ObjectContextModel instanceModel;
	ApplicationContext applicationContext;
	// Set<LuceneBackedObjectContextWrapper> sourceResources;
	// Map<String, LuceneBackedObjectContextWrapper> sourceResourcesTable;
	List<ComparisonPair> selectedPairs;
	// Map<IObjectContextWrapper, List<ComparisonPair>> candidatePairs;
	ILuceneBlocker indexer;
	double threshold;
	List<AtomicMapping> mappings;
	boolean multiOntologyCase = false;
	
	Map<Integer, Double> results;
	
	String sourceQuery = null;
	
	String notFoundFile = "not-found.txt";
	
	long totalTimeSearch = 0;
	long totalTimeComparison = 0;
	long totalTimeRetrieval = 0;
	
	Map<String, Set<String>> targetUrisByClass;
	
	Set<String> goldStandardPairs = null;
	
	boolean goldStandardAvailable = false;
	
	MemoryInstanceCache cache;
	Map<Integer, Double> preliminaryResults;
	
	Map<Integer, Set<Integer>> compsBySourceInstance;
	
	boolean selectionAll = true;
	
	private static Logger log = Logger.getLogger(ObjectContextModelMatcherTestNeighbourhoodGrowth.class); 
	
	private String outputFile = null;
	
	Map<Integer, Integer> mapNeighbourhoodGrowthBySourceId = null;
	Map<Integer, Boolean> mapClosestCorrectBySourceId = null;
	
	private String criterion = GeneticAlgorithmObjectIdentificationMethod.CRITERION_NEIGHBOURHOOD_GROWTH;
	
	private void init() {
		// sourceResources = new HashSet<LuceneBackedObjectContextWrapper>();
		// sourceResourcesTable = new HashMap<String, LuceneBackedObjectContextWrapper>();
		// candidatePairs = new HashMap<IObjectContextWrapper, List<ComparisonPair>>();
		selectedPairs = new ArrayList<ComparisonPair>();
		mappings = new ArrayList<AtomicMapping>();
		targetUrisByClass = new HashMap<String, Set<String>>();
		preliminaryResults = new HashMap<Integer, Double>();
		compsBySourceInstance = new HashMap<Integer, Set<Integer>>();
		
		cache = new MemoryInstanceCache();
		results = new HashMap<Integer, Double>();
	}
	
	public ObjectContextModelMatcherTestNeighbourhoodGrowth() {
		init();
	}
	
	public void setGoldStandard(Set<String> goldStandard) {
		this.goldStandardPairs = goldStandard;
		this.goldStandardAvailable = true;
	}
	
	private void fillList(RepositoryConnection connection) {
		LuceneBackedObjectContextWrapper resource;
		
		String sQuery = instanceModel.serializeQuerySPARQLSource();
		
		List<LuceneBackedObjectContextWrapper> resList = new ArrayList<LuceneBackedObjectContextWrapper>();
		
		try {
			TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, sQuery);
		
			TupleQueryResult rsSource = query.evaluate();
			
			try {
				String curUri;
				resource = new LuceneBackedObjectContextWrapper(instanceModel);
				
				int tuples = 0;
				int resources = 0;
				
				BindingSet tuple;
				while(rsSource.hasNext()) {
					tuple = rsSource.next();
					tuples++;
					
					if(!(tuple.getValue("uri") instanceof URI)) {
						continue;
					} 
					
					if(!resource.getPropertiesFromQueryResult(tuple)) {
						
						resList.add(resource);
						resources++;
						// resTable.put(resource.getIndividual().toString(), resource);
						resource = new LuceneBackedObjectContextWrapper(instanceModel);
						
						resource.getPropertiesFromQueryResult(tuple);
					} 
				}
				
				if(resource.getIndividual()!=null) {
					resList.add(resource);
				}
				log.info("Resources: "+resources);
			} finally {
				rsSource.close();
			}
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		CacheEntry sourceEntry;
		
		Map<String, IAttribute> attributesByPropertyPath;
		Map<IAttribute, List<? extends Object>> valuesMap;
				
		// Map<IAttribute, String> propertyPathsMap; 
		
		for(LuceneBackedObjectContextWrapper item : resList) {
			transformCacheEntryFromLuceneBackedWrapper(item, true);			
		}
	}
	
	
	protected void fillCache() {
		
		Set<String> missedGoldStandardUris = new HashSet<String>();
		Map<String, List<String>> valuesByPropertyPath = new HashMap<String, List<String>>();
		
		Map<String, Document> candidateDocs;
		
		int comparisons = 0;
		
		try {
			
			if(this.goldStandardAvailable) {
				missedGoldStandardUris.addAll(this.goldStandardPairs);
			}
			
			// PrintWriter writerTest = new PrintWriter(System.out);
			// PrintWriter writerTestAccepted = null;
			String type = null;
			
			if(!instanceModel.getRestrictedTypesTarget().isEmpty()) {
				type = instanceModel.getRestrictedTypesTarget().get(0).toString();
			}
			
			
			long currentTime;
			// long initTime = System.currentTimeMillis();
			
			boolean isGoldStandard;
			
			log.info("Concept type : "+type);
			int size = cache.getSourceCachedEntries().size();
			log.info("SourceResources: " + size);
			
			Set<String> targetTypes = new HashSet<String>();
			
			Map<String, List<? extends Object>> valsByPath;
			List<? extends Object> vals;
			CacheEntryBackedObjectContextWrapper resSource;
			
			CacheEntry targetEntry;
			
			
			int i = 0, j = 0;
			
			int tp = 0;
			
			
			for(CacheEntry sourceEntry : cache.getSourceCachedEntries()) {

				// j++;
				
				resSource = new CacheEntryBackedObjectContextWrapper(sourceEntry, instanceModel);
				
				log.info(i + " out of "+size+", cache size "+cache.getSize());
			
				valuesByPropertyPath.clear();
				List<String> tmpStringList;
				
				valsByPath = sourceEntry.getValueTable();
				
				for(String path : valsByPath.keySet()) {
					vals = valsByPath.get(path);
					tmpStringList = new ArrayList<String>(vals.size());
					
					valuesByPropertyPath.put(path, tmpStringList);
					
					for(Object val : vals) {
						if(val instanceof String) {
							tmpStringList.add((String)val);
						}
					}
					
				}
				
				
					
					try {
						
						currentTime = System.currentTimeMillis();
						
						if(resSource.getIndividual().toString().equals("http://data.linkedevents.org/event/f16352aa-7f1b-473c-8a3d-764757d400cc")) {
							log.debug("");
						}
						
						candidateDocs = indexer.findClosestDocuments(valuesByPropertyPath, indexer.getThreshold(), type);
						totalTimeSearch+=(System.currentTimeMillis()-currentTime);
						
					} catch(Exception e) {
						e.printStackTrace();
						candidateDocs = new HashMap<String, Document>();
					}

					comparisons+=candidateDocs.size();

					if(candidateDocs.size()==0) {
						log.debug("Not found: "+resSource.getIndividual().toString());
					}

					for(String tmp : candidateDocs.keySet()) {

						Document doc = candidateDocs.get(tmp);
						
						targetTypes.clear();
						
						j++;
						List<String> types = this.getTypesFromLuceneDocument(doc);
						
						if(!types.contains(type)) {
							// log.info("not comparable: !types.contains(type): "+doc.toString());
							continue;
						}
						
						
						
						URI ind = FusionEnvironment.getInstance().getMainKbValueFactory().createURI(tmp);
						
						targetEntry = cache.getTargetCacheEntry(ind);
						
						targetEntry.readPropertiesFromLuceneDocument(doc); 
						

						if(tmp.equals(sourceEntry.getUri().toString())) {
							continue;
						}
						
						isGoldStandard = false;
						
						if(goldStandardAvailable) {
							if(missedGoldStandardUris.remove(resSource.getIndividual().toString()+" : "+tmp)) {
								isGoldStandard = true;
								tp++;
							}
						}
						
						cache.addPairToCache(sourceEntry, targetEntry, isGoldStandard);
							
					}
					
					i++;
			}
			
			if(this.goldStandardAvailable) {
				double recall = 1-((double)missedGoldStandardUris.size())/goldStandardPairs.size();
				log.info("Blocking recall: "+recall);
				
				recall = tp/goldStandardPairs.size();
				log.info("Blocking recall: "+recall);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
			
	}
	
	protected int calculateSimilarities() {
		int i, j;
		IObjectContextWrapper resTarget;
		
		double similarity;

		int comparisons = 0;
		ComparisonPair pair;
		
		
		
		try {
			
			String type = null;
			
			if(!instanceModel.getRestrictedTypesTarget().isEmpty()) {
				type = instanceModel.getRestrictedTypesTarget().get(0).toString();
			}
			
			i=0;
			j=0;
			long currentTime;
			long initTime = System.currentTimeMillis();
			
			log.info("Concept type : "+type);
			int size = cache.getSourceCachedEntries().size();
			log.info("SourceResources: " + size);
			
			IObjectContextWrapper resSource;
			
			CachedPair cachedPair;
			
			Iterator<CachedPair> iterator = cache.getComparablePairsIterator(false);
			
			while(iterator.hasNext()) {
				cachedPair = iterator.next();
			
				resSource = cachedPair.getCandidateInstance().getObjectContextWrapper(instanceModel, true);
				resTarget = cachedPair.getTargetInstance().getObjectContextWrapper(instanceModel, false);
				
				pair = new ComparisonPair(resSource, resTarget);
				
				currentTime = System.currentTimeMillis();
				similarity = instanceModel.getSimilarity(pair);
				pair.setSimilarity(similarity);
				totalTimeComparison+=(System.currentTimeMillis()-currentTime);
				
				preliminaryResults.put(cachedPair.getId(), similarity);
				
				Utils.addToSetMap(cachedPair.getCandidateInstance().getId(), cachedPair.getId(), this.compsBySourceInstance);
				
/*				if(similarity>=threshold) {
					Utils.addToListMap(resSource, pair, candidatePairs);
				}*/
			}
				
			i++;
			

			log.info("Actually compared: "+j);
			
			log.info("Total time: "+(System.currentTimeMillis()-initTime));
			log.info("Total search time: "+totalTimeSearch);
			log.info("Total comparison time: "+totalTimeComparison);
			
			
		} catch(Exception e) {
			e.printStackTrace();
			return -1;
		}
		return comparisons;
	}
	
	private void calculateNeighbourhoodGrowth() {
		if(criterion.equals(GeneticAlgorithmObjectIdentificationMethod.CRITERION_UNBIASED_NEIGHBOURHOOD_GROWTH)) {
			UnsupervisedFitnessNeighbourhoodGrowthWholeSet fitness = UnsupervisedFitnessNeighbourhoodGrowthWholeSet.calculateUnsupervisedFitness(null, preliminaryResults, cache);
			
			log.info("Neighbourhood growth fitness: " +fitness.getValue() + " precision: "+fitness.getPrecision()+" recall "+fitness.getRecall());
		} else {
			UnsupervisedFitnessNeighbourhoodGrowth fitness = UnsupervisedFitnessNeighbourhoodGrowth.calculateUnsupervisedFitness(null, preliminaryResults, cache);
			
			log.info("Neighbourhood growth fitness: " +fitness.getValue() + " precision: "+fitness.getPrecision()+" recall "+fitness.getRecall());
		}
		
		
	}
	
	private void calculateNeighbourhoodGrowths(Map<Integer, Double> solutionResults, MemoryInstanceCache cache) {
		
		//MemoryInstanceCache cache = FusionEnvironment.getInstance().getMemoryInstanceCache();
		
		Map<Integer, List<Integer>> compsBySourceInstance = new HashMap<Integer, List<Integer>>();
		Map<Integer, List<Integer>> compsByTargetInstance = new HashMap<Integer, List<Integer>>();
		
		Map<Integer, Integer> idCachedPairMap = new HashMap<Integer, Integer>();
		
		CachedPair pair;
		CacheEntry entry;
		
		for(Integer res : solutionResults.keySet()) {
			pair = cache.getCachedPairById(res);
			Utils.addToListMap(pair.getCandidateInstance().getId(), pair.getId(), compsBySourceInstance);
			Utils.addToListMap(pair.getTargetInstance().getId(), pair.getId(), compsByTargetInstance);
		}
		
		List<Integer> pairIds;
		CachedPair testPair;
		SimilarityComparator comparator = new SimilarityComparator(solutionResults);
		int selectedPairId, i;
		double sim, epsilon;
		
		
		int neighbourhoodGrowth;
		mapNeighbourhoodGrowthBySourceId = new HashMap<Integer, Integer>();
		mapClosestCorrectBySourceId = new HashMap<Integer, Boolean>();
		
		Map<Integer, Double> mapNeighbourhoodGrowthWeights = new HashMap<Integer, Double>();
		
		for(Integer sourceId : compsBySourceInstance.keySet()) {
			
			pairIds = compsBySourceInstance.get(sourceId);
			
			Collections.sort(pairIds, comparator);
			compsBySourceInstance.put(sourceId, pairIds);
			selectedPairId = pairIds.get(0);
			testPair = cache.getCachedPairById(selectedPairId);

			if(testPair.isGoldStandard()) {
				mapClosestCorrectBySourceId.put(sourceId, true);
			} else {
				mapClosestCorrectBySourceId.put(sourceId, false);
			}
			
			sim = solutionResults.get(selectedPairId);
			epsilon = 1-sim;
			
			mapNeighbourhoodGrowthWeights.put(sourceId, 1.0);
			neighbourhoodGrowth = 1;
			
			for(i = 1;i<pairIds.size();i++) {
				selectedPairId = pairIds.get(i);
				sim = solutionResults.get(selectedPairId);
				if((1-sim)<=UnsupervisedFitnessNeighbourhoodGrowth.NEIGHBOURHOOD_FACTOR*epsilon) {
					neighbourhoodGrowth++;
					// results.put(selectedPairId, sim);
				} else {
					
					break;
				}
			}
			mapNeighbourhoodGrowthBySourceId.put(sourceId, neighbourhoodGrowth);
		}
		
	}
	
	/*private void calculateUnsupervised() {
		// Calculate pseudo precision & recall
		double averageSimilarity = 0;
		double average = 0;
		List<ComparisonPair> pairs;
		double solutionResults = 0.0;
		
		for(IObjectContextWrapper wrapper : candidatePairs.keySet()) {
			average += candidatePairs.get(wrapper).size();
			pairs = candidatePairs.get(wrapper);
			
			for(ComparisonPair pair : pairs) {
				if(pair.getSimilarity() >= threshold) {
					average ++;
					solutionResults ++;
					averageSimilarity += pair.getSimilarity();
				}
			}
		}
		
		averageSimilarity = averageSimilarity / solutionResults;
		average = average / candidatePairs.size();
		
		double pseudoPrecision = 1 / average;
		double pseudoRecall = ((double)candidatePairs.size()) / cache.getSourceCachedEntries().size();
		
		log.info("Pseudo precision: "+pseudoPrecision+", pseudo recall: "+pseudoRecall);
		
	}*/
	
	private ObjectContextWrapper getTargetFromLuceneDocument(String uri, Document document) {
		LuceneBackedObjectContextWrapper resource = new LuceneBackedObjectContextWrapper(instanceModel);
		URI ind = FusionEnvironment.getInstance().getMainKbValueFactory().createURI(uri);
		resource.setIndividual(ind);
		resource.getPropertiesFromLuceneIndexedDocument(document, ind);
		return resource;
	}
	
	private CacheEntry transformCacheEntryFromLuceneBackedWrapper(LuceneBackedObjectContextWrapper wrapper, boolean isSource) {
		CacheEntry result;
		
		Map<String, IAttribute> attributesByPropertyPath;
		Map<IAttribute, List<? extends Object>> valuesMap;
				
		valuesMap = wrapper.getValues();
		if(isSource) {
			result = cache.getSourceCacheEntry(wrapper.getIndividual());
		} else {
			result = cache.getTargetCacheEntry(wrapper.getIndividual());
		}
		
		List<? extends Object> vals;
		for(IAttribute attr : valuesMap.keySet()) {
			attributesByPropertyPath = attr.getAtomicAttributesByPropertyPath();
				
			for(String path : attributesByPropertyPath.keySet()) {
				vals = valuesMap.get(attributesByPropertyPath.get(path));
				for(Object val : vals) {
					result.addValue(path, val);
				}
			}
				
		}
			
		return result;
	}
	
	
	private List<String> getTypesFromLuceneDocument(Document doc) {
		List<String> answer = new ArrayList<String>();
		
		Field[] typeFields = doc.getFields(RDF.TYPE.toString());
		for(int i=0;i<typeFields.length;i++) {
			Field typeField = typeFields[i];
			String tmp = typeField.stringValue().trim();
			if(tmp.indexOf(' ')>=0) {
				StringTokenizer tokenizer = new StringTokenizer(tmp, " ");
				while(tokenizer.hasMoreTokens()) {
					answer.add(tokenizer.nextToken());
				}
			} else {
				answer.add(typeField.stringValue());
			}
		}
		return answer;
	}

		
	public List<AtomicMapping> execute(double threshold, ILuceneBlocker indexer) {
		this.threshold = threshold;
		this.indexer = indexer;
		
		selectedPairs.clear();
		// candidatePairs.clear();
		this.sourceQuery = instanceModel.serializeQuerySPARQLSource();
		
		log.info("Loading sources... "+this.instanceModel.getRestrictedTypesSource().get(0));
		fillList(FusionEnvironment.getInstance().getFusionRepositoryConnection());
		log.info("Loading targets... "+this.instanceModel.getRestrictedTypesTarget().get(0));
		log.info("Indexing... "+this.instanceModel.getRestrictedTypesTarget().get(0));
		log.info("Candidate individuals: "+cache.getSourceCachedEntries().size());
		fillCache();
		log.info("Compared pairs: "+cache.getSize());
		//((LuceneDiskIndexerAllFields)indexer).collectPropertyNames(this.instanceModel.getRestrictedTypesTarget().get(0));
		log.info("Calculating similarities... "+this.instanceModel.getRestrictedTypesTarget().get(0));
		Calendar calendarBefore = new GregorianCalendar();
		int comparisons = calculateSimilarities();
		
		generateResults();
		
		Calendar calendarAfter = new GregorianCalendar();
		log.info("Comparisons: "+comparisons);
		log.info("Time cost: "+(calendarAfter.getTimeInMillis()-calendarBefore.getTimeInMillis()));
		
		// calculateUnsupervised();
		
		calculateNeighbourhoodGrowth();
		
		addToModel();
		// writeNotFoundOnes();
		
		return mappings; 
	}
	
		

	protected void addToModel() {
		int i;
		// ComparisonPair comparator;
		AtomicMapping curAtomicMapping;
		CachedPair pair;
		
		for(Integer pairId : results.keySet()) {
			pair = cache.getCachedPairById(pairId);
			
			curAtomicMapping = createAtomicMappingFromCachedPair(pair);
			if(curAtomicMapping!=null) {
				mappings.add(curAtomicMapping);
				curAtomicMapping.setAccepted(true);
				/*if(results.get(pairId)>=threshold) {
					curAtomicMapping.setAccepted(true);
				}*/
			}
		}
			
		
	}
	
	private void generateResults() {
		double epsilon = 0.0000001;
		
		int i;
		
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
			Collections.sort(pairsSorted, comparator);
			
			List<Integer> sortedPairIds;
			Set<Integer> pairIds;
			Integer selectedPairId;
			double bestSim;
			
			CachedPair testPair;
			
			double beta = UnsupervisedFitness.BETA;
			// double alpha = 1;
			
			for(Integer sourceId : compsBySourceInstance.keySet()) {
				
				pairIds = compsBySourceInstance.get(sourceId);
				sortedPairIds = new ArrayList<Integer>(pairIds);
				Collections.sort(sortedPairIds, comparator);
				selectedPairId = sortedPairIds.get(0);
				testPair = cache.getCachedPairById(selectedPairId);
										
				bestSim = preliminaryResults.get(selectedPairId);
				if(bestSim > epsilon) {
					if(isValid(instanceModel, testPair)) {
						topPairs.put(selectedPairId, bestSim);
					}
				}
			}
			
			// Now, sort everything
			topPairsSorted.addAll(topPairs.keySet());
			Collections.sort(topPairsSorted, comparator);
			
			double threshold, bestThreshold = 0;
			
			double tp, p = 0, total = compsBySourceInstance.size();
			double pseudoPrecision, pseudoRecall, pseudoF, maxPseudoF = -1, maxPseudoP = -1, maxPseudoR = -1.0;
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
				pseudoF = (1+beta)*pseudoPrecision*pseudoRecall/(beta*pseudoPrecision+pseudoRecall);

				if(pseudoF>=maxPseudoF) {
					maxPseudoF = pseudoF;
					maxPseudoP = pseudoPrecision;
					maxPseudoR = pseudoRecall;
					bestThreshold = threshold;
					
				}
				
			}
			
			log.info("Best pseudo F: "+maxPseudoF+", p: "+maxPseudoP+", r: "+maxPseudoR);
			log.info("Best threshold: "+bestThreshold);
			
			
			for(Integer key : preliminaryResults.keySet()) {
				if(preliminaryResults.get(key)>=bestThreshold) {
					results.put(key, preliminaryResults.get(key));
				}
			}
			
		}
		
		writeNeighbourhoodGrowthsToArff();
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
			
			if(sourceValues.isEmpty()||targetValues.isEmpty()) {
				return false;
			}
		}
		
		return true;
	}
	
	protected AtomicMapping createAtomicMappingFromCachedPair(CachedPair pair) {
		AtomicMapping res;
		CacheEntryBackedObjectContextWrapper resource;
		res = new AtomicMapping();
		String sourceLabel, targetLabel;
		resource = (CacheEntryBackedObjectContextWrapper)pair.getCandidateInstance().getObjectContextWrapper(instanceModel, true);
		
		res.addIndividual(resource.getIndividual(), true);
		sourceLabel = "";
		;
		for(IAttribute key : instanceModel.getSourceAttributes()) {
			if(key instanceof AtomicAttribute) {
				if(((AtomicAttribute) key).getVariableName().equals("uri")) continue;
				if(((AtomicAttribute) key).getPropertyPath().endsWith("name")||((AtomicAttribute) key).getPropertyPath().endsWith("label")||((AtomicAttribute) key).getPropertyPath().endsWith("title")) {
					sourceLabel = resource.getValuesByAttribute(key).get(0).toString();
				}
			}
		}
		res.setSourceLabel(sourceLabel);
		//res.addLabel(resource.getIndividual(), sourceLabel);
		
		resource = (CacheEntryBackedObjectContextWrapper)pair.getTargetInstance().getObjectContextWrapper(instanceModel, false);
		res.addIndividual(resource.getIndividual(), false);
		targetLabel = "";
		for(IAttribute key : instanceModel.getTargetAttributes()) {
			if(key instanceof AtomicAttribute) {
				if(((AtomicAttribute) key).getVariableName().equals("uri")) continue;
				if(((AtomicAttribute) key).getPropertyPath().endsWith("name")||((AtomicAttribute) key).getPropertyPath().endsWith("label")||((AtomicAttribute) key).getPropertyPath().endsWith("title")) {
					targetLabel = resource.getValuesByAttribute(key).get(0).toString();
				}
			}
		}
		
		//res.addLabel(resource.getIndividual(), targetLabel);
		res.setTargetLabel(targetLabel);
		
		res.setSimilarity(results.get(pair.getId()));
		
		return res;
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

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}
	
	private void writeNeighbourhoodGrowthsToArff() {
		if(this.outputFile==null) {
			return;
		}
		
		try {
			
			PrintWriter writer = Utils.openPrintFileWriter(this.outputFile);
			this.calculateNeighbourhoodGrowths(preliminaryResults, cache);
			
			try {
				writer.println("@RELATION test-relation");
				writer.println();
				// writer.println("@ATTRIBUTE uri1 STRING");
				// writer.println("@ATTRIBUTE uri2 STRING");
				writer.println("@ATTRIBUTE ngrowth NUMERIC");
				writer.println("@ATTRIBUTE classLabel {true, false}");
				writer.println();
				
				writer.println("@data");
				for(Integer sourceId : this.mapNeighbourhoodGrowthBySourceId.keySet()) {
					writer.print(this.mapNeighbourhoodGrowthBySourceId.get(sourceId));
					writer.print(",");
					writer.println(this.mapClosestCorrectBySourceId.get(sourceId));					
				}
				
				
			} finally {
				writer.close();
			}
			
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}

	public boolean isSelectionAll() {
		return selectionAll;
	}

	public void setSelectionAll(boolean selectionAll) {
		this.selectionAll = selectionAll;
	}

	public String getCriterion() {
		return criterion;
	}

	public void setCriterion(String criterion) {
		this.criterion = criterion;
	}
	
	
	
}
