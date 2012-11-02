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
package uk.ac.open.kmi.fusion.learning.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.vocabulary.RDFS;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.AtomicAttribute;
import uk.ac.open.kmi.fusion.api.impl.AttributeType;
import uk.ac.open.kmi.fusion.api.impl.valuematching.ValueMatchingFunctionFactory;
import uk.ac.open.kmi.fusion.learning.CandidateSolution;
import uk.ac.open.kmi.fusion.learning.Genotype;
import uk.ac.open.kmi.fusion.learning.genetic.crossover.CrossoverOperatorFactory;
import uk.ac.open.kmi.fusion.learning.genetic.crossover.ICrossoverOperator;
import uk.ac.open.kmi.fusion.learning.genetic.mutation.IMutationOperator;
import uk.ac.open.kmi.fusion.learning.genetic.mutation.MutationOperatorFactory;

public class CandidateSolutionTest {

	List<IAttribute> sourceProperties;
	List<IAttribute> targetProperties;
	
	ApplicationContext applicationContext;
	
	Map<IAttribute, Map<IAttribute, List<IValueMatchingFunction<? extends Object>>>> mapApplicableFunctions;
	
	String[] sp = {
			RDFS.LABEL.toString(),
			"http://data.linkedmdb.org/movie/music_contributor_name"
	};
	
	String[] tp = {
			RDFS.LABEL.toString(),
			"http://dbpedia.org/property/shortDescription",
			"http://dbpedia.org/property/description"
	};

	@Before
	public void setUp() throws Exception {
		
		mapApplicableFunctions = new HashMap<IAttribute, Map<IAttribute, List<IValueMatchingFunction<? extends Object>>>>();
		
		sourceProperties = new ArrayList<IAttribute>(sp.length);
		targetProperties = new ArrayList<IAttribute>(tp.length);
		IAttribute attribute;
		for(String s : sp) {
			attribute = new AtomicAttribute(s, AttributeType.NOMINAL_MULTI_TOKEN);
			sourceProperties.add(attribute);
		} 
		for(String t : tp) {
			attribute = new AtomicAttribute(t, AttributeType.NOMINAL_MULTI_TOKEN);
			targetProperties.add(attribute);
		}
		
		Map<IAttribute, List<IValueMatchingFunction<? extends Object>>> validFunctionsMap;
		List<IValueMatchingFunction<? extends Object>> validFunctionList;
		for(IAttribute sourceAttribute : sourceProperties) {
			validFunctionsMap = new HashMap<IAttribute, List<IValueMatchingFunction<? extends Object>>>();
			for(IAttribute targetAttribute : targetProperties) {
				validFunctionList = ValueMatchingFunctionFactory.getApplicableFunctionsForAttributes(sourceAttribute, targetAttribute);
				validFunctionsMap.put(targetAttribute, validFunctionList);
			}
			mapApplicableFunctions.put(sourceAttribute, validFunctionsMap);
		}
		
		applicationContext = new ApplicationContext();
		applicationContext.setRestrictionSource("?uri a <http://data.linkedmdb.org/movie/music_contributor> .");
		applicationContext.setRestrictionTarget("?uri a <http://dbpedia.org/ontology/MusicalArtist> .");
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testCreateRandom() {
		CandidateSolution solution = CandidateSolution.createRandom(applicationContext, sourceProperties, targetProperties, mapApplicableFunctions, false);
		
		System.out.println(solution.getModelSpec().serializeQuerySPARQLSource());
		System.out.println(solution.getModelSpec().serializeQuerySPARQLTarget());
				
	}
	
	@Test
	public final void testMutate() {
		CandidateSolution solution = CandidateSolution.createRandom(applicationContext, sourceProperties, targetProperties, mapApplicableFunctions, false);
		System.out.println(solution.toString());
		
		IMutationOperator mutator = MutationOperatorFactory.getInstance(IMutationOperator.MUTATION_DEFAULT);
		
		solution = mutator.mutate(solution, sourceProperties, targetProperties, mapApplicableFunctions, false);
		System.out.println(solution.toString());
		
		Genotype genotype = new Genotype(sourceProperties.size(), targetProperties.size());
		
		IValueMatchingFunction<? extends Object>[][] functions = genotype.getGenotypeFunctions();
		Double[][] weights = genotype.getGenotypeWeights();
		
		functions[0][0] = ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.JARO_WINKLER);
		weights[0][0] = 1.0;
		
		functions[0][1] = ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.JARO_WINKLER);
		weights[0][1] = 1.0;
		
		functions[1][1] = ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.JARO_WINKLER);
		weights[1][1] = 1.0;
		
		genotype.setThreshold(0.5);
		
		solution = new CandidateSolution(applicationContext, genotype, sourceProperties, targetProperties);
		System.out.println(solution.toString());
		
		solution = mutator.mutate(solution, sourceProperties, targetProperties, mapApplicableFunctions, false);
		System.out.println(solution.toString());
	}
	
	@Test
	public final void testCrossover() {
		Genotype genotype1 = new Genotype(sourceProperties.size(), targetProperties.size());
		IValueMatchingFunction[][] functions = genotype1.getGenotypeFunctions();
		CandidateSolution[] parents = new CandidateSolution[2];
		
		Double[][] weights = genotype1.getGenotypeWeights();
		functions[0][0] = ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.JARO_WINKLER);
		weights[0][0] = 1.0;
		
		functions[1][1] = ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.JARO_WINKLER);
		weights[1][1] = 1.0;
		genotype1.setThreshold(0.5);
		parents[0] = new CandidateSolution(applicationContext, genotype1, sourceProperties, targetProperties);
		Genotype genotype2 = new Genotype(sourceProperties.size(), targetProperties.size());
		functions = genotype1.getGenotypeFunctions();
		weights = genotype1.getGenotypeWeights();
		functions[1][0] = ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.JARO_WINKLER);
		weights[1][0] = 1.0;
		
		functions[0][1] = ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.JARO_WINKLER);
		weights[0][1] = 1.0;
		genotype2.setThreshold(0.5);
		parents[1] = new CandidateSolution(applicationContext, genotype2, sourceProperties, targetProperties);
		
		ICrossoverOperator crossoverOperator = CrossoverOperatorFactory.getInstance(ICrossoverOperator.CROSSOVER_SPLIT_BY_MATRIX_INDEX);
		
		Set<CandidateSolution> solutions = crossoverOperator.crossover(parents, sourceProperties, targetProperties);
		for(CandidateSolution solution : solutions) {
			System.out.println(solution.toString());
		}
	}

}
