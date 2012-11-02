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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;


import uk.ac.open.kmi.common.utils.sparql.SPARQLUtils;
import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.valuematching.ValueMatchingFunctionFactory;
import uk.ac.open.kmi.fusion.util.FusionException;
import uk.ac.open.kmi.fusion.util.SesameUtils;

public class VariableComparisonSpecification extends FusionConfigurationObject {

	public static final String TYPE_URI = FusionMetaVocabulary.VARIABLE_COMPARISON_SPEC;
	
	ObjectContextModel objectContextModel;
	// String variableName;
	// String sourceVariableName = null;
	
	IAttribute sourceAttribute = null;
	IAttribute targetAttribute = null;

	// String sourcePath = "";
	// String targetPath = "";
	
	List<String> sourcePaths = new ArrayList<String>();
	List<String> targetPaths = new ArrayList<String>();
	
	List<String> sourceVariables = new ArrayList<String>();
	List<String> variables = new ArrayList<String>();
	
	IValueMatchingFunction valueMatchingFunction;
	
	double weight = 1.0;
	
	public VariableComparisonSpecification(ObjectContextModel model) {
		this.objectContextModel = model;
	}
	
	public VariableComparisonSpecification(Resource rdfIndividual, FusionEnvironment fusionEnvironment) {
		super(rdfIndividual, fusionEnvironment);
	}

		
	@Override
	public void readFromRDFIndividual(RepositoryConnection connection)
			throws FusionException {
		super.readFromRDFIndividual(connection);
		
		Map<String, String> targetPathsByVariables = new HashMap<String, String>();
		Map<String, String> sourcePathsByVariables = new HashMap<String, String>();
		/*List<Statement> statements = SesameUtils.getStatements(
				this.rdfIndividual, 
				null, 
				null, connection);
		for(Statement statement : statements) {
			readFromPropertyMember(statement);
		}*/
		
		if(sourceAttribute==null) {
			for(String var : variables) {
				for(String path : sourcePaths) {
					if(path.contains("?"+var)) {
						sourcePathsByVariables.put(var, SPARQLUtils.presentExpandedTriplesAsPath(path, "uri", var, FusionEnvironment.getInstance().getNamespaceURITable()));
					}
				}
			}
			if(sourcePaths.size()>=2) {
				sourceAttribute = CompositeAttribute.createFromPropertyPaths(sourcePathsByVariables);
			} else {
				sourceAttribute = new AtomicAttribute(SPARQLUtils.presentExpandedTriplesAsPath(sourcePaths.get(0), "uri", variables.get(0), FusionEnvironment.getInstance().getNamespaceURITable()));
				((AtomicAttribute)sourceAttribute).setVariableName(variables.get(0));
			}
		} 
		
		if(targetAttribute==null) {
			for(String var : variables) {
				for(String path : targetPaths) {
					if(path.contains("?"+var)) {
						targetPathsByVariables.put(var, SPARQLUtils.presentExpandedTriplesAsPath(path, "uri", var, FusionEnvironment.getInstance().getNamespaceURITable()));
					}
				}
			}
			if(targetPaths.size()>=2) {
				targetAttribute = CompositeAttribute.createFromPropertyPaths(targetPathsByVariables);
			} else {
				targetAttribute = new AtomicAttribute(SPARQLUtils.presentExpandedTriplesAsPath(targetPaths.get(0), "uri", variables.get(0), FusionEnvironment.getInstance().getNamespaceURITable()));
				((AtomicAttribute)targetAttribute).setVariableName(variables.get(0));
			}
		} 
	}

	@Override
	protected void readFromPropertyMember(Statement statement)
			throws RepositoryException {
		Literal lit;
		Resource res;
		super.readFromPropertyMember(statement);
		if(statement.getPredicate().toString().equals(FusionMetaVocabulary.METRIC)) {
			if(statement.getObject() instanceof Literal) {
				lit = (Literal)statement.getObject();
				this.instantiateValueMatchingFunction(lit.stringValue());
			}
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.WEIGHT)) {
			if(statement.getObject() instanceof Literal) {
				lit = (Literal)statement.getObject();
				this.weight = lit.doubleValue();
			}
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.SOURCE_PATH)) {
			if(statement.getObject() instanceof Literal) {
				lit = (Literal)statement.getObject();
				// this.sourcePath = lit.stringValue();
				this.sourcePaths.add(lit.stringValue());
			}
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.TARGET_PATH)) {
			if(statement.getObject() instanceof Literal) {
				lit = (Literal)statement.getObject();
				// this.targetPath = lit.stringValue();
				this.targetPaths.add(lit.stringValue());
			}
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.VARIABLE)) {
			if(statement.getObject() instanceof Literal) {
				lit = (Literal)statement.getObject();
				this.variables.add(lit.stringValue());
				// this.variableName = lit.stringValue();
			}
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.SOURCE_ATTRIBUTE)) {
			if(statement.getObject() instanceof Resource) {
				res = (Resource)statement.getObject();
				IAttribute attr = (IAttribute)FusionEnvironment.getInstance().findConfigurationObjectByID(res);
				this.sourceAttribute = attr;
			}
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.TARGET_ATTRIBUTE)) {
			if(statement.getObject() instanceof Resource) {
				res = (Resource)statement.getObject();
				IAttribute attr = (IAttribute)FusionEnvironment.getInstance().findConfigurationObjectByID(res);
				this.targetAttribute = attr;
			}
		}
	}

	/*public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}*/

	/*public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String restrictionSource) {
		this.sourcePath = restrictionSource;
	}

	public String getTargetPath() {
		return targetPath;
	}

	public void setTargetPath(String restrictionTarget) {
		this.targetPath = restrictionTarget;
	}*/

	public IValueMatchingFunction getValueMatchingFunction() {
		return valueMatchingFunction;
	}

	public void setValueMatchingFunction(
			IValueMatchingFunction valueMatchingFunction) {
		this.valueMatchingFunction = valueMatchingFunction;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public ObjectContextModel getObjectContextModel() {
		return objectContextModel;
	}

	public void setObjectContextModel(ObjectContextModel objectContextModel) {
		this.objectContextModel = objectContextModel;
	}

	private void instantiateValueMatchingFunction(String functionName) {
		this.valueMatchingFunction = ValueMatchingFunctionFactory.getInstance(functionName);
	}
	
	public double getSimilarity(List<? extends Object> valuesSource, List<? extends Object> valuesTarget) {
		
		Double max = null;
		double val;
		
		if((valuesSource==null)||(valuesTarget==null)) {
			return 0;
		}
		
		for(Object value1 : valuesSource) {
			for(Object value2 : valuesTarget) {
				try {
					val = valueMatchingFunction.getSimilarity(this.sourceAttribute, this.targetAttribute, value1, value2);
				
					if(max==null) {
						max = val;
					} else {
						if(val>max) {
							max = val;
						}
					}
				} catch(ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
		}
		if(max==null) {
			return 0;
		}

		return max.doubleValue();
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(weight);
		result.append("*");
		result.append(valueMatchingFunction.toString());
		result.append("(");
		//result.append(getSourceVariableName());
		result.append(this.sourceAttribute.writePathAsString());
		result.append(":");
		result.append(this.targetAttribute.writePathAsString());
		result.append(")");
		return result.toString();
	}
	
	/*public String getSourceVariableName() {
		if(sourceVariableName==null) {
			sourceVariableName = variableName;
		}
		return sourceVariableName;
	}

	public void setSourceVariableName(String sourceVariableName) {
		this.sourceVariableName = sourceVariableName;
	}*/

	public IAttribute getSourceAttribute() {
		return sourceAttribute;
	}

	public void setSourceAttribute(IAttribute sourceAttribute) {
		this.sourceAttribute = sourceAttribute;
	}

	public IAttribute getTargetAttribute() {
		return targetAttribute;
	}

	public void setTargetAttribute(IAttribute targetAttribute) {
		this.targetAttribute = targetAttribute;
	}
	
}
