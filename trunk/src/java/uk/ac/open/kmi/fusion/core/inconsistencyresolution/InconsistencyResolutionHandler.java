package uk.ac.open.kmi.fusion.core.inconsistencyresolution;

import uk.ac.open.kmi.fusion.core.AtomicTaskHandler;

public class InconsistencyResolutionHandler extends AtomicTaskHandler {

	public InconsistencyResolutionHandler() {
		// TODO Auto-generated constructor stub
		super();
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		chooser = new InconsistencyResolutionMethodSetChooser();
		invoker = new InconsistencyResolutionMethodSetInvoker();
		combiner = new InconsistencyResolutionResultCombiner();
	}

}
