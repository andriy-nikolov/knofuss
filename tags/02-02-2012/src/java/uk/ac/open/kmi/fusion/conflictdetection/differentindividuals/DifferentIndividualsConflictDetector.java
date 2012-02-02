package uk.ac.open.kmi.fusion.conflictdetection.differentindividuals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;


import uk.ac.open.kmi.fusion.api.IConflictDetectionMethod;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.AtomicMapping;
import uk.ac.open.kmi.fusion.api.impl.ConflictStatementCluster;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.api.impl.FusionMethodWrapper;
import uk.ac.open.kmi.fusion.api.impl.MappingSet;

public class DifferentIndividualsConflictDetector implements
		IConflictDetectionMethod {

	ApplicationContext context = null;
	FusionEnvironment environment = null;
	List<List<AtomicMapping>> competingGroupsList = null;
	
	FusionMethodWrapper descriptor = null;
	
	boolean allSourceDifferent = false;
	boolean allTargetDifferent = false;
	
	private static Logger log = Logger.getLogger(DifferentIndividualsConflictDetector.class);
	
	public DifferentIndividualsConflictDetector() {
		
	}
	
	private ConflictStatementCluster createConflictSet(MappingSet mappingSet, List<AtomicMapping> conflictingMappings) {
		ConflictStatementCluster conflictSet = new ConflictStatementCluster();
		conflictSet.setMappingSet(mappingSet);
		mappingSet.addConflict(conflictSet);
		for(AtomicMapping atomMapping : conflictingMappings) {
			conflictSet.addAtomicMapping(atomMapping);
		}
		return conflictSet;
	}

	public List<ConflictStatementCluster> discoverAllConflicts(
			FusionEnvironment onto) {
		List<ConflictStatementCluster> conflictingSets = new ArrayList<ConflictStatementCluster>();
		List<ConflictStatementCluster> tmpList;
		List<MappingSet> mappingSets = FusionEnvironment.getInstance().getMappingSets();
		for(MappingSet mappingSet : mappingSets) {
			tmpList = this.discoverConflicts(mappingSet);
			if(tmpList!=null) {
				conflictingSets.addAll(tmpList);
			}
		}
		return conflictingSets;
	}

	public List<ConflictStatementCluster> discoverConflicts(MappingSet mappingSet) {
		List<AtomicMapping> mappings;
		//List<IAtomicMapping> competingMappings;
		List<List<AtomicMapping>> competingMappings;
		Map<URI, List<AtomicMapping>> edgeTable = new HashMap<URI, List<AtomicMapping>>(); 
		List<AtomicMapping> edgeList;
		List<ConflictStatementCluster> conflictingSets = new ArrayList<ConflictStatementCluster>();
		ConflictStatementCluster conflictSet;
		
		mappings = mappingSet.getMappings();
		if(mappings.size()>1) {
			for(AtomicMapping mapping : mappingSet.getMappings()) {
				for(URI ind : mapping.getIndividuals()) {
					if(edgeTable.containsKey(ind)) {
						edgeList = edgeTable.get(ind);
						edgeList.add(mapping);
					} else {
						edgeList = new ArrayList<AtomicMapping>();
						edgeList.add(mapping);
						edgeTable.put(ind, edgeList);
					}
				}
			}
			if(this.allTargetDifferent) {
				for(URI ind : mappingSet.getCandidateIndividuals()) { 
					if(edgeTable.containsKey(ind)) {
						if(edgeTable.get(ind).size()>1) {
							displayConflictingMappings(ind, edgeTable.get(ind));
							competingMappings = this.getPairwiseConflicts(edgeTable.get(ind));
							for(List<AtomicMapping> tmpList : competingMappings) {
								conflictSet = this.createConflictSet(mappingSet, tmpList);
								conflictingSets.add(conflictSet);
							}							
						}
					}
				}
			}
			if(this.allSourceDifferent) {
				for(URI ind : mappingSet.getMainKBIndividuals()) {
					if(edgeTable.containsKey(ind)) {
						if(edgeTable.get(ind).size()>1) {
							displayConflictingMappings(ind, edgeTable.get(ind));
							competingMappings = this.getPairwiseConflicts(edgeTable.get(ind));
							for(List<AtomicMapping> tmpList : competingMappings) {
								conflictSet = this.createConflictSet(mappingSet, tmpList);
								conflictingSets.add(conflictSet);
							}
						}
					}
				}
			}
		} 
		
		return conflictingSets;
	}
	
	private void displayConflictingMappings(URI ind, List<AtomicMapping> mappings) {
		log.info("Conflict : "+ind.toString());
		for(AtomicMapping mapping : mappings) {
			try {
				log.info("\t Conflicting owl:sameAs mapping: ");
				for(URI confInd : mapping.getIndividuals()) {
					log.info("\t\t"+confInd.toString());
				}
				
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private List<List<AtomicMapping>> getPairwiseConflicts(List<AtomicMapping> mappings) {
		List<List<AtomicMapping>> res = new ArrayList<List<AtomicMapping>>();
		List<AtomicMapping> tmpList;
		for(int i=0;i<mappings.size();i++) {
			for(int j=i+1;j<mappings.size();j++) {
				tmpList = new ArrayList<AtomicMapping>(2);
				tmpList.add(mappings.get(i));
				tmpList.add(mappings.get(j));
				res.add(tmpList);
			}
		}
		return res;
	}

	public void setApplicationContext(ApplicationContext context) {
		this.context = context;
	}

	public FusionMethodWrapper getDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setMethodDescriptor(FusionMethodWrapper descriptor) {
		this.descriptor = descriptor;
		String val;
		Map<String, String> properties = this.descriptor.getProperties();
		for(String key : properties.keySet()) {
			val = properties.get(key);
			if(key.equals("http://kmi.open.ac.uk/fusion/conflictdetector#allDifferentSource")) {
				 if(Boolean.parseBoolean(val)) {
					 this.allSourceDifferent = true;
				 }
			} else if(key.equals("http://kmi.open.ac.uk/fusion/conflictdetector#allDifferentTarget")) {
				if(Boolean.parseBoolean(val)) {
					 this.allTargetDifferent = true;
				 }
			}
		}
	}

}
