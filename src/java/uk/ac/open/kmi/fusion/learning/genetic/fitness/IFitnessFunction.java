package uk.ac.open.kmi.fusion.learning.genetic.fitness;

public interface IFitnessFunction {

	public double getValue();

	public abstract void setRecall(double recall);

	public abstract double getRecall();

	public abstract void setPrecision(double precision);

	public abstract double getPrecision();
	
}
