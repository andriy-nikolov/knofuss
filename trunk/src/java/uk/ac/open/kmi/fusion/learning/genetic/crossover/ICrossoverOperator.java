package uk.ac.open.kmi.fusion.learning.genetic.crossover;

import java.util.List;
import java.util.Set;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.learning.CandidateSolution;

public interface ICrossoverOperator {
	
	public static final int CROSSOVER_SPLIT_BY_MATRIX_INDEX = 0;

	public abstract Set<CandidateSolution> crossover(CandidateSolution[] parents, List<IAttribute> sourceProperties, List<IAttribute> targetProperties);

}
