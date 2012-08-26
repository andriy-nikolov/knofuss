package uk.ac.open.kmi.fusion.learning.tree;

import java.util.ArrayList;
import java.util.List;

import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;

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
