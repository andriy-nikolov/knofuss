package uk.ac.open.kmi.fusion.learning.tree;

import java.util.List;
import java.util.Set;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.ComparisonPair;
import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;

public abstract class DecisionNode extends TreeNode {

	double minimalPassingSimilarity;
	
	public DecisionNode() {
		super();
	}
	
	
	public abstract boolean isEquivalentPair(ComparisonPair pair);
	
	public abstract void normalizeWeights();
		
	public abstract Set<ObjectContextModel> getModelSpecs(ApplicationContext context);
	
	public abstract Set<IAttribute> getSourceAttributes();
	
	public abstract Set<IAttribute> getTargetAttributes();
	
	public double getLastMinimalPassingSimilarity() {
		return minimalPassingSimilarity;
	}
	
}
