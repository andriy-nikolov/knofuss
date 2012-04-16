package uk.ac.open.kmi.fusion.learning;

import uk.ac.open.kmi.fusion.learning.genetic.fitness.F1Fitness;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.IFitnessFunction;

public class CandidateSolutionFitnessResult {

	private IFitnessFunction unsupervisedFitness;
	private F1Fitness realFitness;
	private int numberOfResults = 0;
	
	public CandidateSolutionFitnessResult() {
		// TODO Auto-generated constructor stub
	}

	public IFitnessFunction getUnsupervisedFitness() {
		return unsupervisedFitness;
	}

	public void setUnsupervisedFitness(IFitnessFunction unsupervisedFitness) {
		this.unsupervisedFitness = unsupervisedFitness;
	}

	public F1Fitness getRealFitness() {
		return realFitness;
	}

	public void setRealFitness(F1Fitness realFitness) {
		this.realFitness = realFitness;
	}

	public int getNumberOfResults() {
		return numberOfResults;
	}

	public void setNumberOfResults(int numberOfResults) {
		this.numberOfResults = numberOfResults;
	}
	
	
	
}
