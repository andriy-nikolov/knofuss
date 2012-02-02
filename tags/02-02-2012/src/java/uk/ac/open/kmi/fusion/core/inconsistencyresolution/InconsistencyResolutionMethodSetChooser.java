package uk.ac.open.kmi.fusion.core.inconsistencyresolution;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;


import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.FusionMethodWrapper;
import uk.ac.open.kmi.fusion.core.MethodSetChooser;

public class InconsistencyResolutionMethodSetChooser extends MethodSetChooser {

	public InconsistencyResolutionMethodSetChooser() {
		super();
	}

	@Override
	protected void selectAvailableMethods() {
		selectAvailableMethods(FusionMethodWrapper.FUSION_METHOD_CAPABILITY_INCONSISTENCY_RESOLUTION);
	}

	@Override
	protected void selectApplicableMethods() {
		List<URI> processedIndividuals;
		List<URI> allProcessedIndividuals;
		Map<Resource, ApplicationContext> contexts;
		ApplicationContext context;
		for(FusionMethodWrapper stub : this.availableMethods) {
			contexts = stub.getApplicationContexts();
			for(Resource contextResource : contexts.keySet()) {
				context = contexts.get(contextResource);
				processedIndividuals = getRelevantIndividuals(context.serializeQuerySPARQLSource());
				if(processedIndividuals.size()>=0) {
					if(!applicableMethods.containsKey(context)) {
						applicableMethods.put(context, stub);
					}
					
				}
					
			}
			
		}
	}
	

	/*private ApplicationContext createGenericApplicationContext(FusionMethodWrapper wrapper) {
		ApplicationContext res = new ApplicationContext(wrapper);
				
		MySPARQLParser parser = new MySPARQLParser(wrapper.getQuerySPARQLSelect());
		res.setQuerySPARQLSelect(parser.getFilteredQuery());
		
		return res;
	}*/
	
	
}
