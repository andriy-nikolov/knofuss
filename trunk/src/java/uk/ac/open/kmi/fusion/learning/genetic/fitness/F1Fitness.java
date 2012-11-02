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
package uk.ac.open.kmi.fusion.learning.genetic.fitness;

import java.util.Set;

public class F1Fitness implements IFitnessFunction {

	double f1;
	double precision;
	double recall;
	
	int truePositives;
	int falsePositives;
	int falseNegatives;
	
	public static F1Fitness getF1Fitness(Set<Integer> goldStandardSet, Set<Integer> solutionResults) {
		int fp = 0;
		int fn = 0;
		int tp = 0;
		
		String key;
		double precision, recall, f1;
		
		for(int mapping : solutionResults) {
			if(goldStandardSet.contains(mapping)) {
				tp++;
			} else {
				fp++;
			}
		}
		
		fn = goldStandardSet.size()-tp;
		
		return new F1Fitness(tp, fp, fn);
	}
	
	
	private F1Fitness(int tp, int fp, int fn) {
		setTruePositives(tp);
		setFalsePositives(fp);
		setFalseNegatives(fn);
		
		calculate();
	}
	
	private void calculate() {
		if(this.truePositives==0) {
			this.precision = 0;
			this.recall = 0;
			this.f1 = 0;
		} else {
		
			precision = ((double)truePositives)/(truePositives+falsePositives);
			recall = ((double)truePositives)/(truePositives+falseNegatives);
			
			f1 = 2*precision*recall/(precision+recall);
		}
	}

	public double getF1() {
		return f1;
	}

	public void setF1(double f1) {
		this.f1 = f1;
	}

	public double getPrecision() {
		return precision;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public double getRecall() {
		return recall;
	}

	public void setRecall(double recall) {
		this.recall = recall;
	}

	public int getTruePositives() {
		return truePositives;
	}

	public void setTruePositives(int truePosititves) {
		this.truePositives = truePosititves;
	}

	public int getFalsePositives() {
		return falsePositives;
	}

	public void setFalsePositives(int falsePositives) {
		this.falsePositives = falsePositives;
	}

	public int getFalseNegatives() {
		return falseNegatives;
	}

	public void setFalseNegatives(int falseNegatives) {
		this.falseNegatives = falseNegatives;
	}

	@Override
	public double getValue() {
		return getF1();
	}

	
	
}
