package uk.ac.open.kmi.fusion.api.impl;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.IAttribute;

public abstract class AbstractAttribute extends FusionConfigurationObject implements IAttribute {

	protected AttributeType type;
	protected boolean attributeTypeKnown = false;
	boolean optional = false;
	
	protected static Map<String, AttributeType> attributeTypesByName = new HashMap<String, AttributeType>();
	
	static {
		attributeTypesByName.put("string", AttributeType.NOMINAL_MULTI_TOKEN);
		attributeTypesByName.put("nominal", AttributeType.NOMINAL);
		attributeTypesByName.put("double", AttributeType.CONTINUOUS);
		attributeTypesByName.put("composite", AttributeType.COMPOSITE);
		attributeTypesByName.put("text", AttributeType.LONG_TEXT);
		attributeTypesByName.put("date", AttributeType.DATE);
		attributeTypesByName.put("integer", AttributeType.INTEGER);
	}

	public AbstractAttribute() {
		super();
	}

	public AbstractAttribute(Resource rdfIndividual,
			FusionEnvironment environment) {
		super(rdfIndividual, environment);
	}

	@Override
	protected void readFromPropertyMember(Statement statement)
			throws RepositoryException {
				super.readFromPropertyMember(statement);
				Literal lit;
				if(statement.getPredicate().toString().equals(FusionMetaVocabulary.OPTIONAL)) {
					if(statement.getObject() instanceof Literal) {
						lit = (Literal)statement.getObject();
						this.optional = lit.booleanValue();
					}
				} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.ATTRIBUTE_TYPE)) {
					if(statement.getObject() instanceof Literal) {
						lit = (Literal)statement.getObject();
						String attrTypeStringValue = lit.stringValue();
						if(attributeTypesByName.containsKey(attrTypeStringValue)) {
							this.setType(attributeTypesByName.get(attrTypeStringValue));
						}
					}
				}   
				
			}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public boolean isAttributeTypeKnown() {
		return this.attributeTypeKnown;
	}

	@Override
	public AttributeType getType() {
		return type;
	}

	public void setType(AttributeType type) {
		this.type = type;
		this.attributeTypeKnown = true;
	}


}