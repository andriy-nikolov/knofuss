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
import java.util.List;

public abstract class TreeNode {

	// GenotypeTree genotype;
	
	TreeNode parent;
	List<TreeNode> children;
	
	public TreeNode() {
		this.children = new ArrayList<TreeNode>();
		// this.genotype = genotype;
	}

	public TreeNode getParent() {
		return parent;
	}

	public void setParent(TreeNode parent) {
		if(this.parent!=null) {
			this.parent.removeChild(this);
		}
		if(this.parent!=parent) {
			if(parent!=null) {
				if(!parent.getChildren().contains(this)) {
					if(!parent.isChildAllowed(this)) {
						throw new IllegalArgumentException("Incompatible classes. Parent: "+parent.getClass().getSimpleName()+", child: "+this.getClass().getSimpleName());
					}
					this.parent.addChild(this);
				}
			}
			this.parent = parent;
		}
	}

	public List<TreeNode> getChildren() {
		return children;
	}
	
	public void addChild(TreeNode child) {
		if(!children.contains(child)) {
			if(!isChildAllowed(child)) {
				throw new IllegalArgumentException("Incompatible classes. Parent: "+this.getClass().getSimpleName()+", child: "+child.getClass().getSimpleName());
			}
			children.add(child);
			child.setParent(this);
		}
	}
	
	public void replaceChild(TreeNode toRemove, TreeNode toReplace) {
		if(!isChildAllowed(toReplace)) {
			throw new IllegalArgumentException("Incompatible classes. Parent: "+this.getClass().getSimpleName()+", child: "+toReplace.getClass().getSimpleName());
		}
		removeChild(toRemove);
		addChild(toReplace);
	}
	
	public void removeChild(TreeNode child) {
		this.children.remove(child);
	}
	
	public abstract boolean isChildAllowed(TreeNode child);

	protected abstract TreeNode createCopy();
	
	public TreeNode getCopy() {
		
		TreeNode copy = createCopy();
		
		for(TreeNode child : children) {
			copy.addChild(child.getCopy());
		}
		
		return copy;
	}

	public abstract void visit(IUnaryTreeOperationVisitor visitor);
	
}
