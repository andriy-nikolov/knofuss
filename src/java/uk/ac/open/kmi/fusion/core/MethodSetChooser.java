package uk.ac.open.kmi.fusion.core;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.apache.log4j.Logger;
import org.openrdf.OpenRDFException;
import org.openrdf.model.*;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.*;
import org.openrdf.rio.n3.N3WriterFactory;
import org.openrdf.model.vocabulary.*;

import uk.ac.open.kmi.fusion.*;
import uk.ac.open.kmi.fusion.api.*;
import uk.ac.open.kmi.fusion.api.impl.*;
import uk.ac.open.kmi.fusion.util.SesameUtils;


public abstract class MethodSetChooser {

	protected List<FusionMethodWrapper> availableMethods;
	protected Hashtable<ApplicationContext, FusionMethodWrapper> methodsToInvoke;
	protected Hashtable<ApplicationContext, FusionMethodWrapper> applicableMethods;
	protected Hashtable<String, URI> tableIndividuals;
	protected AtomicTaskHandler taskHandler;
	protected int capability;
	protected FusionEnvironment fusionEnvironment;
	
	private static Logger log = Logger.getLogger(MethodSetChooser.class);
	
	public void init() {
		availableMethods = new ArrayList<FusionMethodWrapper>();
		applicableMethods = new Hashtable<ApplicationContext, FusionMethodWrapper>();
		methodsToInvoke = new Hashtable<ApplicationContext, FusionMethodWrapper>();
		tableIndividuals = new Hashtable<String, URI>();
	}
	
	public MethodSetChooser() {
		super();
		init();
	}
	
	public void execute() throws RepositoryException {
		log.info("<h2>Choosing methods</h2>");
		selectAvailableMethods();
		fillTableIndividuals();
		selectApplicableMethods();
		selectMethodsToInvoke();
	}

	protected abstract void selectAvailableMethods();
	
	protected void selectAvailableMethods(int capability) {
		this.capability = capability;
		List<FusionMethodWrapper> methods;
		
		availableMethods.clear();
		methods = taskHandler.getDispatcher().getFusionEnvironment().getFusionMethods(capability);
		availableMethods.addAll(methods);
	}
	
	protected void fillTableIndividuals() throws RepositoryException {
		//List<Resource> inds = SesameUtils.findAllIndividuals(FusionEnvironment.getInstance().getFusionRepositoryConnection());
	
		Set<URI> contentUris = FusionEnvironment.getInstance().getContentIndividualUris();
		for(URI uri : contentUris) {
			try {
				
				if(!tableIndividuals.containsKey(uri.toString())) {
					tableIndividuals.put(uri.toString(), uri);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		log.info("Individuals at method selection: "+tableIndividuals.size());
		
	}
	
	public AtomicTaskHandler getTaskHandler() {
		return taskHandler;
	}

	public void setTaskHandler(AtomicTaskHandler taskHandler) {
		this.taskHandler = taskHandler;
	}

	public Map<ApplicationContext, FusionMethodWrapper> getMethodsToInvoke() {
		return methodsToInvoke;
	}

	protected abstract void selectApplicableMethods();
	
	protected void selectMethodsToInvoke() {
		methodsToInvoke.putAll(applicableMethods);
	}
	
	protected List<URI> getRelevantIndividuals(String query) {
		List<URI> result = new ArrayList<URI>();
		
		log.info(query);
		
		
		
		String uri;
		
		try {
			TupleQuery sesameQuery = FusionEnvironment.getInstance().getFusionRepositoryConnection().prepareTupleQuery(
					QueryLanguage.SPARQL, 
					query);
			
			
			TupleQueryResult queryResult = sesameQuery.evaluate();
			BindingSet tuple;
			try {
				while(queryResult.hasNext()) {
					tuple = queryResult.next();
					if(tuple.getValue("uri") instanceof URI) {
						if(tableIndividuals.containsKey(tuple.getValue("uri").toString())) {
							result.add((URI)tuple.getValue("uri"));
						}
					}
					
				}
			} finally {
				queryResult.close();
			}
			
		} catch(OpenRDFException e) {
			
			e.printStackTrace();
		}
		log.info("The selection query covers "+result.size()+" individuals");
		return result;
	}
}
