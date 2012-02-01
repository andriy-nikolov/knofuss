package uk.ac.open.kmi.fusion.api.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.ITransformationFunction;

public abstract class TransformationAttribute<T> implements IAttribute {

	List<IAttribute> attributes = new ArrayList<IAttribute>();
	ITransformationFunction<T> transformationFunction;
	Map<String, Object> linkedObjects = new HashMap<String, Object>();
	AttributeType attributeType;
	
	public TransformationAttribute(AttributeType type, ITransformationFunction<T> transformationFunction) {
		this.attributeType = type;
		this.transformationFunction = transformationFunction;
	}

	@Override
	public List<String> getVariableNames() {
		List<String> varNames = new ArrayList<String>(this.attributes.size());
		for(IAttribute attribute : this.attributes) {
			varNames.addAll(attribute.getVariableNames());
		}
		return varNames;
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

	@Override
	public String writePathAsString() {
		StringBuffer str = new StringBuffer();
		boolean started = false;
		for(IAttribute attribute : attributes) {
			if(started) {
				str.append(" , ");
			} else {
				started = true;
			}
			str.append(attribute.writePathAsString());
		}
		return str.toString();
	}

	@Override
	public Map<String, Object> getLinkedObjects() {
		return linkedObjects;
	}

	@Override
	public AttributeType getType() {
		return attributeType;
	}

	public ITransformationFunction<T> getTransformationFunction() {
		return transformationFunction;
	}

	public void setTransformationFunction(
			ITransformationFunction<T> transformationFunction) {
		this.transformationFunction = transformationFunction;
	}
	
	

}
