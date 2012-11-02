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
package uk.ac.open.kmi.fusion.learning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;
import uk.ac.open.kmi.fusion.api.impl.VariableComparisonSpecification;
import uk.ac.open.kmi.fusion.api.impl.aggregation.AggregationFunctionFactory;
import uk.ac.open.kmi.fusion.api.impl.valuematching.ValueMatchingFunctionFactory;
import uk.ac.open.kmi.fusion.learning.cache.MemoryInstanceCache;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.IFitnessFunction;
// import uk.ac.open.kmi.fusion.api.impl.AtomicAttribute;

public class CandidateSolution {

	private static final double pPropertyPairCompare = 0.5;
	private static final int initialLimit = 1;
		
	ObjectContextModel modelSpec;
	IFitnessFunction fitness;
	Genotype genotype;
	
	Map<Integer, Double> solutions = null;
	
	double validPairsRatio = 1.0;
	
	Logger log = Logger.getLogger(CandidateSolution.class);
			
	public Genotype getGenotype() {
		return genotype;
	}
	
	public void setGenotype(Genotype genotype) {
		this.genotype = genotype;
	}
	
	public CandidateSolution(ApplicationContext context, Genotype genotype, List<IAttribute> sourceProperties, List<IAttribute> targetProperties) {
		
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
		IAttribute sourceProperty, targetProperty;
		
		for(int i=0;i<sourceProperties.size();i++) {
			sourceProperty = sourceProperties.get(i);
			for(int j=0;j<targetProperties.size();j++) {
				targetProperty = targetProperties.get(j);
				if(genotypeFunctions[i][j]!=null) {
					specification = new VariableComparisonSpecification(modelSpec);

					specification.setSourceAttribute(sourceProperty);
					specification.setTargetAttribute(targetProperty);
					specification.setValueMatchingFunction(genotypeFunctions[i][j]);
					specification.setWeight(genotypeWeights[i][j]);
					
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

	public static CandidateSolution createRandom(ApplicationContext applicationContext, List<IAttribute> sourceProperties, List<IAttribute> targetProperties, Map<IAttribute, Map<IAttribute, List<IValueMatchingFunction<? extends Object>>>> mapApplicableFunctions, boolean aligned) {
				
		List<IAttribute> sourcePropertiesShuffled = new ArrayList<IAttribute>(sourceProperties);
		List<IAttribute> targetPropertiesShuffled = new ArrayList<IAttribute>(targetProperties);
		
		Collections.shuffle(sourcePropertiesShuffled);
		Collections.shuffle(targetPropertiesShuffled);
		IAttribute sourceProperty, targetProperty;
		Set<Integer> alreadyUsedSourceProperties = new HashSet<Integer>();
		Set<Integer> alreadyUsedTargetProperties = new HashSet<Integer>();
		
		Double[][] genotypeWeightsCurrent = new Double[sourceProperties.size()][targetProperties.size()];
		IValueMatchingFunction[][] genotypeFunctionsCurrent = new IValueMatchingFunction[sourceProperties.size()][targetProperties.size()];
		
		Map<IAttribute, List<IValueMatchingFunction>> validFunctionsMap;
		
		int k, l;
		for(int i = 0; i < sourceProperties.size(); i++) {
			sourceProperty = sourcePropertiesShuffled.get(i);
			k = sourceProperties.indexOf(sourceProperty);
			for(int j = 0; j < targetProperties.size(); j++) {
				targetProperty = targetPropertiesShuffled.get(j);
				l = targetProperties.indexOf(targetProperty);
				genotypeWeightsCurrent[k][l]=0.0;
				genotypeFunctionsCurrent[k][l]=null;
				
				if(aligned) {
					if(!sourceProperty.samePropertyPathAs(targetProperty)) {
						continue;
					}
				}
			
				if(mapApplicableFunctions.get(sourceProperty).get(targetProperty).isEmpty()) continue;
				
				if(alreadyUsedSourceProperties.contains(k)) continue;
				if(alreadyUsedTargetProperties.contains(l)) continue;
				
				if((Math.random()<=pPropertyPairCompare)&&(alreadyUsedSourceProperties.size()<initialLimit)) {
					genotypeWeightsCurrent[k][l] = Math.random();
					genotypeFunctionsCurrent[k][l] = ValueMatchingFunctionFactory.getRandomInstanceForAttributes(sourceProperty, targetProperty);
					alreadyUsedSourceProperties.add(k);
					alreadyUsedTargetProperties.add(l);
				} 
			}
		}
		
		if(alreadyUsedSourceProperties.isEmpty()) {
			k = (int)(Math.random()*sourceProperties.size());
			l = (int)(Math.random()*targetProperties.size());
			genotypeWeightsCurrent[k][l] = Math.random();
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
		

	public Map<Integer, Double> applySolution(MemoryInstanceCache cache, boolean useSampling, boolean isFinal, String criterion) {
		if(!isFinal) {
			if((!useSampling)||(solutions==null)) {
				
				if(criterion.equals(GeneticAlgorithmObjectIdentificationMethod.CRITERION_NEIGHBOURHOOD_GROWTH)
						||criterion.equals(GeneticAlgorithmObjectIdentificationMethod.CRITERION_UNBIASED_NEIGHBOURHOOD_GROWTH)) {
					ContextModelMatcherForGeneticNeighborhoodGrowth matcher = new ContextModelMatcherForGeneticNeighborhoodGrowth();
					matcher.setObjectContextModel(modelSpec);
					solutions = matcher.execute(modelSpec.getThreshold(), cache, useSampling, isFinal);
					this.validPairsRatio = matcher.getValidPairsRatio();
				} else {
				
					ObjectContextModelMatcherForGenetic matcher = new ObjectContextModelMatcherForGenetic();
										
					matcher.setObjectContextModel(modelSpec);
					solutions = matcher.execute(modelSpec.getThreshold(), cache, useSampling);
				}
			} else {
				log.info(this.toString());
			}
		} else {
			if(criterion.equals(GeneticAlgorithmObjectIdentificationMethod.CRITERION_NEIGHBOURHOOD_GROWTH)
					||criterion.equals(GeneticAlgorithmObjectIdentificationMethod.CRITERION_UNBIASED_NEIGHBOURHOOD_GROWTH)) {
				ContextModelMatcherForGeneticNeighborhoodGrowth matcher = new ContextModelMatcherForGeneticNeighborhoodGrowth();
				matcher.setObjectContextModel(modelSpec);
				solutions = matcher.execute(modelSpec.getThreshold(), cache, useSampling, isFinal);
			} else {
				ObjectContextModelMatcherForGenetic matcher = new ObjectContextModelMatcherForGenetic();
				matcher.setObjectContextModel(modelSpec);
				solutions = matcher.execute(modelSpec.getThreshold(), cache, useSampling);
			}

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

	public double getValidPairsRatio() {
		return validPairsRatio;
	}

	
}
