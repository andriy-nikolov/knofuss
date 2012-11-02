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
package uk.ac.open.kmi.fusion.learning.tree;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.AtomicMapping;
import uk.ac.open.kmi.fusion.api.impl.valuematching.ValueMatchingFunctionFactory;
import uk.ac.open.kmi.fusion.learning.CandidateSolutionFitnessResult;
import uk.ac.open.kmi.fusion.learning.ChartPoint2D;
import uk.ac.open.kmi.fusion.learning.cache.CachedPair;
import uk.ac.open.kmi.fusion.learning.cache.MemoryInstanceCache;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.F1Fitness;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.IFitnessFunction;
import uk.ac.open.kmi.fusion.learning.tree.genetic.crossover.TreeBasedCrossoverOperator;
import uk.ac.open.kmi.fusion.learning.tree.genetic.mutation.TreeBasedMutationOperator;
// import uk.ac.open.kmi.fusion.learning.genetic.fitness.UnsupervisedFitnessNeighbourhoodGrowth;

public class CandidateSolutionPoolMultiThreadTreeBased {

	private class CandidateSolutionComparatorTree implements
			Comparator<CandidateSolutionTree> {

		@Override
		public int compare(CandidateSolutionTree arg0, CandidateSolutionTree arg1) {
			
			return (arg0.getFitness().getValue()>arg1.getFitness().getValue())?-1:((arg0.getFitness().getValue()<arg1.getFitness().getValue())?1:0);
			
		}

	}

	private int populationSize = 1000;
	private double crossoverRate = 0.3;
	
	private double mutationRate = 0.6;
	private int maxIterations = 20;

	private final CandidateSolutionComparatorTree comparator = new CandidateSolutionComparatorTree(); 
	
	private List<CandidateSolutionTree> population;
	private List<IAttribute> sourcePropertiesPool;
	private List<IAttribute> targetPropertiesPool;
	private Set<Integer> goldStandardSet;
	private Set<Integer> sampleGoldStandard;
	
	private String criterion = GeneticAlgorithmObjectIdentificationMethodTreeBased.CRITERION_PSEUDO_F_MEASURE;
	
	private ApplicationContext context;
	
	private Map<Integer, Double> rouletteWheel;
	
	private List<ChartPoint2D> fitnessCorrelationPoints;
	private List<ChartPoint2D> unsupervisedFitnessPoints;
	private List<ChartPoint2D> bestUnsupervisedFitnessPoints;
	private List<ChartPoint2D> realFitnessPoints;
	private List<ChartPoint2D> bestRealFitnessPoints;
	private List<ChartPoint2D> bestRealFitnessCorrespondingToBestUnsupervisedFitnessPoints;
	
	Map<IAttribute, Map<IAttribute, List<IValueMatchingFunction<? extends Object>>>> mapApplicableFunctions;

	private TreeBasedCrossoverOperator crossoverOperator;
	private TreeBasedMutationOperator mutationOperator;
	
	private MemoryInstanceCache cache;
	
	private Map<Integer, Double> finalSolutionResults;
	
	private CandidateSolutionTree finalSolution = null;
	
	private static Logger log = Logger.getLogger(CandidateSolutionPoolMultiThreadTreeBased.class);
	
	private boolean useUnsupervisedFitness = true;
	private boolean useThresholdCut = false;
	
	private boolean aligned = false;
	
	private boolean completed = false;
	
	private int maxIterationsWithoutIncrease = 10;
	
	private int N_THREADS = 10;
	
	private ExecutorService threadPool;
	
	// private boolean useSampling = false;
	
	public CandidateSolutionPoolMultiThreadTreeBased(ApplicationContext context, List<IAttribute> sourcePropertiesPool, List<IAttribute> targetPropertiesPool, MemoryInstanceCache cache) {
		this.context = context;
		this.cache = cache;
		population = new ArrayList<CandidateSolutionTree>(populationSize);
		setSourcePropertiesPool(sourcePropertiesPool);
		setTargetPropertiesPool(targetPropertiesPool);
		
		rouletteWheel = new HashMap<Integer, Double>();
		
		fitnessCorrelationPoints = new LinkedList<ChartPoint2D>();
		unsupervisedFitnessPoints = new LinkedList<ChartPoint2D>();
		realFitnessPoints = new LinkedList<ChartPoint2D>();
		
		bestUnsupervisedFitnessPoints = new LinkedList<ChartPoint2D>();
		bestRealFitnessCorrespondingToBestUnsupervisedFitnessPoints = new LinkedList<ChartPoint2D>();
		bestRealFitnessPoints = new LinkedList<ChartPoint2D>();
		mapApplicableFunctions = new HashMap<IAttribute, Map<IAttribute, List<IValueMatchingFunction<? extends Object>>>>();
		
		this.crossoverOperator = TreeBasedCrossoverOperator.getInstance();
		this.mutationOperator = TreeBasedMutationOperator.getInstance();
		
		this.threadPool = Executors.newFixedThreadPool(N_THREADS);
	}
	
	public Map<Integer, Double> run(boolean useSampling) {
		
		if(!useSampling) {
			sampleGoldStandard = goldStandardSet;
		}
		
		Map<IAttribute, List<IValueMatchingFunction<? extends Object>>> validFunctionsMap;
		List<IValueMatchingFunction<? extends Object>> validFunctions;
		for(IAttribute attribute1 : this.sourcePropertiesPool) {
			validFunctionsMap = new HashMap<IAttribute, List<IValueMatchingFunction<? extends Object>>>();
			for(IAttribute attribute2 : this.targetPropertiesPool) {
				validFunctions = ValueMatchingFunctionFactory.getApplicableFunctionsForAttributes(attribute1, attribute2);
				validFunctionsMap.put(attribute2, validFunctions);
			}
			this.mapApplicableFunctions.put(attribute1, validFunctionsMap);
		}
		
		// initialize population
		completed = false;
				
		initializePopulation();
		// run
		Map<Integer, Double> solutionResults;
		
		double averageUnsupervisedFitness = 0, previousAverageUnsupervisedFitness = 0;
		double averageRealFitness = 0, previousAverageRealFitness = 0;
		double epsilon = 0.000001;
		int numberOfIterations = 0;
		F1Fitness realFitness;
		//UnsupervisedFitness unsupervisedFitness;
		IFitnessFunction unsupervisedFitness;
		//UnsupervisedFitnessNeighbourhoodGrowth unsupervisedFitnessNG;
		
		// UnsupervisedFitnessNeighbourhoodGrowth unsupervisedFitness;
	
		int iterations = 0;
		int solutions;
		CandidateSolutionTree bestSolution = null;
		double bestRealFitnessValue = 0;
		F1Fitness bestRealFitness = null;
		F1Fitness chosenMaxRealFitness = null;
		double bestUnsupervisedFitness = 0;
		
		double previousBestFitness = 0;
		
		double originalMutationRate = mutationRate;
		
		List<CandidateSolutionTree> crossoverResults, mutationResults;
		int currentIndex;
		
		Map<Integer, CandidateSolutionFitnessResult> mapResults = new HashMap<Integer, CandidateSolutionFitnessResult>();
		List<Future<CandidateSolutionFitnessResult>> futureResults = new ArrayList<Future<CandidateSolutionFitnessResult>>(population.size());
		
		Future<CandidateSolutionFitnessResult> futureResult;
		CandidateSolutionFitnessResult solutionResultMeasurement;
		
		CandidateSolutionTree solution;
		CandidateSolutionPoolEvaluatorRunnableTreeBased solutionEvaluationTask;
		
		while(((numberOfIterations<maxIterationsWithoutIncrease)&&(iterations<maxIterations))/*||((bestUnsupervisedFitness<0.9)&&(iterations<100))*/) {
			averageUnsupervisedFitness = 0;
			averageRealFitness = 0;
			iterations++;
			bestRealFitnessValue = 0;
			bestUnsupervisedFitness = 0;
			
			futureResults.clear();
			
			for(int i=0;i<population.size();i++) {
				solution = population.get(i);
				solutionEvaluationTask = new CandidateSolutionPoolEvaluatorRunnableTreeBased(iterations, i, solution, cache, sampleGoldStandard, useSampling, useUnsupervisedFitness);
				solutionEvaluationTask.setCriterion(this.criterion);
				futureResult = threadPool.submit(solutionEvaluationTask);
				futureResults.add(futureResult);
			}
				
			for(int i=0;i<population.size();i++) {	
				futureResult = futureResults.get(i);
				solution = population.get(i);
				try {
					solutionResultMeasurement = futureResult.get();
					realFitness = solutionResultMeasurement.getRealFitness();
					unsupervisedFitness = solutionResultMeasurement.getUnsupervisedFitness();
					
					if(i==10) {
						log.info("Top 10");
					}
					
					fitnessCorrelationPoints.add(new ChartPoint2D(realFitness.getValue(), unsupervisedFitness.getValue()));
					
					//averageFitness+=fitness.getValue();
					averageUnsupervisedFitness += unsupervisedFitness.getValue();
					averageRealFitness += realFitness.getValue();
					//solution.setFitness(realFitness);
					if(useUnsupervisedFitness) {
						solution.setFitness(unsupervisedFitness);
					} else {
						solution.setFitness(realFitness);
					}
					if(realFitness.getValue() >= bestRealFitnessValue) {
						if(!useUnsupervisedFitness) {
							previousBestFitness = bestRealFitnessValue;
							bestSolution = solution;
						}
						bestRealFitnessValue = realFitness.getValue();
						bestRealFitness = realFitness;
					}
					
					if(unsupervisedFitness.getValue() >= bestUnsupervisedFitness) {
						if(useUnsupervisedFitness) {
							previousBestFitness = bestUnsupervisedFitness;
							bestSolution = solution;
						}
						chosenMaxRealFitness = realFitness;
						bestUnsupervisedFitness = unsupervisedFitness.getValue();
					}
					
					
				} catch(ExecutionException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
			if(bestSolution.getFitness().getValue()==0.0) {
				log.info("Could not find any good solution");
				this.finalSolution = population.get(0);
				this.completed = true;
				return new HashMap<Integer, Double>();
			}
			averageUnsupervisedFitness = averageUnsupervisedFitness/population.size();
			averageRealFitness = averageRealFitness/population.size();
			log.info("Average unsupervised fitness: "+averageUnsupervisedFitness);
			log.info("Average actual fitness: "+averageRealFitness);
			updateRouletteWheel();
			if((averageUnsupervisedFitness-previousAverageUnsupervisedFitness<epsilon)/*||(bestSolution.getFitness().getValue()-previousBestFitness<epsilon)*/) {
				numberOfIterations ++;
				
				//mutationRate += (1-originalMutationRate)*3/(maxIterationsWithoutIncrease);
				log.info("New mutation rate: "+mutationRate);
				
			} else {
				numberOfIterations = 0;
				//mutationRate = originalMutationRate;
			}
			if(averageUnsupervisedFitness > previousAverageUnsupervisedFitness) {
				previousAverageUnsupervisedFitness = averageUnsupervisedFitness;
			}
			previousAverageRealFitness = averageRealFitness;
			
			this.unsupervisedFitnessPoints.add(new ChartPoint2D((double)iterations, averageUnsupervisedFitness));
			this.realFitnessPoints.add(new ChartPoint2D((double)iterations, averageRealFitness));
			
			this.bestRealFitnessPoints.add(new ChartPoint2D((double)iterations, bestRealFitnessValue));
			this.bestUnsupervisedFitnessPoints.add(new ChartPoint2D((double)iterations, bestUnsupervisedFitness));
			this.bestRealFitnessCorrespondingToBestUnsupervisedFitnessPoints.add(new ChartPoint2D((double)iterations, chosenMaxRealFitness.getValue()));
			
			log.info("crossover...");
			crossoverResults = doCrossover();
			log.info("mutation...");
			mutationResults = doMutation();
			currentIndex = 0;
			for(CandidateSolutionTree tmpSolution : mutationResults) {
				if(currentIndex>=population.size()) break;
				population.set(population.size()-1-currentIndex, tmpSolution);
				currentIndex++;
			}
			for(CandidateSolutionTree tmpSolution : crossoverResults) {
				if(currentIndex>=population.size()) break;
				population.set(population.size()-1-currentIndex, tmpSolution);
				currentIndex++;
			}
			population.set(0, bestSolution);
			
			log.info((maxIterationsWithoutIncrease-numberOfIterations)+" before stop if average fitness does not increase...");
		}
		
		this.finalSolution = bestSolution;
		
		threadPool.shutdown();
		
		log.info("Final average unsupervised fitness: "+averageUnsupervisedFitness);
		log.info("Best real fitness (sample): "+bestRealFitnessValue+", precision: "+bestRealFitness.getPrecision()+", recall: "+bestRealFitness.getRecall());
		log.info("Best unsupervised fitness: "+bestUnsupervisedFitness+", pseudo precision: "+bestSolution.getFitness().getPrecision()+", pseudo recall: "+bestSolution.getFitness().getRecall());
		log.info("Best real fitness corresponding to the best unsupervised fitness (sample): "+chosenMaxRealFitness.getValue()+", precision: "+chosenMaxRealFitness.getPrecision()+", recall: "+chosenMaxRealFitness.getRecall());
		log.info("Best solution: "+bestSolution.toString());
		
		
		this.finalSolutionResults = bestSolution.applySolution(cache, false, true, criterion);
		realFitness = this.evaluateFitness(finalSolution, finalSolutionResults.keySet(), goldStandardSet);
		log.info("Best real fitness corresponding to the best unsupervised fitness: "+realFitness.getValue()+", precision: "+realFitness.getPrecision()+", recall: "+realFitness.getRecall());
		
		processCorrelationsForTestPurposes();
		writePointsToFileForTestPurposes(unsupervisedFitnessPoints, "logs/unsupervised.dat");
		writePointsToFileForTestPurposes(realFitnessPoints, "logs/actualFitness.dat");
		
		writePointsToFileForTestPurposes(bestRealFitnessPoints, "logs/bestActualFitness.dat");
		writePointsToFileForTestPurposes(bestUnsupervisedFitnessPoints, "logs/bestUnsupervisedFitness.dat");
		
		writePointsToFileForTestPurposes(bestRealFitnessCorrespondingToBestUnsupervisedFitnessPoints, "logs/correspondingRealFitness.dat");
		
		writeFalsePositivesSolutionResultsToFileForTestPurposes(finalSolutionResults, bestSolution);
		writeFalseNegativesSolutionResultsToFileForTestPurposes(finalSolutionResults, bestSolution);
		
		// processFitnessByType(this.finalSolutionResults);
		
		completed = true;
		
		return finalSolutionResults;
		
	}

	private void initializePopulation() {
		for(int i = 0;i<populationSize;i++) {
			CandidateSolutionTree solution = CandidateSolutionTree.createRandom(context, sourcePropertiesPool, targetPropertiesPool, mapApplicableFunctions, aligned);
			while(solution.toString().contains("()")) {
				solution = CandidateSolutionTree.createRandom(context, sourcePropertiesPool, targetPropertiesPool, mapApplicableFunctions, aligned);
			}
			population.add(solution);
		}
		
		/*for(int i = 0;i<POPULATION_SIZE;i++) {
			population.add(CandidateSolution.createRandom(context, sourcePropertiesPool, targetPropertiesPool));
		}
		
		Genotype g = new Genotype(sourcePropertiesPool.size(), targetPropertiesPool.size());
		g.setThreshold(0.95);
		
		double[][] weights = g.getGenotypeWeights();
		IValueMatchingFunction[][] functions = g.getGenotypeFunctions();
		
		int i = sourcePropertiesPool.indexOf("<http://purl.org/vocab/bio/0.1/event>/<http://purl.org/vocab/bio/0.1/date>");
		int j = targetPropertiesPool.indexOf(Utils.DBPEDIA_ONTOLOGY_NS+"birthDate");
		
		weights[i][j] = 0.5;
		functions[i][j] = ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.MONGE_ELKAN);
		
		i = sourcePropertiesPool.indexOf(Utils.FOAF_NS+"name");
		j = targetPropertiesPool.indexOf(RDFS.LABEL.toString());
		
		weights[i][j] = 0.5;
		functions[i][j] = ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.L2_JARO_WINKLER);
		
		CandidateSolution solution = new CandidateSolution(context, g, sourcePropertiesPool, targetPropertiesPool);
		population.add(solution);
		
		Map<Integer, Double> res = solution.applySolution(cache);
		
		UnsupervisedFitness fitness = UnsupervisedFitness.getUnsupervisedFitness(res, cache);
		log.info("Unsupervised: "+fitness.getValue()+", precision: "+fitness.getPrecision()+", recall: "+fitness.getRecall());
		F1Fitness realFitness = F1Fitness.getF1Fitness(goldStandardSet, res.keySet());
		log.info("F1: "+realFitness.getValue()+", precision: "+realFitness.getPrecision()+", recall: "+realFitness.getRecall());*/
		
	}
	
	
	
	public String getCriterion() {
		return criterion;
	}

	public void setCriterion(String criterion) {
		this.criterion = criterion;
	}

	private F1Fitness evaluateFitness(CandidateSolutionTree solution, Set<Integer> solutionResults, Set<Integer> goldStandard) {
		return F1Fitness.getF1Fitness(goldStandard, solutionResults);
		
	}
	
	public void setTargetPropertiesPool(List<IAttribute> targetPropertiesPool) {
		this.targetPropertiesPool = targetPropertiesPool;
	}

	public List<IAttribute> getTargetPropertiesPool() {
		return targetPropertiesPool;
	}

	public void setSourcePropertiesPool(List<IAttribute> sourcePropertiesPool) {
		this.sourcePropertiesPool = sourcePropertiesPool;
	}

	public List<IAttribute> getSourcePropertiesPool() {
		return sourcePropertiesPool;
	}

	public ApplicationContext getContext() {
		return context;
	}

	public void setContext(ApplicationContext context) {
		this.context = context;
	}
	
	public Set<Integer> getGoldStandardSet() {
		return goldStandardSet;
	}

	public void setGoldStandardSet(Set<Integer> goldStandardSet) {
		this.goldStandardSet = goldStandardSet;
	}

	private CandidateSolutionTree[] selectParentsForCrossover() {
		CandidateSolutionTree[] parents = new CandidateSolutionTree[2];

		int i1 = selectByRouletteWheel();
		int i2 = i1;
		while(i2==i1) {
			i2 = selectByRouletteWheel();
		}
		parents[0] = population.get(i1);
		parents[1] = population.get(i2);
		return parents; 
	}
	
	private List<CandidateSolutionTree> doCrossover() {
		
		int pairs = (int)(populationSize*crossoverRate*0.5);
		List<CandidateSolutionTree> crossoverResults = new ArrayList<CandidateSolutionTree>(pairs*2);
		
		CandidateSolutionTree[] parents;
		Set<CandidateSolutionTree> children;
		for(int i=0;i<pairs;i++) {
			parents = selectParentsForCrossover();
			
			children = this.crossoverOperator.crossover(parents, sourcePropertiesPool, targetPropertiesPool, mapApplicableFunctions);
			crossoverResults.addAll(children);
			
		}
		
		return crossoverResults;


	}
	
	private List<CandidateSolutionTree> doMutation() {
		int mutationSize = (int)(populationSize*mutationRate);
		List<CandidateSolutionTree> mutationResults = new ArrayList<CandidateSolutionTree>(mutationSize);
		int selected;
		for(int i=0;i<mutationSize;i++) {
			selected = selectByRouletteWheel();
			mutationResults.add(this.mutationOperator.mutate(population.get(selected), sourcePropertiesPool, targetPropertiesPool, this.mapApplicableFunctions, aligned));
		}
		
		return mutationResults;
		
	}
	
	private int selectByRouletteWheel() {
		double value = Math.random();
		
		for(int i=0;i<population.size();i++) {
			if(value<rouletteWheel.get(i)) {
				return i;
			}
		}
		
		return 0;
	}
	
	private void updateRouletteWheel() {
		Collections.sort(population, comparator);
		
		double totalFitness = 0;
		for(CandidateSolutionTree solution : population) {
			totalFitness += solution.getFitness().getValue();
		}
		if(totalFitness==0.0) {
			
			double runningSum = 0;
			for(int i = 0 ; i<population.size();i++) {
				runningSum+=1/population.size();
				rouletteWheel.put(i, runningSum);
			}
		} else {
		
			double runningSum = 0;
			for(int i = 0 ; i<population.size();i++) {
				runningSum+=population.get(i).getFitness().getValue()/totalFitness;
				rouletteWheel.put(i, runningSum);
			}
		}
		
	}
	
	private void writeFalseNegativesSolutionResultsToFileForTestPurposes(Map<Integer, Double> solutionResults, CandidateSolutionTree solution) {
		try {
			PrintWriter writer = Utils.openPrintFileWriter("logs/test-false-negatives.xml");
			
			try {
				int i = 0;
				
				
				Iterator<CachedPair> iterator = cache.getAllPairsIterator();
				
				Set<Integer> falseNegatives = new HashSet<Integer>(goldStandardSet);
				
				CachedPair cachedPair;
				
				while(iterator.hasNext()) {
					cachedPair=iterator.next();
					if(solutionResults.containsKey(cachedPair.getId())
							&&(goldStandardSet.contains(cachedPair.getId()))) {
						falseNegatives.remove(cachedPair.getId());
					}
					
				}
				
				CachedPair cp;
				AtomicMapping mapping;
				
				writer.println("<?xml version=\"1.0\" encoding=\"windows-1251\"?>");
				writer.println("<DocElement>");
				writer.println("<ResultSet id=\"false-negatives\">");
				
				for(Integer pairId : falseNegatives) {
					i++;
					cp = cache.getCachedPairById(pairId);
					mapping = cp.convertToAtomicMapping(solution.getAlternativeModelSpecs(), 0, solution.getFitness().getPrecision());
					
					mapping.writeToXML(writer);
				}
				
				writer.println("</DocElement>");
				log.info("Written "+i+" false negatives");
			} finally {
				writer.close();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void writeFalsePositivesSolutionResultsToFileForTestPurposes(Map<Integer, Double> solutionResults, CandidateSolutionTree solution) {
		try {
			
				List<CachedPair> falsePositives = new ArrayList<CachedPair>();
				
				Iterator<CachedPair> iterator = cache.getAllPairsIterator();
				
				CachedPair cachedPair;
				
				while(iterator.hasNext()) {
					cachedPair=iterator.next();
					
					if(solutionResults.containsKey(cachedPair.getId())
						&&(!goldStandardSet.contains(cachedPair.getId()))) {
						falsePositives.add(cachedPair);
					}
					
				}
				
			PrintWriter writer = Utils.openPrintFileWriter("logs/test-false-positives.xml");	
			
			try {
			
				writer.println("<?xml version=\"1.0\" encoding=\"windows-1251\"?>");
				writer.println("<DocElement>");
				writer.println("<ResultSet id=\"false-positives\">");
				AtomicMapping mapping;
				for(CachedPair pair : falsePositives) {
					mapping = pair.convertToAtomicMapping(solution.getAlternativeModelSpecs(), solutionResults.get(pair.getId()), solution.getFitness().getPrecision());
					mapping.writeToXML(writer);
				}
				
				writer.println("</DocElement>");
				
				log.info("Written "+falsePositives.size()+" false positives");
			} finally {
				writer.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void writePointsToFileForTestPurposes(List<ChartPoint2D> list, String filePath) {
		try {
			PrintWriter writer = Utils.openPrintFileWriter(filePath);
			try {
				double averageX = 0, averageY = 0;
				for(ChartPoint2D point : list) {
					point.writeToDat(writer);
					
					averageX += point.getLeft();
					averageY += point.getRight();
				}
			} finally {
				writer.close();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
		
	private void processCorrelationsForTestPurposes() {
		
		writePointsToFileForTestPurposes(fitnessCorrelationPoints, "logs/correlation.dat");
		
		double averageX = 0, averageY = 0;
		for(ChartPoint2D point : fitnessCorrelationPoints) {
		
			averageX += point.getLeft();
			averageY += point.getRight();
		}
		
		averageX = averageX / fitnessCorrelationPoints.size();
		averageY = averageY / fitnessCorrelationPoints.size();
			
		// Calculate correlation
		log.info("Average x: "+averageX+", average y: "+averageY);
			
		double sum = 0;
		double sumSqX = 0, sumSqY = 0;
		double diffX, diffY;
		for(ChartPoint2D point : fitnessCorrelationPoints) {
			diffX = point.getLeft() - averageX;
			diffY = point.getRight() - averageY;
			sum += diffX * diffY;
			sumSqX += diffX * diffX;
			sumSqY += diffY * diffY;
		}
			
		double correlation = sum / Math.sqrt(sumSqX * sumSqY);
		
		log.info("Correlation between supervised and unsupervised: "+correlation);

	}

	public Map<Integer, Double> getFinalSolutionResults() {
		if(!completed) {
			throw new IllegalStateException("Selection not completed yet");
		}
		return finalSolutionResults;
	}

	public CandidateSolutionTree getFinalSolution() {
		if(!completed) {
			throw new IllegalStateException("Selection not completed yet");
		}
		return finalSolution;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	public boolean isUseUnsupervisedFitness() {
		return useUnsupervisedFitness;
	}

	public void setUseUnsupervisedFitness(boolean useUnsupervisedFitness) {
		this.useUnsupervisedFitness = useUnsupervisedFitness;
	}

	public double getCrossoverRate() {
		return crossoverRate;
	}

	public void setCrossoverRate(double crossoverRate) {
		this.crossoverRate = crossoverRate;
	}

	public double getMutationRate() {
		return mutationRate;
	}

	public void setMutationRate(double mutationRate) {
		this.mutationRate = mutationRate;
	}

	public Set<Integer> getSampleGoldStandard() {
		return sampleGoldStandard;
	}

	public void setSampleGoldStandard(Set<Integer> sampleGoldStandard) {
		this.sampleGoldStandard = sampleGoldStandard;
	}

	public boolean isAligned() {
		return aligned;
	}

	public void setAligned(boolean aligned) {
		this.aligned = aligned;
	}
	
	
	
}
