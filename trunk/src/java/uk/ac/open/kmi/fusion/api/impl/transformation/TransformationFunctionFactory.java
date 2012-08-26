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
		ITransformationFunction.CONCATENATE
	};
	
	private static Map<String, ITransformationFunction<? extends Object>> pool = new HashMap<String, ITransformationFunction<? extends Object>>();
	
	static {
		
		pool.put(ITransformationFunction.CONCATENATE, ConcatenateAttributesTransformationFunction.getInstance());
	}
	
	public static boolean hasInstance(String name) {
		if(pool.containsKey(name)) {
			return true;
		} else {
			return false;
		}
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
	

}
