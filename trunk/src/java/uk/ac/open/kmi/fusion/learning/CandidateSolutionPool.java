package uk.ac.open.kmi.fusion.learning;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openrdf.model.vocabulary.RDFS;

import uk.ac.open.kmi.common.utils.Utils;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.AtomicMapping;
import uk.ac.open.kmi.fusion.api.impl.AtomicAttribute;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;
import uk.ac.open.kmi.fusion.api.impl.valuematching.JaroWinklerValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.valuematching.ValueMatchingFunctionFactory;
import uk.ac.open.kmi.fusion.learning.cache.CachedPair;
import uk.ac.open.kmi.fusion.learning.cache.MemoryInstanceCache;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.DefaultFitnessFunction;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.F1Fitness;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.IFitnessFunction;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.UnsupervisedFitness;
// import uk.ac.open.kmi.fusion.learning.genetic.fitness.UnsupervisedFitnessNeighbourhoodGrowth;
// import uk.ac.open.kmi.fusion.learning.genetic.fitness.UnsupervisedFitnessNeighbourhoodGrowth;
import uk.ac.open.kmi.fusion.learning.genetic.crossover.CrossoverOperatorFactory;
import uk.ac.open.kmi.fusion.learning.genetic.crossover.ICrossoverOperator;
import uk.ac.open.kmi.fusion.learning.genetic.mutation.IMutationOperator;
import uk.ac.open.kmi.fusion.learning.genetic.mutation.MutationOperatorFactory;

public class CandidateSolutionPool {

	private class CandidateSolutionComparator implements
			Comparator<CandidateSolution> {

		@Override
		public int compare(CandidateSolution arg0, CandidateSolution arg1) {
			
			return (arg0.getFitness().getValue()>arg1.getFitness().getValue())?-1:((arg0.getFitness().getValue()<arg1.getFitness().getValue())?1:0);
			
		}

	}

	private int populationSize = 1000;
	private double crossoverRate = 0.3;
	
	private double mutationRate = 0.6;
	private int maxIterations = 20;

	private final CandidateSolutionComparator comparator = new CandidateSolutionComparator(); 
	
	private List<CandidateSolution> population;
	private List<IAttribute> sourcePropertiesPool;
	private List<IAttribute> targetPropertiesPool;
	private Set<Integer> goldStandardSet;
	private Set<Integer> sampleGoldStandard;
	
	private ApplicationContext context;
	
	private Map<Integer, Double> rouletteWheel;
	
	private List<ChartPoint2D> fitnessCorrelationPoints;
	private List<ChartPoint2D> unsupervisedFitnessPoints;
	private List<ChartPoint2D> bestUnsupervisedFitnessPoints;
	private List<ChartPoint2D> realFitnessPoints;
	private List<ChartPoint2D> bestRealFitnessPoints;
	private List<ChartPoint2D> bestRealFitnessCorrespondingToBestUnsupervisedFitnessPoints;
	
	Map<IAttribute, Map<IAttribute, List<IValueMatchingFunction<? extends Object>>>> mapApplicableFunctions;

	private ICrossoverOperator crossoverOperator;
	private IMutationOperator mutationOperator;
	
	private MemoryInstanceCache cache;
	
	private Map<Integer, Double> finalSolutionResults;
	
	private CandidateSolution finalSolution = null;
	
	private static Logger log = Logger.getLogger(CandidateSolutionPool.class);
	
	private boolean useUnsupervisedFitness = true;
	private boolean useThresholdCut = false;
	
	private boolean aligned = false;
	
	private boolean completed = false;
	
	private int maxIterationsWithoutIncrease = 10;
	
	// private boolean useSampling = false;
	
	public CandidateSolutionPool(ApplicationContext context, List<IAttribute> sourcePropertiesPool, List<IAttribute> targetPropertiesPool, MemoryInstanceCache cache) {
		this.context = context;
		this.cache = cache;
		population = new ArrayList<CandidateSolution>(populationSize);
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
		
		this.crossoverOperator = CrossoverOperatorFactory.getInstance(ICrossoverOperator.CROSSOVER_SPLIT_BY_MATRIX_INDEX);
		this.mutationOperator = MutationOperatorFactory.getInstance(IMutationOperator.MUTATION_DEFAULT);
		
		
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
		UnsupervisedFitness unsupervisedFitness;
		// UnsupervisedFitnessNeighbourhoodGrowth unsupervisedFitness;
		// UnsupervisedFitnessNeighbourhoodGrowth unsupervisedFitness;
	
		int iterations = 0;
		int solutions;
		CandidateSolution bestSolution = null;
		double bestRealFitnessValue = 0;
		F1Fitness bestRealFitness = null;
		F1Fitness chosenMaxRealFitness = null;
		double bestUnsupervisedFitness = 0;
		
		double previousBestFitness = 0;
		
		double originalMutationRate = mutationRate;
		
		List<CandidateSolution> crossoverResults, mutationResults;
		int currentIndex;
		while(((numberOfIterations<maxIterationsWithoutIncrease)&&(iterations<maxIterations))/*||((bestUnsupervisedFitness<0.9)&&(iterations<100))*/) {
			averageUnsupervisedFitness = 0;
			averageRealFitness = 0;
			iterations++;
			solutions = 0;
			bestRealFitnessValue = 0;
			bestUnsupervisedFitness = 0;
			for(CandidateSolution solution : population) {
				solutions ++;
				solutionResults = solution.applySolution(cache, useSampling, false);
				realFitness = this.evaluateFitness(solution, solutionResults.keySet(), sampleGoldStandard);
				unsupervisedFitness = UnsupervisedFitness.calculateUnsupervisedFitness(solution, solutionResults, cache);
				// unsupervisedFitness = UnsupervisedFitnessNeighbourhoodGrowth.calculateUnsupervisedFitness(solution, solutionResults, cache);
				// unsupervisedFitness = UnsupervisedFitnessNeighbourhoodGrowth.calculateUnsupervisedFitness(solution, solutionResults, cache);
				
				if(solutions==10) {
					log.info("Top 10");
				}
				/*if(solution==testSolution) {
					log.debug();
					writeSolutionResultsToFileForTestPurposes(solutionResults);
				}*/
				log.info("Iteraton: "+iterations+", solution: "+solutions+", results: "+solutionResults.size());
				log.info("F1 fitness: "+realFitness.getF1()+", precision: "+realFitness.getPrecision()+", recall: "+realFitness.getRecall());
				log.info("Unsupervised fitness: "+unsupervisedFitness.getValue()+", pseudo precision: "+unsupervisedFitness.getPrecision()+", pseudo recall: "+unsupervisedFitness.getRecall());
				
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
			for(CandidateSolution tmpSolution : mutationResults) {
				if(currentIndex>=population.size()) break;
				population.set(population.size()-1-currentIndex, tmpSolution);
				currentIndex++;
			}
			for(CandidateSolution tmpSolution : crossoverResults) {
				if(currentIndex>=population.size()) break;
				population.set(population.size()-1-currentIndex, tmpSolution);
				currentIndex++;
			}
			population.set(0, bestSolution);
			
			log.info((maxIterationsWithoutIncrease-numberOfIterations)+" before stop if average fitness does not increase...");
		}
		
		this.finalSolution = bestSolution;
		
		log.info("Final average unsupervised fitness: "+averageUnsupervisedFitness);
		log.info("Best real fitness (sample): "+bestRealFitnessValue+", precision: "+bestRealFitness.getPrecision()+", recall: "+bestRealFitness.getRecall());
		log.info("Best unsupervised fitness: "+bestUnsupervisedFitness+", pseudo precision: "+bestSolution.getFitness().getPrecision()+", pseudo recall: "+bestSolution.getFitness().getRecall());
		log.info("Best real fitness corresponding to the best unsupervised fitness (sample): "+chosenMaxRealFitness.getValue()+", precision: "+chosenMaxRealFitness.getPrecision()+", recall: "+chosenMaxRealFitness.getRecall());
		log.info("Best solution: "+bestSolution.getModelSpec().toString());
		
		
		this.finalSolutionResults = bestSolution.applySolution(cache, false, true);
		realFitness = this.evaluateFitness(finalSolution, finalSolutionResults.keySet(), goldStandardSet);
		log.info("Best real fitness corresponding to the best unsupervised fitness: "+realFitness.getValue()+", precision: "+realFitness.getPrecision()+", recall: "+realFitness.getRecall());
		
		if(this.useUnsupervisedFitness&&this.useThresholdCut) {
			log.info("Now applying the threshold cut...");
					
			// this.finalSolutionResults = bestSolution.applySolution(cache);
			
			this.finalSolution.cutThreshold(finalSolutionResults);
			
			this.finalSolutionResults = bestSolution.applySolution(cache, false, true);
			
			realFitness = this.evaluateFitness(finalSolution, finalSolutionResults.keySet(), goldStandardSet);
			unsupervisedFitness = UnsupervisedFitness.calculateUnsupervisedFitness(finalSolution, finalSolutionResults, cache);
			// unsupervisedFitness = UnsupervisedFitnessNeighbourhoodGrowth.calculateUnsupervisedFitness(finalSolution, finalSolutionResults, cache);
			// unsupervisedFitness = UnsupervisedFitnessNeighbourhoodGrowth.calculateUnsupervisedFitness(finalSolution, finalSolutionResults, cache);
			
			log.info("Best unsupervised fitness after threshold cut-off: "+unsupervisedFitness.getValue()+", pseudo precision: "+unsupervisedFitness.getPrecision()+", pseudo recall: "+unsupervisedFitness.getRecall());
			log.info("Best real fitness corresponding to the best unsupervised fitness after threshold cut-off: "+realFitness.getValue()+", precision: "+realFitness.getPrecision()+", recall: "+realFitness.getRecall());
			log.info("New threshold: "+this.finalSolution.getModelSpec().getThreshold());
		}
		
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
			CandidateSolution solution = CandidateSolution.createRandom(context, sourcePropertiesPool, targetPropertiesPool, mapApplicableFunctions, aligned);
			while((solution.getModelSpec().getVariableComparisonSpecifications().size()==0)||(solution.toString().contains("()"))) {
				solution = CandidateSolution.createRandom(context, sourcePropertiesPool, targetPropertiesPool, mapApplicableFunctions, aligned);
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
	
	private F1Fitness evaluateFitness(CandidateSolution solution, Set<Integer> solutionResults, Set<Integer> goldStandard) {
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

	private CandidateSolution[] selectParentsForCrossover() {
		CandidateSolution[] parents = new CandidateSolution[2];

		int i1 = selectByRouletteWheel();
		int i2 = i1;
		while(i2==i1) {
			i2 = selectByRouletteWheel();
		}
		parents[0] = population.get(i1);
		parents[1] = population.get(i2);
		return parents; 
	}
	
	private List<CandidateSolution> doCrossover() {
		
		int pairs = (int)(populationSize*crossoverRate*0.5);
		List<CandidateSolution> crossoverResults = new ArrayList<CandidateSolution>(pairs*2);
		
		CandidateSolution[] parents;
		Set<CandidateSolution> children;
		for(int i=0;i<pairs;i++) {
			parents = selectParentsForCrossover();
			
			children = this.crossoverOperator.crossover(parents, sourcePropertiesPool, targetPropertiesPool);
			crossoverResults.addAll(children);
			
		}
		
		return crossoverResults;


	}
	
	private List<CandidateSolution> doMutation() {
		int mutationSize = (int)(populationSize*mutationRate);
		List<CandidateSolution> mutationResults = new ArrayList<CandidateSolution>(mutationSize);
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
		for(CandidateSolution solution : population) {
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
	
	private void writeFalseNegativesSolutionResultsToFileForTestPurposes(Map<Integer, Double> solutionResults, CandidateSolution solution) {
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
					mapping = cp.convertToAtomicMapping(solution.getModelSpec(), 0, solution.getFitness().getPrecision());
					
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
	
	private void writeFalsePositivesSolutionResultsToFileForTestPurposes(Map<Integer, Double> solutionResults, CandidateSolution solution) {
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
					mapping = pair.convertToAtomicMapping(solution.getModelSpec(), solutionResults.get(pair.getId()), solution.getFitness().getPrecision());
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

	public CandidateSolution getFinalSolution() {
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
