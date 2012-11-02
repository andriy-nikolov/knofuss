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
package uk.ac.open.kmi.fusion.objectidentification.standard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import uk.ac.shef.wit.simmetrics.math.MathFuncs;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;

public class ISubDistance extends AbstractStringMetric implements Serializable {

	 /**
     * maximum prefix length to use.
     */
    private static final int MINPREFIXTESTLENGTH = 6;
    private static final float PREFIXADJUSTMENTSCALE = 0.1f;
    private static final float DIFFERENCEPARAMETER = 0.6f;
	
	
	public ISubDistance() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getLongDescriptionString() {
		return "Implements the I-Sub distance metric proposed by Stoilos, Stamou & Kollias";
	}

	@Override
	public String getShortDescriptionString() {
		return "I-Sub";
	}

	@Override
	public float getSimilarity(String s, String t) {
		int[] longestSubstringParams;
		int[] currentlyLongestSubstringParams;
		int[] tmpSubstringParams;
		//boolean allSubstringsFound = false;
		String tmp1 = s.toLowerCase(), tmp2 = t.toLowerCase();
		float sumlength = 0;
		StringBuffer tmpBuffer;
		
		List<String> tokens1 = new LinkedList<String>(), tokens2 = new LinkedList<String>(); 
		
		tokens1.add(tmp1);
		tokens2.add(tmp2);
		while(true) {
			longestSubstringParams = null;
			String topToken1 = null, topToken2 = null;
			int bestLength = 0;
			
			for(String token1 : tokens1) {
				for(String token2 : tokens2) {		
					tmpSubstringParams = getLongestCommonSubstringPosition(token1, token2);
					if(tmpSubstringParams[2]>bestLength) {
						bestLength = tmpSubstringParams[2];
						topToken1 = token1;
						topToken2 = token2;
						longestSubstringParams = tmpSubstringParams;
					}
				}
			}
			
			
			
			// if(bestLength>0) {
			if(bestLength>1) {
				sumlength+=longestSubstringParams[2];
				if(longestSubstringParams[0]>0) {
					tokens1.add(topToken1.substring(0, longestSubstringParams[0]));
				}
				if((longestSubstringParams[0]+longestSubstringParams[2])<topToken1.length()) {
					tokens1.add(topToken1.substring(longestSubstringParams[0]+longestSubstringParams[2]));
				}
				tokens1.remove(topToken1);
				
				if(longestSubstringParams[1]>0) {
					tokens2.add(topToken2.substring(0, longestSubstringParams[1]));
				}
				if((longestSubstringParams[1]+longestSubstringParams[2])<topToken2.length()) {
					tokens2.add(topToken2.substring(longestSubstringParams[1]+longestSubstringParams[2]));
				}
				tokens2.remove(topToken2);
			} else {
				break;
			}
			
		}
		
		float comm = (2*sumlength)/(s.length()+t.length());
		
		float ulen1 = 0; 
		for(String token1 : tokens1) {
			ulen1 += token1.length();
		}
		ulen1 = ulen1/s.length();

		float ulen2 = 0; 
		for(String token2 : tokens2) {
			ulen2 += token2.length();
		}
		ulen2 = ulen2/t.length();
		
		float diff = (ulen1*ulen2)/(DIFFERENCEPARAMETER+(1-DIFFERENCEPARAMETER)*(ulen1+ulen2-ulen1*ulen2));
		float sim = comm-diff;
		int prefixLength = getPrefixLength(s, t);
		sim+=(prefixLength*PREFIXADJUSTMENTSCALE*(1-sim));
		
		if(sim>1.1) {
			System.out.println(s+" : "+t);
		}
		
		return (sim+1)/2;
	}

	@Override
	public String getSimilarityExplained(String arg0, String arg1) {
		
		return null;
	}

	@Override
	public float getSimilarityTimingEstimated(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getUnNormalisedSimilarity(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return getSimilarity(arg0, arg1);
	}

		
	/**
	 * 
	 * @param s
	 * @param t
	 * @return array of three int values, [0] - index of the substring in the first string [1] - index of the substring in the second string [2] - length of the substring
	 */
	private int[] getLongestCommonSubstringPosition(final String s, final String t) {
		
		if((s==null)||(t==null)) {
			return new int[] {0, 0, 0};
		}
		
		if((s.isEmpty())||(t.isEmpty())) {
			return new int[] {0, 0, 0};
		}
		
		int[] result = new int[3];
		
		int num[][] = new int[s.length()][t.length()];
		int maxlen = 0;
		//int lastSubsBegin = 0;
		int pos1 = 0, pos2 = 0;
		
		for(int i=0;i<s.length();i++) {
			for (int j=0;j<t.length();j++) {
				if(s.charAt(i)!=t.charAt(j)) {
					num[i][j] = 0;
				} else {
					if((i==0)||(j==0)) {
						num[i][j] = 1;
					} else {
						num[i][j] = 1 + num[i-1][j-1];
					}
					if(num[i][j]>maxlen) {
						maxlen = num[i][j];
						pos1 = i-maxlen+1;
						pos2 = j-maxlen+1;
					}
					
				}
			}
		}
		result[0] = pos1;
		result[1] = pos2;
		result[2] = maxlen;
		
		return result;
		
	}
	
	private static int getPrefixLength(final String string1, final String string2) {
        final int n = MathFuncs.min3(MINPREFIXTESTLENGTH, string1.length(), string2.length());
        //check for prefix similarity of length n
        for (int i = 0; i < n; i++) {
//check the prefix is the same so far
            if (string1.charAt(i) != string2.charAt(i)) {
//not the same so return as far as got
                return i;
            }
        }
        return n; // first n characters are the same
    }
	
}
