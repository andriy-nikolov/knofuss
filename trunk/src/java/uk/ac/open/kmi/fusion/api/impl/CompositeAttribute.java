package uk.ac.open.kmi.fusion.api.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.common.utils.sparql.SPARQLUtils;
import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.util.FusionException;

public class CompositeAttribute extends AbstractAttribute {

	public static final String TYPE_URI = FusionMetaVocabulary.COMPOSITE_ATTRIBUTE;
	
	List<IAttribute> attributes = new ArrayList<IAttribute>();
	Map<String, Object> linkedObjects = new HashMap<String, Object>();
	
	List<String> variablesWithoutAttributes = new ArrayList<String>();
	List<String> propertyPathsWithoutAttributes = new ArrayList<String>();
	
	public CompositeAttribute() {
		super();
		this.attributeTypeKnown = true;
	}
	
	public CompositeAttribute(Resource rdfIndividual,
			FusionEnvironment environment) {
		super(rdfIndividual, environment);
	}
	
	
	public CompositeAttributeValue createCompositeAttributeValueHavingAttributes(Map<IAttribute, List<? extends Object>> valueTable) {
		
		CompositeAttributeValue value = new CompositeAttributeValue(this);
		for(IAttribute attr : attributes) {
				value.getAttributeValues().put(attr, attr.getValuesHavingAttributes(valueTable));
		}
		return value;
	}
	
	@Override
	protected void readFromPropertyMember(Statement statement)
			throws RepositoryException {
		super.readFromPropertyMember(statement);
		Literal lit;
		Resource res;
		if(statement.getPredicate().toString().equals(FusionMetaVocabulary.PROPERTY_PATH)) {
			if(statement.getObject() instanceof Literal) {
				lit = (Literal)statement.getObject();
				this.propertyPathsWithoutAttributes.add(lit.stringValue());
				// this.propertyPath = lit.stringValue();
			}
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.VARIABLE)) {
			if(statement.getObject() instanceof Literal) {
				lit = (Literal)statement.getObject();
				this.variablesWithoutAttributes.add(lit.stringValue());
			}
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.HAS_ATTRIBUTE)) {
			if(statement.getObject() instanceof Resource) {
				res = (Resource)statement.getObject();
				AbstractAttribute attr = (AbstractAttribute)FusionEnvironment.getInstance().findConfigurationObjectByID(res);
				this.attributes.add(attr);
			}
		}
		
	}

	@Override
	public void readFromRDFIndividual(RepositoryConnection connection)
			throws FusionException {
		super.readFromRDFIndividual(connection);
		
		AtomicAttribute attr;
		for(String var : this.variablesWithoutAttributes) {
			for(String propertyPath : propertyPathsWithoutAttributes) {
				if(propertyPath.contains(var)) {
					attr = new AtomicAttribute(SPARQLUtils.presentExpandedTriplesAsPath(propertyPath, "uri", var, FusionEnvironment.getInstance().getNamespaceURITable()));
					attr.setVariableName(var);
					attributes.add(attr);
				}
			}
		}
		
	}

	public CompositeAttributeValue createCompositeAttributeValueHavingPropertyPaths(Map<String, List<? extends Object>> valueTable) {
		
		CompositeAttributeValue value = new CompositeAttributeValue(this);
		for(IAttribute attr : attributes) {
				value.getAttributeValues().put(attr, attr.getValuesHavingPropertyPaths(valueTable));
		}
		return value;
	}
	
	@Override
	public List<? extends Object> getValuesHavingAttributes(
			Map<IAttribute, List<? extends Object>> valueTable) {
		List<CompositeAttributeValue> tmpList = new ArrayList<CompositeAttributeValue>(1);
		CompositeAttributeValue tmpVal = createCompositeAttributeValueHavingAttributes(valueTable);
		tmpList.add(tmpVal);
		return tmpList;
	}

	@Override
	public List<? extends Object> getValuesHavingPropertyPaths(
			Map<String, List<? extends Object>> valueTable) {
		List<CompositeAttributeValue> tmpList = new ArrayList<CompositeAttributeValue>(1);
		CompositeAttributeValue tmpVal = createCompositeAttributeValueHavingPropertyPaths(valueTable);
		tmpList.add(tmpVal);
		return tmpList;
	}

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
			tmp.setVariableName(var);
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

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public boolean isAttributeTypeKnown() {
		return false;
	}

	@Override
	public String writeSPARQLWhereClause(Map<String, String> namespaceMap) {
		
		StringBuffer buffer = new StringBuffer();
		if(this.isOptional()) {
			buffer.append("OPTIONAL { ");
		}
		
		for(IAttribute attribute : attributes) {
			buffer.append(attribute.writeSPARQLWhereClause(namespaceMap));
			buffer.append(" ");
		}
	
		if(this.isOptional()) {
			buffer.append(" } ");
		} 
		buffer.append("\n");
		return buffer.toString();
		
	
	}

	@Override
	public boolean samePropertyPathAs(Object obj) {
		if(!(obj instanceof CompositeAttribute)) {
			return false;
		}
		
		List<IAttribute> attrs = ((CompositeAttribute)obj).getAttributes();
		if(attrs.size()!=this.attributes.size()) {
			return false;
		}
		for(int i = 0;i<this.attributes.size();i++) {
			if(!this.attributes.get(i).equals(attrs.get(i))) {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public List<IAttribute> dependsOn() {
		List<IAttribute> list = new ArrayList<IAttribute>();
		
		for(IAttribute attr: attributes) {
			if(attr instanceof AtomicAttribute) {
				list.add(attr);
			} else {
				list.addAll(attr.dependsOn());
			}
			
		}
		
		return list;
	}

	@Override
	public void setType(AttributeType type) {
		
	}
	
	
	
}
