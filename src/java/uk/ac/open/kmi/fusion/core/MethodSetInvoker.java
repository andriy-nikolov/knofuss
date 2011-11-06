package uk.ac.open.kmi.fusion.core;

import java.util.Map;

import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.FusionMethodWrapper;

public abstract class MethodSetInvoker {

	protected Map<ApplicationContext, FusionMethodWrapper> methods;
	protected AtomicTaskHandler taskHandler;
	
	public MethodSetInvoker() {
		super();
	}
	
	public abstract void execute();

	public Map<ApplicationContext, FusionMethodWrapper> getMethods() {
		return methods;
	}

	public void setMethodList(Map<ApplicationContext, FusionMethodWrapper> methods) {
		this.methods = methods;
	}

	public AtomicTaskHandler getTaskHandler() {
		return taskHandler;
	}

	public void setTaskHandler(AtomicTaskHandler taskHandler) {
		this.taskHandler = taskHandler;
	}

}
