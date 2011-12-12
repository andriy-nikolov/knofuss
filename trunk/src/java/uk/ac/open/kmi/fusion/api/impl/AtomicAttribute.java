package uk.ac.open.kmi.fusion.api.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.open.kmi.common.utils.sparql.SPARQLUtils;
import uk.ac.open.kmi.fusion.api.IAttribute;

public class AtomicAttribute implements IAttribute {

	String propertyPath;
	AttributeType type;
	
	double min = -180.0;
	double max = 180.0;
	
	String variableName;
	
	static int attributeNumber = 0;
	
	Map<String, Object> linkedObjects;

	public AtomicAttribute(String propertyPath) {
		super();
		this.setPropertyPath(propertyPath);
		this.variableName = "attr"+(attributeNumber++);
		this.linkedObjects = new HashMap<String, Object>();
	}
	
	public AtomicAttribute(String propertyPath, AttributeType type) {
		this(propertyPath);
		this.setType(type);
	}
	
	

	@Override
	public List<String> getPropertyPaths() {
		List<String> propertyPaths = new ArrayList<String>(1);
		propertyPaths.add(propertyPath);
		return propertyPaths;
	}
	
	@Override
	public List<String> getPropertyPathsAsQueryTriples() {
		List<String> propertyPaths = new ArrayList<String>(1);
		propertyPaths.add(SPARQLUtils.expandPath("uri", propertyPath, this.variableName));
		return propertyPaths;
	}

	public String getPropertyPath() {
		return propertyPath;
	}

	public void setPropertyPath(String propertyPath) {
		this.propertyPath = propertyPath;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.fusion.api.impl.IAttribute#getType()
	 */
	@Override
	public AttributeType getType() {
		return type;
	}

	public void setType(AttributeType type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof AtomicAttribute) {
			return this.getPropertyPath().equals(((AtomicAttribute)obj).getPropertyPath());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.propertyPath.hashCode();
	}

	@Override
	public String toString() {
		return this.getPropertyPath();
	}
	
	public double getMin() {
		return min;
	}
	
	public double getMax() {
		return max;
	}
	
	public void setMin(double min) {
		this.min = min;
	}
	
	public void setMax(double max) {
		this.max = max;
	}

	@Override
	public String writePathAsString() {
		return getPropertyPath();
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	@Override
	public List<String> getVariableNames() {
		List<String> varNames = new ArrayList<String>(1);
		varNames.add(this.variableName);
		return varNames;
	}

	public Map<String, Object> getLinkedObjects() {
		return linkedObjects;
	}
	
	public void addLinkedObject(String key, Object object) {
		this.linkedObjects.put(key, object);
	}
	
}
