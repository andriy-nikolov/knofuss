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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openjena.atlas.logging.Log;

import uk.ac.open.kmi.fusion.api.IAggregationFunction;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.VariableComparisonSpecification;
import uk.ac.open.kmi.fusion.api.impl.aggregation.AggregationFunctionFactory;
import uk.ac.open.kmi.fusion.api.impl.valuematching.ValueMatchingFunctionFactory;

public class GenotypeTree {
	
	private static final double pPropertyPairCompare = 0.5;
	private static final int initialLimit = 1;

	DecisionNode rootNode;
	
	List<IAttribute> sourceProperties, targetProperties;
	
	ApplicationContext applicationContext;
	
	private static Logger log = Logger.getLogger(GenotypeTree.class);
	
	public GenotypeTree(ApplicationContext applicationContext, List<IAttribute> sourceProperties, List<IAttribute> targetProperties) {
		this.applicationContext = applicationContext;
		this.sourceProperties = sourceProperties;
		this.targetProperties = targetProperties;
	}
	
	public int getRows() {
		return sourceProperties.size();
	}

	public int getColumns() {
		return targetProperties.size();
	}
	
	public List<IAttribute> getSourceProperties() {
		return sourceProperties;
	}

	public List<IAttribute> getTargetProperties() {
		return targetProperties;
	}

	public DecisionNode getRootNode() {
		return rootNode;
	}

	public void setRootNode(DecisionNode rootNode) {
		this.rootNode = rootNode;
	}
	
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public static GenotypeTree createRandom(ApplicationContext applicationContext, List<IAttribute> sourceProperties, List<IAttribute> targetProperties, Map<IAttribute, Map<IAttribute, List<IValueMatchingFunction<? extends Object>>>> mapApplicableFunctions, boolean aligned) {
		
		List<IAttribute> sourcePropertiesShuffled = new ArrayList<IAttribute>(sourceProperties);
		List<IAttribute> targetPropertiesShuffled = new ArrayList<IAttribute>(targetProperties);
		
		Collections.shuffle(sourcePropertiesShuffled);
		Collections.shuffle(targetPropertiesShuffled);
		IAttribute sourceProperty, targetProperty;
		Set<Integer> alreadyUsedSourceProperties = new HashSet<Integer>();
		Set<Integer> alreadyUsedTargetProperties = new HashSet<Integer>();
		
		Double[][] genotypeWeightsCurrent = new Double[sourceProperties.size()][targetProperties.size()];
		IValueMatchingFunction[][] genotypeFunctionsCurrent = new IValueMatchingFunction[sourceProperties.size()][targetProperties.size()];
		
		// Map<IAttribute, List<IValueMatchingFunction>> validFunctionsMap;
		
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
		
		IValueMatchingFunction tmpFunction;
		while(alreadyUsedSourceProperties.isEmpty()) {
			k = (int)(Math.random()*sourceProperties.size());
			l = (int)(Math.random()*targetProperties.size());
			tmpFunction = ValueMatchingFunctionFactory.getRandomInstanceForAttributes(sourceProperties.get(k), targetProperties.get(l));
			if(tmpFunction!=null) {
				genotypeWeightsCurrent[k][l] = Math.random();
				genotypeFunctionsCurrent[k][l] = tmpFunction;
				alreadyUsedSourceProperties.add(k);
			}
		}
		
		double threshold = Math.random();
		
		GenotypeTree genotype = new GenotypeTree(applicationContext, sourceProperties, targetProperties);
		
		AtomicDecisionNode parentNode = new AtomicDecisionNode();
		
		AggregationNode aggregationNode = new AggregationNode();
		
		aggregationNode.setAggregationFunction(AggregationFunctionFactory.getRandomInstance());
		genotype.setRootNode(parentNode);
		
		AttributeSimilarityNode similarityNode;
		for(int i=0;i<sourceProperties.size();i++) {
			for(int j=0;j<targetProperties.size();j++) {
				if(genotypeFunctionsCurrent[i][j]!=null) {
					similarityNode = new AttributeSimilarityNode();
					similarityNode.setValueMatchingFunction(genotypeFunctionsCurrent[i][j]);
					similarityNode.setWeight(genotypeWeightsCurrent[i][j]);
					similarityNode.setSourceAttribute(sourceProperties.get(i));
					similarityNode.setTargetAttribute(targetProperties.get(j));
					aggregationNode.addChild(similarityNode);
				}
			}
		}
		
		parentNode.setAggregationNode(aggregationNode);
		parentNode.setThreshold(threshold);
		
		genotype.setRootNode(parentNode);
		
		if(parentNode.toString().contains("()")) {
			log.error("Invalid parent node: "+parentNode.toString());
		}
		
		return genotype;
	}
	
	public void normalizeWeights() {
		rootNode.normalizeWeights();
	}
	
	public double getMinimalPassingSimilarity() {
		return rootNode.getLastMinimalPassingSimilarity();
	}
	
}
