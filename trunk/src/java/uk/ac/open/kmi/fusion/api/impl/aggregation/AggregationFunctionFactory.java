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

import java.util.HashMap;
import java.util.Map;

import uk.ac.open.kmi.fusion.api.IAggregationFunction;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;

public final class AggregationFunctionFactory {

	private static String[] availableFunctionTypes = new String[]{
		IAggregationFunction.AVERAGE// ,
		// IAggregationFunction.MAX
	};
	
	private static Map<String, IAggregationFunction> pool = new HashMap<String, IAggregationFunction>();
	
	static {
		pool.put(IAggregationFunction.AVERAGE, AverageAggregationFunction.getInstance());
		// pool.put(IAggregationFunction.MAX, MaxAggregationFunction.getInstance());
	}
	
	private AggregationFunctionFactory() {
		// TODO Auto-generated constructor stub
	}
	
	public static IAggregationFunction getInstance(String name) {
		if(pool.containsKey(name)) {
			return pool.get(name);
		} else {
			throw new IllegalArgumentException("Unknown aggregation function: "+name);
		}
	}
	
	public static IAggregationFunction getRandomInstance() {
		IAggregationFunction res;
		
		int val = (int)(Math.random()*availableFunctionTypes.length);
		
		res = getInstance(availableFunctionTypes[val]);
		
		return res;
	}

}
