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

import java.util.Map;

import uk.ac.open.kmi.fusion.api.impl.ComparisonPair;
import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;

public class AndNode extends LogicalNode {

	public AndNode() {
		super();
	}

	@Override
	public boolean isEquivalentPair(
			ComparisonPair pair) {
		
		this.minimalPassingSimilarity = 0.0;
		
		double minSimilarity = 1.0;
		for(TreeNode child : children) {
			if(child instanceof DecisionNode) {
				if(!((DecisionNode)child).isEquivalentPair(pair)) {
					return false;
				} else {
					minSimilarity = Math.min(minSimilarity, ((DecisionNode)child).getLastMinimalPassingSimilarity());
				}
			}
		}
		
		this.minimalPassingSimilarity = minSimilarity;
		return true;
	}
	
	@Override
	public String toString() {
		StringBuffer res = new StringBuffer();
		if(this.children.size()>0) {
			res.append("(");
			res.append(this.children.get(0).toString());
			res.append(")");
			
			for(int i=1;i<this.children.size();i++) {
				res.append(" AND ");
				res.append("(");
				res.append(this.children.get(i).toString());
				res.append(")");
			}
		}
		
		return res.toString(); 
	}

	@Override
	protected AndNode createCopy() {

		AndNode copy = new AndNode();
		return copy;
	}
	
	@Override
	public void visit(IUnaryTreeOperationVisitor visitor) {
		visitor.visit(this);
	}

}
