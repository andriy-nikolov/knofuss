package uk.ac.open.kmi.fusion.learning.tree.genetic.crossover;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.open.kmi.fusion.api.IAggregationFunction;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.AtomicAttribute;
import uk.ac.open.kmi.fusion.learning.CandidateSolution;
import uk.ac.open.kmi.fusion.learning.Genotype;
import uk.ac.open.kmi.fusion.learning.genetic.crossover.ICrossoverOperator;
import uk.ac.open.kmi.fusion.learning.tree.AggregationNode;
import uk.ac.open.kmi.fusion.learning.tree.AtomicDecisionNode;
import uk.ac.open.kmi.fusion.learning.tree.AttributeSimilarityNode;
import uk.ac.open.kmi.fusion.learning.tree.CandidateSolutionTree;
import uk.ac.open.kmi.fusion.learning.tree.DecisionNode;
import uk.ac.open.kmi.fusion.learning.tree.GenotypeTree;
import uk.ac.open.kmi.fusion.learning.tree.LogicalNode;
import uk.ac.open.kmi.fusion.learning.tree.OrNode;
import uk.ac.open.kmi.fusion.learning.tree.TreeNode;
import uk.ac.open.kmi.fusion.learning.tree.genetic.mutation.TreeBasedMutationOperatorVisitor;

public class TreeBasedCrossoverOperator {

	private static TreeBasedCrossoverOperator INSTANCE = new TreeBasedCrossoverOperator();
	
	Logger log = Logger.getLogger(TreeBasedCrossoverOperator.class);
	
	private TreeBasedCrossoverOperator() {
		
	}
	
	public static TreeBasedCrossoverOperator getInstance() {
		return INSTANCE;
	}
	
	
	
	public Set<CandidateSolutionTree> crossover(CandidateSolutionTree[] parents,
			List<IAttribute> sourceProperties, List<IAttribute> targetProperties, Map<IAttribute, Map<IAttribute, List<IValueMatchingFunction<? extends Object>>>> mapApplicableFunctions) {
		if(parents.length!=2) throw new IllegalArgumentException("Number of parents: "+parents.length);
		
		DecisionNode[] rootNodes = new DecisionNode[2];
		
		Set<CandidateSolutionTree> results = new HashSet<CandidateSolutionTree>();
		
		for(int i=0;i<2;i++) {
			rootNodes[i] = (DecisionNode)parents[i].getGenotype().getRootNode().getCopy();
		}
		
		TreeNodeCrossover crossoverOperator = new TreeNodeCrossover(
				sourceProperties, 
				targetProperties, 
				mapApplicableFunctions);
		
		DecisionNode[] childrenRootNodes = crossoverOperator.crossoverNodes(rootNodes);
		CandidateSolutionTree child;
		GenotypeTree childGenotype;
		
		CandidateSolutionTree result;
		for(int i=0;i<2;i++) {
			childGenotype = new GenotypeTree(parents[0].getApplicationContext(), sourceProperties, targetProperties);
			childGenotype.setRootNode(childrenRootNodes[i]);
			child = new CandidateSolutionTree(parents[0].getApplicationContext(), childGenotype, sourceProperties, targetProperties);
			results.add(child);
			
			result = child;
			
			if(result.getGenotype().getRootNode() instanceof OrNode) {
				OrNode rootNode = (OrNode)result.getGenotype().getRootNode();
				
				List<TreeNode> children = rootNode.getChildren();
				if(children.size()>1) {
					if((children.get(0) instanceof AtomicDecisionNode)&&(children.get(1) instanceof AtomicDecisionNode)) {
						if(((AtomicDecisionNode)children.get(0)).getAggregationNode().comparesSameAttributesAs(((AtomicDecisionNode)children.get(1)).getAggregationNode())) {
							log.error("Crossover error");
						}
					}
				}
				
			}
			
		}
		
		return results;
	}
	
}
