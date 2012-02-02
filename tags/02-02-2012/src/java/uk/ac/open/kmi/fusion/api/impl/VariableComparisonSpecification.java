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
	
	Map<String, String> targetPathsByVariables = new HashMap<String, String>();
	Map<String, String> sourcePathsByVariables = new HashMap<String, String>();
	
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
		/*List<Statement> statements = SesameUtils.getStatements(
				this.rdfIndividual, 
				null, 
				null, connection);
		for(Statement statement : statements) {
			readFromPropertyMember(statement);
		}*/
		
		for(String var : variables) {
			for(String path : targetPaths) {
				if(path.contains("?"+var)) {
					targetPathsByVariables.put(var, path);
				}
			}
			for(String path : sourcePaths) {
				if(path.contains("?"+var)) {
					sourcePathsByVariables.put(var, path);
				}
			}
		}
		
		if(targetPaths.size()>=2) {
			AtomicAttribute tmp;
			targetAttribute = CompositeAttribute.createFromPropertyPaths(targetPathsByVariables);
			sourceAttribute = CompositeAttribute.createFromPropertyPaths(sourcePathsByVariables);
		} else {
			targetAttribute = new AtomicAttribute(targetPaths.get(0));
			((AtomicAttribute)targetAttribute).setVariableName(variables.get(0));
			sourceAttribute = new AtomicAttribute(sourcePaths.get(0));
			((AtomicAttribute)sourceAttribute).setVariableName(variables.get(0));
		}
	}

	@Override
	protected void readFromPropertyMember(Statement statement)
			throws RepositoryException {
		Literal lit;
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
				val = valueMatchingFunction.getSimilarity(this.sourceAttribute, this.targetAttribute, value1, value2);
				if(max==null) {
					max = val;
				} else {
					if(val>max) {
						max = val;
					}
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