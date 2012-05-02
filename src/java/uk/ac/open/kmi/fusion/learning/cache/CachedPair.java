package uk.ac.open.kmi.fusion.learning.cache;

import java.util.ArrayList;
import java.util.List;
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
	
	public AtomicMapping convertToAtomicMapping(ObjectContextModel modelSpec, double similarity, double confidence) {
		AtomicMapping mapping = convertToAtomicMapping(similarity, confidence);
		
		List<VariableComparisonSpecification> varSpecs = new ArrayList<VariableComparisonSpecification>();
		if(modelSpec!=null) {
			varSpecs.addAll(modelSpec.getVariableComparisonSpecifications());
		}

		String sourceProperty, targetProperty;
		String sourceLabel = "", targetLabel = "";
		String value;
		for(VariableComparisonSpecification varSpec : varSpecs) {
			if(varSpec.getTargetAttribute() instanceof AtomicAttribute) {
				sourceProperty = ((AtomicAttribute)varSpec.getSourceAttribute()).getPropertyPath();
				targetProperty = ((AtomicAttribute)varSpec.getTargetAttribute()).getPropertyPath();
				
				if(this.candidateInstance.getValueTable().containsKey(sourceProperty)) {
					sourceLabel = sourceLabel.concat(" "+this.candidateInstance.getValueTable().get(sourceProperty).get(0).toString()+" ");
				} else {
				//	log.error("Property value missing: "+this.candidateInstance.getUri().toString()+", "+sourceProperty);
				}
				
				if(this.targetInstance.getValueTable().containsKey(targetProperty)) {
					value = this.targetInstance.getValueTable().get(targetProperty).get(0).toString().toString();
					targetLabel = targetLabel.concat(" "+value+" ");
				} else {
				//	log.error("Property value missing: "+this.targetInstance.getUri().toString()+", "+targetProperty);
				}
				// this.candidateInstance.getValueTable().get(sourceProperty).get(0);
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
	
	

}
