package uk.ac.open.kmi.fusion.learning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.learning.cache.MemoryInstanceCache;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.F1Fitness;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.IFitnessFunction;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.UnsupervisedFitness;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.UnsupervisedFitnessNeighbourhoodGrowth;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.UnsupervisedFitnessNeighbourhoodGrowthWholeSet;

public class CandidateSolutionPoolEvaluatorRunnable implements Callable<CandidateSolutionFitnessResult> {

	private static Logger log = Logger.getLogger(CandidateSolutionPoolEvaluatorRunnable.class);
	
	private CandidateSolution solution;
	private int solutionNum;
	private Set<Integer> sampleGoldStandard;
	private MemoryInstanceCache cache;
	private String criterion = GeneticAlgorithmObjectIdentificationMethod.CRITERION_PSEUDO_F_MEASURE;
	private boolean useSampling;
	private boolean useUnsupervisedFitness;
	private int iterationNum;
	
	public CandidateSolutionPoolEvaluatorRunnable(int iterationNum, int solutionNum, CandidateSolution solution, MemoryInstanceCache cache, Set<Integer> sampleGoldStandard, boolean useSampling, boolean useUnsupervisedFitness) {
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
		if(criterion.equals(GeneticAlgorithmObjectIdentificationMethod.CRITERION_NEIGHBOURHOOD_GROWTH)) {
			unsupervisedFitness = UnsupervisedFitnessNeighbourhoodGrowth.calculateUnsupervisedFitness(solution, solutionResults, cache);
		} else if(criterion.equals(GeneticAlgorithmObjectIdentificationMethod.CRITERION_UNBIASED_NEIGHBOURHOOD_GROWTH)) { 
			unsupervisedFitness = UnsupervisedFitnessNeighbourhoodGrowthWholeSet.calculateUnsupervisedFitness(solution, solutionResults, cache);
		} else {
			unsupervisedFitness = UnsupervisedFitness.calculateUnsupervisedFitness(solutionResults, cache);
		}
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
	
	private F1Fitness evaluateFitness(CandidateSolution solution, Set<Integer> solutionResults, Set<Integer> goldStandard) {
		return F1Fitness.getF1Fitness(goldStandard, solutionResults);
		
	}

}
