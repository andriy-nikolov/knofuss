package uk.ac.open.kmi.fusion.learning.cache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.open.kmi.fusion.api.impl.AtomicAttribute;
import uk.ac.open.kmi.fusion.api.impl.AtomicMapping;
import uk.ac.open.kmi.fusion.api.impl.CompositeAttribute;
import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;
import uk.ac.open.kmi.fusion.api.impl.VariableComparisonSpecification;

public class CachedPair {

	private CacheEntry candidateInstance;
	private CacheEntry targetInstance;
	private int id;
	private boolean missing;
	private boolean goldStandard = false;
	
	Logger log = Logger.getLogger(CachedPair.class);
	
	CachedPair(CacheEntry candidate, CacheEntry target, int id, boolean goldStandard) {
		this(candidate, target, id, goldStandard, false);
	}
	
	CachedPair(CacheEntry candidate, CacheEntry target, int id, boolean goldStandard, boolean missing) {
		this.candidateInstance = candidate;
		this.targetInstance = target;
		this.id = id;
		this.missing = missing;
		this.goldStandard = goldStandard;
	}

	public CacheEntry getCandidateInstance() {
		return candidateInstance;
	}

	public CacheEntry getTargetInstance() {
		return targetInstance;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public AtomicMapping convertToAtomicMapping() {
		
		AtomicMapping mapping = new AtomicMapping();
		
		mapping.setTargetIndividual(this.targetInstance.getUri());
		mapping.setSourceIndividual(this.candidateInstance.getUri());
		
		return mapping;
		
	}
	
	public AtomicMapping convertToAtomicMapping(double similarity) {
		
		AtomicMapping mapping = convertToAtomicMapping();
		mapping.setSimilarity(similarity);
		
		return mapping;
		
	}
	
	public AtomicMapping convertToAtomicMapping(double similarity, double confidence) {
		
		AtomicMapping mapping = convertToAtomicMapping(similarity);
		mapping.setConfidence(confidence);
		
		return mapping;
		
	}
	
	public AtomicMapping convertToAtomicMapping(Set<ObjectContextModel> modelSpecs, double similarity, double confidence) {
		AtomicMapping mapping = convertToAtomicMapping(similarity, confidence);
		
		List<String> sourceProperties = new ArrayList<String>();
		List<String> targetProperties = new ArrayList<String>();
		
		Set<String> tmpSet;	
		
		for(ObjectContextModel modelSpec : modelSpecs) {
			if(modelSpec!=null) {
				for(VariableComparisonSpecification varSpec : modelSpec.getVariableComparisonSpecifications()) {
					tmpSet = varSpec.getSourceAttribute().getAtomicAttributesByPropertyPath().keySet();
					for (String tmp : tmpSet) {
						if(!sourceProperties.contains(tmp)) {
							sourceProperties.add(tmp);
						}
					}
					
					tmpSet = varSpec.getTargetAttribute().getAtomicAttributesByPropertyPath().keySet();
					for(String tmp : tmpSet) {
						if(!targetProperties.contains(tmp)) {
							targetProperties.add(tmp);
						}
					}
				}
			}
		}
		
		

		String sourceLabel = "", targetLabel = "";
		
		for(String tmp : sourceProperties) {
			if(this.candidateInstance.getValueTable().containsKey(tmp)) {
				sourceLabel = sourceLabel.concat(" "+this.candidateInstance.getValueTable().get(tmp).get(0).toString()+" ");
			}
		}
		
		for(String tmp : targetProperties) {
			if(this.targetInstance.getValueTable().containsKey(tmp)) {
				targetLabel = targetLabel.concat(" "+this.targetInstance.getValueTable().get(tmp).get(0).toString()+" ");
			}
		}
		
		mapping.setSourceLabel(sourceLabel.trim());
		mapping.setTargetLabel(targetLabel.trim());
		mapping.setSimilarity(similarity);
		return mapping;
		
	}

	public boolean isMissing() {
		return missing;
	}

	public void setMissing(boolean missing) {
		this.missing = missing;
	}

	public boolean isGoldStandard() {
		return goldStandard;
	}

	public void setGoldStandard(boolean goldStandard) {
		this.goldStandard = goldStandard;
	}

	public AtomicMapping convertToAtomicMapping(ObjectContextModel modelSpec,
			double similarity, double precision) {
		
		Set<ObjectContextModel> modelSpecs = new HashSet<ObjectContextModel>();
		modelSpecs.add(modelSpec);
		return convertToAtomicMapping(modelSpecs, similarity, precision);
	}
	
	

}
