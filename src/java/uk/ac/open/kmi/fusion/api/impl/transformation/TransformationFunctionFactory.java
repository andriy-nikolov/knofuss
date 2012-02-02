package uk.ac.open.kmi.fusion.api.impl.transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.ICustomTransformationFunction;
import uk.ac.open.kmi.fusion.api.ITransformationFunction;
import uk.ac.open.kmi.fusion.api.impl.AttributeType;

public final class TransformationFunctionFactory {

	
	private static String[] availableFunctionTypes = new String[]{
		
	};
	
	private static Map<String, ITransformationFunction<? extends Object>> pool = new HashMap<String, ITransformationFunction<? extends Object>>();
	
	static {
		
		
	}
	
	public static ITransformationFunction<? extends Object> getInstance(String name) {
		if(pool.containsKey(name)) {
			return pool.get(name);
		} else {
			throw new IllegalArgumentException("Unknown value matching function: "+name);
		}
	}
	
	public static void addToPool(ICustomTransformationFunction<? extends Object> function) {
		pool.put(function.toString(), function);
	}
	
/*	public static IValueMatchingFunction getRandomInstance() {
		IValueMatchingFunction res;
		
		int val = (int)(Math.random()*availableFunctionTypes.length);
		
		res = getInstance(availableFunctionTypes[val]);
		
		return res;
	}*/
	
	/*public static List<ITransformationFunction<? extends Object>> getApplicableFunctionsForAttributes(IAttribute attr1, IAttribute attr2) {
		
		Set<ITransformationFunction<? extends Object>> applicableFunctions = new HashSet<IValueMatchingFunction<? extends Object>>();
		ITransformationFunction<? extends Object> tmp;
		
		for(String key : pool.keySet()) {
			tmp = pool.get(key);
			if(tmp.isSuitableForAttributes(attr1, attr2)) {
				//if(tmp instanceof DoubleValueMatchingFunction) {
					//System.out.println("here");
				//}
				applicableFunctions.add(tmp);
			}
		}
		
		return new ArrayList<IValueMatchingFunction<? extends Object>>(applicableFunctions);
		
	}*/
	
	/*public static IValueMatchingFunction<? extends Object> getRandomInstanceForAttributes(IAttribute attr1, IAttribute attr2) {
		
		IValueMatchingFunction<? extends Object> res = null;
		
		List<IValueMatchingFunction<? extends Object>> applicableFunctions = getApplicableFunctionsForAttributes(attr1, attr2);
		
		if(applicableFunctions.size()>0) {
			Collections.shuffle(applicableFunctions);
			res = applicableFunctions.get(0);
			return res;
		} 
		return null;
	}*/
	
}
