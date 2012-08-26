package uk.ac.open.kmi.fusion.learning.tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.ComparisonPair;
import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;

public abstract class LogicalNode extends DecisionNode {

	public LogicalNode() {
		super();
	}

	@Override
	public boolean isChildAllowed(TreeNode child) {
		if(child instanceof DecisionNode) {
			return true;
		}
		return false;
	}

	@Override
	public void normalizeWeights() {
		for(TreeNode child : this.getChildren()) {
			if(child instanceof DecisionNode) {
				((DecisionNode)child).normalizeWeights();
			}
		}
	}

	@Override
	public Set<ObjectContextModel> getModelSpecs(ApplicationContext context) {
		
		Set<ObjectContextModel> modelSpecs = new HashSet<ObjectContextModel>();
		
		for(TreeNode child : this.getChildren()) {
			if(child instanceof DecisionNode) {
				modelSpecs.addAll(((DecisionNode)child).getModelSpecs(context));
			}
		}
		return modelSpecs;
	}

	@Override
	public Set<IAttribute> getSourceAttributes() {
		Set<IAttribute> attributes = new HashSet<IAttribute>();
		for(TreeNode child : this.children) {
			if(child instanceof DecisionNode) {
				attributes.addAll(((DecisionNode)child).getSourceAttributes());
			}
		}
		return attributes;
	}

	@Override
	public Set<IAttribute> getTargetAttributes() {
		Set<IAttribute> attributes = new HashSet<IAttribute>();
		for(TreeNode child : this.children) {
			if(child instanceof DecisionNode) {
				attributes.addAll(((DecisionNode)child).getTargetAttributes());
			}
		}
		return attributes;
	}

		
	
}
