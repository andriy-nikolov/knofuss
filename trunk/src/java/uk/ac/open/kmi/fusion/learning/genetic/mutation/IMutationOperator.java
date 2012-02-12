package uk.ac.open.kmi.fusion.learning.genetic.mutation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.learning.CandidateSolution;

public interface IMutationOperator {
	
	public static final int MUTATION_DEFAULT = 0;

	public abstract CandidateSolution mutate(CandidateSolution original, List<IAttribute> sourceProperties, List<IAttribute> targetProperties, Map<IAttribute, Map<IAttribute, List<IValueMatchingFunction<? extends Object>>>> mapApplicableFunctions, boolean aligned);

}
