package uk.ac.open.kmi.fusion.api.impl.valuematching;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.AttributeType;
import uk.ac.open.kmi.fusion.objectidentification.standard.SimMetricsObjectIdentificationUtils;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Jaro;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

public class DoubleValueMatchingFunction implements IValueMatchingFunction<String> {

	private static final DoubleValueMatchingFunction INSTANCE = new DoubleValueMatchingFunction();
	
	private static AttributeType[][] supportedTypes = {
		{ AttributeType.CONTINUOUS, AttributeType.CONTINUOUS }
	};
	
	private IAttribute attr1, attr2;
	
	public DoubleValueMatchingFunction() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public double getSimilarity(IAttribute attr1, IAttribute attr2, String value1, String value2) {
		double interval = 360;
		
		double val1 = Double.parseDouble(value1);
		double val2 = Double.parseDouble(value2);
		
		
		return 1-Math.abs(val1-val2)/interval;
	}

	static DoubleValueMatchingFunction getInstance() {
		return INSTANCE;
	}

	@Override
	public String toString() {
		return IValueMatchingFunction.DOUBLE;
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
		this.attr1 = attr1;
		this.attr2 = attr2;
		
	}
	
	
}
