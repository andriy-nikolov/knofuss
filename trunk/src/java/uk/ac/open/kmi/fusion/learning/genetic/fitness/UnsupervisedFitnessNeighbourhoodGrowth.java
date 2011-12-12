package uk.ac.open.kmi.fusion.learning.genetic.fitness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.learning.CandidateSolution;
import uk.ac.open.kmi.fusion.learning.SimilarityComparator;
import uk.ac.open.kmi.fusion.learning.cache.CacheEntry;
import uk.ac.open.kmi.fusion.learning.cache.CachedPair;
import uk.ac.open.kmi.fusion.learning.cache.MemoryInstanceCache;

public class UnsupervisedFitnessNeighbourhoodGrowth implements IFitnessFunction {
	
	double value;
	
	static final double neighbourhoodFactor = 1.1;
	
		
	private static Logger log = Logger.getLogger(UnsupervisedFitnessNeighbourhoodGrowth.class);
	
	public static UnsupervisedFitnessNeighbourhoodGrowth calculateUnsupervisedFitness(CandidateSolution solution, Map<Integer, Double> solutionResults, MemoryInstanceCache cache) {
		
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
		Map<Integer, Integer> mapNeighbourhoodGrowthBySourceId = new HashMap<Integer, Integer>();
		Map<Integer, Integer> mapNeighbourhoodGrowthByTargetId = new HashMap<Integer, Integer>();
		
		for(Integer sourceId : compsBySourceInstance.keySet()) {
			
			pairIds = compsBySourceInstance.get(sourceId);
			
			Collections.sort(pairIds, comparator);
			compsBySourceInstance.put(sourceId, pairIds);
			selectedPairId = pairIds.get(0);
			testPair = cache.getCachedPairById(selectedPairId);
			if(testPair.getCandidateInstance().getUri().toString().equals("http://data.nytimes.com/N45527707190659418771")) {
				log.info("here");
			}
			
			sim = solutionResults.get(selectedPairId);
			epsilon = 1-sim;
			neighbourhoodGrowth = 1;
			for(i = 1;i<pairIds.size();i++) {
				selectedPairId = pairIds.get(i);
				sim = solutionResults.get(selectedPairId);
				if((1-sim)<=neighbourhoodFactor*epsilon) {
					neighbourhoodGrowth++;
					// results.put(selectedPairId, sim);
				} else {
					
					break;
				}
			}
			mapNeighbourhoodGrowthBySourceId.put(sourceId, neighbourhoodGrowth);
		}
		
		double std = 0;
		
		double average = 0;
		
		for(int sourceId : mapNeighbourhoodGrowthBySourceId.keySet()) {
			average+=mapNeighbourhoodGrowthBySourceId.get(sourceId);
		}
		
		average = average/mapNeighbourhoodGrowthBySourceId.size();
		
		/*double val;
		for(Integer res : solutionResults.keySet()) {
			val = averageSimilarity - solutionResults.get(res);
			std += val*val;
		}
		
		std = Math.sqrt(std/solutionResults.size());*/
		
		
		
		return new UnsupervisedFitnessNeighbourhoodGrowth(average);
		
		
	}
	
	private UnsupervisedFitnessNeighbourhoodGrowth(double averageNeighbourhoodGrowth) {
		
		value = 1/averageNeighbourhoodGrowth;
		
	}

	@Override
	public double getValue() {
		return this.value;
	}
	
	

	@Override
	public double getPrecision() {
		return this.value;
	}

	@Override
	public void setPrecision(double pseudoPrecision) {
		
	}

	@Override
	public double getRecall() {
		return 1;
	}

	@Override
	public void setRecall(double pseudoRecall) {
		
	}

	
	
	

}
