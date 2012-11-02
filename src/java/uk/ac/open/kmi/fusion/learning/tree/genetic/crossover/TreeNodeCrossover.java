/* Copyright (c) 2012, Knowledge Media Institute
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   * Neither the name of the <organization> nor the
 *     names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.open.kmi.fusion.learning.tree.genetic.crossover;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import uk.ac.open.kmi.fusion.api.IAggregationFunction;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.learning.tree.AggregationNode;
import uk.ac.open.kmi.fusion.learning.tree.AtomicDecisionNode;
import uk.ac.open.kmi.fusion.learning.tree.AttributeSimilarityNode;
import uk.ac.open.kmi.fusion.learning.tree.DecisionNode;
import uk.ac.open.kmi.fusion.learning.tree.LogicalNode;
import uk.ac.open.kmi.fusion.learning.tree.OrNode;
import uk.ac.open.kmi.fusion.learning.tree.ThresholdNode;
import uk.ac.open.kmi.fusion.learning.tree.TreeNode;

public class TreeNodeCrossover {
	
	private static double P_SWAP_CHILDREN = 0.5;
	
	private static double P_SWAP_ATTRIBUTE_COMPARISONS = 0.5;
	private static double P_SWAP_AGGREGATION_FUNCTIONS = 0.5;
	private static double P_SWAP_THRESHOLDS = 0.5;
	private static double P_SPLIT_BY_ROWS = 0.5;
	private static double P_MERGE = 0.5;
	
	List<IAttribute> sourceAttributes; 
	List<IAttribute> targetAttributes; 
	Map<IAttribute, 
	Map<IAttribute, 
		List<IValueMatchingFunction<? extends Object>>>> 
			mapApplicableFunctions;

	public TreeNodeCrossover(
			List<IAttribute> sourceAttributes, 
			List<IAttribute> targetAttributes, 
			Map<IAttribute, 
			Map<IAttribute, 
				List<IValueMatchingFunction<? extends Object>>>> 
					mapApplicableFunctions) {
		this.sourceAttributes = sourceAttributes;
		this.targetAttributes = targetAttributes;
		this.mapApplicableFunctions = mapApplicableFunctions;
	}
	
	private AggregationNode[] splitXOverGenotypeByRows(
			AggregationNode[] parents, 
			List<IAttribute> sourceAttributes, 
			List<IAttribute> targetAttributes, 
			Map<IAttribute, 
			Map<IAttribute, 
				List<IValueMatchingFunction<? extends Object>>>> 
					mapApplicableFunctions) {
		
		if(parents.length!=2) {
			throw new IllegalArgumentException("Number of parents: "+parents.length);
		}
		
		AggregationNode[] res = new AggregationNode[2];
		
		double weights[][][] = new double[2][sourceAttributes.size()][targetAttributes.size()];
		IValueMatchingFunction[][][] functions = new IValueMatchingFunction[2][sourceAttributes.size()][targetAttributes.size()];
		IAggregationFunction[] aggregationFunctions = new IAggregationFunction[2];
		
		AttributeSimilarityNode tmpNode;
		for(int i=0;i<2;i++) {
			for(int j=0;j<sourceAttributes.size();j++) {
				for(int k=0;k<targetAttributes.size();k++) {
					weights[i][j][k] = 0;
					functions[i][j][k] = null;
				}
			}
			
			aggregationFunctions[i] = parents[i].getAggregationFunction();
			
			for(TreeNode child : parents[i].getChildren()) {
				if(child instanceof AttributeSimilarityNode) {
					tmpNode = (AttributeSimilarityNode)child;
					weights[i][sourceAttributes.indexOf(tmpNode.getSourceAttribute())][targetAttributes.indexOf(tmpNode.getTargetAttribute())] = tmpNode.getWeight();
					functions[i][sourceAttributes.indexOf(tmpNode.getSourceAttribute())][targetAttributes.indexOf(tmpNode.getTargetAttribute())] = tmpNode.getValueMatchingFunction();
				}
			}
		}
		
		double tmpWeight;
		IValueMatchingFunction tmpFunction;
		for(int i=0;i<sourceAttributes.size();i++) {
			if(Math.random()<P_SWAP_ATTRIBUTE_COMPARISONS) {
				for(int j=0;j<targetAttributes.size();j++) {
					tmpWeight = weights[0][i][j];
					tmpFunction = functions[0][i][j];
					weights[0][i][j] = weights[1][i][j];
					functions[0][i][j] = functions[1][i][j];
					weights[1][i][j] = tmpWeight;
					functions[1][i][j] = tmpFunction;				
				}
			}
		}
		
		if(Math.random()<P_SWAP_AGGREGATION_FUNCTIONS) {
			IAggregationFunction tmpAggregationFunction = aggregationFunctions[0];
			aggregationFunctions[0] = aggregationFunctions[1];
			aggregationFunctions[1] = tmpAggregationFunction;
		}
		
		for(int i=0;i<2;i++) {
			res[i] = createAggregationNodeByMatrix(weights[i], functions[i], aggregationFunctions[i]);
			
			if(res[i].getChildren().isEmpty()) {
				res[i].addChild(createRandomAttributeSimilarityNode());
			}
		}
		
		return res;
	}
	
	private AttributeSimilarityNode createRandomAttributeSimilarityNode() {
		
		AttributeSimilarityNode node = new AttributeSimilarityNode();
		
		IAttribute sourceAttribute = null, targetAttribute = null;
		IValueMatchingFunction function;
		
		List<IValueMatchingFunction<? extends Object>> validFunctions = null;
		
		while(true) {
			sourceAttribute = sourceAttributes.get((int)(Math.random()*sourceAttributes.size()));
			targetAttribute = targetAttributes.get((int)(Math.random()*targetAttributes.size()));
			validFunctions = mapApplicableFunctions.get(sourceAttribute).get(targetAttribute);
			if(validFunctions!=null) {
				if(validFunctions.size()>0) {
					node.setSourceAttribute(sourceAttribute);
					node.setTargetAttribute(targetAttribute);
					node.setValueMatchingFunction(validFunctions.get((int)(Math.random()*validFunctions.size())));
					break;
				}
			}
		}
		
		node.setWeight(1.0);
		return node;
	}
	
	private AggregationNode[] splitXOverGenotypeByColumns(
			AggregationNode[] parents, 
			List<IAttribute> sourceAttributes, 
			List<IAttribute> targetAttributes, 
			Map<IAttribute, 
				Map<IAttribute, 
					List<IValueMatchingFunction<? extends Object>>>> 
						mapApplicableFunctions) {
		
		if(parents.length!=2) {
			throw new IllegalArgumentException("Number of parents: "+parents.length);
		}
		
		AggregationNode[] res = new AggregationNode[2];
		
		double weights[][][] = new double[2][sourceAttributes.size()][targetAttributes.size()];
		IValueMatchingFunction[][][] functions = new IValueMatchingFunction[2][sourceAttributes.size()][targetAttributes.size()];
		IAggregationFunction[] aggregationFunctions = new IAggregationFunction[2];
		
		AttributeSimilarityNode tmpNode;
		for(int i=0;i<2;i++) {
			for(int j=0;j<sourceAttributes.size();j++) {
				for(int k=0;k<targetAttributes.size();k++) {
					weights[i][j][k] = 0;
					functions[i][j][k] = null;
				}
			}
			
			aggregationFunctions[i] = parents[i].getAggregationFunction();
			
			for(TreeNode child : parents[i].getChildren()) {
				if(child instanceof AttributeSimilarityNode) {
					tmpNode = (AttributeSimilarityNode)child;
					weights[i][sourceAttributes.indexOf(tmpNode.getSourceAttribute())][targetAttributes.indexOf(tmpNode.getTargetAttribute())] = tmpNode.getWeight();
					functions[i][sourceAttributes.indexOf(tmpNode.getSourceAttribute())][targetAttributes.indexOf(tmpNode.getTargetAttribute())] = tmpNode.getValueMatchingFunction();
				}
			}
		}
		
		double tmpWeight;
		IValueMatchingFunction tmpFunction;
		for(int j=0;j<targetAttributes.size();j++) {
			if(Math.random()<P_SWAP_ATTRIBUTE_COMPARISONS) {
				for(int i=0;i<sourceAttributes.size();i++) {
					tmpWeight = weights[0][i][j];
					tmpFunction = functions[0][i][j];
					weights[0][i][j] = weights[1][i][j];
					functions[0][i][j] = functions[1][i][j];
					weights[1][i][j] = tmpWeight;
					functions[1][i][j] = tmpFunction;				
				}
			}
		}
		
		if(Math.random()<P_SWAP_AGGREGATION_FUNCTIONS) {
			IAggregationFunction tmpAggregationFunction = aggregationFunctions[0];
			aggregationFunctions[0] = aggregationFunctions[1];
			aggregationFunctions[1] = tmpAggregationFunction;
		}
		
		for(int i=0;i<2;i++) {
			res[i] = createAggregationNodeByMatrix(
						weights[i], 
						functions[i], 
						aggregationFunctions[i]);
			
			if(res[i].getChildren().isEmpty()) {
				res[i].addChild(createRandomAttributeSimilarityNode());
			}
		}
		
		return res;
	}
	
	private AggregationNode createAggregationNodeByMatrix(
			double[][] weights, 
			IValueMatchingFunction[][] functions, 
			IAggregationFunction aggregation) {
		
		AggregationNode node = new AggregationNode();
		
		AttributeSimilarityNode tmpNode;
		
		for(int i = 0;i<sourceAttributes.size();i++) {
			for(int j=0;j<targetAttributes.size();j++) {
				if(functions[i][j]!=null) {
					tmpNode = new AttributeSimilarityNode();
					tmpNode.setSourceAttribute(sourceAttributes.get(i));
					tmpNode.setTargetAttribute(targetAttributes.get(j));
					tmpNode.setValueMatchingFunction(functions[i][j]);
					tmpNode.setWeight(weights[i][j]);
					node.addChild(tmpNode);
				}
			}
		}
		
		node.setAggregationFunction(aggregation);
		
		return node;
		
	}
	
	public DecisionNode[] crossoverNodes(DecisionNode[] nodes) {
		
		if(nodes.length!=2) {
			throw new IllegalArgumentException("Number of parents: "+nodes.length);
		}
		
		if((nodes[0] instanceof LogicalNode)&&(nodes[1] instanceof LogicalNode)) {
			// Swap children or crossover children by components
			// Pick a random child
			AtomicDecisionNode[] toXOver = new AtomicDecisionNode[2];
			toXOver[0] = (AtomicDecisionNode)nodes[0].getChildren().get((int)(Math.random()*nodes[0].getChildren().size()));
			toXOver[1] = (AtomicDecisionNode)nodes[1].getChildren().get((int)(Math.random()*nodes[1].getChildren().size()));
			
			boolean redundant[] = new boolean[2];
			redundant[0] = isRedundant(toXOver[0], (LogicalNode)nodes[1]);
			redundant[1] = isRedundant(toXOver[1], (LogicalNode)nodes[0]);
			
			if(Math.random()<P_SWAP_CHILDREN) {
				nodes[0].removeChild(toXOver[0]);
				nodes[1].removeChild(toXOver[1]);
				if(!redundant[0]) {
					nodes[1].addChild(toXOver[0]);
				}
				if(!redundant[1]) {
					nodes[0].addChild(toXOver[1]);
				}
				
				if(nodes[0].getChildren().size()==1) {
					nodes[0] = (DecisionNode)nodes[0].getChildren().get(0);
					nodes[0].setParent(null);
				}
				
				if(nodes[1].getChildren().size()==1) {
					nodes[1] = (DecisionNode)nodes[1].getChildren().get(0);
					nodes[1].setParent(null);
				}
				
			} else {
				AtomicDecisionNode[] crossoverResults = crossoverByComponents(toXOver);
				nodes[0].replaceChild(toXOver[0], crossoverResults[0]);
				nodes[1].replaceChild(toXOver[1], crossoverResults[1]);
			}
			
		} else if((nodes[0] instanceof LogicalNode)&&(nodes[1] instanceof AtomicDecisionNode)) {
			AtomicDecisionNode[] toXOver = new AtomicDecisionNode[2];
			toXOver[0] = (AtomicDecisionNode)nodes[0].getChildren().get((int)(Math.random()*nodes[0].getChildren().size()));
			toXOver[1] = (AtomicDecisionNode)nodes[1];
			if(Math.random()<P_SWAP_CHILDREN) {
				nodes[1] = toXOver[0];
				
				nodes[0].removeChild(toXOver[0]);
				if(!isRedundant(toXOver[1], (LogicalNode)nodes[0])) {
					nodes[0].addChild(toXOver[1]);
				}
				nodes[1].setParent(null);
				
				if(nodes[0].getChildren().size()==1) {
					nodes[0] = (DecisionNode)nodes[0].getChildren().get(0);
					nodes[0].setParent(null);
				}
			} else {
				AtomicDecisionNode[] crossoverResults = crossoverByComponents(toXOver);
				nodes[0].replaceChild(toXOver[0], crossoverResults[0]);
				nodes[1] = crossoverResults[1];
				nodes[1].setParent(null);
			}
			
		} else if((nodes[0] instanceof AtomicDecisionNode)&&(nodes[1] instanceof LogicalNode)) {
			
			DecisionNode tmp = nodes[0];
			nodes[0] = nodes[1];
			nodes[1] = tmp;
			
			nodes = crossoverNodes(nodes);
			
		} else if((nodes[0] instanceof AtomicDecisionNode)&&(nodes[1] instanceof AtomicDecisionNode)) {
			if((Math.random()<P_MERGE)&&(!((AtomicDecisionNode)nodes[0]).getAggregationNode().comparesSameAttributesAs(((AtomicDecisionNode)nodes[1]).getAggregationNode()))) {
				return mergeWithOR(Arrays.copyOf(nodes, nodes.length, AtomicDecisionNode[].class));
			} else {
				return crossoverByComponents(Arrays.copyOf(nodes, nodes.length, AtomicDecisionNode[].class));
			}
		}
		
		for(int i=0;i<2;i++) {
			if(nodes[i] instanceof LogicalNode) {
				nodes[i] = removeRedundancy((LogicalNode)nodes[i]);
			}
		}
		
		return nodes;
	}
	
	private AtomicDecisionNode[] crossoverByComponents(
			AtomicDecisionNode[] nodes) {
		
		AtomicDecisionNode[] results = new AtomicDecisionNode[2];
		
		AggregationNode[] aggregationNodes = new AggregationNode[2];
		
		for(int i=0;i<2;i++) {
			aggregationNodes[i] = nodes[i].getAggregationNode();
			results[i] = (AtomicDecisionNode)nodes[i].getCopy();
		}
		
		if(Math.random()<P_SPLIT_BY_ROWS) {
			aggregationNodes = splitXOverGenotypeByRows(aggregationNodes, sourceAttributes, targetAttributes, mapApplicableFunctions); 
		} else {
			aggregationNodes = splitXOverGenotypeByColumns(aggregationNodes, sourceAttributes, targetAttributes, mapApplicableFunctions);
		}
		
		for(int i=0;i<2;i++) {
			results[i].setAggregationNode(aggregationNodes[i]);
		}
		
		if(Math.random()<P_SWAP_THRESHOLDS) {
			ThresholdNode tmp;
			tmp = results[0].getThresholdNode();
			results[0].setThresholdNode(results[1].getThresholdNode());
			results[1].setThresholdNode(tmp);
		}
		
		return results;
	}
	
	private DecisionNode[] mergeWithOR(AtomicDecisionNode[] nodes) {
		DecisionNode[] results = new DecisionNode[2];
		
		results[0] = new OrNode();
		results[0].addChild(nodes[0]);
		results[0].addChild(nodes[1]);
		
		AtomicDecisionNode[] componentBasedMerged = crossoverByComponents(nodes); 
		
		results[1] = componentBasedMerged[0];
		
		return results;
	}

	public boolean isRedundant(AtomicDecisionNode node, LogicalNode parentNode) {
		// A decision node is useless if there is already a sibling comparing the same set of properties
		
		for(TreeNode child : parentNode.getChildren()) {
			if(node!=child) {
				if(child instanceof AtomicDecisionNode) {
					if(node.getAggregationNode().comparesSameAttributesAs(((AtomicDecisionNode)child).getAggregationNode())) {
						return true;
					}
				}
			}
		}

		
		return false;
	}
	
	public DecisionNode removeRedundancy(LogicalNode node) {
		List<TreeNode> children = node.getChildren();
		if(children.size()>1) {
			if((children.get(0) instanceof AtomicDecisionNode)&&(children.get(1) instanceof AtomicDecisionNode)) {
				if(((AtomicDecisionNode)children.get(0)).getAggregationNode().comparesSameAttributesAs(((AtomicDecisionNode)children.get(1)).getAggregationNode())) {
					return (DecisionNode)children.get(0);
				}
			}
		}
		
		return node;
	
	}
	
}
