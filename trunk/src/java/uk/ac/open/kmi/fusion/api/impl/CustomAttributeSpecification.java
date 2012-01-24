package uk.ac.open.kmi.fusion.api.impl;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.fusion.api.IAttribute;

public class CustomAttributeSpecification extends FusionConfigurationObject {

	private IAttribute attribute;
	
	
	public CustomAttributeSpecification() {
	}

	public CustomAttributeSpecification(Resource rdfIndividual,
			FusionEnvironment environment) {
		super(rdfIndividual, environment);
	}

	public IAttribute getAttribute() {
		return attribute;
	}

	public void setAttribute(IAttribute attribute) {
		this.attribute = attribute;
	}

	@Override
	protected void readFromPropertyMember(Statement statement)
			throws RepositoryException {
		super.readFromPropertyMember(statement);
	}
	
	

}
