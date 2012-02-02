package uk.ac.open.kmi.fusion.learning;

import java.util.List;

import uk.ac.open.kmi.fusion.api.IAggregationFunction;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.AtomicAttribute;
import uk.ac.open.kmi.fusion.api.impl.valuematching.ValueMatchingFunctionFactory;

public class Genotype {

	Double[][] genotypeWeights;
	IValueMatchingFunction[][] genotypeFunctions;
	double threshold;
	int rows = 0;
	int columns = 0;
	IAggregationFunction aggregationFunction;
	
	//public Genotype() {
		
	//}
	
	public Genotype(int rows, int columns) {
		this.rows = rows;
		this.columns = columns;
		this.genotypeWeights = new Double[rows][columns];
		this.genotypeFunctions = new IValueMatchingFunction[rows][columns];
		for(int i = 0;i<rows;i++) {
			for(int j = 0;j<columns;j++) {
				this.genotypeWeights[i][j] = 0.0;
				this.genotypeFunctions[i][j] = null;
			}
		}
	}
	
	public Double[][] getGenotypeWeights() {
		return genotypeWeights;
	}
	
	public void setGenotypeWeights(Double[][] genotypeWeights) {
		this.genotypeWeights = genotypeWeights;
	}
	
	public IValueMatchingFunction[][] getGenotypeFunctions() {
		return genotypeFunctions;
	}
	
	public void setGenotypeFunctions(IValueMatchingFunction[][] genotypeFunctions) {
		this.genotypeFunctions = genotypeFunctions;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public int getRows() {
		return rows;
	}

	public int getColumns() {
		return columns;
	}
	
	public void normalizeWeights() {
		double sum = 0;
		for(int i=0;i<genotypeWeights.length;i++) {
			for(int j=0;j<genotypeWeights[i].length;j++) {
				sum += genotypeWeights[i][j];
			}
		}
		for(int i=0;i<genotypeWeights.length;i++) {
			for(int j=0;j<genotypeWeights[i].length;j++) {
				genotypeWeights[i][j] = genotypeWeights[i][j]/sum;
				if(genotypeWeights[i][j].isNaN()) {
					genotypeWeights[i][j] = 0.0;
					genotypeFunctions[i][j] = null;
				}
			}
		}
	}
	
	public void addRandomComponent(List<AtomicAttribute> sourceAttributes, List<AtomicAttribute> targetAttributes) {
		int k = (int)(Math.random()*getRows());
		int l = (int)(Math.random()*getColumns());
		genotypeWeights[k][l] = Math.random();
		genotypeFunctions[k][l] = ValueMatchingFunctionFactory.getRandomInstanceForAttributes(sourceAttributes.get(k), targetAttributes.get(l));
		
	}

	
	public boolean isEmpty() {
		
		for(int i = 0;i<getRows();i++) {
			for(int j = 0;j<getColumns();j++) {
				if(getGenotypeFunctions()[i][j]!=null) 
					return false;
			}
		}
		return true;
	}

	public IAggregationFunction getAggregationFunction() {
		return aggregationFunction;
	}

	public void setAggregationFunction(IAggregationFunction aggregationFunction) {
		this.aggregationFunction = aggregationFunction;
	}
	
	
}
