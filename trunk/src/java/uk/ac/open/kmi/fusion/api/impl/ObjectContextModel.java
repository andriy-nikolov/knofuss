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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;


import uk.ac.open.kmi.common.utils.sparql.SPARQLUtils;
import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.IAggregationFunction;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IDump;
import uk.ac.open.kmi.fusion.api.IObjectContextModel;
import uk.ac.open.kmi.fusion.api.IObjectContextWrapper;
import uk.ac.open.kmi.fusion.api.impl.aggregation.AggregationFunctionFactory;
import uk.ac.open.kmi.fusion.util.FusionException;
import uk.ac.open.kmi.fusion.util.KnoFussUtils;
import uk.ac.open.kmi.fusion.util.SesameUtils;

public class ObjectContextModel extends AbstractObjectContextModel implements IObjectContextModel {

	public static final String TYPE_URI = FusionMetaVocabulary.OBJECT_CONTEXT_MODEL;
	
	protected List<VariableComparisonSpecification> variableComparisonSpecifications;
	
	protected IAggregationFunction aggregationFunction;
	
	protected double threshold;
	
	Logger log = Logger.getLogger(ObjectContextModel.class);
	
	private void init() {
		
		variableComparisonSpecifications = new ArrayList<VariableComparisonSpecification>();
	}

	public ObjectContextModel() {
		super();
		init();
	}
	
	
	public ObjectContextModel(ObjectContextModel copy) {
		this();
		
		setApplicationContext(copy.getApplicationContext());
		
		setAggregationFunction(copy.getAggregationFunction());
		setEnvironment(copy.getEnvironment());
		setQuerySPARQLSource(copy.getQuerySPARQLSource());
		setQuerySPARQLTarget(copy.getQuerySPARQLTarget());
		setThreshold(copy.getThreshold());
		
		prepare();
	}
	
	public ObjectContextModel(Resource individual, FusionEnvironment ontology) {
		super(individual, ontology);
		init();
	}
		
	public List<VariableComparisonSpecification> getVariableComparisonSpecifications() {
		return variableComparisonSpecifications;
	}
	
	/*public Map<String, VariableComparisonSpecification> getVariableComparisonSpecificationsByName() {
		return variableComparisonSpecificationsByName;
	}*/
	
	@Override
	public void readFromRDFIndividual(RepositoryConnection connection) throws FusionException {
		super.readFromRDFIndividual(connection);
	}

	@Override
	protected void readFromPropertyMember(Statement statement)
			throws RepositoryException {
				super.readFromPropertyMember(statement);
				if(statement.getPredicate().toString().equals(FusionMetaVocabulary.VARIABLE_COMPARISON)) {
					VariableComparisonSpecification spec = (VariableComparisonSpecification)FusionEnvironment.getInstance().findConfigurationObjectByID((Resource)statement.getObject());
					spec.setObjectContextModel(this);
					addVariableComparisonSpecification(spec);						
				} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.THRESHOLD)) {
					Literal lit = (Literal)statement.getObject();
					this.threshold = lit.doubleValue();
				} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.AGGREGATION_FUNCTION)) {
					Literal lit = (Literal)statement.getObject();
					this.aggregationFunction = AggregationFunctionFactory.getInstance(lit.stringValue());
				} 
			}

/*	public FusionEnvironment getOntology() {
		return ontology;
	}

	public void setOntology(FusionEnvironment ontology) {
		this.ontology = ontology;
	}*/
			
	public IAggregationFunction getAggregationFunction() {
		return aggregationFunction;
	}

	public void setAggregationFunction(IAggregationFunction aggregationFunction) {
		this.aggregationFunction = aggregationFunction;
	}
	
	@Override
	protected void fillAttributeMaps() {
		IAttribute attr;
		String varName;
		
		for(VariableComparisonSpecification spec : this.variableComparisonSpecifications) {
			
			attr = spec.getSourceAttribute();
			if(attr instanceof AtomicAttribute) {
				this.sourceAttributesByVarName.put(((AtomicAttribute) attr).getVariableName(), attr);
				// this.sourceAttributesByPath.put(((AtomicAttribute) attr).getPropertyPath(), attr);
			} else if(attr instanceof CompositeAttribute) {
				this.sourceAttributesByVarName.putAll(((CompositeAttribute)attr).getAtomicAttributesByVariable());
				// this.sourceAttributesByPath.putAll(((CompositeAttribute)attr).getAtomicAttributesByPropertyPath());
			} else if(attr instanceof TransformationAttribute){
				this.sourceAttributesByVarName.putAll(((TransformationAttribute)attr).getAtomicAttributesByVariable());
				// this.sourceAttributesByPath.putAll(((CompositeAttribute)attr).getAtomicAttributesByPropertyPath());
			}
			
			attr = spec.getTargetAttribute();
			if(attr instanceof AtomicAttribute) {
				this.targetAttributesByVarName.put(((AtomicAttribute) attr).getVariableName(), attr);
			//	this.targetAttributesByPath.put(((AtomicAttribute) attr).getPropertyPath(), attr);
			} else if(attr instanceof CompositeAttribute) {
				this.targetAttributesByVarName.putAll(((CompositeAttribute)attr).getAtomicAttributesByVariable());
			//	this.targetAttributesByPath.putAll(((CompositeAttribute)attr).getAtomicAttributesByPropertyPath());
			} else if(attr instanceof TransformationAttribute){
				this.targetAttributesByVarName.putAll(((TransformationAttribute)attr).getAtomicAttributesByVariable());
				// this.sourceAttributesByPath.putAll(((CompositeAttribute)attr).getAtomicAttributesByPropertyPath());
			}
		}
	}
	
	protected Query generateQuery(boolean isTarget) {
		// List<String> variables = new ArrayList<String>();
		List<String> restrictions = new ArrayList<String>();
		
		
		if(isTarget) {
			restrictions.add(this.applicationContext.getRestrictionTarget());
		} else {
			restrictions.add(this.applicationContext.getRestrictionSource());
		}
		
		List<IAttribute> attributes = new ArrayList<IAttribute>();
		
		for(VariableComparisonSpecification varSpec : variableComparisonSpecifications) {
			if(isTarget) {
				attributes.add(varSpec.getTargetAttribute());
				// restrictions.add(varSpec.getTargetPath());
			} else {
				attributes.add(varSpec.getSourceAttribute());
				// restrictions.add(varSpec.getSourcePath());
			}
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
	
	
	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.fusion.api.impl.IObjectContextModel#serializeQuerySPARQLSource()
	 */
	@Override
	public String serializeQuerySPARQLSource() {
		if(this.querySPARQLSource==null) {
			this.getQuerySPARQLSource();
		}
		return this.querySPARQLSource.serialize();
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.fusion.api.impl.IObjectContextModel#serializeQuerySPARQLTarget()
	 */
	@Override
	public String serializeQuerySPARQLTarget() {
		if(this.querySPARQLTarget==null) {
			this.getQuerySPARQLTarget();
		}
		return this.querySPARQLTarget.serialize();
	}

	
	
	/*public Map<String, String> getVariablePathMapSource() {
		if(this.querySPARQLSource==null) {
			this.getQuerySPARQLSource();
		}
		return variablePathMapSource;
	}

	public Map<String, String> getVariablePathMapTarget() {
		if(this.querySPARQLTarget==null) {
			this.getQuerySPARQLTarget();
		}
		return variablePathMapTarget;
	}*/

	public double getSimilarity(ComparisonPair pair) {
		return this.aggregationFunction.calculate(pair.getSourceInstance(), pair.getTargetInstance(), this.variableComparisonSpecifications);
	}
	
	public void addVariableComparisonSpecification(VariableComparisonSpecification spec) {
		this.variableComparisonSpecifications.add(spec);
		/*if(spec.getVariableName()!=null) {
			this.getVariableComparisonSpecificationsByName().put(spec.getVariableName(), spec);
		}*/
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("{ \n");
		//result.append("\t source query: ");
		//result.append(this.serializeQuerySPARQLSource());
		//result.append("\n");
		//result.append("\t target query: ");
		//result.append(this.serializeQuerySPARQLTarget());
		//result.append("\n");
		result.append(aggregationFunction.toString());
		result.append("(");
		for(int i=0;i<variableComparisonSpecifications.size();i++) {
			if(i>0) {
				result.append("+");
			}
			result.append(variableComparisonSpecifications.get(i).toString());
		}
		result.append(")>=");
		result.append(threshold);
		result.append("\n}");
		return result.toString();
	}

}
