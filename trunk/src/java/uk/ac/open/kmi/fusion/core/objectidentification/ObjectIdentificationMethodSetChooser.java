package uk.ac.open.kmi.fusion.core.objectidentification;

import java.util.*;

import org.apache.log4j.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;

import uk.ac.open.kmi.fusion.*;
import uk.ac.open.kmi.fusion.api.*;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.FusionMethodWrapper;
import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;
import uk.ac.open.kmi.fusion.core.MethodSetChooser;
import uk.ac.open.kmi.fusion.util.SesameUtils;

public class ObjectIdentificationMethodSetChooser extends MethodSetChooser {

	private static Logger log = Logger.getLogger(ObjectIdentificationMethodSetChooser.class);
	
	public ObjectIdentificationMethodSetChooser() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void selectAvailableMethods() {
		selectAvailableMethods(FusionMethodWrapper.FUSION_METHOD_CAPABILITY_OBJECTIDENTIFICATION);
	}

	@Override
	protected void selectApplicableMethods() {
		List<URI> processedIndividuals;
		List<URI> allProcessedIndividuals;
		List<ApplicationContext> sortedContexts = new ArrayList<ApplicationContext>();
		Set<URI> types;
		Hashtable<String, ApplicationContext> contextTable = new Hashtable<String, ApplicationContext>();
		Map<Resource, ApplicationContext> contexts;
		ApplicationContext context;
		String query;
		for(FusionMethodWrapper stub : this.availableMethods) {

			
				contexts = stub.getApplicationContexts();
				for(Resource contextUri : contexts.keySet()) {
					context = contexts.get(contextUri);
					query = context.serializeQuerySPARQLSource();
					/*if(this.getTaskHandler().getDispatcher().isMultiOntology()) {
						try {
							query = this.getTaskHandler().getDispatcher().getMultiOntologyUtil().translateSPARQLQuery(query);
						} catch(Exception e) {
							e.printStackTrace();
						}
					}*/
					processedIndividuals = getRelevantIndividuals(query);
					if(processedIndividuals.size()>0) {
						if(!applicableMethods.containsKey(context)) {
							applicableMethods.put(context, stub);
						}
					}
				}
				
				// Create ApplicationContext objects for individuals not processed before
				
/*				if(truethis.getTaskHandler().getDispatcher().isMultiOntology()) {
					return;
				}*/
				
		
			
		}
	}
	
	/*private ApplicationContext createApplicationContextForConcept(FusionMethodWrapper wrapper, URI concept) {
		log.info("Creating application context for the concept: "+concept.toString());
		ApplicationContext res = new ApplicationContext(wrapper);
		
		MySPARQLParser parser = new MySPARQLParser(wrapper.getQuerySPARQLSelect());
		log.debug(wrapper.getQuerySPARQLSelect());
		
		parser.bindVarType("uri", concept.toString());
		
		res.setConcept(concept);
		log.debug(parser.getFilteredQuery());
		res.setQuerySPARQLSelect(parser.getFilteredQuery());
		
		SimpleFeatureSelector selector = new SimpleFeatureSelector();
		selector.setEnvironment(FusionEnvironment.getInstance());
		ObjectContextModel model = selector.getContextModelForContext(res, concept);
		res.addConnectedObject(model);
		res.setReliability(wrapper.getReliability());
		return res;
	}*/
	
	
}
