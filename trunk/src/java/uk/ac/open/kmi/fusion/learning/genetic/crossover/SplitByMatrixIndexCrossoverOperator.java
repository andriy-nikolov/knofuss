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
package uk.ac.open.kmi.fusion.learning.genetic.crossover;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.AtomicAttribute;
import uk.ac.open.kmi.fusion.learning.CandidateSolution;
import uk.ac.open.kmi.fusion.learning.Genotype;

public class SplitByMatrixIndexCrossoverOperator implements ICrossoverOperator {

	private static ICrossoverOperator INSTANCE = new SplitByMatrixIndexCrossoverOperator(); 
	
	private SplitByMatrixIndexCrossoverOperator() {
		
	}
	
	static ICrossoverOperator getInstance() {
		return INSTANCE;
	}
	
	
	
	@Override
	public Set<CandidateSolution> crossover(CandidateSolution[] parents,
			List<IAttribute> sourceProperties, List<IAttribute> targetProperties) {
		if(parents.length!=2) throw new IllegalArgumentException("Number of parents: "+parents.length);
		Genotype[] parentGenotypes = new Genotype[2];
		Genotype[] childGenotypes;
		
		ApplicationContext context = parents[0].getModelSpec().getApplicationContext();
		
		for(int i=0;i<2;i++) {
			parentGenotypes[i] = parents[i].getGenotype();
		}
		
		if(Math.random()<0.5) {
			childGenotypes = splitXOverGenotypeByRows(parentGenotypes);
		} else {
			childGenotypes = splitXOverGenotypeByColumns(parentGenotypes);
		}
		
		if(Math.random()<0.5) {
			childGenotypes[0].setThreshold(parentGenotypes[0].getThreshold());
			childGenotypes[1].setThreshold(parentGenotypes[1].getThreshold());
		} else {
			childGenotypes[0].setThreshold(parentGenotypes[1].getThreshold());
			childGenotypes[1].setThreshold(parentGenotypes[0].getThreshold());
		}
		
		if(Math.random()<0.5) {
			childGenotypes[0].setAggregationFunction(parentGenotypes[0].getAggregationFunction());
			childGenotypes[1].setAggregationFunction(parentGenotypes[1].getAggregationFunction());
		} else {
			childGenotypes[0].setAggregationFunction(parentGenotypes[1].getAggregationFunction());
			childGenotypes[1].setAggregationFunction(parentGenotypes[0].getAggregationFunction());
		}
		
		Set<CandidateSolution> children = new HashSet<CandidateSolution>();
		
		for(int i=0;i<2;i++) {
			if(childGenotypes[i].isEmpty()) {
				childGenotypes[i].addRandomComponent(sourceProperties, targetProperties);
			}
			children.add(new CandidateSolution(context, childGenotypes[i], sourceProperties, targetProperties));
		}
		
		return children;
	}

	
	private static Genotype[] splitXOverGenotypeByRows(Genotype[] parentGenotypes) {
		Genotype[] childGenotypes = new Genotype[2];
		int rows = parentGenotypes[0].getRows();
		int columns = parentGenotypes[0].getColumns();
		for(int i=0;i<2;i++) {
			childGenotypes[i] = new Genotype(rows, columns);
		}
		
		for(int i=0;i<rows;i++) {
			if(Math.random()<0.5) {
				for(int j=0;j<columns;j++) {
					childGenotypes[0].getGenotypeWeights()[i][j] = parentGenotypes[0].getGenotypeWeights()[i][j];
					childGenotypes[1].getGenotypeWeights()[i][j] = parentGenotypes[1].getGenotypeWeights()[i][j];
					childGenotypes[0].getGenotypeFunctions()[i][j] = parentGenotypes[0].getGenotypeFunctions()[i][j];
					childGenotypes[1].getGenotypeFunctions()[i][j] = parentGenotypes[1].getGenotypeFunctions()[i][j];
				}
			} else {
				for(int j=0;j<columns;j++) {
					childGenotypes[0].getGenotypeWeights()[i][j] = parentGenotypes[1].getGenotypeWeights()[i][j];
					childGenotypes[1].getGenotypeWeights()[i][j] = parentGenotypes[0].getGenotypeWeights()[i][j];
					childGenotypes[0].getGenotypeFunctions()[i][j] = parentGenotypes[1].getGenotypeFunctions()[i][j];
					childGenotypes[1].getGenotypeFunctions()[i][j] = parentGenotypes[0].getGenotypeFunctions()[i][j];
				}
			}
		}
		
		return childGenotypes;
	}
	
	private static Genotype[] splitXOverGenotypeByColumns(Genotype[] parentGenotypes) {
		Genotype[] childGenotypes = new Genotype[2];
		int rows = parentGenotypes[0].getRows();
		int columns = parentGenotypes[0].getColumns();
		for(int i=0;i<2;i++) {
			childGenotypes[i] = new Genotype(rows, columns);
		}
		
		for(int j=0;j<columns;j++) {
			if(Math.random()<0.5) {
				for(int i=0;i<rows;i++) {
					childGenotypes[0].getGenotypeWeights()[i][j] = parentGenotypes[0].getGenotypeWeights()[i][j];
					childGenotypes[1].getGenotypeWeights()[i][j] = parentGenotypes[1].getGenotypeWeights()[i][j];
					childGenotypes[0].getGenotypeFunctions()[i][j] = parentGenotypes[0].getGenotypeFunctions()[i][j];
					childGenotypes[1].getGenotypeFunctions()[i][j] = parentGenotypes[1].getGenotypeFunctions()[i][j];
				}
			} else {
				for(int i=0;i<rows;i++) {
					childGenotypes[0].getGenotypeWeights()[i][j] = parentGenotypes[1].getGenotypeWeights()[i][j];
					childGenotypes[1].getGenotypeWeights()[i][j] = parentGenotypes[0].getGenotypeWeights()[i][j];
					childGenotypes[0].getGenotypeFunctions()[i][j] = parentGenotypes[1].getGenotypeFunctions()[i][j];
					childGenotypes[1].getGenotypeFunctions()[i][j] = parentGenotypes[0].getGenotypeFunctions()[i][j];
				}
			}
		}
		
		
		return childGenotypes;
	} 
	
}
