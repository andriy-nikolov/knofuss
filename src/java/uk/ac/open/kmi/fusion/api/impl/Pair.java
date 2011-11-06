package uk.ac.open.kmi.fusion.api.impl;

public class Pair<K, L> {

	private K left;
	private L right;
	
	public Pair() {
		
	}
	
	public Pair(K left, L right) {
		this.left = left;
		this.right = right;
	}

	public K getLeft() {
		return left;
	}

	public void setLeft(K left) {
		this.left = left;
	}

	public L getRight() {
		return right;
	}

	public void setRight(L right) {
		this.right = right;
	}

	
	
}
