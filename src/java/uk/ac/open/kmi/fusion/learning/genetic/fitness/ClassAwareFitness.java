package uk.ac.open.kmi.fusion.learning.genetic.fitness;

public class ClassAwareFitness implements IFitnessFunction {
	
	double recall;
	double precision;
	double value;

	public ClassAwareFitness() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public double getValue() {
		return value;
	}

	@Override
	public void setRecall(double recall) {
		this.recall = recall;
	}

	@Override
	public double getRecall() {
		return this.recall;
	}

	@Override
	public void setPrecision(double precision) {
		this.precision = precision;
	}

	@Override
	public double getPrecision() {
		return this.precision;
	}

}
