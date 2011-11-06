package uk.ac.open.kmi.fusion.api.impl;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;


import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.util.FusionException;
import uk.ac.open.kmi.fusion.util.SesameUtils;

import java.util.Set;

public abstract class FusionConfigurationObject {

	protected Resource rdfIndividual;
	protected String comment;
	protected String uri;
	public FusionEnvironment environment;
	
	public static final String TYPE_URI = FusionMetaVocabulary.FUSION_CONFIGURATION_OBJECT;
	
	protected final void addCommentToOntology() {
		try {
			FusionEnvironment.getInstance().getFusionRepositoryConnection().add(
					rdfIndividual,	
					RDFS.COMMENT, 
					FusionEnvironment.getInstance().getFusionKbValueFactory().createLiteral(comment));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public FusionConfigurationObject() {
		this.comment = "";
	}
	
	public FusionConfigurationObject(Resource rdfIndividual, FusionEnvironment environment) {
		this();
		this.setRDFIndividual(rdfIndividual);
		this.setEnvironment(environment);
	}

	public Resource getRDFIndividual() {
		return rdfIndividual;
	}

	public void setRDFIndividual(Resource omsIndividual) {
		this.rdfIndividual = omsIndividual;
	}

	/*public void readFromRDFIndividual(Resource rdfIndividual, RepositoryConnection connection) throws RepositoryException {
		this.rdfIndividual = rdfIndividual;
		this.uri = this.rdfIndividual.toString();
		for(Statement statement : SesameUtils.getStatements(this.rdfIndividual, null, null, connection)) {
			readFromPropertyMember(statement);
		}
	}*/
	
	public void readFromRDFIndividual(RepositoryConnection connection) throws FusionException {
		try {
			for(Statement statement : SesameUtils.getStatements(this.rdfIndividual, null, null, connection)) {
				readFromPropertyMember(statement);
			}
		} catch(RepositoryException e) {
			throw new FusionException("Could not read the properties of the configuration object: "+this.rdfIndividual.toString());
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.fusion.api.impl.IFusionOntologyObject#getComment()
	 */
	public String getComment() {
		return comment;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.fusion.api.impl.IFusionOntologyObject#setComment(java.lang.String)
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	protected void readFromPropertyMember(Statement statement) throws RepositoryException {
		if(statement.getPredicate().equals(RDFS.COMMENT)) {
			this.setComment(statement.getObject().stringValue());
		}
	}
	
	
	public FusionEnvironment getEnvironment() {
		return environment;
	}

	public void setEnvironment(FusionEnvironment environment) {
		this.environment = environment;
	}
	
}
