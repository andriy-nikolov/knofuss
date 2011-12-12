package uk.ac.open.kmi.fusion.learning;

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
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.AtomicAttribute;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;
import uk.ac.open.kmi.fusion.api.impl.VariableComparisonSpecification;
import uk.ac.open.kmi.fusion.api.impl.aggregation.AggregationFunctionFactory;
import uk.ac.open.kmi.fusion.api.impl.valuematching.ValueMatchingFunctionFactory;
import uk.ac.open.kmi.fusion.learning.cache.MemoryInstanceCache;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.IFitnessFunction;

public class CandidateSolution {

	private static final double pPropertyPairCompare = 0.5;
	private static final int initialLimit = 1;
		
	ObjectContextModel modelSpec;
	IFitnessFunction fitness;
	Genotype genotype;
	
	Map<Integer, Double> solutions = null;
	
	Logger log = Logger.getLogger(CandidateSolution.class);
			
	public Genotype getGenotype() {
		return genotype;
	}
	
	public void setGenotype(Genotype genotype) {
		this.genotype = genotype;
	}
	
	public CandidateSolution(ApplicationContext context, Genotype genotype, List<AtomicAttribute> sourceProperties, List<AtomicAttribute> targetProperties) {
		String currentSourceVarName, currentTargetVarName;
		int currentSourceVarIndex = 0, currentTargetVarIndex = 0;
		
		Double[][] genotypeWeights = genotype.getGenotypeWeights();
		
		IValueMatchingFunction[][] genotypeFunctions = genotype.getGenotypeFunctions();
		double threshold = genotype.getThreshold();
		
		//genotype = new Genotype(sourceProperties.size(), targetProperties.size());
		//genotype.setThreshold(threshold);
		//genotype.setGenotypeWeights(genotypeWeights);
		//genotype.setGenotypeFunctions(genotypeFunctions);
		
		genotype.normalizeWeights();
		this.genotype = genotype;
		
		modelSpec = new ObjectContextModel();
		modelSpec.setEnvironment(FusionEnvironment.getInstance());
		modelSpec.setThreshold(threshold);
		modelSpec.setApplicationContext(context);
		modelSpec.setAggregationFunction(genotype.getAggregationFunction());
		//modelSpec.setAggregationFunction(AggregationFunctionFactory.getInstance("max"));
		
		setModelSpec(modelSpec);
		VariableComparisonSpecification specification;
		AtomicAttribute sourceProperty, targetProperty;
		
		Map<Integer, String> sourceVariableByProperty = new HashMap<Integer, String>();
		Map<Integer, String> targetVariableByProperty = new HashMap<Integer, String>();
		
		String str;
		int tmpIndex = 0;
		for(int i=0;i<sourceProperties.size();i++) {
			sourceProperty = sourceProperties.get(i);
			for(int j=0;j<targetProperties.size();j++) {
				targetProperty = targetProperties.get(j);
				if(genotypeFunctions[i][j]!=null) {
					specification = new VariableComparisonSpecification(modelSpec);
					//if(!sourceVariableByProperty.containsKey(i)) {
					//	currentSourceVarName = "var"+currentSourceVarIndex;
					//	currentSourceVarIndex++;
						// str = SPARQLUtils.expandPath("uri", sourceProperty.getPropertyPath(), currentSourceVarName, tmpIndex);
					//	tmpIndex+=2;
						//specification.setSourcePath(str);
						specification.setSourceAttribute(sourceProperty);
					//	sourceVariableByProperty.put(i, currentSourceVarName);
					//} else {
					//	currentSourceVarName = sourceVariableByProperty.get(i);
					//}
					//if(!targetVariableByProperty.containsKey(j)) {
					//	currentTargetVarName = "var"+currentTargetVarIndex;
					//	currentTargetVarIndex++;
						// str = SPARQLUtils.expandPath("uri", targetProperty.getPropertyPath(), currentTargetVarName, tmpIndex);
					//	tmpIndex+=2;
						// specification.setTargetPath(str);
						specification.setTargetAttribute(targetProperty);
					//	targetVariableByProperty.put(j, currentTargetVarName);
					//} else {
					//	currentTargetVarName = targetVariableByProperty.get(j);
					//}
					//specification.setSourceVariableName(currentSourceVarName);
					//specification.setVariableName(currentTargetVarName);
					specification.setValueMatchingFunction(genotypeFunctions[i][j]);
					specification.setWeight(genotypeWeights[i][j]);
					
					if(specification.getSourceAttribute()==null) {
						System.out.println();
					}
					
					modelSpec.addVariableComparisonSpecification(specification);
					
				}
				
			}
		}
		
		modelSpec.prepare();
		
	}

	public ObjectContextModel getModelSpec() { 
		return modelSpec;
	}

	public void setModelSpec(ObjectContextModel spec) {
		this.modelSpec = spec;
	}

	public IFitnessFunction getFitness() {
		return fitness;
	}

	public void setFitness(IFitnessFunction fitness) {
		this.fitness = fitness;
	}

	private Double[][] getGenotypeWeights() {
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
	}

	public static CandidateSolution createRandom(ApplicationContext applicationContext, List<AtomicAttribute> sourceProperties, List<AtomicAttribute> targetProperties, Map<AtomicAttribute, Map<AtomicAttribute, List<IValueMatchingFunction>>> mapApplicableFunctions, boolean aligned) {
		int currentVarIndex = 0;
		String currentVarName;
		VariableComparisonSpecification specification;
		
		List<AtomicAttribute> sourcePropertiesShuffled = new ArrayList<AtomicAttribute>(sourceProperties);
		List<AtomicAttribute> targetPropertiesShuffled = new ArrayList<AtomicAttribute>(targetProperties);
		
		Collections.shuffle(sourcePropertiesShuffled);
		Collections.shuffle(targetPropertiesShuffled);
		AtomicAttribute sourceProperty, targetProperty;
		Set<Integer> alreadyUsedSourceProperties = new HashSet<Integer>();
		Set<Integer> alreadyUsedTargetProperties = new HashSet<Integer>();
		
		Double[][] genotypeWeightsCurrent = new Double[sourceProperties.size()][targetProperties.size()];
		IValueMatchingFunction[][] genotypeFunctionsCurrent = new IValueMatchingFunction[sourceProperties.size()][targetProperties.size()];
		
		Map<AtomicAttribute, List<IValueMatchingFunction>> validFunctionsMap;
		
		int k, l;
		for(int i = 0; i < sourceProperties.size(); i++) {
			sourceProperty = sourcePropertiesShuffled.get(i);
			k = sourceProperties.indexOf(sourceProperty);
			for(int j = 0; j < targetProperties.size(); j++) {
				targetProperty = targetPropertiesShuffled.get(j);
				l = targetProperties.indexOf(targetProperty);
				genotypeWeightsCurrent[k][l]=0.0;
				genotypeFunctionsCurrent[k][l]=null;
				
				if((aligned)
						&&(!sourceProperty.getPropertyPath().equals(targetProperty.getPropertyPath()))) {
					continue;
				}
				
				if(mapApplicableFunctions.get(sourceProperty).get(targetProperty).isEmpty()) continue;
				
				if(alreadyUsedSourceProperties.contains(k)) continue;
				if(alreadyUsedTargetProperties.contains(l)) continue;
				
				if((Math.random()<=pPropertyPairCompare)&&(alreadyUsedSourceProperties.size()<initialLimit)) {
					genotypeWeightsCurrent[k][l] = Math.random();
					//genotypeWeightsCurrent[k][l] = 1;
					genotypeFunctionsCurrent[k][l] = ValueMatchingFunctionFactory.getRandomInstanceForAttributes(sourceProperty, targetProperty);
					//genotypeFunctionsCurrent[k][l] = ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.JARO_WINKLER);
					alreadyUsedSourceProperties.add(k);
					alreadyUsedTargetProperties.add(l);
				} 
			}
		}
		
		if(alreadyUsedSourceProperties.isEmpty()) {
			k = (int)(Math.random()*sourceProperties.size());
			l = (int)(Math.random()*targetProperties.size());
			genotypeWeightsCurrent[k][l] = Math.random();
			//genotypeWeightsCurrent[k][l] = 1;
			//genotypeFunctionsCurrent[k][l] = ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.JARO_WINKLER);
			genotypeFunctionsCurrent[k][l] = ValueMatchingFunctionFactory.getRandomInstanceForAttributes(sourceProperties.get(k), targetProperties.get(l));
		}
		
		double threshold = Math.random();
		
		Genotype genotype = new Genotype(sourceProperties.size(), targetProperties.size());
		genotype.setGenotypeFunctions(genotypeFunctionsCurrent);
		genotype.setGenotypeWeights(genotypeWeightsCurrent);
		genotype.setThreshold(threshold);
		genotype.setAggregationFunction(AggregationFunctionFactory.getRandomInstance());
		
		CandidateSolution solution = new CandidateSolution(applicationContext, genotype, sourceProperties, targetProperties);
		
		return solution;
		
	}
		
	/*public Map<Integer, Double> applySolution(MemoryInstanceCache cache) {
		
		
		return applySolution(cache, false);
	}*/
	
	public Map<Integer, Double> applySolution(MemoryInstanceCache cache, boolean useSampling, boolean isFinal) {
		if(!isFinal) {
			if((!useSampling)||(solutions==null)) {
				// ObjectContextModelMatcherForGenetic matcher = new ObjectContextModelMatcherForGenetic();
				// ObjectContextModelMatcherForGeneticExperimental matcher = new ObjectContextModelMatcherForGeneticExperimental();
				
				// ObjectContextModelMatcherForGeneticExperimental2 matcher = new ObjectContextModelMatcherForGeneticExperimental2();
				ContextModelMatcherForGeneticNeighborhoodGrowth matcher = new ContextModelMatcherForGeneticNeighborhoodGrowth();
				
				matcher.setObjectContextModel(modelSpec);
				
				solutions = matcher.execute(modelSpec.getThreshold(), cache, useSampling, isFinal);
				// solutions = matcher.execute(modelSpec.getThreshold(), cache, useSampling);
			} else {
				log.info(this.toString());
			}
		} else {
			// ObjectContextModelMatcherForGenetic matcher = new ObjectContextModelMatcherForGenetic();
			// ObjectContextModelMatcherForGeneticExperimental matcher = new ObjectContextModelMatcherForGeneticExperimental();
			// ObjectContextModelMatcherForGeneticExperimental2 matcher = new ObjectContextModelMatcherForGeneticExperimental2();
			ContextModelMatcherForGeneticNeighborhoodGrowth matcher = new ContextModelMatcherForGeneticNeighborhoodGrowth();
			
			matcher.setObjectContextModel(modelSpec);
			
			solutions = matcher.execute(modelSpec.getThreshold(), cache, useSampling, isFinal);
			// solutions = matcher.execute(modelSpec.getThreshold(), cache, useSampling);
		}
		 
		return solutions;
	}

	@Override
	public String toString() {
		
		return modelSpec.toString();
	}
	
	public void cutThreshold(Map<Integer, Double> solutionResults) {
		double threshold = this.modelSpec.getThreshold();
		double averageSimilarity = 0;
		if(solutionResults.size()>0) {
			for(Integer res : solutionResults.keySet()) {
				averageSimilarity += solutionResults.get(res);
			}
			
			averageSimilarity = averageSimilarity / solutionResults.size();
			
			double std = 0;
			double val;
			for(Integer res : solutionResults.keySet()) {
				val = averageSimilarity - solutionResults.get(res);
				std += val*val;
			}
			
			std = std/solutionResults.size();
			log.info("Threshold: "+threshold+", std: "+std+", changed: "+(averageSimilarity-10*std));
			std = Math.sqrt(std/solutionResults.size());
			threshold = Math.max(threshold, averageSimilarity-10*std);
			this.modelSpec.setThreshold(threshold);
		}
	}
	
}
