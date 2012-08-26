package uk.ac.open.kmi.fusion.learning.tree.genetic.mutation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.open.kmi.fusion.api.IAggregationFunction;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.aggregation.AggregationFunctionFactory;
import uk.ac.open.kmi.fusion.api.impl.valuematching.ValueMatchingFunctionFactory;
import uk.ac.open.kmi.fusion.learning.CandidateSolution;
import uk.ac.open.kmi.fusion.learning.Genotype;
// import uk.ac.open.kmi.fusion.learning.genetic.fitness.UnsupervisedFitness;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.IFitnessFunction;
import uk.ac.open.kmi.fusion.learning.tree.AggregationNode;
import uk.ac.open.kmi.fusion.learning.tree.AtomicDecisionNode;
import uk.ac.open.kmi.fusion.learning.tree.AttributeSimilarityNode;
import uk.ac.open.kmi.fusion.learning.tree.CandidateSolutionTree;
import uk.ac.open.kmi.fusion.learning.tree.DecisionNode;
import uk.ac.open.kmi.fusion.learning.tree.GenotypeTree;
import uk.ac.open.kmi.fusion.learning.tree.LogicalNode;
import uk.ac.open.kmi.fusion.learning.tree.OrNode;
import uk.ac.open.kmi.fusion.learning.tree.TreeNode;

public class TreeBasedMutationOperator {

	private static TreeBasedMutationOperator INSTANCE = new TreeBasedMutationOperator();
	
	Logger log = Logger.getLogger(TreeBasedMutationOperator.class);
	
	public TreeBasedMutationOperator() {
		// TODO Auto-generated constructor stub
	}
	
	public static TreeBasedMutationOperator getInstance() {
		return INSTANCE;
	}
	

	public CandidateSolutionTree mutate(
			CandidateSolutionTree original,
			List<IAttribute> sourceProperties,
			List<IAttribute> targetProperties,
			Map<IAttribute, Map<IAttribute, List<IValueMatchingFunction<? extends Object>>>> mapApplicableFunctions,
			boolean aligned) {
		
		if(original.getGenotype().getRootNode() instanceof OrNode) {
			OrNode rootNode = (OrNode)original.getGenotype().getRootNode();
			
			List<TreeNode> children = rootNode.getChildren();
			if(children.size()>1) {
				if((children.get(0) instanceof AtomicDecisionNode)&&(children.get(1) instanceof AtomicDecisionNode)) {
					if(((AtomicDecisionNode)children.get(0)).getAggregationNode().comparesSameAttributesAs(((AtomicDecisionNode)children.get(1)).getAggregationNode())) {
						log.error("Mutation error");
					}
				}
			}
			
		}
		
		TreeBasedMutationOperatorVisitor visitor = new TreeBasedMutationOperatorVisitor(
				original,
				sourceProperties,
				targetProperties,
				mapApplicableFunctions,
				aligned);
		
		CandidateSolutionTree result = visitor.mutate();
		
		if(result.getGenotype().getRootNode() instanceof OrNode) {
			OrNode rootNode = (OrNode)result.getGenotype().getRootNode();
			
			List<TreeNode> children = rootNode.getChildren();
			if(children.size()>1) {
				if((children.get(0) instanceof AtomicDecisionNode)&&(children.get(1) instanceof AtomicDecisionNode)) {
					if(((AtomicDecisionNode)children.get(0)).getAggregationNode().comparesSameAttributesAs(((AtomicDecisionNode)children.get(1)).getAggregationNode())) {
						log.error("Mutation error");
					}
				}
			}
			
		}
		
		return result;
		
	}
	
	
	
}
