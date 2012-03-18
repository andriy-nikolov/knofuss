package uk.ac.open.kmi.fusion.api.impl.valuematching;

import java.util.List;

import com.wcohen.ss.MongeElkan;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.ICustomValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.AttributeType;
import uk.ac.open.kmi.fusion.api.impl.CompositeAttribute;
import uk.ac.open.kmi.fusion.api.impl.CompositeAttributeValue;
import uk.ac.open.kmi.fusion.api.impl.ValueMatchingFunctionWrapper;
import uk.ac.open.kmi.fusion.objectidentification.standard.SimMetricsObjectIdentificationUtils;

public class TestEventValueMatchingFunction implements ICustomValueMatchingFunction<CompositeAttributeValue> {

	private static final TestEventValueMatchingFunction INSTANCE = new TestEventValueMatchingFunction(); 
	private static final MongeElkan matcher = new MongeElkan();
	
	private static AttributeType[][] supportedTypes = {
		{ AttributeType.COMPOSITE, AttributeType.COMPOSITE }
		
	};
	
	public TestEventValueMatchingFunction() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public double getSimilarity(IAttribute attr1, IAttribute attr2, CompositeAttributeValue value1, CompositeAttributeValue value2) {
		// return matcher.score(value1.toLowerCase(), value2.toLowerCase());
		
		CompositeAttribute compAttr1 = (CompositeAttribute)attr1;
		CompositeAttribute compAttr2 = (CompositeAttribute)attr2;
		
		compAttr1 = (CompositeAttribute)attr1;
		compAttr2 = (CompositeAttribute)attr2;
		
		List<String> longitudes1 = (List<String>)value1.getAttributeValues().get(compAttr1.getAtomicAttributesByVariable().get("long"));
		List<String> latitudes1 = (List<String>)value1.getAttributeValues().get(compAttr1.getAtomicAttributesByVariable().get("lat"));
		
		List<String> longitudes2 = (List<String>)value2.getAttributeValues().get(compAttr2.getAtomicAttributesByVariable().get("long"));
		List<String> latitudes2 = (List<String>)value2.getAttributeValues().get(compAttr2.getAtomicAttributesByVariable().get("lat"));
		
		if((longitudes1==null)||(latitudes1==null)||(longitudes2==null)||(latitudes2==null)) {
			return 0;
		}
		
		if(longitudes1.isEmpty()||latitudes1.isEmpty()||longitudes2.isEmpty()||latitudes2.isEmpty()) {
			return 0;
		}
		
		double dist = ((Math.abs(Double.parseDouble(longitudes1.get(0))-Double.parseDouble(longitudes2.get(0)))/360)
				+(Math.abs(Double.parseDouble(latitudes1.get(0))-Double.parseDouble(latitudes2.get(0))))/180)/2;
		
		
		return 1-dist;
	}

	static TestEventValueMatchingFunction getInstance() {
		return INSTANCE;
	}

	@Override
	public String toString() {
		return "event-space";
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
		
		if((attribute1 instanceof CompositeAttribute)&&(attribute2 instanceof CompositeAttribute)) {
			return true;
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

	@Override
	public void setFunctionDescriptor(ValueMatchingFunctionWrapper descriptor) {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
