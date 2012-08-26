package uk.ac.open.kmi.fusion.learning.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import uk.ac.open.kmi.fusion.api.impl.ComparisonPair;
import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;

public class OrNode extends LogicalNode {
	
	public OrNode() {
		super();
	}

	@Override
	public boolean isEquivalentPair(
			ComparisonPair pair) {
		
		this.minimalPassingSimilarity = 0.0;
		
		boolean result = false;
		double minSimilarity = 1.0;
		
		for(TreeNode child : this.children) {
			if(child instanceof DecisionNode) {
				if(((DecisionNode)child).isEquivalentPair(pair)) {
					result = true;
					minSimilarity = Math.min(minSimilarity, ((DecisionNode)child).getLastMinimalPassingSimilarity());
				}
			}
		}
		
		if(result) {
			this.minimalPassingSimilarity = minSimilarity;
		}
		
		return result;
	}

	@Override
	public String toString() {
		StringBuffer res = new StringBuffer();
		if(this.children.size()>0) {
			res.append("(");
			res.append(this.children.get(0).toString());
			res.append(")");
			
			for(int i=1;i<this.children.size();i++) {
				res.append(" OR ");
				res.append("(");
				res.append(this.children.get(i).toString());
				res.append(")");
			}
		}
		
		return res.toString(); 
	}

	@Override
	protected OrNode createCopy() {
		OrNode copy = new OrNode();
		return copy;
	}

	@Override
	public void visit(IUnaryTreeOperationVisitor visitor) {
		visitor.visit(this);		
	}

	
	

}
