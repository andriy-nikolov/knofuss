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
