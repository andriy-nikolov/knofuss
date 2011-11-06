package uk.ac.open.kmi.fusion.api.impl.valuematching;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;

public abstract class TokenBasedSimilarityValueMatchingFunction implements
		IValueMatchingFunction<String> {

	protected TokenBasedSimilarityValueMatchingFunction() {
		
	}

	@Override
	public double getSimilarity(IAttribute attr1, IAttribute attr2, String value1, String value2) {
		return getScore(attr1, attr2, value1, value2);
	}
	
	protected double getScore(IAttribute attr1, IAttribute attr2, String s1, String s2) {
		List<String> list1 = tokenize(s1);
		List<String> list2 = tokenize(s2);
		try {
			return getScore(attr1, attr2, list1, list2);
		} catch(AbstractMethodError e) {
			e.printStackTrace();
			getScore(attr1, attr2, list1, list2);
			return 0;
		}
	}
	
	protected abstract double getScore(IAttribute attr1, IAttribute attr2, List<String> list1, List<String> list2);
	
	static private List<String> tokenize(String val) {
		return tokenize(val, " \t\n\r\f:(),-.");
		
	}
	
	static private List<String> tokenize(String val, String chars) {
		List<String> res = new ArrayList<String>();
		String token;
		StringTokenizer tokenizer = new StringTokenizer(val.toLowerCase(), chars);
		while(tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken();
			res.add(token);
		}
		return res;
	}
}
