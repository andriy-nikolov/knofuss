package uk.ac.open.kmi.fusion.api.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.open.kmi.fusion.api.IAttribute;

public class CompositeAttributeValue {

	Map<IAttribute, List<? extends Object>> attributeValues;
	CompositeAttribute attribute;
	
	public CompositeAttributeValue(CompositeAttribute attribute) {
		this.attribute = attribute;
		attributeValues = new HashMap<IAttribute, List<? extends Object>>();
	}
	
	public static CompositeAttributeValue createCompositeAttributeValueHavingAttributes(CompositeAttribute attribute, Map<IAttribute, List<? extends Object>> valueTable) {
		
		CompositeAttributeValue value = new CompositeAttributeValue(attribute);
		
		List<IAttribute> subAttributes = attribute.getAttributes();
		
		for(IAttribute attr : subAttributes) {
			if(attr instanceof AtomicAttribute) {
				value.attributeValues.put(attr, valueTable.get(attr));
			} else {
				List<CompositeAttributeValue> tmpList = new ArrayList<CompositeAttributeValue>(1);
				CompositeAttributeValue tmpVal = createCompositeAttributeValueHavingAttributes((CompositeAttribute)attr, valueTable);
				tmpList.add(tmpVal);
				value.attributeValues.put(attr, tmpList);
			}
		}
		
		return value;
	}
	
	public static CompositeAttributeValue createCompositeAttributeValueHavingPropertyPaths(CompositeAttribute attribute, Map<String, List<? extends Object>> valueTable) {
		
		CompositeAttributeValue value = new CompositeAttributeValue(attribute);
		
		List<IAttribute> subAttributes = attribute.getAttributes();
		
		for(IAttribute attr : subAttributes) {
			if(attr instanceof AtomicAttribute) {
				value.attributeValues.put(attr, valueTable.get(((AtomicAttribute) attr).getPropertyPath()));
			} else {
				List<CompositeAttributeValue> tmpList = new ArrayList<CompositeAttributeValue>(1);
				CompositeAttributeValue tmpVal = createCompositeAttributeValueHavingPropertyPaths((CompositeAttribute)attr, valueTable);
				tmpList.add(tmpVal);
				value.attributeValues.put(attr, tmpList);
			}
		}
		
		return value;
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();
		
		for(IAttribute attr : attribute.getAttributes()) {
			str.append(attributeValues.get(attr));
			str.append(" ");
		}
		
		return "("+str.toString().trim()+")";
	}

	
	
}
