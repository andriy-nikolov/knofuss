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
package uk.ac.open.kmi.fusion.learning.tree;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.impl.AbstractObjectContextModel;
import uk.ac.open.kmi.fusion.api.impl.ComparisonPair;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.util.FusionException;
import uk.ac.open.kmi.fusion.util.KnoFussUtils;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;

public class ObjectContextModelTree extends AbstractObjectContextModel {
	
	public static final String TYPE_URI = FusionMetaVocabulary.OBJECT_CONTEXT_MODEL_TREE;

	GenotypeTree genotype;
	
	Logger log = Logger.getLogger(ObjectContextModelTree.class);
	
	public ObjectContextModelTree() {
		super();
		
	}

	@Override
	protected void fillAttributeMaps() {
		// String varName;
		
		// Set<IAttribute> attributes = genotypeTree.getRootNode().getSourceAttributes();
		
		for(IAttribute attr : genotype.getRootNode().getSourceAttributes()) {
			this.sourceAttributesByVarName.putAll(attr.getAtomicAttributesByVariable());
		}
		
		for(IAttribute attr : genotype.getRootNode().getTargetAttributes()) {
			this.targetAttributesByVarName.putAll(attr.getAtomicAttributesByVariable());
		}
	}

	@Override
	public Query generateQuery(boolean isTarget) {
		// List<String> variables = new ArrayList<String>();
		List<String> restrictions = new ArrayList<String>();
		
		
		if(isTarget) {
			restrictions.add(this.applicationContext.getRestrictionTarget());
		} else {
			restrictions.add(this.applicationContext.getRestrictionSource());
		}
		
		List<IAttribute> attributes = new ArrayList<IAttribute>();
		
		if(isTarget) {
			attributes.addAll(genotype.getRootNode().getTargetAttributes());
		} else {
			attributes.addAll(genotype.getRootNode().getSourceAttributes());
		}
		
		String sQuery = KnoFussUtils.generateQuery(restrictions, attributes, FusionEnvironment.getInstance().getNamespaceURITable());
		try {	
			Query result = QueryFactory.create(sQuery);
			return result;
		} catch(QueryParseException e) {
			log.error("Could not parse query: \n "+sQuery, e);
			throw e;
		}

	}

	@Override
	public void readFromRDFIndividual(RepositoryConnection connection)
			throws FusionException {
		super.readFromRDFIndividual(connection);
	}

	@Override
	protected void readFromPropertyMember(Statement statement)
			throws RepositoryException {
		super.readFromPropertyMember(statement);
	}

	public GenotypeTree getGenotype() {
		return genotype;
	}

	public void setGenotypeTree(GenotypeTree genotype) {
		this.genotype = genotype;
	}

	public boolean isEquivalentPair(ComparisonPair pair) {
		return genotype.getRootNode().isEquivalentPair(pair);
	}

	@Override
	public String toString() {
		
		return this.getGenotype().getRootNode().toString();
	}
	
	
	
}
