package uk.ac.open.kmi.fusion.learning.tree;

public interface IUnaryTreeOperationVisitor {

	public void visit(AtomicDecisionNode node);
	
	public void visit(OrNode node);
	
	public void visit(AndNode node);
	
	public void visit(ThresholdNode node);
	
	public void visit(AggregationNode node);
	
	public void visit(AttributeSimilarityNode node);
	
}
