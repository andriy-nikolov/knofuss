package uk.ac.open.kmi.fusion.learning.genetic.fitness;

public class DefaultFitnessFunction implements IFitnessFunction {
	
	private double value;
	private double precision;
	private double recall;

	public DefaultFitnessFunction(double precision, double recall, double value) {
		this.precision = precision;
		this.recall = recall;
		this.value = value;
	}

	@Override
	public double getValue() {
		return this.value;
	}

	@Override
	public void setRecall(double pseudoRecall) {
		this.recall = pseudoRecall;

	}

	@Override
	public double getRecall() {
		return this.recall;
	}

	@Override
	public void setPrecision(double pseudoPrecision) {
		this.precision = pseudoPrecision;

	}

	@Override
	public double getPrecision() {
		return this.precision;
	}

}
