package uk.ac.open.kmi.fusion.learning.genetic.mutation;

public class MutationOperatorFactory {

	public MutationOperatorFactory() {
		
	}

	public static IMutationOperator getInstance(int mutationOperatorType) {
		switch(mutationOperatorType) {
		case IMutationOperator.MUTATION_DEFAULT:
			return DefaultMutationOperator.getInstance();
		default:
			return DefaultMutationOperator.getInstance();
		}
	}
	
}
