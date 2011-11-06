package uk.ac.open.kmi.fusion.core;

import java.util.*;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;


import uk.ac.open.kmi.fusion.api.ILinkSession;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;

public abstract class AtomicTaskHandler {
	
	protected MethodSetChooser chooser;
	protected MethodSetInvoker invoker;
	protected ResultCombiner combiner;
	protected ILinkSession dispatcher;
	protected String methodInvocationURI;
	protected Hashtable<String, URI> methodInvocationIds;
	
	private static Logger log = Logger.getLogger(AtomicTaskHandler.class);
	
	public abstract void init();
	
	public AtomicTaskHandler() {
		super();
		init();
		chooser.setTaskHandler(this);
		invoker.setTaskHandler(this);
		combiner.setTaskHandler(this);
		methodInvocationIds = new Hashtable<String, URI>();
	}

	public void handleTask() throws RepositoryException {
		chooser.execute();
		invoker.setMethodList(chooser.getMethodsToInvoke());
		log.info("<h2>Invoking methods</h2>");
		invoker.execute();
		log.info("<h2>Processing results</h2>");
		combiner.execute();
	}

	public ILinkSession getDispatcher() {
		return dispatcher;
	}

	public void setDispatcher(ILinkSession dispatcher) {
		this.dispatcher = dispatcher;
	}

	public URI getMethodInvocationId(String descriptorURI) {
		return methodInvocationIds.get(descriptorURI);
	}

	public void addMethodInvocationId(String descriptorURI, URI methodInvocation) {
		this.methodInvocationIds.put(descriptorURI, methodInvocation);
	}
	
}
