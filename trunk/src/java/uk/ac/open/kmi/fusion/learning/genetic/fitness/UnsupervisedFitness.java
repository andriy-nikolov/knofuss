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
package uk.ac.open.kmi.fusion.learning.genetic.fitness;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.fusion.learning.cache.CachedPair;
import uk.ac.open.kmi.fusion.learning.cache.MemoryInstanceCache;

public class UnsupervisedFitness implements IFitnessFunction {
	
	public static double BETA = 0.01;
	
	public static double OVERLAP_DEGREE = 1;
	
	double value;
	
	double averagePerIndividual = 0;
	double averageSimilarity = 0;
	int coveredIndividualsSize = 0;
	
	double pseudoPrecision, pseudoRecall;
	double standardDeviation = 0;
	
	// double threshold;
	
		
	private static Logger log = Logger.getLogger(UnsupervisedFitness.class);
	
	public static UnsupervisedFitness calculateUnsupervisedFitness(Map<Integer, Double> solutionResults, MemoryInstanceCache cache) {
		
		Map<Integer, Integer> idCachedPairMap = new HashMap<Integer, Integer>();
		
		CachedPair pair;
		
		double averageSimilarity = 0;
		
		for(Integer res : solutionResults.keySet()) {
			pair = cache.getCachedPairById(res);
			averageSimilarity += solutionResults.get(res);
			Utils.increaseCounter(pair.getCandidateInstance().getId(), idCachedPairMap);		
		}
		
		averageSimilarity = averageSimilarity / solutionResults.size();
		
		double std = 0;
		
		double average = 0;
		for(int index : idCachedPairMap.keySet()) {
			average += idCachedPairMap.get(index);
		}
		
		average = average / idCachedPairMap.size();
		
		return new UnsupervisedFitness(average, idCachedPairMap.size(), averageSimilarity, std, cache);
	}
	
	private UnsupervisedFitness(double averagePerIndividual, int solutionsSize, double averageSimilarity, double standardDeviation, MemoryInstanceCache cache) {
		
		this.averagePerIndividual = averagePerIndividual;
		this.coveredIndividualsSize = solutionsSize;
		this.averageSimilarity = averageSimilarity;
		// this.threshold = threshold;
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
			
			pseudoPrecision = 1/this.averagePerIndividual;
			int sourceInstances = cache.getSourceCachedEntries().size();
			if(cache.getSampleSize()>0) {
				sourceInstances = cache.getSampleSize();
			}
			
			int targetInstances = cache.getTargetCachedEntries().size();
			int minSetSize = Math.min(sourceInstances, targetInstances);
		
			pseudoRecall = ((double)this.coveredIndividualsSize) / (OVERLAP_DEGREE * minSetSize);
			
			if(pseudoRecall > 2.0) {
				log.fatal("Pseudo recall too high: "+pseudoRecall);
				log.error("Source instances: "+sourceInstances+", targetInstances: "+targetInstances+", minSetSize: "+minSetSize);
			}
			
			this.value = (1+BETA)*pseudoPrecision*pseudoRecall / (BETA*pseudoPrecision + pseudoRecall);
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
