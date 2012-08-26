package uk.ac.open.kmi.fusion.learning.tree;

import java.util.ArrayList;
import java.util.List;

import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;

public class ThresholdNode extends TreeNode {

	private double threshold;
	
	public ThresholdNode() {
		super();
	}

	@Override
	public boolean isChildAllowed(TreeNode child) {
		return false;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	@Override
	public String toString() {
		return Double.toString(threshold);
	}

	@Override
	protected ThresholdNode createCopy() {
		
		ThresholdNode copy = new ThresholdNode();
		
		copy.setThreshold(this.getThreshold());
		
		return copy;
	}
	
	@Override
	public void visit(IUnaryTreeOperationVisitor visitor) {
		visitor.visit(this);		
	}

}
