package uk.ac.open.kmi.fusion.api.impl.aggregation;

import java.util.List;
import org.apache.log4j.Logger;
import uk.ac.open.kmi.fusion.api.IAggregationFunction;
import uk.ac.open.kmi.fusion.api.IObjectContextWrapper;
import uk.ac.open.kmi.fusion.api.impl.AtomicAttribute;
import uk.ac.open.kmi.fusion.api.impl.VariableComparisonSpecification;

public class AverageAggregationFunction implements IAggregationFunction {

	private static final AverageAggregationFunction INSTANCE = new AverageAggregationFunction();
	
	private static Logger log = Logger.getLogger(AverageAggregationFunction.class);
	
	private AverageAggregationFunction() {
		// TODO Auto-generated constructor stub
	}
		
	public static AverageAggregationFunction getInstance() {
		return INSTANCE;
	}

	@Override
	public double calculate(IObjectContextWrapper source,
			IObjectContextWrapper target,
			List<VariableComparisonSpecification> variableComparisons) {
		
		Double result = 0.0;
		double sumWeights = 0;
		Double similarity;
		
		for(VariableComparisonSpecification spec : variableComparisons) {
			
			
			
			List<? extends Object> sourceValues = source.getValuesByAttribute(spec.getSourceAttribute());
			List<? extends Object> targetValues = target.getValuesByAttribute(spec.getTargetAttribute());
			
			/*if((sourceValues==null)||(targetValues==null)) {
				return 0.0;
			}*/
			/*if(spec.getSourcePath().contains("tmp")
					&&spec.getTargetPath().contains("http://xmlns.com/foaf/0.1/name")) {
				if(sourceValues!=null) {
				//	log.debug("here");
				}
			}*/
			

			similarity = spec.getSimilarity(
					sourceValues, targetValues);
			
			/*if(spec.getSourcePath().contains("tmp")
					&&spec.getTargetPath().contains("http://xmlns.com/foaf/0.1/name")
					&&similarity>0.5) {
					log.info("Achtung!!! "+sourceValues.toString()+", "+targetValues.toString());
			}*/
			
			if(similarity.isNaN()) {
				similarity = 0.0;
			}
			
			if(spec.getSourceAttribute() instanceof AtomicAttribute) {
				if(((AtomicAttribute)spec.getSourceAttribute()).getPropertyPath().contains("atPlace")) {
					if(similarity>=0.6) {
						log.debug("here");
					}
					if(!targetValues.isEmpty()) {
						log.debug("here");
					}
				}
			}
			
			result += similarity*spec.getWeight();
			sumWeights += spec.getWeight();
			
		}
		
		result = result/sumWeights;
		
		if(result>=0.7499999) {
			log.debug("here");
		}
		
		if(result.isNaN()) {
			result = 0.0;
		}
		
		return result;
	}

	@Override
	public String toString() {
		return "average";
	}
	
	
	
}
