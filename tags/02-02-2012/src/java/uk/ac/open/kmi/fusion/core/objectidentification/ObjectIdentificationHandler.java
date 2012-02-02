package uk.ac.open.kmi.fusion.core.objectidentification;

import uk.ac.open.kmi.fusion.core.AtomicTaskHandler;

public class ObjectIdentificationHandler extends AtomicTaskHandler {

	public ObjectIdentificationHandler() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		chooser = new ObjectIdentificationMethodSetChooser();
		combiner = new ObjectIdentificationResultCombiner();
		invoker = new ObjectIdentificationMethodSetInvoker();
	}

}
