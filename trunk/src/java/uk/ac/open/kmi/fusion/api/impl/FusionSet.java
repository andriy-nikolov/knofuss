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
