package uk.ac.open.kmi.fusion.api;

import java.util.List;

import uk.ac.open.kmi.fusion.api.impl.VariableComparisonSpecification;

public interface IAggregationFunction {

	public static final String AVERAGE = "average";
	public static final String MAX = "max";
	
	public double calculate(IObjectContextWrapper source, IObjectContextWrapper target, List<VariableComparisonSpecification> variableComparisons);
	
}
