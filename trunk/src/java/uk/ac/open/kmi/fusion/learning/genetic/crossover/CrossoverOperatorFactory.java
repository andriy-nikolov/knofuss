package uk.ac.open.kmi.fusion.learning.genetic.crossover;

public class CrossoverOperatorFactory {
	
	
	
	public CrossoverOperatorFactory() {
		// TODO Auto-generated constructor stub
	}
	
	public static ICrossoverOperator getInstance(int crossoverOperatorType) {
		switch(crossoverOperatorType) {
		case ICrossoverOperator.CROSSOVER_SPLIT_BY_MATRIX_INDEX:
			return SplitByMatrixIndexCrossoverOperator.getInstance();
			
		default:
			return SplitByMatrixIndexCrossoverOperator.getInstance();
		}
	}

}
