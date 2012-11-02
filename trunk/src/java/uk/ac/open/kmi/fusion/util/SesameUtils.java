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
package uk.ac.open.kmi.fusion.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;

public abstract class SesameUtils {
	
	public static boolean isReasoningOn = true;
	
	public static Set<URI> getNamedTypes(Resource individual, RepositoryConnection con) throws RepositoryException {
				
		return getNamedTypes(individual, con, isReasoningOn);
		
	}
	
	public static Set<URI> findAllNamedConcepts(RepositoryConnection con) throws RepositoryException {
		Set<URI> answer = new HashSet<URI>();
		try {
			String sQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
					"PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
					"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
					"SELECT ?uri WHERE { \n" +
					"{ ?uri rdf:type owl:Class . } \n" +
					"UNION \n" +
					"{?uri rdf:type rdfs:Class . } \n" +
					"}";
			
			TupleQuery tq = con.prepareTupleQuery(QueryLanguage.SPARQL, sQuery);
			
			TupleQueryResult queryResult = tq.evaluate();
			BindingSet bs;
			
			while(queryResult.hasNext()) {
				bs = queryResult.next();
				if(bs.getValue("uri") instanceof URI) {
					answer.add((URI)bs.getValue("uri"));
				}
			}
			
		} catch(MalformedQueryException e) {
			e.printStackTrace();
		} catch(QueryEvaluationException e) {
			e.printStackTrace();
		}
		
		return answer;
		
	}
	
	public static Set<URI> getNamedTypes(Resource individual, RepositoryConnection con, boolean isReasoningOn) throws RepositoryException {
		Set<URI> results = new HashSet<URI>();
		
		RepositoryResult<Statement> resultSet = con.getStatements(individual, RDF.TYPE, null, isReasoningOn);
		
		Statement stmt;
		Value obj;
		URI type;
		try {
			while(resultSet.hasNext()) {
				stmt = resultSet.next();
				obj = stmt.getObject();
				if(obj instanceof URI) {
					type = (URI)obj;
					if(!results.contains(type)) {
						results.add(type);
					}
				}
			}
		} finally {
			resultSet.close();
		}
		
		return results;
		
	}
	
	public static Set<URI> getNamedSuperconcepts(Resource concept, RepositoryConnection con) throws RepositoryException {
		return getNamedSuperconcepts(concept, con, isReasoningOn);
	}
	
	public static Set<URI> getNamedSuperconcepts(Resource concept, RepositoryConnection con, boolean isReasoningOn) throws RepositoryException {
		Set<URI> results = new HashSet<URI>();
		
		RepositoryResult<Statement> resultSet = con.getStatements(concept, RDFS.SUBCLASSOF, null, isReasoningOn);
		
		Statement stmt;
		Value obj;
		URI type;
		
		try {
			while(resultSet.hasNext()) {
				stmt = resultSet.next();
				obj = stmt.getObject();
				if(obj instanceof URI) {
					type = (URI)obj;
					if((!results.contains(type))
							&&(!type.equals(concept))) {
						results.add(type);
					}
				}
			}
		} finally {
			resultSet.close();
		}
		
		return results;
		
	}
	
	public static Set<URI> getNamedSubconcepts(Resource concept, RepositoryConnection con) throws RepositoryException {
		return getNamedSubconcepts(concept, con, isReasoningOn);
	}
	
	public static Set<URI> getNamedSubconcepts(Resource concept, RepositoryConnection con, boolean isReasoningOn) throws RepositoryException {
		Set<URI> results = new HashSet<URI>();
		
		RepositoryResult<Statement> resultSet = con.getStatements(null, RDFS.SUBCLASSOF, concept, isReasoningOn);
		
		Statement stmt;
		Value obj;
		URI type;
		
		try {
			while(resultSet.hasNext()) {
				stmt = resultSet.next();
				obj = stmt.getSubject();
				if(obj instanceof URI) {
					type = (URI)obj;
					if((!results.contains(type))
							&&(!type.equals(concept))) {
						results.add(type);
					}
				}
			}
		} finally {
			resultSet.close();
		}
		
		return results;
		
	}
	
	
	public static List<Statement> getStatements(Resource subj, URI pred, Value obj, RepositoryConnection con) throws RepositoryException {
		
		return con.getStatements(subj, pred, obj, isReasoningOn).asList();
		
	}
	
	public static List<Statement> getObjectPropertyStatements(Resource subj, URI pred, Value obj, RepositoryConnection con, boolean isReasoningOn) throws RepositoryException {
		
		List<Statement> res = new ArrayList<Statement>();
		List<Statement> tmp = con.getStatements(subj, pred, obj, isReasoningOn).asList();
		for(Statement stmt : tmp) {
			if(stmt.getObject() instanceof Resource) {
				res.add(stmt);
			}
		}
		return res;
	}
	
	public static List<Statement> getDatatypePropertyStatements(Resource subj, URI pred, Value obj, RepositoryConnection con, boolean isReasoningOn) throws RepositoryException {
		
		List<Statement> res = new ArrayList<Statement>();
		List<Statement> tmp = con.getStatements(subj, pred, obj, isReasoningOn).asList();
		for(Statement stmt : tmp) {
			if(stmt.getObject() instanceof Literal) {
				res.add(stmt);
			}
		}
		return res;
	}
	
	public static List<Statement> getDatatypePropertyStatements(Resource subj, URI pred, Value obj, RepositoryConnection con) throws RepositoryException {
		return getDatatypePropertyStatements(subj, pred, obj, con, isReasoningOn);
	}
	
	public static List<Statement> getObjectPropertyStatements(Resource subj, URI pred, Value obj, RepositoryConnection con) throws RepositoryException {
		return getObjectPropertyStatements(subj, pred, obj, con, isReasoningOn);
	}
	
	public static List<Statement> getStatements(Resource subj, URI pred, Value obj, RepositoryConnection con, boolean isReasoningOn) throws RepositoryException {
		
		return con.getStatements(subj, pred, obj, isReasoningOn).asList();
		
	}
	
	public static Set<Value> getPropertyValues(Resource subj, URI pred, RepositoryConnection con) throws RepositoryException {
		
		
		return getPropertyValues(subj, pred, con, isReasoningOn);
	}
	
	public static Set<Value> getPropertyValues(Resource subj, URI pred, RepositoryConnection con, boolean isReasoningOn) throws RepositoryException {
		Set<Value> result = new HashSet<Value>();
		List<Statement> stmts = getStatements(subj, pred, null, con);
		
		for(Statement stmt : stmts) {
			result.add(stmt.getObject());
		}
		
		return result;
	}
	
	public static String getLabel(Resource res, RepositoryConnection con) throws RepositoryException {
		Set<Value> vals = getPropertyValues(res, RDFS.LABEL, con, true);
		if(vals.size()==0) {
			vals.addAll(getPropertyValues(res, con.getValueFactory().createURI("http://xmlns.com/foaf/0.1/name"), con, true));
		} 
		if(vals.size()==0) {
			vals.addAll(getPropertyValues(res, con.getValueFactory().createURI("http://www.w3.org/2004/02/skos/core#prefLabel"), con, true));
		}
		if(vals.size()>0) {
			return (new ArrayList<Value>(vals)).get(0).stringValue();
		}
		return null;
	}
	
	public static Set<Resource> findClassMembers(Resource type, RepositoryConnection con) throws RepositoryException {
		Set<Resource> answer = new HashSet<Resource>();
		List<Statement> stmts = getStatements(null, RDF.TYPE, type, con);
		for(Statement stmt : stmts) {
			answer.add(stmt.getSubject());
		}
		return answer;
	}
	
	public static Set<Resource> findAllIndividuals(RepositoryConnection con) throws RepositoryException {
		Set<Resource> answer = new HashSet<Resource>();
		
		RepositoryResult<Statement> sesResult = con.getStatements(null, RDF.TYPE, null, false);
		
		Statement stmt;
		try {
			while(sesResult.hasNext()) {
				stmt = sesResult.next();
				if(stmt.getObject() instanceof URI) {
					if(((URI)stmt.getObject()).getNamespace().equals(RDFS.NAMESPACE)||
							((URI)stmt.getObject()).getNamespace().equals(RDF.NAMESPACE)||
							(((URI)stmt.getObject()).getNamespace().startsWith("http://www.w3.org/2002/07/owl#")&&
								(!((URI)stmt.getObject()).toString().equals(OWL.NAMESPACE+"NamedIndividual")))) {
						continue;
					}

					answer.add(stmt.getSubject());
				}
			}
		} finally {
			sesResult.close();
		}
		
		return answer;
	}
	
	public static Set<URI> getPropertiesFrom(Resource ind, RepositoryConnection con) throws RepositoryException {
		Set<URI> answer = new HashSet<URI>();
		
		RepositoryResult<Statement> stmts = con.getStatements(ind, null, null, false);
		
		try {
			Statement stmt;
			
			while(stmts.hasNext()) {
				stmt = stmts.next();
				answer.add(stmt.getPredicate());
			}
		} finally {
			stmts.close();
		}
		return answer;
	}
	
	public static Set<URI> getObjectPropertiesFrom(Resource ind, RepositoryConnection con) throws RepositoryException {
		Set<URI> answer = new HashSet<URI>();
		
		RepositoryResult<Statement> stmts = con.getStatements(ind, null, null, false);
		
		try {
			Statement stmt;
			
			while(stmts.hasNext()) {
				stmt = stmts.next();
				if(stmt.getObject() instanceof Resource) {
					answer.add(stmt.getPredicate());
				}
			}
		} finally {
			stmts.close();
		}
		return answer;
	}
	
	public static Set<URI> getDatatypePropertiesFrom(Resource ind, RepositoryConnection con) throws RepositoryException {
		Set<URI> answer = new HashSet<URI>();
		
		RepositoryResult<Statement> stmts = con.getStatements(ind, null, null, false);
		
		try {
			Statement stmt;
			
			while(stmts.hasNext()) {
				stmt = stmts.next();
				if(stmt.getObject() instanceof Literal) {
					answer.add(stmt.getPredicate());
				}
			}
		} finally {
			stmts.close();
		}
		return answer;
	}
	
	public static Set<URI> getPropertiesTo(Resource ind, RepositoryConnection con) throws RepositoryException {
		Set<URI> answer = new HashSet<URI>();
		
		RepositoryResult<Statement> stmts = con.getStatements(null, null, ind, false);
		
		try {
			Statement stmt;
			
			while(stmts.hasNext()) {
				stmt = stmts.next();
				answer.add(stmt.getPredicate());
			}
		} finally {
			stmts.close();
		}
		return answer;
	}
	
	public static Set<URI> getPropertyRanges(URI property, RepositoryConnection con) throws RepositoryException {
		Set<URI> answer = new HashSet<URI>();
		
		RepositoryResult<Statement> stmts = con.getStatements(property, RDFS.RANGE, null, false);
		
		try {
			Statement stmt;
			
			while(stmts.hasNext()) {
				stmt = stmts.next();
				if(stmt.getObject() instanceof URI) {
					answer.add((URI)stmt.getObject());
				}
			}
		} finally {
			stmts.close();
		}
		
		return answer;
	}
	
	public static boolean isDatatypeProperty(URI property, RepositoryConnection con) throws RepositoryException {
		
		Set<URI> types = getNamedTypes(property, con);
		boolean res = true;
		for(URI type : types) {
			if(type.equals(OWL.DATATYPEPROPERTY)) {
				return true;
			} 
			if(type.equals(OWL.OBJECTPROPERTY)) {
				return false;
			}
		}
		
		RepositoryResult<Statement> stmts = con.getStatements(null, property, null, false);
		try {
			Statement stmt;
			while(stmts.hasNext()) {
				stmt = stmts.next();
				if(stmt.getObject() instanceof Literal) {
					return true;
				} else if(stmt.getObject() instanceof Resource ) {
					return false;
					
				}
			}
		} finally {
			stmts.close();
		}
		
		return res;
	}
	
	public static Set<URI> findAllSuperconcepts(URI concept, RepositoryConnection connection) throws RepositoryException {
		
		//Set<INamedConcept> answer = new HashSet<INamedConcept>();
		Set<URI> answer = new HashSet<URI>();
		
			
		findAllSuperconcepts(concept, connection, answer);
		return answer;
		
	}
	
	private static void findAllSuperconcepts(URI concept, RepositoryConnection connection, Set<URI> answer) throws RepositoryException {
		
		Set<URI> directSuperconcepts = getNamedSuperconcepts(concept, connection);
		for(URI superconcept : directSuperconcepts) {
			
				boolean contains = false;
				for(URI tmp : answer) {
					if(tmp.toString().equals(superconcept.toString())) {
						contains = true;
						break;
					}
				}
				if(!contains) {
					answer.add(superconcept);
				}
				
			
		}
		for(URI superconcept : directSuperconcepts) {
			if(superconcept instanceof URI) {
				findAllSuperconcepts(superconcept, connection, answer);
			}
		}
		
	}
	
	/**
	 * Finds all subconcepts of a concept (including inferred ones). 
	 * Only to be used in cases when the back-end repository does not support 
	 * subclass reasoning.   
	 * 
	 * @param concept
	 * @return List of subconcepts of the input concept
	 */
	public static Set<URI> findAllSubconcepts(URI concept, RepositoryConnection connection) throws RepositoryException {
		Set<URI> answer = new HashSet<URI>();
		
		findAllSubconcepts(concept, connection, answer);
		return answer;
			
		
	}
	
	private static void findAllSubconcepts(URI concept, RepositoryConnection connection, Set<URI> answer) throws RepositoryException {
		Set<URI> directSubconcepts = getNamedSubconcepts(concept, connection);
		for(URI subconcept : directSubconcepts) {
			
				boolean contains = false;
				for(URI tmp : answer) {
					if(tmp.toString().equals(subconcept.toString())) {
						contains = true;
						break;
					}
				}
				if(!contains) {
					answer.add(subconcept);
					findAllSubconcepts(subconcept, connection, answer);
				}
			
		}
	}
	
	public static RDFFormat getRDFFormatForFileName(String fileName) {
		if(fileName.endsWith("n3")) {
			return RDFFormat.N3;
		} else if(fileName.endsWith("nt")) {
			return RDFFormat.NTRIPLES;
		} else {
			return RDFFormat.RDFXML;
		}
		
	}
	public static Object getJavaObjectFromSesameValue(Value value) throws FusionException {
		if(value!=null) {
			
			if(value instanceof Resource) {
				return value;
			} else if(value instanceof Literal ) {
				Literal literal = (Literal)value;
				return literal.stringValue();
			} else {
				return value.toString();
			}
		}
		
		return null;
	}
	
	public static Value getSesameValueFromJavaObject(Object object, ValueFactory vf) {
		if(object==null) {
			return null;
		} if(object instanceof Resource) {
			return (Resource)object;
		} /*else if(object instanceof Double) {
			return vf.createLiteral((Double)object);
		} else if(object instanceof Float) {
			return vf.createLiteral((Float)object);
		} else if(object instanceof Boolean) {
			return vf.createLiteral((Boolean)object);
		} else if(object instanceof Integer) {
			return vf.createLiteral((Integer)object);
		} else if(object instanceof Long) {
			return vf.createLiteral((Long)object);
		} else if(object instanceof XMLGregorianCalendar) {
			return vf.createLiteral((XMLGregorianCalendar)object);
		}*/ else {
			return vf.createLiteral((String)object);
		}
	}
	
	public static TupleQueryResult executeSelect(String sQuery, RepositoryConnection con) throws OpenRDFException {
		
		TupleQuery query = con.prepareTupleQuery(QueryLanguage.SPARQL, sQuery);
		return query.evaluate();
		
		
	}
	
}
