package uk.ac.open.kmi.fusion.api.impl.valuematching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openjena.atlas.logging.Log;

import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.ICustomValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.AttributeType;
import uk.ac.open.kmi.fusion.api.impl.TransformationAttribute;
import uk.ac.open.kmi.fusion.learning.GeneticAlgorithmObjectIdentificationMethod;

public final class ValueMatchingFunctionFactory {

	private static String[] availableFunctionTypes = new String[]{
		IValueMatchingFunction.JARO,
		IValueMatchingFunction.JARO_WINKLER,
		IValueMatchingFunction.LEVENSHTEIN,
		IValueMatchingFunction.MONGE_ELKAN,
		IValueMatchingFunction.I_SUB,
		IValueMatchingFunction.SMITH_WATERMAN,
		IValueMatchingFunction.L2_JARO,
		IValueMatchingFunction.tokenwise_JARO,
		IValueMatchingFunction.L2_JARO_WINKLER,
		IValueMatchingFunction.L2_LEVENSHTEIN,
		IValueMatchingFunction.L2_MONGE_ELKAN,
		IValueMatchingFunction.L2_SMITH_WATERMAN,
		IValueMatchingFunction.JACCARD,
		IValueMatchingFunction.OVERLAP,
		IValueMatchingFunction.DICE,
		IValueMatchingFunction.tokenwise_JARO_WINKLER,
		IValueMatchingFunction.tokenwise_LEVENSHTEIN,
		IValueMatchingFunction.tokenwise_SMITH_WATERMAN,
		// IValueMatchingFunction.ABBREVIATION,
		IValueMatchingFunction.DATE,
		IValueMatchingFunction.DOUBLE,
		//IValueMatchingFunction.AVERAGE_JARO_WINKLER,
	};



	private static Map<String, IValueMatchingFunction<? extends Object>> pool = new HashMap<String, IValueMatchingFunction<? extends Object>>();

	private static Logger log = Logger.getLogger(ValueMatchingFunctionFactory.class);

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
		// pool.put(IValueMatchingFunction.ABBREVIATION, AbbreviationValueMatchingFunction.getInstance());
		pool.put(IValueMatchingFunction.DATE, DateValueMatchingFunction.getInstance());
		pool.put(IValueMatchingFunction.DOUBLE, DoubleValueMatchingFunction.getInstance());
		pool.put(IValueMatchingFunction.tokenwise_JARO, TokenWiseValueMatchingFunction.getInstance(JaroValueMatchingFunction.getInstance()));
		pool.put(IValueMatchingFunction.tokenwise_JARO_WINKLER, TokenWiseValueMatchingFunction.getInstance(JaroWinklerValueMatchingFunction.getInstance()));
		pool.put(IValueMatchingFunction.tokenwise_LEVENSHTEIN, TokenWiseValueMatchingFunction.getInstance(LevenshteinValueMatchingFunction.getInstance()));
		pool.put(IValueMatchingFunction.tokenwise_SMITH_WATERMAN, TokenWiseValueMatchingFunction.getInstance(SmithWatermanValueMatchingFunction.getInstance()));
	}



	public static IValueMatchingFunction<? extends Object> getInstance(String name) {
		if(pool.containsKey(name)) {
			return pool.get(name);
		} else {
			throw new IllegalArgumentException("Unknown value matching function: "+name);
		}
	}

	public static void addToPool(ICustomValueMatchingFunction<? extends Object> function) {
		pool.put(function.toString(), function);
	}

/*	public static IValueMatchingFunction getRandomInstance() {
		IValueMatchingFunction res;

		int val = (int)(Math.random()*availableFunctionTypes.length);

		res = getInstance(availableFunctionTypes[val]);

		return res;
	}*/

	public static List<IValueMatchingFunction<? extends Object>> getApplicableFunctionsForAttributes(IAttribute attr1, IAttribute attr2) {

		Set<IValueMatchingFunction<? extends Object>> applicableFunctions = new HashSet<IValueMatchingFunction<? extends Object>>();
		IValueMatchingFunction<? extends Object> tmp;

		if((attr1 instanceof TransformationAttribute)&&(attr2 instanceof TransformationAttribute)) {
			log.info("here");
		}

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

	}

	public static IValueMatchingFunction<? extends Object> getRandomInstanceForAttributes(IAttribute attr1, IAttribute attr2) {

		IValueMatchingFunction<? extends Object> res = null;

		List<IValueMatchingFunction<? extends Object>> applicableFunctions = getApplicableFunctionsForAttributes(attr1, attr2);

		if(applicableFunctions.size()>0) {
			Collections.shuffle(applicableFunctions);
			res = applicableFunctions.get(0);
			return res;
		}
		return null;
	}

}
