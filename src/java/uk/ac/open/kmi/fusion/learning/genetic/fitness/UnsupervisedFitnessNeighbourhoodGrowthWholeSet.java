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

public class UnsupervisedFitnessNeighbourhoodGrowthWholeSet implements IFitnessFunction {
	
	double value;
	
	// static final double neighbourhoodFactor = 1.1;
	
	public static double NEIGHBOURHOOD_FACTOR = 1.1;
	
			
	private static Logger log = Logger.getLogger(UnsupervisedFitnessNeighbourhoodGrowthWholeSet.class);
	
	public static UnsupervisedFitnessNeighbourhoodGrowthWholeSet calculateUnsupervisedFitness(CandidateSolution solution, Map<Integer, Double> solutionResults, MemoryInstanceCache cache) {
		
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
		Map<Integer, Double> mapNeighbourhoodGrowthWeights = new HashMap<Integer, Double>();
		
		for(Integer sourceId : compsBySourceInstance.keySet()) {
			
			pairIds = compsBySourceInstance.get(sourceId);
			
			Collections.sort(pairIds, comparator);
			compsBySourceInstance.put(sourceId, pairIds);
			selectedPairId = pairIds.get(0);
			testPair = cache.getCachedPairById(selectedPairId);

			
			sim = solutionResults.get(selectedPairId);
			epsilon = 1-sim;
			
			mapNeighbourhoodGrowthWeights.put(sourceId, 1.0);
			neighbourhoodGrowth = 1;
			
			for(i = 1;i<pairIds.size();i++) {
				selectedPairId = pairIds.get(i);
				sim = solutionResults.get(selectedPairId);
				if((1-sim)<=NEIGHBOURHOOD_FACTOR*epsilon) {
					neighbourhoodGrowth++;
					// results.put(selectedPairId, sim);
				} else {
					
					break;
				}
			}
			mapNeighbourhoodGrowthBySourceId.put(sourceId, neighbourhoodGrowth);
		}
		
		double average = 0;
		double weightSum = 0, weight;
		
		for(int sourceId : mapNeighbourhoodGrowthBySourceId.keySet()) {
			weight = mapNeighbourhoodGrowthWeights.get(sourceId);
			average+=(mapNeighbourhoodGrowthBySourceId.get(sourceId))*weight;
			weightSum+=weight;
		}
		
		// average = average/mapNeighbourhoodGrowthBySourceId.size();
		average = average/weightSum;
		
		double validPairsRatio = 1.0;
		if(solution!=null) {
			if(UnsupervisedFitnessNeighbourhoodGrowth.discountRareAttributes) {
				validPairsRatio = solution.getValidPairsRatio();
				log.info("Valid pairs ratio: "+validPairsRatio);
			}
		}
		/*double val;
		for(Integer res : solutionResults.keySet()) {
			val = averageSimilarity - solutionResults.get(res);
			std += val*val;
		}
		
		std = Math.sqrt(std/solutionResults.size());*/
		
		
		
		return new UnsupervisedFitnessNeighbourhoodGrowthWholeSet(average, validPairsRatio);
		
		
	}
	
	private UnsupervisedFitnessNeighbourhoodGrowthWholeSet(double averageNeighbourhoodGrowth, double validPairsRatio) {
		
		value = validPairsRatio/(averageNeighbourhoodGrowth+1);
		
		if(Double.isNaN(value)) {
			value = 0.0;
		}
		
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
