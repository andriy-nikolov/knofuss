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
package uk.ac.open.kmi.fusion.learning.tree;

import java.util.HashSet;

import java.util.Set;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.ComparisonPair;
import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;

public class AtomicDecisionNode extends DecisionNode {

	public AtomicDecisionNode() {
		super();
	}

	@Override
	public boolean isChildAllowed(TreeNode child) {
		if(this.children.size()<2) {
			if((child instanceof AggregationNode)||(child instanceof ThresholdNode)) {
				return true;
			}
		}
		
		return false;
	}
	
	public AggregationNode getAggregationNode() {
		for(TreeNode child : children) {
			if(child instanceof AggregationNode) {
				return (AggregationNode)child;
			}
		}
		
		throw new IllegalStateException("Aggregation node has not been set");
	}
	
	public ThresholdNode getThresholdNode() {
		for(TreeNode child : children) {
			if(child instanceof ThresholdNode) {
				return (ThresholdNode)child;
			}
		}
		
		throw new IllegalStateException("Threshold node has not been set");
	}
	
	public void setThreshold(double threshold) {
		ThresholdNode node = new ThresholdNode();
		node.setThreshold(threshold);
		setThresholdNode(node);
	}
	
	public void setThresholdNode(ThresholdNode node) {
		try {
			ThresholdNode oldThresholdNode = getThresholdNode();
			removeChild(oldThresholdNode);
		} catch(IllegalStateException e) {
			
		}
		
		this.addChild(node);
		
	}
	
	public void setAggregationNode(AggregationNode node) {
		try {
			AggregationNode oldAggregationNode = getAggregationNode();
			removeChild(oldAggregationNode);
		} catch(IllegalStateException e) {
			
		}
		
		this.addChild(node);
		
	}
	

	@Override
	public boolean isEquivalentPair(
			ComparisonPair pair) {
		AggregationNode aggregationNode = this.getAggregationNode();
		ThresholdNode thresholdNode = this.getThresholdNode();
		
		double sim = aggregationNode.getModelSpec().getSimilarity(pair);
		if(sim >= thresholdNode.getThreshold()) {
			this.minimalPassingSimilarity = sim;
			return true;
		}
		this.minimalPassingSimilarity = 0.0;
		return false;
	}
	
	

	@Override
	public String toString() {
		
		AggregationNode aggregationNode = this.getAggregationNode();
		ThresholdNode thresholdNode = this.getThresholdNode();
		
		StringBuffer res = new StringBuffer(aggregationNode.toString());
		res.append(">=");
		res.append(thresholdNode.toString());
		
		return res.toString();
	}

	@Override
	protected AtomicDecisionNode createCopy() {
		AtomicDecisionNode copy = new AtomicDecisionNode();
		
		return copy;
	}

	@Override
	public void normalizeWeights() {
		this.getAggregationNode().normalizeWeights();	
	}

	@Override
	public Set<ObjectContextModel> getModelSpecs(ApplicationContext context) {
		ObjectContextModel modelSpec = this.getAggregationNode().getModelSpec();
		modelSpec.setApplicationContext(context);
		modelSpec.setThreshold(this.getThresholdNode().getThreshold());
		modelSpec.prepare();
		Set<ObjectContextModel> modelSpecs = new HashSet<ObjectContextModel>(1);
		modelSpecs.add(modelSpec);
		return modelSpecs;
	}

	@Override
	public Set<IAttribute> getSourceAttributes() {
		return this.getAggregationNode().getSourceAttributes();
	}

	@Override
	public Set<IAttribute> getTargetAttributes() {
		return this.getAggregationNode().getTargetAttributes();
	}

	@Override
	public void visit(IUnaryTreeOperationVisitor visitor) {
		visitor.visit(this);		
	}
	
}
