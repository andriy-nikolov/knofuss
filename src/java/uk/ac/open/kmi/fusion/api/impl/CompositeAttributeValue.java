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
	
	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();
		List<? extends Object> valueList;
		// Object obj;
		for(IAttribute attr : attribute.getAttributes()) {
			if(attributeValues.containsKey(attr)) {
				valueList = attributeValues.get(attr);
				if(valueList!=null) {
					for(Object obj : valueList) {
						str.append(obj.toString());
						str.append(" ");
					}
				}
			}
		}
		
		return str.toString().trim();
	}

	public CompositeAttribute getAttribute() {
		return attribute;
	}

	public void setAttribute(CompositeAttribute attribute) {
		this.attribute = attribute;
	}

	public Map<IAttribute, List<? extends Object>> getAttributeValues() {
		return attributeValues;
	}

	
	
}
