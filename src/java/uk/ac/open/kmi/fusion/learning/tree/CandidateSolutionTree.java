package uk.ac.open.kmi.fusion.learning.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openjena.atlas.logging.Log;


import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.common.utils.sparql.SPARQLUtils;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
// import uk.ac.open.kmi.fusion.api.impl.AtomicAttribute;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;
import uk.ac.open.kmi.fusion.api.impl.TransformationAttribute;
import uk.ac.open.kmi.fusion.api.impl.VariableComparisonSpecification;
import uk.ac.open.kmi.fusion.api.impl.aggregation.AggregationFunctionFactory;
import uk.ac.open.kmi.fusion.api.impl.valuematching.ValueMatchingFunctionFactory;
import uk.ac.open.kmi.fusion.learning.ContextModelMatcherForGeneticNeighborhoodGrowth;
import uk.ac.open.kmi.fusion.learning.GeneticAlgorithmObjectIdentificationMethod;
import uk.ac.open.kmi.fusion.learning.ObjectContextModelMatcherForGenetic;
import uk.ac.open.kmi.fusion.learning.cache.MemoryInstanceCache;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.IFitnessFunction;

public class CandidateSolutionTree {

	private static final double pPropertyPairCompare = 0.5;
	private static final int initialLimit = 1;
		
	IFitnessFunction fitness;
	GenotypeTree genotype;
	
	ObjectContextModelTree modelSpec;
	ApplicationContext applicationContext;
	
	Map<Integer, Double> solutions = null;
	
	
	
	Logger log = Logger.getLogger(CandidateSolutionTree.class);
			
	public GenotypeTree getGenotype() {
		return genotype;
	}
	
	public void setGenotype(GenotypeTree genotype) {
		this.genotype = genotype;
	}
	
	public CandidateSolutionTree(ApplicationContext context, GenotypeTree genotype, List<IAttribute> sourceProperties, List<IAttribute> targetProperties) {
		
		genotype.normalizeWeights();
		this.genotype = genotype;
		this.applicationContext = context;
		
		modelSpec = new ObjectContextModelTree();
		modelSpec.setGenotypeTree(genotype);
		modelSpec.setApplicationContext(context);
	}

	public IFitnessFunction getFitness() {
		return fitness;
	}

	public void setFitness(IFitnessFunction fitness) {
		this.fitness = fitness;
	}

	/*private Double[][] getGenotypeWeights() {
		return genotype.getGenotypeWeights();
	}

	private void setGenotypeWeights(Double[][] genotypeWeights) {
		this.genotype.setGenotypeWeights(genotypeWeights);
	}

	private IValueMatchingFunction[][] getGenotypeFunctions() {
		return genotype.getGenotypeFunctions();
	}

	private void setGenotypeFunctions(IValueMatchingFunction[][] genotypeFunctions) {
		this.genotype.setGenotypeFunctions(genotypeFunctions);
	}*/

	public static CandidateSolutionTree createRandom(ApplicationContext applicationContext, List<IAttribute> sourceProperties, List<IAttribute> targetProperties, Map<IAttribute, Map<IAttribute, List<IValueMatchingFunction<? extends Object>>>> mapApplicableFunctions, boolean aligned) {
	
		GenotypeTree genotype = GenotypeTree.createRandom(applicationContext, sourceProperties, targetProperties, mapApplicableFunctions, aligned);
		CandidateSolutionTree solution = new CandidateSolutionTree(applicationContext, genotype, sourceProperties, targetProperties);
		
		return solution;
	}
		

	public Map<Integer, Double> applySolution(MemoryInstanceCache cache, boolean useSampling, boolean isFinal, String criterion) {
		if(!isFinal) {
			if((!useSampling)||(solutions==null)) {
				if(criterion.equals(GeneticAlgorithmObjectIdentificationMethod.CRITERION_PSEUDO_F_MEASURE)) {
					ObjectContextModelMatcherForGeneticTreeBased matcher = new ObjectContextModelMatcherForGeneticTreeBased();
					
					matcher.setModel(modelSpec);
					
					solutions = matcher.execute(cache, useSampling);
					
				} else {
					throw new IllegalArgumentException("Only the pseudo-F-measure criterion is supported for tree-based decision rules");
				}
				
			} else {
				log.info(this.toString());
			}
		} else {
			
			if(criterion.equals(GeneticAlgorithmObjectIdentificationMethod.CRITERION_PSEUDO_F_MEASURE)) {
				ObjectContextModelMatcherForGeneticTreeBased matcher = new ObjectContextModelMatcherForGeneticTreeBased();
				
				matcher.setModel(modelSpec);
				
				solutions = matcher.execute(cache, useSampling);
				
			} else {
				throw new IllegalArgumentException("Only the pseudo-F-measure criterion is supported for tree-based decision rules");
			}

		}
		 
		return solutions;
	}

	@Override
	public String toString() {
		
		return modelSpec.toString();
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public Set<ObjectContextModel> getAlternativeModelSpecs() {
		return this.genotype.getRootNode().getModelSpecs(this.applicationContext);
	}
	
}