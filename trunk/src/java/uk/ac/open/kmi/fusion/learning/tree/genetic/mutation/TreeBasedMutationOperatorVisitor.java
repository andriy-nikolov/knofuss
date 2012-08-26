package uk.ac.open.kmi.fusion.learning.tree.genetic.mutation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.ac.open.kmi.fusion.api.IAggregationFunction;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.aggregation.AggregationFunctionFactory;
import uk.ac.open.kmi.fusion.api.impl.valuematching.ValueMatchingFunctionFactory;
import uk.ac.open.kmi.fusion.learning.tree.AggregationNode;
import uk.ac.open.kmi.fusion.learning.tree.AndNode;
import uk.ac.open.kmi.fusion.learning.tree.AtomicDecisionNode;
import uk.ac.open.kmi.fusion.learning.tree.AttributeSimilarityNode;
import uk.ac.open.kmi.fusion.learning.tree.CandidateSolutionTree;
import uk.ac.open.kmi.fusion.learning.tree.DecisionNode;
import uk.ac.open.kmi.fusion.learning.tree.LogicalNode;
import uk.ac.open.kmi.fusion.learning.tree.GenotypeTree;
import uk.ac.open.kmi.fusion.learning.tree.IUnaryTreeOperationVisitor;
import uk.ac.open.kmi.fusion.learning.tree.OrNode;
import uk.ac.open.kmi.fusion.learning.tree.ThresholdNode;
import uk.ac.open.kmi.fusion.learning.tree.TreeNode;

public class TreeBasedMutationOperatorVisitor implements
		IUnaryTreeOperationVisitor {

	CandidateSolutionTree original;
	List<IAttribute> sourceProperties;
	List<IAttribute> targetProperties;
	Map<IAttribute, Map<IAttribute, List<IValueMatchingFunction<? extends Object>>>> mapApplicableFunctions;
	boolean aligned;
	
	CandidateSolutionTree result;
	
	private static final double P_ADD_OR_REMOVE = 0.48;
	private static final double P_CHANGE = 0.48;
	private static final double P_THRESHOLD_INCREASE = 0.5;
	private static final double P_CHANGE_AGGREGATION = 0.6;
	private static final double P_CHANGE_THRESHOLD = 0.3;
	private static final double P_CHANGE_WEIGHT = 0.5;
	
	double pAddOrRemove = P_ADD_OR_REMOVE;
	double pChange = P_CHANGE;
	double pThresholdIncrease = P_THRESHOLD_INCREASE;
	double pChangeAggregation = P_CHANGE_AGGREGATION;
	double pChangeThreshold = P_CHANGE_THRESHOLD;
	double pChangeWeight = P_CHANGE_WEIGHT;
	
	Logger log = Logger.getLogger(TreeBasedMutationOperatorVisitor.class);
	
	public TreeBasedMutationOperatorVisitor(CandidateSolutionTree original,
			List<IAttribute> sourceProperties,
			List<IAttribute> targetProperties,
			Map<IAttribute, Map<IAttribute, List<IValueMatchingFunction<? extends Object>>>> mapApplicableFunctions,
			boolean aligned) {
		this.original = original;
		this.sourceProperties = sourceProperties;
		this.targetProperties = targetProperties;
		this.mapApplicableFunctions = mapApplicableFunctions;
		
		GenotypeTree childGenotype = new GenotypeTree(original.getApplicationContext(), sourceProperties, targetProperties);
		
		childGenotype.setRootNode((DecisionNode)original.getGenotype().getRootNode().getCopy());
		
		this.result = new CandidateSolutionTree(original.getApplicationContext(), childGenotype, sourceProperties, targetProperties);
	}

	@Override
	public void visit(AtomicDecisionNode node) {

		double val = Math.random();
		if(val<pChangeAggregation) {
			node.getAggregationNode().visit(this);
		} else if(val<pChangeAggregation + pChangeThreshold) {
			node.getThresholdNode().visit(this);
		} else {
			// Split the node : add a copy and mutate it
			// Check if there is a parent
			// If yes, add a copy of node
			// If not, create an OR node as a parent
			AtomicDecisionNode copy = (AtomicDecisionNode)node.getCopy();
			
			if(node.getParent()!=null) {
				if(node.getParent() instanceof LogicalNode) {
					if(node.getParent().getChildren().size() < 2) {
						node.getParent().addChild(copy);
					}
				}
			} else {
				OrNode parentNode = new OrNode();
				result.getGenotype().setRootNode(parentNode);
				parentNode.addChild(node);
				parentNode.addChild(copy);
			}
			
			// Make sure that we don't end with decision rules comparing the same properties
			pAddOrRemove = 1.0;
			pChangeAggregation = 1.0;
			
			copy.visit(this);
			
		}

	}

	@Override
	public void visit(OrNode node) {
		// Choose a random child and mutate it
		DecisionNode child = (DecisionNode)node.getChildren().get((int)(Math.random()*node.getChildren().size()));
		
		
		boolean validMutation = false;
		
		while(!validMutation) {
			child.visit(this);
			
			List<TreeNode> children = node.getChildren();
			if(children.size()>1) {
				if((children.get(0) instanceof AtomicDecisionNode)&&(children.get(1) instanceof AtomicDecisionNode)) {
					if(((AtomicDecisionNode)children.get(0)).getAggregationNode().comparesSameAttributesAs(((AtomicDecisionNode)children.get(1)).getAggregationNode())) {
						validMutation = false;
					} else {
						validMutation = true;
					}
				}
			}
		}
			
		
		
	}

	@Override
	public void visit(AndNode node) {
		
	}

	@Override
	public void visit(ThresholdNode node) {
		
		double change;
		double minChange, maxChange;
		
		double threshold = node.getThreshold();
		
		if(Math.random()<pThresholdIncrease) {
			maxChange = (1-original.getFitness().getPrecision())*(1-threshold);
			minChange = Math.max(maxChange/4, 0.01);
			maxChange = Math.max(maxChange, minChange);
			change = Math.random()*(maxChange-minChange)+minChange;
			if((threshold+change)>1) {
				log.error("Threshold too high: "+(threshold+change));
			}
			threshold = Math.min(threshold+change, 1.0);
			
		} else {
			maxChange = (1-original.getFitness().getRecall())*threshold;
			minChange = Math.max(maxChange/4, 0.01);
			maxChange = Math.max(maxChange, minChange);
			change = Math.random()*(maxChange-minChange)+minChange;
			
			if((threshold-change)<0) {
				log.error("Negative threshold: "+(threshold-change));
			}
			
			threshold = Math.max(threshold-change, 0.0);
		}
					
		node.setThreshold(threshold);

	}

	@Override
	public void visit(AggregationNode node) {

		int columns = targetProperties.size();
		
		List<Integer> validComponents = getValidComponents(node); 
		List<Integer> potentiallyValidComponents = getPotentiallyValidComponents(validComponents);
		
		double val = Math.random();
		
		int index, x, y;
		
		if(val<pAddOrRemove) {
			double pRemove;
			
			pRemove = 0.5*(validComponents.size()-1);
			
			if(Math.random()<pRemove) {
				TreeNode toRemove = node.getChildren().get((int)(Math.random()*validComponents.size()));
				
				node.removeChild(toRemove);
				
			} else {
				if(potentiallyValidComponents.size()>0) {
					index = potentiallyValidComponents.get((int)(Math.random()*potentiallyValidComponents.size()));
									
					y = index%columns;
					x = index/columns;
				
					// Flip the bit
					List<IValueMatchingFunction<? extends Object>> functionsList = mapApplicableFunctions.get(sourceProperties.get(x)).get(targetProperties.get(y));
					
					IValueMatchingFunction vmFunction = functionsList.get((int)Math.random()*functionsList.size());
					
					AttributeSimilarityNode toAdd = new AttributeSimilarityNode();
					toAdd.setValueMatchingFunction(vmFunction);
					toAdd.setWeight(Math.random());
					
					toAdd.setSourceAttribute(sourceProperties.get(x));
					toAdd.setTargetAttribute(targetProperties.get(y));
					
					node.addChild(toAdd);
				}

			}
			
		} else if(val<pAddOrRemove + pChange){
			if(validComponents.size()>0) {
				
				AttributeSimilarityNode toChange = (AttributeSimilarityNode)node.getChildren().get((int)(Math.random()*validComponents.size()));
				
				if((validComponents.size()>1)||(Math.random()<0.5)) {
					
					double weightChange = Math.random()*(0.8/node.getChildren().size())+0.2/node.getChildren().size();
					
					if(Math.random()<0.5) {
						toChange.setWeight(Math.max(toChange.getWeight()-weightChange, 0));
					} else {
						toChange.setWeight(Math.min(toChange.getWeight()+weightChange, 1));
					}
				} else {
					toChange.setValueMatchingFunction(ValueMatchingFunctionFactory.getRandomInstanceForAttributes(toChange.getSourceAttribute(), toChange.getTargetAttribute()));
				}
			}
		} else {
			node.setAggregationFunction(this.mutateAggregationFunction(node.getAggregationFunction()));
		}

	}

	@Override
	public void visit(AttributeSimilarityNode node) {
		List<Integer> validComponents = getValidComponents((AggregationNode)node.getParent());
		if((validComponents.size()>1)||(Math.random()<pChangeWeight)) {
			
			double weightChange = Math.random()*(0.8/node.getChildren().size())+0.2/node.getChildren().size();
			
			if(Math.random()<0.5) {
				node.setWeight(Math.max(node.getWeight()-weightChange, 0));
			} else {
				node.setWeight(Math.min(node.getWeight()+weightChange, 1));
			}
		} else {
			node.setValueMatchingFunction(ValueMatchingFunctionFactory.getRandomInstanceForAttributes(node.getSourceAttribute(), node.getTargetAttribute()));
		}

	}
	
	private List<Integer> getValidComponents(AggregationNode from) {
		
		List<Integer> validComponents = new ArrayList<Integer>();

		int i, j;
		
		AttributeSimilarityNode childSimilarityNode;
		for(TreeNode child : from.getChildren()) {
			childSimilarityNode = (AttributeSimilarityNode)child;
			
			i = sourceProperties.indexOf(childSimilarityNode.getSourceAttribute());
			j = targetProperties.indexOf(childSimilarityNode.getTargetAttribute());
			
			validComponents.add(i*targetProperties.size() + j);
		}
		
		return validComponents;
		
	}
	
	private List<Integer> getPotentiallyValidComponents(List<Integer> validComponents) {
		
		List<Integer> potentiallyValidComponents = new ArrayList<Integer>();
		int rows = sourceProperties.size();
		int columns = targetProperties.size();
		
		int i, j;
		
		
		// Potentially valid components are only those pairs of properties that are 
		// not compared in the current decision rule and
		// can be compared with at least one similarity function
		
		for(i=0;i<rows;i++) {
			for(j=0;j<columns;j++) {
				if(!validComponents.contains(i*columns + j)) {
					// validComponents.add(i*columns+j);

					if(aligned) {
						if(!sourceProperties.get(i).samePropertyPathAs(targetProperties.get(j))) {
							continue;
						}
					}
					if(!mapApplicableFunctions.get(sourceProperties.get(i)).get(targetProperties.get(j)).isEmpty()) {
						potentiallyValidComponents.add(i*columns+j);
					}
				}
			}
		}
		
		return potentiallyValidComponents;
	}

	private IAggregationFunction mutateAggregationFunction(IAggregationFunction from) {
		IAggregationFunction func = AggregationFunctionFactory.getRandomInstance();
		while(!from.toString().equals(func.toString())) {
			func = AggregationFunctionFactory.getRandomInstance();
		}
		
		return func;
	}
	
	public CandidateSolutionTree mutate() {
		this.result.getGenotype().getRootNode().visit(this);
		
		return this.result;
	}
	
	public boolean isRedundant(AtomicDecisionNode node) {
		// A decision node is useless if there is already a sibling comparing the same set of properties
		
		if(result.getGenotype().getRootNode() instanceof LogicalNode) {
			for(TreeNode child : result.getGenotype().getRootNode().getChildren()) {
				if(node!=child) {
					if(child instanceof AtomicDecisionNode) {
						if(node.getAggregationNode().comparesSameAttributesAs(((AtomicDecisionNode)child).getAggregationNode())) {
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}

}
