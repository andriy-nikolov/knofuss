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
package uk.ac.open.kmi.fusion.conflictdetection.cardinality;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.fusion.api.IConflictDetectionMethod;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.ConflictStatementCluster;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.api.impl.FusionMethodWrapper;
import uk.ac.open.kmi.fusion.api.impl.MappingSet;
import uk.ac.open.kmi.fusion.util.SesameUtils;

/**
 * @author an2548
 *
 */
public class CardinalityConflictDetector implements IConflictDetectionMethod {

	
	ApplicationContext context;
	
	/**
	 * 
	 */
	public CardinalityConflictDetector() {
		// TODO Auto-generated constructor stub
	}

	public List<ConflictStatementCluster> discoverAllConflicts(FusionEnvironment onto) {
		List<ConflictStatementCluster> res = new ArrayList<ConflictStatementCluster>();
		for(MappingSet mapping : onto.getMappingSets()) {
			
			res.addAll(this.discoverConflicts(mapping));
			
		}
		return res;
	}

	public List<ConflictStatementCluster> discoverConflicts(MappingSet mapping) {
		List<ConflictStatementCluster> resSet = new ArrayList<ConflictStatementCluster>();
		Map<URI, Set<Statement>> restrictedPropertyMap = new HashMap<URI, Set<Statement>>();
		Set<Statement> conflictingSet;
		
		//IPropertyDao propertyDao;
		//IOntology theOntology;
		//ISession theSession;
		
		addConflictingStatementsToMap(restrictedPropertyMap, FusionEnvironment.getInstance().getFusionRepositoryConnection(), mapping.getCandidateIndividuals());
		addConflictingStatementsToMap(restrictedPropertyMap, FusionEnvironment.getInstance().getMainKbRepositoryConnection(), mapping.getMainKBIndividuals());
		
	
		ConflictStatementCluster conflictSet;
		for(Entry<URI, Set<Statement>> entry : restrictedPropertyMap.entrySet()) {
			conflictingSet = entry.getValue();
			if(conflictingSet.size()>=2) {
				conflictSet = new ConflictStatementCluster();
				for(Statement conflictingStatement : conflictingSet) {
					conflictSet.addConfictingABoxStatement(conflictingStatement);
				}
				// find relevant TBox statements
				
				try {
					for(Statement stmt : SesameUtils.getStatements(
							entry.getKey(), 
							RDF.TYPE, 
							OWL.FUNCTIONALPROPERTY,
							FusionEnvironment.getInstance().getFusionRepositoryConnection())) {
										
								conflictSet.addTBoxAxiom(stmt);
						
						}
				} catch(RepositoryException e) {
					e.printStackTrace();
				}
				resSet.add(conflictSet);
			}
			
		}
	
		return resSet;
	}
	
	public void addConflictingStatementsToMap(Map<URI, Set<Statement>> restrictedPropertyMap, RepositoryConnection con, Set<URI> individuals) {
		
		String functionalQuery;
		TupleQuery query;
		TupleQueryResult result;
		URI property;
		BindingSet tuple;
		Statement stmtToAdd;
		
		for(URI ind : individuals) {
			
			functionalQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
			"PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
			"SELECT ?uri ?propertyUri ?object \n" +
			"WHERE {\n" +
			"?uri ?propertyUri ?object . \n" +
			"?propertyUri rdf:type owl:FunctionalProperty . \n" +
			"FILTER( ?uri = <"+ind.toString()+"> ) \n" +
			"}";
			try {
				query = con.prepareTupleQuery(QueryLanguage.SPARQL, functionalQuery);
				result = query.evaluate();
				try {
					while(result.hasNext()) {
						tuple = result.next();
						property = (URI)tuple.getValue("propertyUri");
						
						stmtToAdd = new StatementImpl(
								(Resource)tuple.getValue("uri"),
								property,
								tuple.getValue("object"));
						
						Utils.addToSetMap(property, stmtToAdd, restrictedPropertyMap);
						
					}
				} finally {
					result.close();
				} 

			} catch(OpenRDFException e) {
				e.printStackTrace();
			} 
		}
		
	}

	public void setApplicationContext(ApplicationContext context) {
		this.context = context;
	}

	public FusionMethodWrapper getDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setMethodDescriptor(FusionMethodWrapper descriptor) {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
 