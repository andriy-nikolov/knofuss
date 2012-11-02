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
package uk.ac.open.kmi.fusion.api.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;


import org.apache.log4j.Logger;
import org.openrdf.model.URI;


import uk.ac.open.kmi.fusion.FusionMetaVocabulary;

public abstract class FusionSet extends FusionMethodResult {
	
	protected static Logger log = Logger.getLogger(FusionSet.class);
	

	public FusionSet(FusionMethodWrapper producedBy) {
		super(producedBy);
		
	}
	
	public FusionSet() {
		super();
		
	}
	
	public boolean isIdenticalTo(FusionSet dataInstance) {
		String curIndividual1, curIndividual2;
		FusionSet mergeSet;
		try {
			mergeSet = (FusionSet)dataInstance;
		} catch(ClassCastException e) {
			return false;
		}
		
		return false;
	}


	public abstract  Set<URI> getCandidateIndividuals();

	public Set<URI> getIndividuals() {
		Set<URI> result = new HashSet<URI>();
		result.addAll(getCandidateIndividuals());
		result.addAll(getMainKBIndividuals());
		
		return result;
	}

	public abstract Set<URI> getMainKBIndividuals();

	public Set<URI> getOtherIndividuals(URI individual) {
		Set<URI> res = new HashSet<URI>();

		for(URI ind : getMainKBIndividuals() ) {
			if(!ind.equals(individual)) {
				res.add(ind);
			}
		}
		for(URI ind : getCandidateIndividuals() ) {
			if(!ind.equals(individual)) {
				res.add(ind);
			}
		}
		return res;
	}
}
