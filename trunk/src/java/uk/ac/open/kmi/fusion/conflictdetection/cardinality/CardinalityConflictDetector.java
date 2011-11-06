/**
 * 
 */
package uk.ac.open.kmi.fusion.conflictdetection.cardinality;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Map;

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
import uk.ac.open.kmi.fusion.*;
import uk.ac.open.kmi.fusion.api.*;
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

	
	FusionEnvironment environment;
	String descriptorURI;
	Set<URI> restrictedProperties;
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
		URI propertyInd;
		for(URI prop : restrictedPropertyMap.keySet()) {
			conflictingSet = restrictedPropertyMap.get(prop);
			if(conflictingSet.size()>=2) {
				conflictSet = new ConflictStatementCluster();
				for(Statement conflictingStatement : conflictingSet) {
					conflictSet.addConfictingABoxStatement(conflictingStatement);
				}
				// find relevant TBox statements
				
				try {
					for(Statement stmt : SesameUtils.getStatements(
							prop, 
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
 