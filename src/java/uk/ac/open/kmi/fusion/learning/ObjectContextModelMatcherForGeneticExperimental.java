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
	
	
	private class SimilarityComparator implements Comparator<Integer> {

		private Map<Integer, Double> similarityMap;
		
		public SimilarityComparator(Map<Integer, Double> similarityMap) {
			this.similarityMap = similarityMap;
		}

		@Override
		public int compare(Integer o1, Integer o2) {
			double sim1 = similarityMap.get(o1);
			double sim2 = similarityMap.get(o2);
			
			
			return 0;
		}
		
	}
	
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
					
				if(similarity>=threshold) {
					preliminaryResults.put(cachedPair.getId(), similarity);
					Utils.addToSetMap(cachedPair.getCandidateInstance().getId(), cachedPair.getId(), compsBySourceInstance);
					// Utils.addToSetMap(cachedPair.getTargetInstance().getId(), cachedPair.getId(), compsByTargetInstance);
					// results.put(cachedPair.getId(), similarity);	
				}
				i++;
			}
			
			averageSimilarity = totalSimilarity/comparisons;
			if(Double.toString(averageSimilarity).equals("NaN")) {
				log.error("NaN");
			}
			log.info("similarity: average: "+averageSimilarity+" max: "+maxSimilarity);
			
			for(Integer sourceId : compsBySourceInstance.keySet()) {
				
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
