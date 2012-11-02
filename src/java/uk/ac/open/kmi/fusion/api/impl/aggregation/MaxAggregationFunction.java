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
package uk.ac.open.kmi.fusion.api.impl.aggregation;

import java.util.List;

import org.apache.log4j.Logger;

import uk.ac.open.kmi.fusion.api.IAggregationFunction;
import uk.ac.open.kmi.fusion.api.IObjectContextWrapper;
import uk.ac.open.kmi.fusion.api.impl.VariableComparisonSpecification;

public class MaxAggregationFunction implements IAggregationFunction {

	private static final MaxAggregationFunction INSTANCE = new MaxAggregationFunction();
	
	private static Logger log = Logger.getLogger(MaxAggregationFunction.class);
	
	private MaxAggregationFunction() {
		// TODO Auto-generated constructor stub
	}
		
	public static MaxAggregationFunction getInstance() {
		return INSTANCE;
	}

	@Override
	public double calculate(IObjectContextWrapper source,
			IObjectContextWrapper target,
			List<VariableComparisonSpecification> variableComparisons) {
		
		Double result = 0.0;

		Double similarity;
		
		for(VariableComparisonSpecification spec : variableComparisons) {
			
			similarity = spec.getSimilarity(
					source.getValuesByAttribute(spec.getSourceAttribute()), 
					target.getValuesByAttribute(spec.getTargetAttribute()));
			
			if(similarity.isNaN()) {
				similarity = 0.0;
			}
			
			result = Math.max(similarity, result);
			
		}
		
		if(result.isNaN()) {
			result = 0.0;
		}
		
		return result;
	}

	@Override
	public String toString() {
		return "max";
	}
	
	
	
}
