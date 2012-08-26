package uk.ac.open.kmi.fusion.learning.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.fusion.api.IAggregationFunction;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;
import uk.ac.open.kmi.fusion.api.impl.VariableComparisonSpecification;

public class AggregationNode extends TreeNode {
	
	private IAggregationFunction aggregationFunction;
	
	private ObjectContextModel modelSpec = null;

	public AggregationNode() {
		super();
	}

	public IAggregationFunction getAggregationFunction() {
		return aggregationFunction;
	}

	public void setAggregationFunction(IAggregationFunction aggregationFunction) {
		this.aggregationFunction = aggregationFunction;
	}
	
	public ObjectContextModel getModelSpec() {
		if(modelSpec==null) {
			generateModelSpec();
		}
		
		return modelSpec;
	}

	private void generateModelSpec() {
		ObjectContextModel newSpec = new ObjectContextModel();
		
		VariableComparisonSpecification varSpec;
		for(TreeNode child : this.children) {
			((AttributeSimilarityNode)child).generateVariableComparisonSpecification(newSpec);
			varSpec = ((AttributeSimilarityNode)child).getVariableComparisonSpecification();
			newSpec.addVariableComparisonSpecification(varSpec);
		}
		
		newSpec.setAggregationFunction(this.aggregationFunction);
		
		this.modelSpec = newSpec;
	}

	@Override
	public boolean isChildAllowed(TreeNode child) {
		if(child instanceof AttributeSimilarityNode) {
			return true;
		}
		return false;
	}
	
	public List<AttributeSimilarityNode> getAttributeSimilarityNodes() {
		List<AttributeSimilarityNode> nodes = new ArrayList<AttributeSimilarityNode>(children.size());
		
		for(TreeNode child : children) {
			nodes.add((AttributeSimilarityNode)child);
		}
		
		return nodes;
	}
	
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		//result.append("\t source query: ");
		//result.append(this.serializeQuerySPARQLSource());
		//result.append("\n");
		//result.append("\t target query: ");
		//result.append(this.serializeQuerySPARQLTarget());
		//result.append("\n");
		result.append(aggregationFunction.toString());
		result.append("(");
		for(int i=0;i<children.size();i++) {
			if(i>0) {
				result.append(" ; ");
			}
			result.append(children.get(i).toString());
		}
		result.append(")");
		return result.toString();
		
	}

	@Override
	protected AggregationNode createCopy() {
		AggregationNode copy = new AggregationNode(); 
		copy.setAggregationFunction(this.getAggregationFunction());

		return copy;
	}
	
	public void normalizeWeights() {
		double sum = 0;
		
		for(TreeNode child : this.getChildren()) {
			if(child instanceof AttributeSimilarityNode) {
				sum += ((AttributeSimilarityNode)child).getWeight();
			}
		}
		
		for(TreeNode child : this.getChildren()) {
			if(child instanceof AttributeSimilarityNode) {
				((AttributeSimilarityNode)child).setWeight(
						((AttributeSimilarityNode)child).getWeight()/sum );
			}
		}
	}
	
	public Set<IAttribute> getSourceAttributes() {
		return getAttributes(false);
	}
	
	public Set<IAttribute> getTargetAttributes() {
		return getAttributes(true);
	}
	
	private Set<IAttribute> getAttributes(boolean isTarget) {
		Set<IAttribute> attributes = new HashSet<IAttribute>(children.size());
		
		IAttribute attribute;
		for(TreeNode child : children) {
			if(child instanceof AttributeSimilarityNode) {
				if(isTarget) {
					attribute = ((AttributeSimilarityNode)child).getTargetAttribute();
				} else {
					attribute = ((AttributeSimilarityNode)child).getSourceAttribute();
				}
				attributes.add(attribute);
			}
		}
		
		return attributes;
	}
	
	@Override
	public void visit(IUnaryTreeOperationVisitor visitor) {
		visitor.visit(this);		
	}

	public boolean comparesSameAttributesAs(AggregationNode node) {
		if(this.getChildren().size()!=node.getChildren().size()) {
			return false;
		}
		Map<IAttribute, Set<IAttribute>> sourceToTargetComparisonsMap = new HashMap<IAttribute, Set<IAttribute>>();
		Map<IAttribute, Set<IAttribute>> sourceToTargetComparisonsMapNode = new HashMap<IAttribute, Set<IAttribute>>();
		
		for(AttributeSimilarityNode child : getAttributeSimilarityNodes()) {
			Utils.addToSetMap(child.getSourceAttribute(), child.getTargetAttribute(), sourceToTargetComparisonsMap);
		}
		
		for(AttributeSimilarityNode child : node.getAttributeSimilarityNodes()) {
			Utils.addToSetMap(child.getSourceAttribute(), child.getTargetAttribute(), sourceToTargetComparisonsMapNode);
		}
		
		Set<IAttribute> targetSet1, targetSet2;
		if(!(sourceToTargetComparisonsMap.keySet().containsAll(sourceToTargetComparisonsMapNode.keySet())
				&& (sourceToTargetComparisonsMap.keySet().size()==sourceToTargetComparisonsMapNode.keySet().size()))) {
			return false;
		}
		
		for(IAttribute sourceAttribute : sourceToTargetComparisonsMap.keySet()) {
			targetSet1 = sourceToTargetComparisonsMap.get(sourceAttribute);
			targetSet2 = sourceToTargetComparisonsMapNode.get(sourceAttribute);
			
			if(!(targetSet1.containsAll(targetSet2)
					&&(targetSet1.size()==targetSet2.size()))) {
				return false;
			}
		}
		// if(sourceToTargetComparisonMap)
		// for(IAttribute sourceAttribute : )
		return true;
	}
	
}