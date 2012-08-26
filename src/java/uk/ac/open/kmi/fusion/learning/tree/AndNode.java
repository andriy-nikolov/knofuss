package uk.ac.open.kmi.fusion.learning.tree;

import java.util.Map;

import uk.ac.open.kmi.fusion.api.impl.ComparisonPair;
import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;

public class AndNode extends LogicalNode {

	public AndNode() {
		super();
	}

	@Override
	public boolean isEquivalentPair(
			ComparisonPair pair) {
		
		this.minimalPassingSimilarity = 0.0;
		
		double minSimilarity = 1.0;
		for(TreeNode child : children) {
			if(child instanceof DecisionNode) {
				if(!((DecisionNode)child).isEquivalentPair(pair)) {
					return false;
				} else {
					minSimilarity = Math.min(minSimilarity, ((DecisionNode)child).getLastMinimalPassingSimilarity());
				}
			}
		}
		
		this.minimalPassingSimilarity = minSimilarity;
		return true;
	}
	
	@Override
	public String toString() {
		StringBuffer res = new StringBuffer();
		if(this.children.size()>0) {
			res.append("(");
			res.append(this.children.get(0).toString());
			res.append(")");
			
			for(int i=1;i<this.children.size();i++) {
				res.append(" AND ");
				res.append("(");
				res.append(this.children.get(i).toString());
				res.append(")");
			}
		}
		
		return res.toString(); 
	}

	@Override
	protected AndNode createCopy() {

		AndNode copy = new AndNode();
		return copy;
	}
	
	@Override
	public void visit(IUnaryTreeOperationVisitor visitor) {
		visitor.visit(this);
	}

}
