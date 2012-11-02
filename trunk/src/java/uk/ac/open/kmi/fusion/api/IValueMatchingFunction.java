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
package uk.ac.open.kmi.fusion.api;

import uk.ac.open.kmi.fusion.api.impl.AttributeType;

public interface IValueMatchingFunction<T> {

	public static final String JARO = "jaro";
	public static final String ABBREVIATION = "abbr";
	public static final String JARO_WINKLER = "jaro-winkler";
	public static final String L2_JARO = "l2 jaro";
	public static final String L2_JARO_WINKLER = "l2 jaro-winkler";
	public static final String AVERAGE_JARO_WINKLER = "average jaro-winkler";
	public static final String LEVENSHTEIN = "levenshtein";
	public static final String L2_LEVENSHTEIN = "l2 levenshtein";
	public static final String MONGE_ELKAN = "monge-elkan";
	public static final String L2_MONGE_ELKAN = "l2 monge-elkan";
	public static final String SMITH_WATERMAN = "smith-waterman";
	public static final String L2_SMITH_WATERMAN = "l2 smith-waterman";
	public static final String PERSON_NAME_JARO = "person name jaro";
	public static final String tokenwise_JARO = "tokenwise_jaro";
	public static final String tokenwise_JARO_WINKLER  = "tokenwise_jarowinkler";
	public static final String tokenwise_LEVENSHTEIN = "tokenwise_levenshtein";
	public static final String tokenwise_SMITH_WATERMAN = "tokenwise_smith";
	public static final String I_SUB = "i-sub";
	public static final String JACCARD = "jaccard";
	public static final String OVERLAP = "overlap";
	public static final String DICE = "dice";
	public static final String DATE = "date";
	public static final String DOUBLE = "double";


	public double getSimilarity(IAttribute attr1, IAttribute attr2, T value1, T value2);

	public boolean isSuitableForAttributeTypes(AttributeType attributeType1, AttributeType attributeType2);
	public boolean isSuitableForAttributes(IAttribute attribute1, IAttribute attribute2);

	public AttributeType[][] suitableForTypes();

	public void setAttributeFeatures(IAttribute attr1, IAttribute attr2);

}
