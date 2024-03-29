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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.AttributeType;

public class L2EventMetaMatchingFunction extends TokenBasedSimilarityValueMatchingFunction {

	private Set<String> stopWords;
	// private double stopWordWeight = 1.0;
	
	private static AttributeType[][] supportedTypes = {
		{ AttributeType.NOMINAL, AttributeType.NOMINAL_MULTI_TOKEN },
		{ AttributeType.NOMINAL_MULTI_TOKEN, AttributeType.NOMINAL_MULTI_TOKEN }
		//{ AttributeType.NOMINAL_MULTI_TOKEN, AttributeType.LONG_TEXT },
	};
	
	IValueMatchingFunction<String> embeddedFunction;
	
	private L2EventMetaMatchingFunction(IValueMatchingFunction<String> embeddedFunction) {
		super();
		this.embeddedFunction = embeddedFunction;
		this.stopWords = new HashSet<String>();
		
		loadStopWords();
		
	}
	
	private void loadStopWords() {
		String path = "resources/stopWords.txt";
		try {
			
			BufferedReader reader = Utils.openBufferedFileReader(path);
			String line;
			while((line=reader.readLine())!=null) {
				stopWords.add(line.trim().toLowerCase());
			}
			
			reader.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}

	public IValueMatchingFunction<String> getEmbeddedFunction() {
		return embeddedFunction;
	}

	public void setEmbeddedFunction(IValueMatchingFunction<String> embeddedFunction) {
		this.embeddedFunction = embeddedFunction;
	}

	public static L2EventMetaMatchingFunction getInstance(IValueMatchingFunction<String> embeddedFunction) {
		return new L2EventMetaMatchingFunction(embeddedFunction);
	}
	
	private double getWeight(IAttribute attr, String term) {
		double weight = 1;
		
		if(this.stopWords.contains(term)) {
			
		}
				
		return weight;
		
	}
	
	protected double getScore(IAttribute attr1, IAttribute attr2, List<String> list1, List<String> list2) {
		
		List<String> small, big;
		List<List<Double>>scores = new ArrayList<List<Double>>();
		if(list1.size()>list2.size()) {
			big = list1;
			small = list2;
		} else {
			big = list2;
			small = list1;
		}
		if(small.size()==0) return 0;
		for(int i=0;i<small.size();i++) {
			List<Double> row = new ArrayList<Double>(big.size());
			for(int j=0;j<big.size();j++) {
				row.add(embeddedFunction.getSimilarity(attr1, attr2, small.get(i).toLowerCase(), big.get(j).toLowerCase()));
			}
			scores.add(row);
		}
		
		double bestscore = 0;
		int maxi, maxj;
		double res = 0;
		double sumweights = 0;
		
		for(int k=0;k<small.size();k++) {
			bestscore = 0;
			maxi = 0;
			maxj = 0;
			for(int i=0;i<scores.size();i++) {
				for(int j=0;j<scores.get(i).size();j++) {
					if(scores.get(i).get(j)>=bestscore) {
						bestscore = scores.get(i).get(j);
						maxi = i;
						maxj = j;
					}
				}
			}
			for(List<Double>tmp : scores) {
				tmp.remove(maxj);
			}
			scores.remove(maxi);
			res+=bestscore;
		}
		res/=small.size();
		
		return res;
	}
		
	@Override
	public String toString() {
		return "l2 " + embeddedFunction.toString();
	}
	
	@Override
	public boolean isSuitableForAttributeTypes(AttributeType attributeType1, AttributeType attributeType2) {
		for(AttributeType[] types : supportedTypes) {
			if(types[0].equals(attributeType1)&&types[1].equals(attributeType2)
					||types[1].equals(attributeType1)&&types[0].equals(attributeType2)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isSuitableForAttributes(IAttribute attribute1, IAttribute attribute2) {
		for(AttributeType[] types : supportedTypes) {
			if(types[0].equals(attribute1.getType())&&types[1].equals(attribute2.getType())
					||types[1].equals(attribute1.getType())&&types[0].equals(attribute2.getType())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public AttributeType[][] suitableForTypes() {
		return supportedTypes;
	}

	@Override
	public void setAttributeFeatures(IAttribute attr1, IAttribute attr2) {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
