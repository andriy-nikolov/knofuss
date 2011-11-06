package uk.ac.open.kmi.fusion.api.impl.valuematching;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.AttributeType;

public class JaccardValueMatchingFunction extends TokenBasedSimilarityValueMatchingFunction {

	private static final JaccardValueMatchingFunction INSTANCE = new JaccardValueMatchingFunction();
	
	private static AttributeType[][] supportedTypes = {
		{ AttributeType.LONG_TEXT, AttributeType.LONG_TEXT },
		{ AttributeType.NOMINAL_MULTI_TOKEN, AttributeType.LONG_TEXT },
		{ AttributeType.NOMINAL_MULTI_TOKEN, AttributeType.NOMINAL_MULTI_TOKEN }
	};
	
	public JaccardValueMatchingFunction() {
		super();
	}

	@Override
	protected double getScore(IAttribute attr1, IAttribute attr2, List<String> list1, List<String> list2) {
		
		Set<String> allTokens = new HashSet<String>(list1);
		Set<String> overlapTokens = new HashSet<String>(list1);
		
		allTokens.addAll(list2);
		overlapTokens.retainAll(list2);
		
		return ((double)overlapTokens.size())/allTokens.size();
	}
	
	static JaccardValueMatchingFunction getInstance() {
		return INSTANCE;
	}

	@Override
	public String toString() {
		return IValueMatchingFunction.JACCARD;
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
