package uk.ac.open.kmi.fusion.api;

import java.util.List;
import java.util.Map;

import uk.ac.open.kmi.fusion.api.impl.AttributeType;

public interface IAttribute {

	public AttributeType getType();
	
	public List<String> getVariableNames();
	
	public List<String> getPropertyPaths();
	
	public List<String> getPropertyPathsAsQueryTriples();

	public String writePathAsString();

	public Map<String, Object> getLinkedObjects();
	
}