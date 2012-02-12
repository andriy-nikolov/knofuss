package uk.ac.open.kmi.fusion.api;

import java.util.List;
import java.util.Map;

import org.openrdf.model.Resource;

import uk.ac.open.kmi.fusion.api.impl.AttributeType;

public interface IAttribute {
	
	public Resource getRDFIndividual();

	public AttributeType getType();
	
	public List<IAttribute> dependsOn();
	
	public List<String> getVariableNames();
	
	public List<String> getPropertyPaths();
	
	public List<String> getPropertyPathsAsQueryTriples();

	public String writePathAsString();

	public Map<String, Object> getLinkedObjects();
	
	public boolean isOptional();

	public boolean isAttributeTypeKnown();

	public Map<String, IAttribute> getAtomicAttributesByPropertyPath();

	public Map<String, IAttribute> getAtomicAttributesByVariable();
	
	public List<? extends Object> getValuesHavingAttributes(Map<IAttribute, List<? extends Object>> valueTable);
	
	public List<? extends Object> getValuesHavingPropertyPaths(Map<String, List<? extends Object>> valueTable);
	
	public String writeSPARQLWhereClause(Map<String, String> namespaceMap);

	public boolean samePropertyPathAs(Object obj);
	
	public void setType(AttributeType type);
}