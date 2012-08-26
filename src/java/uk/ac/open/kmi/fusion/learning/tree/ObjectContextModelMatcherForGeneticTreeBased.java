package uk.ac.open.kmi.fusion.learning.tree;

import java.util.*;
import org.apache.log4j.Logger;
import uk.ac.open.kmi.fusion.api.*;
import uk.ac.open.kmi.fusion.api.impl.*;
import uk.ac.open.kmi.fusion.learning.cache.CachedPair;
import uk.ac.open.kmi.fusion.learning.cache.MemoryInstanceCache;
import uk.ac.open.kmi.fusion.objectidentification.*;

public class ObjectContextModelMatcherForGeneticTreeBased {
	
	ObjectContextModelTree instanceModel = null;
	
	Set<LuceneBackedObjectContextWrapper> sourceResources;
	Map<String, LuceneBackedObjectContextWrapper> sourceResourcesTable;
	List<ComparisonPair> selectedPairs;

	MemoryInstanceCache cache;
	
	Map<Integer, Double> results;
	
	String sourceQuery = null;
	
	String notFoundFile = "not-found.txt";
	
	private static Logger log = Logger.getLogger(ObjectContextModelMatcherForGeneticTreeBased.class); 
	
	private void init() {
		sourceResources = new HashSet<LuceneBackedObjectContextWrapper>();
		sourceResourcesTable = new HashMap<String, LuceneBackedObjectContextWrapper>();
		selectedPairs = new ArrayList<ComparisonPair>();
		results = new HashMap<Integer, Double>();
		
	}
		
	public ObjectContextModelMatcherForGeneticTreeBased() {
		init();
	}
	
	private int calculateSimilarities(boolean useSample) {
		int i;//, j;
		IObjectContextWrapper resTarget;
		double similarity;
		int comparisons = 0;
		ComparisonPair pair;

		log.info(instanceModel.getGenotype().getRootNode().toString());
		
		try {

			i=0;
			long currentTime;
			long totalTimeComparison = 0;
			long totalTimeRetrieval = 0;
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
				
				currentTime = System.currentTimeMillis();
				
				if(instanceModel.isEquivalentPair(pair)) {
					similarity = instanceModel.getGenotype().getMinimalPassingSimilarity();
					pair.setSimilarity(similarity);
					results.put(cachedPair.getId(), similarity);
				}
				
				
				
				totalTimeComparison+=(System.currentTimeMillis()-currentTime);
				comparisons++;
									
				i++;
				
			}
			
			averageSimilarity = totalSimilarity/comparisons;
			if(Double.toString(averageSimilarity).equals("NaN")) {
				log.error("NaN");
			}
			log.info("similarity: average: "+averageSimilarity+" max: "+maxSimilarity);
			
		} catch(Exception e) {
			e.printStackTrace();
			return -1;
		}
		return comparisons;
	}
	
	
	
		
	public Map<Integer, Double> execute(MemoryInstanceCache cache, boolean useSample) {
		this.cache = cache;
		sourceResources.clear();
		selectedPairs.clear();
		Calendar calendarBefore = new GregorianCalendar();
		int comparisons = calculateSimilarities(useSample);
		Calendar calendarAfter = new GregorianCalendar();
		log.info("Comparisons: "+comparisons);
		log.info("Time cost: "+(calendarAfter.getTimeInMillis()-calendarBefore.getTimeInMillis()));
		return results; 
	}

	
	public ObjectContextModelTree getModel() {
		return this.instanceModel;
	}

	public void setModel(ObjectContextModelTree instanceModel) {
		this.instanceModel = instanceModel;
	}

	
	
	
}
