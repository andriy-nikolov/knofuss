package uk.ac.open.kmi.fusion.learning.genetic.fitness;

import java.util.Set;

public class F1Fitness implements IFitnessFunction {

	double f1;
	double precision;
	double recall;
	
	int truePositives;
	int falsePositives;
	int falseNegatives;
	
	public static F1Fitness getF1Fitness(Set<Integer> goldStandardSet, Set<Integer> solutionResults) {
		int fp = 0;
		int fn = 0;
		int tp = 0;
		
		String key;
		double precision, recall, f1;
		
		for(int mapping : solutionResults) {
			if(goldStandardSet.contains(mapping)) {
				tp++;
			} else {
				fp++;
			}
		}
		
		fn = goldStandardSet.size()-tp;
		
		return new F1Fitness(tp, fp, fn);
	}
	
	
	private F1Fitness(int tp, int fp, int fn) {
		setTruePositives(tp);
		setFalsePositives(fp);
		setFalseNegatives(fn);
		
		calculate();
	}
	
	private void calculate() {
		if(this.truePositives==0) {
			this.precision = 0;
			this.recall = 0;
			this.f1 = 0;
		} else {
		
			precision = ((double)truePositives)/(truePositives+falsePositives);
			recall = ((double)truePositives)/(truePositives+falseNegatives);
			
			f1 = 2*precision*recall/(precision+recall);
		}
	}

	public double getF1() {
		return f1;
	}

	public void setF1(double f1) {
		this.f1 = f1;
	}

	public double getPrecision() {
		return precision;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public double getRecall() {
		return recall;
	}

	public void setRecall(double recall) {
		this.recall = recall;
	}

	public int getTruePositives() {
		return truePositives;
	}

	public void setTruePositives(int truePosititves) {
		this.truePositives = truePosititves;
	}

	public int getFalsePositives() {
		return falsePositives;
	}

	public void setFalsePositives(int falsePositives) {
		this.falsePositives = falsePositives;
	}

	public int getFalseNegatives() {
		return falseNegatives;
	}

	public void setFalseNegatives(int falseNegatives) {
		this.falseNegatives = falseNegatives;
	}

	@Override
	public double getValue() {
		return getF1();
	}

	
	
}
