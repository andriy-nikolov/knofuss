package uk.ac.open.kmi.fusion.learning.tree;

import org.apache.log4j.Logger;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;
import uk.ac.open.kmi.fusion.api.impl.VariableComparisonSpecification;
import uk.ac.open.kmi.fusion.learning.tree.genetic.mutation.TreeBasedMutationOperatorVisitor;

public class AttributeSimilarityNode extends TreeNode {

	private IAttribute sourceAttribute;
	private IAttribute targetAttribute;
	
	private double weight;
	
	private IValueMatchingFunction valueMatchingFunction;
	
	private VariableComparisonSpecification varSpec = null;
	
	Logger log = Logger.getLogger(AttributeSimilarityNode.class);

	public AttributeSimilarityNode() {
		super();
	}


	public void setSourceAttribute(IAttribute sourceAttribute) {
		this.sourceAttribute = sourceAttribute;
	}
	
	public void setTargetAttribute(IAttribute targetAttribute) {
		this.targetAttribute = targetAttribute;
	}


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
	
	public IAttribute getSourceAttribute() {
		return sourceAttribute;
	}
	
	public IAttribute getTargetAttribute() {
		return targetAttribute;
	}
	
	public void generateVariableComparisonSpecification(ObjectContextModel modelSpec) {
		
		VariableComparisonSpecification spec = new VariableComparisonSpecification(modelSpec);
		
		spec.setWeight(this.getWeight());
		spec.setValueMatchingFunction(this.valueMatchingFunction);
		spec.setSourceAttribute(sourceAttribute);
		spec.setTargetAttribute(targetAttribute);
		
		this.varSpec = spec;
	}
	
	// TODO Ugly - fix this
	public VariableComparisonSpecification getVariableComparisonSpecification() {
		if(varSpec==null) {
			throw new IllegalStateException("generateVariableComparisonSpecification() has not been called and the variable comparison specification has not been initialized");
		}
		return varSpec;
	}


	public void setVariableComparisonSpecification(VariableComparisonSpecification varSpec) {
		this.varSpec = varSpec;
	}


	@Override
	public boolean isChildAllowed(TreeNode child) {
		return false;
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
		// return getVariableComparisonSpecification().toString();
	}


	@Override
	protected AttributeSimilarityNode createCopy() {
		
		AttributeSimilarityNode copy = new AttributeSimilarityNode();
		
		copy.setSourceAttribute(sourceAttribute);
		copy.setTargetAttribute(targetAttribute);
		copy.setWeight(this.getWeight());
		copy.setValueMatchingFunction(valueMatchingFunction);

		return copy;
	}
	
	@Override
	public void visit(IUnaryTreeOperationVisitor visitor) {
		visitor.visit(this);		
	}
	
}
