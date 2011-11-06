package uk.ac.open.kmi.fusion.api.impl.aggregation;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.open.kmi.fusion.api.IAggregationFunction;
import uk.ac.open.kmi.fusion.api.IObjectContextWrapper;
import uk.ac.open.kmi.fusion.api.impl.VariableComparisonSpecification;
import uk.ac.open.kmi.fusion.learning.CandidateSolutionPool;

public class MaxAggregationFunction implements IAggregationFunction {

	private static final MaxAggregationFunction INSTANCE = new MaxAggregationFunction();
	
	private static Logger log = Logger.getLogger(MaxAggregationFunction.class);
	
	private MaxAggregationFunction() {
		// TODO Auto-generated constructor stub
	}
		
	public static MaxAggregationFunction getInstance() {
		return INSTANCE;
	}

	@Override
	public double calculate(IObjectContextWrapper source,
			IObjectContextWrapper target,
			List<VariableComparisonSpecification> variableComparisons) {
		
		Double result = 0.0;

		Double similarity;
		
		for(VariableComparisonSpecification spec : variableComparisons) {
			
			similarity = spec.getSimilarity(
					source.getValuesByAttribute(spec.getSourceAttribute()), 
					target.getValuesByAttribute(spec.getTargetAttribute()));
			
			if(similarity.isNaN()) {
				similarity = 0.0;
			}
			
			result = Math.max(similarity, result);
			
		}
		
		if(result.isNaN()) {
			result = 0.0;
		}
		
		return result;
	}

	@Override
	public String toString() {
		return "max";
	}
	
	
	
}
