package uk.ac.open.kmi.fusion.core.conflictdetection;


import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.FusionMethodWrapper;
import uk.ac.open.kmi.fusion.core.MethodInvoker;
import uk.ac.open.kmi.fusion.core.MethodSetInvoker;

public class ConflictDetectionMethodSetInvoker extends MethodSetInvoker {

	public ConflictDetectionMethodSetInvoker() {
		super();
	}

	@Override
	public void execute() {
		FusionMethodWrapper curMethod;
		MethodInvoker invoker;
		invoker = new ConflictDetectionMethodInvoker();
		invoker.setMethodSetInvoker(this);
		for(ApplicationContext context: methods.keySet()) {
			curMethod = methods.get(context);
			invoker.setMethod(curMethod);
			invoker.setContext(context);
			invoker.invoke();
		}
	}

}
