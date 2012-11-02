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
package uk.ac.open.kmi.fusion.learning.tree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.learning.CandidateSolutionFitnessResult;
import uk.ac.open.kmi.fusion.learning.cache.MemoryInstanceCache;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.F1Fitness;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.IFitnessFunction;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.UnsupervisedFitness;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.UnsupervisedFitnessNeighbourhoodGrowth;

public class CandidateSolutionPoolEvaluatorRunnableTreeBased implements Callable<CandidateSolutionFitnessResult> {

	private static Logger log = Logger.getLogger(CandidateSolutionPoolEvaluatorRunnableTreeBased.class);
	
	private CandidateSolutionTree solution;
	private int solutionNum;
	private Set<Integer> sampleGoldStandard;
	private MemoryInstanceCache cache;
	private String criterion = GeneticAlgorithmObjectIdentificationMethodTreeBased.CRITERION_PSEUDO_F_MEASURE;
	private boolean useSampling;
	private boolean useUnsupervisedFitness;
	private int iterationNum;
	
	public CandidateSolutionPoolEvaluatorRunnableTreeBased(int iterationNum, int solutionNum, CandidateSolutionTree solution, MemoryInstanceCache cache, Set<Integer> sampleGoldStandard, boolean useSampling, boolean useUnsupervisedFitness) {
		this.solution = solution;
		this.cache = cache;
		this.useSampling = useSampling;
		this.useUnsupervisedFitness = useUnsupervisedFitness;
		this.iterationNum = iterationNum;
		this.solutionNum = solutionNum;
		this.sampleGoldStandard = sampleGoldStandard;
	}


	public int getIterationNum() {
		return iterationNum;
	}


	public void setIterationNum(int iterationNum) {
		this.iterationNum = iterationNum;
	}

	public String getCriterion() {
		return criterion;
	}

	public void setCriterion(String criterion) {
		this.criterion = criterion;
	}

	@Override
	public CandidateSolutionFitnessResult call() {
		Map<Integer, Double> solutionResults;
		
		F1Fitness realFitness;
		IFitnessFunction unsupervisedFitness;
		
		Map<Integer, CandidateSolutionFitnessResult> result = new HashMap<Integer, CandidateSolutionFitnessResult>();
		
		CandidateSolutionFitnessResult tmp; 
		
		solutionResults = solution.applySolution(cache, useSampling, false, criterion);
		realFitness = this.evaluateFitness(solution, solutionResults.keySet(), sampleGoldStandard);
		
		unsupervisedFitness = UnsupervisedFitness.calculateUnsupervisedFitness(solutionResults, cache);
		
		log.info("Iteraton: "+iterationNum+", solution: "+solutionNum+", results: "+solutionResults.size());
		log.info("F1 fitness: "+realFitness.getF1()+", precision: "+realFitness.getPrecision()+", recall: "+realFitness.getRecall());
		log.info("Unsupervised fitness: "+unsupervisedFitness.getValue()+", pseudo precision: "+unsupervisedFitness.getPrecision()+", pseudo recall: "+unsupervisedFitness.getRecall());
			
		if(useUnsupervisedFitness) {
			solution.setFitness(unsupervisedFitness);
		} else {
			solution.setFitness(realFitness);
		}
			
		tmp = new CandidateSolutionFitnessResult();
		tmp.setRealFitness(realFitness);
		tmp.setUnsupervisedFitness(unsupervisedFitness);
		tmp.setNumberOfResults(solutionResults.size());

		return tmp;

	}
	
	private F1Fitness evaluateFitness(CandidateSolutionTree solution, Set<Integer> solutionResults, Set<Integer> goldStandard) {
		return F1Fitness.getF1Fitness(goldStandard, solutionResults);
		
	}

}
