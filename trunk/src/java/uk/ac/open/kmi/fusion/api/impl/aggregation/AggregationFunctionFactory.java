package uk.ac.open.kmi.fusion.api.impl.aggregation;

import java.util.HashMap;
import java.util.Map;

import uk.ac.open.kmi.fusion.api.IAggregationFunction;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;

public final class AggregationFunctionFactory {

	private static String[] availableFunctionTypes = new String[]{
		IAggregationFunction.AVERAGE,
		IAggregationFunction.MAX
	};
	
	private static Map<String, IAggregationFunction> pool = new HashMap<String, IAggregationFunction>();
	
	static {
		pool.put(IAggregationFunction.AVERAGE, AverageAggregationFunction.getInstance());
		pool.put(IAggregationFunction.MAX, MaxAggregationFunction.getInstance());
	}
	
	private AggregationFunctionFactory() {
		// TODO Auto-generated constructor stub
	}
	
	public static IAggregationFunction getInstance(String name) {
		if(pool.containsKey(name)) {
			return pool.get(name);
		} else {
			throw new IllegalArgumentException("Unknown aggregation function: "+name);
		}
	}
	
	public static IAggregationFunction getRandomInstance() {
		IAggregationFunction res;
		
		int val = (int)(Math.random()*availableFunctionTypes.length);
		
		res = getInstance(availableFunctionTypes[val]);
		
		return res;
	}

}
