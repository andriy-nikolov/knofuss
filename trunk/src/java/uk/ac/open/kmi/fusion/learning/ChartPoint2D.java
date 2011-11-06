package uk.ac.open.kmi.fusion.learning;

import java.io.PrintWriter;

import uk.ac.open.kmi.fusion.api.impl.Pair;

public class ChartPoint2D extends Pair<Double, Double> {

	public ChartPoint2D() {
		// TODO Auto-generated constructor stub
	}

	public ChartPoint2D(Double left, Double right) {
		super(left, right);
		// TODO Auto-generated constructor stub
	}

	public void writeToDat(PrintWriter writer) {
		writer.println(this.getLeft().toString()+"\t"+this.getRight().toString());
	}
	
}
