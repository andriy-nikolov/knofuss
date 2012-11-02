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
package uk.ac.open.kmi.fusion.api.impl.valuematching;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;

public abstract class TokenBasedSimilarityValueMatchingFunction implements
		IValueMatchingFunction<String> {

	protected TokenBasedSimilarityValueMatchingFunction() {
		
	}

	@Override
	public double getSimilarity(IAttribute attr1, IAttribute attr2, String value1, String value2) {
		return getScore(attr1, attr2, value1, value2);
	}
	
	protected double getScore(IAttribute attr1, IAttribute attr2, String s1, String s2) {
		List<String> list1 = tokenize(s1);
		List<String> list2 = tokenize(s2);
		try {
			return getScore(attr1, attr2, list1, list2);
		} catch(AbstractMethodError e) {
			e.printStackTrace();
			getScore(attr1, attr2, list1, list2);
			return 0;
		}
	}
	
	protected abstract double getScore(IAttribute attr1, IAttribute attr2, List<String> list1, List<String> list2);
	
	static private List<String> tokenize(String val) {
		return tokenize(val, " \t\n\r\f:(),-.");
		
	}
	
	static private List<String> tokenize(String val, String chars) {
		List<String> res = new ArrayList<String>();
		String token;
		StringTokenizer tokenizer = new StringTokenizer(val.toLowerCase(), chars);
		while(tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken();
			res.add(token);
		}
		return res;
	}
}
