package uk.ac.open.kmi.fusion.core.conflictdetection;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

//import uk.ac.open.kmi.fusion.MySPARQLParser;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;


import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.FusionMethodWrapper;
import uk.ac.open.kmi.fusion.core.MethodSetChooser;

public class ConflictDetectionMethodSetChooser extends MethodSetChooser {

	public ConflictDetectionMethodSetChooser() {
		super();
	}

	@Override
	protected void selectAvailableMethods() {
		selectAvailableMethods(FusionMethodWrapper.FUSION_METHOD_CAPABILITY_CONFLICT_DETECTION);
	}

	@Override
	protected void selectApplicableMethods() {
		List<URI> processedIndividuals;
		List<URI> allProcessedIndividuals;
		Map<Resource, ApplicationContext> contexts;
		ApplicationContext context;
		for(FusionMethodWrapper stub : this.availableMethods) {
			
			contexts = stub.getApplicationContexts();
			for(Resource contextUri : contexts.keySet()) {
				context = contexts.get(contextUri);
				processedIndividuals = getRelevantIndividuals(context.serializeQuerySPARQLSource());
				if(processedIndividuals.size()>=0) {
					if(!applicableMethods.containsKey(context)) {
						applicableMethods.put(context, stub);
					}
				}
			}
		}
	}

	
	
}
