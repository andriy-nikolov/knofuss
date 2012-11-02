/* Copyright (c) 2012, Knowledge Media Institute
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   * Neither the name of the <organization> nor the
 *     names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
