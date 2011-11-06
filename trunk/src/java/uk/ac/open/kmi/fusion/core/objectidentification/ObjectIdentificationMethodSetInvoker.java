package uk.ac.open.kmi.fusion.core.objectidentification;

import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.FusionMethodWrapper;
import uk.ac.open.kmi.fusion.core.MethodInvoker;
import uk.ac.open.kmi.fusion.core.MethodSetInvoker;

public class ObjectIdentificationMethodSetInvoker extends MethodSetInvoker {

	public ObjectIdentificationMethodSetInvoker() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		int i;
		FusionMethodWrapper curMethod;
		MethodInvoker invoker;
		invoker = new ObjectIdentificationMethodInvoker();
		invoker.setMethodSetInvoker(this);
		for(ApplicationContext context: methods.keySet()) {
			curMethod = methods.get(context);
			invoker.setMethod(curMethod);
			invoker.setContext(context);
			invoker.invoke();
		}
	}

}
