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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.ComparisonPair;
import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;

public abstract class LogicalNode extends DecisionNode {

	public LogicalNode() {
		super();
	}

	@Override
	public boolean isChildAllowed(TreeNode child) {
		if(child instanceof DecisionNode) {
			return true;
		}
		return false;
	}

	@Override
	public void normalizeWeights() {
		for(TreeNode child : this.getChildren()) {
			if(child instanceof DecisionNode) {
				((DecisionNode)child).normalizeWeights();
			}
		}
	}

	@Override
	public Set<ObjectContextModel> getModelSpecs(ApplicationContext context) {
		
		Set<ObjectContextModel> modelSpecs = new HashSet<ObjectContextModel>();
		
		for(TreeNode child : this.getChildren()) {
			if(child instanceof DecisionNode) {
				modelSpecs.addAll(((DecisionNode)child).getModelSpecs(context));
			}
		}
		return modelSpecs;
	}

	@Override
	public Set<IAttribute> getSourceAttributes() {
		Set<IAttribute> attributes = new HashSet<IAttribute>();
		for(TreeNode child : this.children) {
			if(child instanceof DecisionNode) {
				attributes.addAll(((DecisionNode)child).getSourceAttributes());
			}
		}
		return attributes;
	}

	@Override
	public Set<IAttribute> getTargetAttributes() {
		Set<IAttribute> attributes = new HashSet<IAttribute>();
		for(TreeNode child : this.children) {
			if(child instanceof DecisionNode) {
				attributes.addAll(((DecisionNode)child).getTargetAttributes());
			}
		}
		return attributes;
	}

		
	
}
