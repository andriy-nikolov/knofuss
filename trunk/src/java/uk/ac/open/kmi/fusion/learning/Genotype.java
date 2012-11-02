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

import java.util.List;

import uk.ac.open.kmi.fusion.api.IAggregationFunction;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.impl.valuematching.ValueMatchingFunctionFactory;

public class Genotype {

	Double[][] genotypeWeights;
	IValueMatchingFunction[][] genotypeFunctions;
	double threshold;
	int rows = 0;
	int columns = 0;
	IAggregationFunction aggregationFunction;
	
	//public Genotype() {
		
	//}
	
	public Genotype(int rows, int columns) {
		this.rows = rows;
		this.columns = columns;
		this.genotypeWeights = new Double[rows][columns];
		this.genotypeFunctions = new IValueMatchingFunction[rows][columns];
		for(int i = 0;i<rows;i++) {
			for(int j = 0;j<columns;j++) {
				this.genotypeWeights[i][j] = 0.0;
				this.genotypeFunctions[i][j] = null;
			}
		}
	}
	
	public Double[][] getGenotypeWeights() {
		return genotypeWeights;
	}
	
	public void setGenotypeWeights(Double[][] genotypeWeights) {
		this.genotypeWeights = genotypeWeights;
	}
	
	public IValueMatchingFunction[][] getGenotypeFunctions() {
		return genotypeFunctions;
	}
	
	public void setGenotypeFunctions(IValueMatchingFunction[][] genotypeFunctions) {
		this.genotypeFunctions = genotypeFunctions;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public int getRows() {
		return rows;
	}

	public int getColumns() {
		return columns;
	}
	
	public void normalizeWeights() {
		double sum = 0;
		for(int i=0;i<genotypeWeights.length;i++) {
			for(int j=0;j<genotypeWeights[i].length;j++) {
				sum += genotypeWeights[i][j];
			}
		}
		for(int i=0;i<genotypeWeights.length;i++) {
			for(int j=0;j<genotypeWeights[i].length;j++) {
				genotypeWeights[i][j] = genotypeWeights[i][j]/sum;
				if(genotypeWeights[i][j].isNaN()) {
					genotypeWeights[i][j] = 0.0;
					genotypeFunctions[i][j] = null;
				}
			}
		}
	}
	
	public void addRandomComponent(List<IAttribute> sourceAttributes, List<IAttribute> targetAttributes) {
		int k = (int)(Math.random()*getRows());
		int l = (int)(Math.random()*getColumns());
		genotypeWeights[k][l] = Math.random();
		genotypeFunctions[k][l] = ValueMatchingFunctionFactory.getRandomInstanceForAttributes(sourceAttributes.get(k), targetAttributes.get(l));
		
	}

	
	public boolean isEmpty() {
		
		for(int i = 0;i<getRows();i++) {
			for(int j = 0;j<getColumns();j++) {
				if(getGenotypeFunctions()[i][j]!=null) 
					return false;
			}
		}
		return true;
	}

	public IAggregationFunction getAggregationFunction() {
		return aggregationFunction;
	}

	public void setAggregationFunction(IAggregationFunction aggregationFunction) {
		this.aggregationFunction = aggregationFunction;
	}
	
	
}
