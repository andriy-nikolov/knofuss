package uk.ac.open.kmi.fusion.core.conflictdetection;


import java.util.*;

import org.openrdf.repository.RepositoryException;


import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.impl.*;
import uk.ac.open.kmi.fusion.core.ResultCombiner;

public class ConflictDetectionResultCombiner extends ResultCombiner {
	
	List<List> competingGroupsList;
	Hashtable<String, Integer> resultsAccepted; 
	
	private void init() {
		competingGroupsList = new ArrayList<List>();
		resultsAccepted = new Hashtable<String, Integer>();
	}

	public ConflictDetectionResultCombiner() {
		super();
		init();
	}

	@Override
	public void execute() throws RepositoryException {
		countProducedResults(
				FusionEnvironment.getInstance().getFusionKbValueFactory().createURI(FusionMetaVocabulary.CONFLICT_SET)
				);
		logProducedResults();
		selectCompetingGroups();
		resolveConflicts();
	}

	protected void selectCompetingGroups() {
		// TODO Reimplement for AccessKnow
	}
	
	protected void addToTable(ConflictStatementCluster conflictSet) {
		// TODO Reimplement for AccessKnow
	}
	
	protected void resolveConflicts() {
		// TODO Reimplement for AccessKnow
	}
	
	protected void processConflictGroup(List<ConflictStatementCluster> conflictInstances) {
		// TODO Reimplement for AccessKnow
	}
	
	/*	@Override
	protected void logOneResult(Resource res) {
		if(!hasResultCorrectClass(res, "ConflictSet")) return;
		Property propSourceInstance = res.getModel().getProperty(FusionGlobals.FUSION_ONTOLOGY_NS+"containsSourceInstance");
		Property propTargetInstance = res.getModel().getProperty(FusionGlobals.FUSION_ONTOLOGY_NS+"containsTargetInstance");
		if(res.getProperty(propSourceInstance)!=null) 
			FusionGlobals.log.info("<tr class=\"td_instance\"><td>Source instance</td><td>"+res.getProperty(propSourceInstance).getResource().getURI()+"</td></tr>");
		if(res.getProperty(propTargetInstance)!=null) 
			FusionGlobals.log.info("<tr class=\"td_instance\"><td>Target instance</td><td>"+res.getProperty(propTargetInstance).getResource().getURI()+"</td></tr>");
		logExplanation(res);
//		FusionGlobals.log.info("<br>");
	}*/
}
