package uk.ac.open.kmi.fusion.learning.genetic.mutation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.open.kmi.fusion.api.IAggregationFunction;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.AtomicAttribute;
import uk.ac.open.kmi.fusion.api.impl.aggregation.AggregationFunctionFactory;
import uk.ac.open.kmi.fusion.api.impl.valuematching.ValueMatchingFunctionFactory;
import uk.ac.open.kmi.fusion.learning.CandidateSolution;
import uk.ac.open.kmi.fusion.learning.Genotype;
// import uk.ac.open.kmi.fusion.learning.genetic.fitness.UnsupervisedFitness;

public class DefaultMutationOperator implements IMutationOperator {

	private static IMutationOperator INSTANCE = new DefaultMutationOperator();
	
	Logger log = Logger.getLogger(DefaultMutationOperator.class);
	
	public DefaultMutationOperator() {
		// TODO Auto-generated constructor stub
	}
	
	static IMutationOperator getInstance() {
		return INSTANCE;
	}

	@Override
	public CandidateSolution mutate(CandidateSolution original, List<AtomicAttribute> sourceProperties, List<AtomicAttribute> targetProperties, Map<AtomicAttribute, Map<AtomicAttribute, List<IValueMatchingFunction<? extends Object>>>> mapApplicableFunctions, boolean aligned) {
		
		int rows = sourceProperties.size();
		int columns = targetProperties.size();
		
		Genotype genotype = original.getGenotype();
		
		Genotype childGenotype = new Genotype(rows, columns);
		
		List<Integer> validComponents = new ArrayList<Integer>();
		List<Integer> potentiallyValidComponents = new ArrayList<Integer>();
		
		
		for(int i=0;i<rows;i++) {
			for(int j=0;j<columns;j++) {
				childGenotype.getGenotypeWeights()[i][j] = genotype.getGenotypeWeights()[i][j];
				childGenotype.getGenotypeFunctions()[i][j] = genotype.getGenotypeFunctions()[i][j];
				if(genotype.getGenotypeFunctions()[i][j]!=null) {
					validComponents.add(i*columns+j);
				} else {
					// Potentially valid components are only those pairs of properties that are 
					// not compared in the current genotype and
					// can be compared with at least one similarity function
					if(aligned) {
						if(!sourceProperties.get(i).getPropertyPath().equals(targetProperties.get(j).getPropertyPath())) {
							continue;
						}
					}
					if(!mapApplicableFunctions.get(sourceProperties.get(i)).get(targetProperties.get(j)).isEmpty()) {
						potentiallyValidComponents.add(i*columns+j);
					}
				}
			}
		}
		
		childGenotype.setThreshold(genotype.getThreshold());
		childGenotype.setAggregationFunction(genotype.getAggregationFunction());
		
		int index, x, y;
		
		// Probability of adding or removing a component
		double pAddOrRemove = 0.30;
		// Probability of changing a weight or changing a comparison function
		double pChangeWeight = 0.30;
		//double pChangeThreshold = 1-pAddOrRemove-pChangeWeight;
		double pChangeThreshold = 0.30;
		
		double val = Math.random();
		
		if(val<pAddOrRemove) {
			double pRemove;
			
			pRemove = 0.5*(validComponents.size()-1);
			/*if(validComponents.size()==0) {
				pRemove = -1;
			} else if(invalidComponents.size()==0) {
				pRemove = 1.1;
			} else {
				pRemove = (validComponents.size()-1)/validComponents.size();
			}*/
			
			if(Math.random()<pRemove) {
				index = validComponents.get((int)(Math.random()*validComponents.size()));
				
				y = index%columns;
				x = index/columns;
			
				// Flip the bit
				childGenotype.getGenotypeFunctions()[x][y] = null;
				childGenotype.getGenotypeWeights()[x][y] = 0.0;
			} else {
				if(potentiallyValidComponents.size()>0) {
					index = potentiallyValidComponents.get((int)(Math.random()*potentiallyValidComponents.size()));
									
					y = index%columns;
					x = index/columns;
				
					// Flip the bit
					//childGenotype.genotypeFunctions[x][y] = ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.JARO_WINKLER);
					List<IValueMatchingFunction<? extends Object>> functionsList = mapApplicableFunctions.get(sourceProperties.get(x)).get(targetProperties.get(y));
					Collections.shuffle(functionsList);
					
					childGenotype.getGenotypeFunctions()[x][y] = functionsList.get(0);
					childGenotype.getGenotypeWeights()[x][y] = Math.random();
				}

			}
			
		} else if(val<pAddOrRemove+pChangeWeight) {
			if(validComponents.size()>0) {
				index = validComponents.get((int)(Math.random()*validComponents.size()));
				
				y = index%columns;
				x = index/columns;
				if((validComponents.size()>1)||(Math.random()<0.5)) {
					
					double weightChange = Math.random()*(0.8/validComponents.size())+0.2/validComponents.size();
					if(Math.random()<0.5) {
						childGenotype.getGenotypeWeights()[x][y] = Math.max(childGenotype.getGenotypeWeights()[x][y]-weightChange, 0);
					} else {
						childGenotype.getGenotypeWeights()[x][y] = Math.min(childGenotype.getGenotypeWeights()[x][y]+weightChange, 1);
					}
				} else {
					childGenotype.getGenotypeFunctions()[x][y] = ValueMatchingFunctionFactory.getRandomInstanceForAttributes(sourceProperties.get(x), targetProperties.get(y));
				}
			}
		} else if(val<pAddOrRemove+pChangeWeight+pChangeThreshold) {

			double sum = 2-original.getFitness().getPrecision()-original.getFitness().getRecall();
			double pThresholdIncrease;
			double change;
			double minChange, maxChange;
			
			//if(sum>=(2-0.00000001)) {
				pThresholdIncrease = 0.5;
			//} else {
			//	pThresholdIncrease = (1-original.getFitness().getPrecision())/(2-original.getFitness().getPrecision()-original.getFitness().getRecall());
			//}
			double threshold = genotype.getThreshold();
			
			// Threshold adjustment
			/*if(original.getFitness() instanceof UnsupervisedFitness) {
				UnsupervisedFitness fitness = (UnsupervisedFitness)original.getFitness();
				threshold = Math.max(threshold, fitness.getAverageSimilarity()-10*fitness.getStandardDeviation());
			}*/
			
			if(Math.random()<pThresholdIncrease) {
				maxChange = (1-original.getFitness().getPrecision())*(1-threshold);
				minChange = Math.max(maxChange/4, 0.01);
				maxChange = Math.max(maxChange, minChange);
				change = Math.random()*(maxChange-minChange)+minChange;
				if((threshold+change)>1) {
					log.error("Threshold too high: "+(threshold+change));
				}
				threshold = Math.min(threshold+change, 1.0);
				
			} else {
				maxChange = (1-original.getFitness().getRecall())*threshold;
				minChange = Math.max(maxChange/4, 0.01);
				maxChange = Math.max(maxChange, minChange);
				change = Math.random()*(maxChange-minChange)+minChange;
				
				if((genotype.getThreshold()-change)<0) {
					log.error("Negative threshold: "+(threshold-change));
				}
				
				threshold = Math.max(threshold-change, 0.0);
			}
						
			if(childGenotype.getThreshold()<0.0000001) {
				log.info("here");
			}
			
			// Threshold adjustment
			/*if(original.getFitness() instanceof UnsupervisedFitness) {
				UnsupervisedFitness fitness = (UnsupervisedFitness)original.getFitness();
				threshold = Math.max(threshold, fitness.getAverageSimilarity()-3*fitness.getStandardDeviation());
			}*/
			childGenotype.setThreshold(threshold);
			
			
			
		} else {
			IAggregationFunction func = AggregationFunctionFactory.getRandomInstance();
			while(!genotype.getAggregationFunction().toString().equals(func.toString())) {
				func = AggregationFunctionFactory.getRandomInstance();
			}
			childGenotype.setAggregationFunction(func);
		}
		
		
		
		return new CandidateSolution(original.getModelSpec().getApplicationContext(), childGenotype, sourceProperties, targetProperties);
		
	}
	
}
