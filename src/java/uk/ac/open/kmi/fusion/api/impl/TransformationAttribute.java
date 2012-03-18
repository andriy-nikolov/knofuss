package uk.ac.open.kmi.fusion.api.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.ITransformationFunction;
import uk.ac.open.kmi.fusion.api.impl.transformation.TransformationFunctionFactory;

public class TransformationAttribute extends AbstractAttribute {

	public static final String TYPE_URI = FusionMetaVocabulary.TRANSFORMATION_ATTRIBUTE;
	
	List<IAttribute> attributes = new ArrayList<IAttribute>();
	ITransformationFunction transformationFunction;
	Map<String, Object> linkedObjects = new HashMap<String, Object>();
	
	public TransformationAttribute(AttributeType type, ITransformationFunction transformationFunction) {
		this.type = type;
		this.transformationFunction = transformationFunction;
	}
	
	public TransformationAttribute(Resource rdfIndividual,
			FusionEnvironment environment) {
		super(rdfIndividual, environment);
	}

	@Override
	protected void readFromPropertyMember(Statement statement)
			throws RepositoryException {
		super.readFromPropertyMember(statement);
		Literal lit;
		Resource res;
		if(statement.getPredicate().toString().equals(FusionMetaVocabulary.HAS_ATTRIBUTE)) {
			if(statement.getObject() instanceof Resource) {
				res = (Resource)statement.getObject();
				AbstractAttribute attr = (AbstractAttribute)FusionEnvironment.getInstance().findConfigurationObjectByID(res);
				this.attributes.add(attr);
			}
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.HAS_TRANSFORMATION_FUNCTION)) {
			if(statement.getObject() instanceof Literal) {
				lit = (Literal)statement.getObject();
				if(TransformationFunctionFactory.hasInstance(lit.stringValue())) {
					this.transformationFunction = TransformationFunctionFactory.getInstance(lit.stringValue());
				}
			}
		}
		
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
		return type;
	}

	public ITransformationFunction getTransformationFunction() {
		return transformationFunction;
	}

	public void setTransformationFunction(
			ITransformationFunction transformationFunction) {
		this.transformationFunction = transformationFunction;
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
	public Map<String, IAttribute> getAtomicAttributesByVariable() {
		Map<String, IAttribute> res = new HashMap<String, IAttribute>();
		for(IAttribute attribute : attributes) {
			res.putAll(attribute.getAtomicAttributesByVariable());
		}
		return res;
	}
	
	@Override
	public Map<String, IAttribute> getAtomicAttributesByPropertyPath() {
		Map<String, IAttribute> res = new HashMap<String, IAttribute>();
		for(IAttribute attribute : attributes) {
			res.putAll(attribute.getAtomicAttributesByPropertyPath());
		}
		return res;
	}
	
	
	
	@Override
	public List<? extends Object> getValuesHavingAttributes(
			Map<IAttribute, List<? extends Object>> valueTable) {
		
		Map<IAttribute, List<? extends Object>> rawValues = new HashMap<IAttribute, List<? extends Object>>();
		for(IAttribute attr : attributes) {
			rawValues.put(attr, attr.getValuesHavingAttributes(valueTable));
		}
		
		return transformationFunction.getTransformationResult(this, rawValues);
	}

	@Override
	public List<? extends Object> getValuesHavingPropertyPaths(
			Map<String, List<? extends Object>> valueTable) {
		Map<IAttribute, List<? extends Object>> rawValues = new HashMap<IAttribute, List<? extends Object>>();
		for(IAttribute attr : attributes) {
			rawValues.put(attr, attr.getValuesHavingPropertyPaths(valueTable));
		}
		
		return transformationFunction.getTransformationResult(this, rawValues);
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
	
	

	public List<IAttribute> getAttributes() {
		return attributes;
	}

	@Override
	public boolean samePropertyPathAs(Object obj) {
		if(!(obj instanceof TransformationAttribute)) {
			return false;
		}
		
		if(!this.transformationFunction.equals(((TransformationAttribute)obj).getTransformationFunction())) {
			return false;
		}
		
		List<IAttribute> attrs = ((TransformationAttribute)obj).getAttributes();
		if(attrs.size()!=this.attributes.size()) {
			return false;
		}
		for(int i = 0;i<this.attributes.size();i++) {
			if(!this.attributes.get(i).samePropertyPathAs(attrs.get(i))) {
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
	
}
