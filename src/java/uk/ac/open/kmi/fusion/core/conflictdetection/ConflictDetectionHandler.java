package uk.ac.open.kmi.fusion.core.conflictdetection;

import uk.ac.open.kmi.fusion.core.AtomicTaskHandler;

public class ConflictDetectionHandler extends AtomicTaskHandler {

	public ConflictDetectionHandler() {
		// TODO Auto-generated constructor stub
		super();
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		chooser = new ConflictDetectionMethodSetChooser();
		invoker = new ConflictDetectionMethodSetInvoker();
		combiner = new ConflictDetectionResultCombiner();
	}

}
