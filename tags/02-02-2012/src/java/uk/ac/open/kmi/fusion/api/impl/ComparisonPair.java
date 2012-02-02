package uk.ac.open.kmi.fusion.api.impl;

import uk.ac.open.kmi.fusion.api.IObjectContextWrapper;

public class ComparisonPair extends Pair<IObjectContextWrapper, IObjectContextWrapper> {

	double similarity;
	
	public double getSimilarity() {
		return similarity;
	}

	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}

	public ComparisonPair() {
		super();
	}
	
	public ComparisonPair(IObjectContextWrapper source, IObjectContextWrapper target) {
		super(source, target);
	}
	
	public IObjectContextWrapper getSourceInstance() {
		return getLeft();
	}
	
	public IObjectContextWrapper getTargetInstance() {
		return getRight();
	}
	
	
	
}
