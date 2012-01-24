package uk.ac.open.kmi.fusion.api.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.open.kmi.fusion.api.IAttribute;

public class CompositeAttribute implements IAttribute {

	List<IAttribute> attributes = new ArrayList<IAttribute>();
	Map<String, Object> linkedObjects = new HashMap<String, Object>();
	
	@Override
	public AttributeType getType() {
		return AttributeType.COMPOSITE;
	}
	
	public List<IAttribute> getAttributes() {
		return attributes;
	}

	@Override
	public List<String> getPropertyPaths() {
		List<String> propertyPaths = new ArrayList<String>();
		for(IAttribute attribute : attributes) {
			propertyPaths.addAll(attribute.getPropertyPaths());
		}
		return propertyPaths;
	}
	
	@Override
	public List<String> getPropertyPathsAsQueryTriples() {
		List<String> propertyPaths = new ArrayList<String>();
		for(IAttribute attribute : attributes) {
			propertyPaths.addAll(attribute.getPropertyPathsAsQueryTriples());
		}
		return propertyPaths;
	}
	
	public void addAttribute(IAttribute attribute) {
		attributes.add(attribute);
	}
	
	public static CompositeAttribute createFromPropertyPaths(Map<String, String> propertyPathsByVariables) {
		CompositeAttribute res = new CompositeAttribute();
		AtomicAttribute tmp;
		String path;
		for(String var : propertyPathsByVariables.keySet()) {
			path = propertyPathsByVariables.get(var);
			tmp = new AtomicAttribute(path);
			res.addAttribute(tmp);
		}
		
		return res;
	}

	@Override
	public String writePathAsString() {
		StringBuffer str = new StringBuffer();
		boolean started = false;
		for(IAttribute attribute : attributes) {
			if(started) {
				str.append(" & ");
			} else {
				started = true;
			}
			str.append(attribute.writePathAsString());
		}
		return str.toString();
	}

	@Override
	public List<String> getVariableNames() {
		List<String> varNames = new ArrayList<String>(this.attributes.size());
		for(IAttribute attribute : this.attributes) {
			varNames.addAll(attribute.getVariableNames());
		}
		return varNames;
	}
	
	public Map<String, IAttribute> getAtomicAttributesByVariable() {
		Map<String, IAttribute> res = new HashMap<String, IAttribute>();
		for(IAttribute attribute : attributes) {
			if(attribute instanceof AtomicAttribute) {
				res.put(((AtomicAttribute) attribute).getVariableName(), attribute);
			} else {
				res.putAll(((CompositeAttribute)attribute).getAtomicAttributesByVariable());
			}
		}
		return res;
	}
	
	public Map<String, IAttribute> getAtomicAttributesByPropertyPath() {
		Map<String, IAttribute> res = new HashMap<String, IAttribute>();
		for(IAttribute attribute : attributes) {
			if(attribute instanceof AtomicAttribute) {
				res.put(((AtomicAttribute) attribute).getPropertyPath(), attribute);
			} else {
				res.putAll(((CompositeAttribute)attribute).getAtomicAttributesByPropertyPath());
			}
		}
		return res;
	}
	
	public Map<String, Object> getLinkedObjects() {
		return linkedObjects;
	}
	
	public void addLinkedObject(String key, Object object) {
		this.linkedObjects.put(key, object);
	}
	
}
