package uk.ac.open.kmi.fusion.api.impl.valuematching;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.ILuceneBlocker;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.AttributeType;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;

public class L2MetaMatchingFunction extends TokenBasedSimilarityValueMatchingFunction {

	private static AttributeType[][] supportedTypes = {
		{ AttributeType.NOMINAL, AttributeType.NOMINAL_MULTI_TOKEN },
		{ AttributeType.NOMINAL_MULTI_TOKEN, AttributeType.NOMINAL_MULTI_TOKEN }
		//{ AttributeType.NOMINAL_MULTI_TOKEN, AttributeType.LONG_TEXT },
	};
	
	IValueMatchingFunction<String> embeddedFunction;
	
	private L2MetaMatchingFunction(IValueMatchingFunction<String> embeddedFunction) {
		super();
		this.embeddedFunction = embeddedFunction;
	}

	public IValueMatchingFunction<String> getEmbeddedFunction() {
		return embeddedFunction;
	}

	public void setEmbeddedFunction(IValueMatchingFunction<String> embeddedFunction) {
		this.embeddedFunction = embeddedFunction;
	}

	public static L2MetaMatchingFunction getInstance(IValueMatchingFunction<String> embeddedFunction) {
		return new L2MetaMatchingFunction(embeddedFunction);
	}
	
	private double getWeight(IAttribute attr, String term) {
		double weight = 1;
		
		if(attr.getLinkedObjects().containsKey("index")) {
			ILuceneBlocker blocker = (ILuceneBlocker)attr.getLinkedObjects().get("index");
			weight = 1-blocker.getDocumentFrequency(attr.getPropertyPaths().get(0), term);
		}
		
		return 0;
		
	}
	
	protected double getScore(IAttribute attr1, IAttribute attr2, List<String> list1, List<String> list2) {
		
		List<String> small, big;
		List<List<Double>>scores = new ArrayList<List<Double>>();
		if(list1.size()>list2.size()) {
			big = list1;
			small = list2;
		} else {
			big = list2;
			small = list1;
		}
		if(small.size()==0) return 0;
		for(int i=0;i<small.size();i++) {
			List<Double> row = new ArrayList<Double>(big.size());
			for(int j=0;j<big.size();j++) {
				row.add(embeddedFunction.getSimilarity(attr1, attr2, small.get(i).toLowerCase(), big.get(j).toLowerCase()));
			}
			scores.add(row);
		}
		
		double bestscore = 0;
		int maxi, maxj;
		double res = 0;
		double sumweights = 0;
		
		for(int k=0;k<small.size();k++) {
			bestscore = 0;
			maxi = 0;
			maxj = 0;
			for(int i=0;i<scores.size();i++) {
				for(int j=0;j<scores.get(i).size();j++) {
					if(scores.get(i).get(j)>=bestscore) {
						bestscore = scores.get(i).get(j);
						maxi = i;
						maxj = j;
					}
				}
			}
			for(List<Double>tmp : scores) {
				tmp.remove(maxj);
			}
			scores.remove(maxi);
			res+=bestscore;
		}
		res/=small.size();
		
		return res;
	}
		
	@Override
	public String toString() {
		return "l2 " + embeddedFunction.toString();
	}
	
	@Override
	public boolean isSuitableForAttributeTypes(AttributeType attributeType1, AttributeType attributeType2) {
		for(AttributeType[] types : supportedTypes) {
			if(types[0].equals(attributeType1)&&types[1].equals(attributeType2)
					||types[1].equals(attributeType1)&&types[0].equals(attributeType2)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isSuitableForAttributes(IAttribute attribute1, IAttribute attribute2) {
		for(AttributeType[] types : supportedTypes) {
			if(types[0].equals(attribute1.getType())&&types[1].equals(attribute2.getType())
					||types[1].equals(attribute1.getType())&&types[0].equals(attribute2.getType())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public AttributeType[][] suitableForTypes() {
		return supportedTypes;
	}

	@Override
	public void setAttributeFeatures(IAttribute attr1, IAttribute attr2) {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
