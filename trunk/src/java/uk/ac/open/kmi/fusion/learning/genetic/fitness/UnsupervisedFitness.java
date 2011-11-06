package uk.ac.open.kmi.fusion.learning.genetic.fitness;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.learning.CandidateSolution;
import uk.ac.open.kmi.fusion.learning.cache.CacheEntry;
import uk.ac.open.kmi.fusion.learning.cache.CachedPair;
import uk.ac.open.kmi.fusion.learning.cache.MemoryInstanceCache;

public class UnsupervisedFitness implements IFitnessFunction {
	
	double value;
	
	double averagePerIndividual = 0;
	double averageSimilarity = 0;
	int coveredIndividualsSize = 0;
	
	double pseudoPrecision, pseudoRecall;
	double standardDeviation = 0;
	
	double threshold;
	
		
	private static Logger log = Logger.getLogger(UnsupervisedFitness.class);
	
	public static UnsupervisedFitness calculateUnsupervisedFitness(CandidateSolution solution, Map<Integer, Double> solutionResults, MemoryInstanceCache cache) {
		
		//MemoryInstanceCache cache = FusionEnvironment.getInstance().getMemoryInstanceCache();
		
		Map<Integer, Integer> idCachedPairMap = new HashMap<Integer, Integer>();
		
		CachedPair pair;
		CacheEntry entry;
		
		double averageSimilarity = 0;
		
		for(Integer res : solutionResults.keySet()) {
			pair = cache.getCachedPairById(res);
			averageSimilarity += solutionResults.get(res);
			Utils.increaseCounter(pair.getCandidateInstance().getId(), idCachedPairMap);		
		}
		
		averageSimilarity = averageSimilarity / solutionResults.size();
		
		double std = 0;
		/*double val;
		for(Integer res : solutionResults.keySet()) {
			val = averageSimilarity - solutionResults.get(res);
			std += val*val;
		}
		
		std = Math.sqrt(std/solutionResults.size());*/
		
		double average = 0;
		for(int index : idCachedPairMap.keySet()) {
			average += idCachedPairMap.get(index);
		}
		
		average = average / idCachedPairMap.size();
		
		return new UnsupervisedFitness(average, idCachedPairMap.size(), averageSimilarity, std, solution.getGenotype().getThreshold(), cache);
	}
	
	private UnsupervisedFitness(double averagePerIndividual, int solutionsSize, double averageSimilarity, double standardDeviation, double threshold, MemoryInstanceCache cache) {
		
		this.averagePerIndividual = averagePerIndividual;
		this.coveredIndividualsSize = solutionsSize;
		this.averageSimilarity = averageSimilarity;
		this.threshold = threshold;
		this.standardDeviation = standardDeviation;
		calculate(cache);
	}

	@Override
	public double getValue() {
		return this.value;
	}
	
	public void calculate(MemoryInstanceCache cache) {
		// Ideally, there should be as many links as in the smaller set, and all links are 1-to-1
		// double minSetSize = Math.min(FusionEnvironment.getInstance().getSourceSetSize(), FusionEnvironment.getInstance().getTargetSetSize());
		if(this.coveredIndividualsSize == 0) {
			
			this.value = 0;
			this.pseudoPrecision = 0;
			this.pseudoRecall = 0;
			
		} else {
			
			// double alpha = 0.01;
			double alpha = 1;
			
			double overlapDegree = 1;
			
			pseudoPrecision = 1/this.averagePerIndividual;
			int sourceInstances = cache.getSourceCachedEntries().size();
			if(cache.getSampleSize()>0) {
				sourceInstances = cache.getSampleSize();
			}
			
			int targetInstances = cache.getTargetCachedEntries().size();
			int minSetSize = Math.min(sourceInstances, targetInstances);
		
			pseudoRecall = ((double)this.coveredIndividualsSize) / (overlapDegree * minSetSize);
			
			if(pseudoRecall > 2.0) {
				log.fatal("Pseudo recall too high: "+pseudoRecall);
				log.error("Source instances: "+sourceInstances+", targetInstances: "+targetInstances+", minSetSize: "+minSetSize);
			}
			
			this.value = (1+alpha)*pseudoPrecision*pseudoRecall / (alpha*pseudoPrecision + pseudoRecall);
			double coefficient = (1-Math.pow(1-averageSimilarity, 2));
			log.info("Coefficient: "+coefficient+", average similarity: "+averageSimilarity);
			
			if(Math.abs(coefficient)>1) {
				log.error(averageSimilarity+" : "+coefficient);
			}
			
			log.info("Average similarity: "+averageSimilarity+", coefficient: "+coefficient);
			
			this.value = this.value*coefficient;
			
			log.info("Avg per individual: "+this.averagePerIndividual+", pseudo precision: "+pseudoPrecision+", covered individuals: "+this.coveredIndividualsSize+", pseudo recall: "+pseudoRecall);
			
		}
	
	}

	@Override
	public double getPrecision() {
		return pseudoPrecision;
	}

	@Override
	public void setPrecision(double pseudoPrecision) {
		this.pseudoPrecision = pseudoPrecision;
	}

	@Override
	public double getRecall() {
		return pseudoRecall;
	}

	@Override
	public void setRecall(double pseudoRecall) {
		this.pseudoRecall = pseudoRecall;
	}

	public double getAverageSimilarity() {
		return averageSimilarity;
	}

	public double getStandardDeviation() {
		return standardDeviation;
	}
	
	

}
