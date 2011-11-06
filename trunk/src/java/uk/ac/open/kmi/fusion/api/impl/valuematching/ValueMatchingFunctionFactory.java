package uk.ac.open.kmi.fusion.api.impl.valuematching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.AttributeType;

public final class ValueMatchingFunctionFactory {

	private static String[] availableFunctionTypes = new String[]{
		IValueMatchingFunction.JARO,
		IValueMatchingFunction.JARO_WINKLER,
		IValueMatchingFunction.LEVENSHTEIN,
		IValueMatchingFunction.MONGE_ELKAN,
		IValueMatchingFunction.I_SUB,
		IValueMatchingFunction.SMITH_WATERMAN,
		IValueMatchingFunction.L2_JARO,
		IValueMatchingFunction.L2_JARO_WINKLER,
		IValueMatchingFunction.L2_LEVENSHTEIN,
		IValueMatchingFunction.L2_MONGE_ELKAN,
		IValueMatchingFunction.L2_SMITH_WATERMAN,
		IValueMatchingFunction.JACCARD,
		IValueMatchingFunction.OVERLAP,
		IValueMatchingFunction.DICE,
		IValueMatchingFunction.ABBREVIATION,
		IValueMatchingFunction.DATE,
		IValueMatchingFunction.DOUBLE,
		//IValueMatchingFunction.AVERAGE_JARO_WINKLER,
	};
	
	private static Map<String, IValueMatchingFunction> pool = new HashMap<String, IValueMatchingFunction>();
	
	static {
		pool.put(IValueMatchingFunction.JARO, JaroValueMatchingFunction.getInstance());
		pool.put(IValueMatchingFunction.JARO_WINKLER, JaroWinklerValueMatchingFunction.getInstance());
		pool.put(IValueMatchingFunction.LEVENSHTEIN, LevenshteinValueMatchingFunction.getInstance());
		pool.put(IValueMatchingFunction.MONGE_ELKAN, MongeElkanValueMatchingFunction.getInstance());
		pool.put(IValueMatchingFunction.I_SUB, ISubValueMatchingFunction.getInstance());
		pool.put(IValueMatchingFunction.SMITH_WATERMAN, SmithWatermanValueMatchingFunction.getInstance());
		pool.put(IValueMatchingFunction.L2_JARO, L2MetaMatchingFunction.getInstance(JaroValueMatchingFunction.getInstance()));
		pool.put(IValueMatchingFunction.L2_JARO_WINKLER, L2MetaMatchingFunction.getInstance(JaroWinklerValueMatchingFunction.getInstance()));
		pool.put(IValueMatchingFunction.L2_LEVENSHTEIN, L2MetaMatchingFunction.getInstance(LevenshteinValueMatchingFunction.getInstance()));
		pool.put(IValueMatchingFunction.L2_SMITH_WATERMAN, L2MetaMatchingFunction.getInstance(MongeElkanValueMatchingFunction.getInstance()));
		pool.put(IValueMatchingFunction.L2_MONGE_ELKAN, L2MetaMatchingFunction.getInstance(SmithWatermanValueMatchingFunction.getInstance()));
		pool.put(IValueMatchingFunction.JACCARD, JaccardValueMatchingFunction.getInstance());
		pool.put(IValueMatchingFunction.OVERLAP, OverlapCoefficientValueMatchingFunction.getInstance());
		// pool.put(IValueMatchingFunction.DICE, DiceCoefficientValueMatchingFunction.getInstance());
		pool.put(IValueMatchingFunction.ABBREVIATION, AbbreviationValueMatchingFunction.getInstance());
		pool.put(IValueMatchingFunction.DATE, DateValueMatchingFunction.getInstance());
		pool.put(IValueMatchingFunction.DOUBLE, DoubleValueMatchingFunction.getInstance());		
		
	}
	
	public static IValueMatchingFunction getInstance(String name) {
		if(pool.containsKey(name)) {
			return pool.get(name);
		} else {
			throw new IllegalArgumentException("Unknown value matching function: "+name);
		}
	}
	
/*	public static IValueMatchingFunction getRandomInstance() {
		IValueMatchingFunction res;
		
		int val = (int)(Math.random()*availableFunctionTypes.length);
		
		res = getInstance(availableFunctionTypes[val]);
		
		return res;
	}*/
	
	public static List<IValueMatchingFunction> getApplicableFunctionsForAttributes(IAttribute attr1, IAttribute attr2) {
		
		Set<IValueMatchingFunction> applicableFunctions = new HashSet<IValueMatchingFunction>();
		IValueMatchingFunction tmp;
		
		for(String key : pool.keySet()) {
			tmp = pool.get(key);
			if(tmp.isSuitableForAttributes(attr1, attr2)) {
				//if(tmp instanceof DoubleValueMatchingFunction) {
					//System.out.println("here");
				//}
				applicableFunctions.add(tmp);
			}
		}
		
		return new ArrayList<IValueMatchingFunction>(applicableFunctions);
		
	}
	
	public static IValueMatchingFunction getRandomInstanceForAttributes(IAttribute attr1, IAttribute attr2) {
		
		IValueMatchingFunction res = null;
		
		List<IValueMatchingFunction> applicableFunctions = getApplicableFunctionsForAttributes(attr1, attr2);
		
		if(applicableFunctions.size()>0) {
			Collections.shuffle(applicableFunctions);
			res = applicableFunctions.get(0);
			return res;
		} 
		return null;
	}
	
}
