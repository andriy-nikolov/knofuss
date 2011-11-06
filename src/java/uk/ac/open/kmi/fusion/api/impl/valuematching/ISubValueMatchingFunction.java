package uk.ac.open.kmi.fusion.api.impl.valuematching;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.AttributeType;
import uk.ac.open.kmi.fusion.objectidentification.standard.SimMetricsObjectIdentificationUtils;

public class ISubValueMatchingFunction implements IValueMatchingFunction<String> {

	private static final ISubValueMatchingFunction INSTANCE = new ISubValueMatchingFunction();
	
	private static AttributeType[][] supportedTypes = {
		{ AttributeType.NOMINAL, AttributeType.NOMINAL },
		{ AttributeType.NOMINAL, AttributeType.NOMINAL_MULTI_TOKEN },
		{ AttributeType.NOMINAL_MULTI_TOKEN, AttributeType.NOMINAL_MULTI_TOKEN }
	};
	
	public ISubValueMatchingFunction() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public double getSimilarity(IAttribute attr1, IAttribute attr2, String value1, String value2) {
		return SimMetricsObjectIdentificationUtils.getDirectISub(value1, value2);
	}

	static ISubValueMatchingFunction getInstance() {
		return INSTANCE;
	}

	@Override
	public String toString() {
		return IValueMatchingFunction.I_SUB;
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
