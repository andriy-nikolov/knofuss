package uk.ac.open.kmi.fusion.learning.tree;

import java.util.HashSet;

import java.util.Set;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.ComparisonPair;
import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;

public class AtomicDecisionNode extends DecisionNode {

	public AtomicDecisionNode() {
		super();
	}

	@Override
	public boolean isChildAllowed(TreeNode child) {
		if(this.children.size()<2) {
			if((child instanceof AggregationNode)||(child instanceof ThresholdNode)) {
				return true;
			}
		}
		
		return false;
	}
	
	public AggregationNode getAggregationNode() {
		for(TreeNode child : children) {
			if(child instanceof AggregationNode) {
				return (AggregationNode)child;
			}
		}
		
		throw new IllegalStateException("Aggregation node has not been set");
	}
	
	public ThresholdNode getThresholdNode() {
		for(TreeNode child : children) {
			if(child instanceof ThresholdNode) {
				return (ThresholdNode)child;
			}
		}
		
		throw new IllegalStateException("Threshold node has not been set");
	}
	
	public void setThreshold(double threshold) {
		ThresholdNode node = new ThresholdNode();
		node.setThreshold(threshold);
		setThresholdNode(node);
	}
	
	public void setThresholdNode(ThresholdNode node) {
		try {
			ThresholdNode oldThresholdNode = getThresholdNode();
			removeChild(oldThresholdNode);
		} catch(IllegalStateException e) {
			
		}
		
		this.addChild(node);
		
	}
	
	public void setAggregationNode(AggregationNode node) {
		try {
			AggregationNode oldAggregationNode = getAggregationNode();
			removeChild(oldAggregationNode);
		} catch(IllegalStateException e) {
			
		}
		
		this.addChild(node);
		
	}
	

	@Override
	public boolean isEquivalentPair(
			ComparisonPair pair) {
		AggregationNode aggregationNode = this.getAggregationNode();
		ThresholdNode thresholdNode = this.getThresholdNode();
		
		double sim = aggregationNode.getModelSpec().getSimilarity(pair);
		if(sim >= thresholdNode.getThreshold()) {
			this.minimalPassingSimilarity = sim;
			return true;
		}
		this.minimalPassingSimilarity = 0.0;
		return false;
	}
	
	

	@Override
	public String toString() {
		
		AggregationNode aggregationNode = this.getAggregationNode();
		ThresholdNode thresholdNode = this.getThresholdNode();
		
		StringBuffer res = new StringBuffer(aggregationNode.toString());
		res.append(">=");
		res.append(thresholdNode.toString());
		
		return res.toString();
	}

	@Override
	protected AtomicDecisionNode createCopy() {
		AtomicDecisionNode copy = new AtomicDecisionNode();
		
		return copy;
	}

	@Override
	public void normalizeWeights() {
		this.getAggregationNode().normalizeWeights();	
	}

	@Override
	public Set<ObjectContextModel> getModelSpecs(ApplicationContext context) {
		ObjectContextModel modelSpec = this.getAggregationNode().getModelSpec();
		modelSpec.setApplicationContext(context);
		modelSpec.setThreshold(this.getThresholdNode().getThreshold());
		modelSpec.prepare();
		Set<ObjectContextModel> modelSpecs = new HashSet<ObjectContextModel>(1);
		modelSpecs.add(modelSpec);
		return modelSpecs;
	}

	@Override
	public Set<IAttribute> getSourceAttributes() {
		return this.getAggregationNode().getSourceAttributes();
	}

	@Override
	public Set<IAttribute> getTargetAttributes() {
		return this.getAggregationNode().getTargetAttributes();
	}

	@Override
	public void visit(IUnaryTreeOperationVisitor visitor) {
		visitor.visit(this);		
	}
	
}
