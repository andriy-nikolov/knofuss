package uk.ac.open.kmi.fusion.api.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import uk.ac.open.kmi.fusion.api.IAttribute;

public abstract class TransformationAttribute implements IAttribute {

	List<IAttribute> attributes = new ArrayList<IAttribute>();
	
	public TransformationAttribute() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<String> getVariableNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getPropertyPaths() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getPropertyPathsAsQueryTriples() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String writePathAsString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> getLinkedObjects() {
		// TODO Auto-generated method stub
		return null;
	}

}
