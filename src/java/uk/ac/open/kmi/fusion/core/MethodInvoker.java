package uk.ac.open.kmi.fusion.core;

import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.FusionMethodWrapper;

public abstract class MethodInvoker {

	protected MethodSetInvoker methodSetInvoker;
	protected FusionMethodWrapper method;
	protected ApplicationContext context;
	
	public MethodInvoker() {
		super();
		// TODO Auto-generated constructor stub
	}

	public abstract void invoke();

	public MethodSetInvoker getMethodSetInvoker() {
		return methodSetInvoker;
	}

	public void setMethodSetInvoker(MethodSetInvoker methodSetInvoker) {
		this.methodSetInvoker = methodSetInvoker;
	}

	public FusionMethodWrapper getMethod() {
		return method;
	}

	public void setMethod(FusionMethodWrapper method) {
		this.method = method;
	}

	public ApplicationContext getContext() {
		return context;
	}

	public void setContext(ApplicationContext context) {
		this.context = context;
	}
}
