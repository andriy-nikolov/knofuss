package uk.ac.open.kmi.fusion.core.conflictdetection;

import java.util.List;

import org.apache.log4j.Logger;



import uk.ac.open.kmi.fusion.api.IConflictDetectionMethod;
import uk.ac.open.kmi.fusion.api.impl.ConflictStatementCluster;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.api.impl.FusionMethodWrapper;
import uk.ac.open.kmi.fusion.core.MethodInvoker;
import uk.ac.open.kmi.fusion.core.MethodSetChooser;

public class ConflictDetectionMethodInvoker extends MethodInvoker {
	
	private static Logger log = Logger.getLogger(ConflictDetectionMethodInvoker.class);

	public ConflictDetectionMethodInvoker() {
		super();
	}
	
	@Override
	public void invoke() {
		try {
			IConflictDetectionMethod impl = loadImplementation(method);
			log.info("Invoking conflict detection method: "+method.getRDFIndividual().toString());
			//impl.setApplicationContext(context);
			List<ConflictStatementCluster> results = impl.discoverAllConflicts(FusionEnvironment.getInstance());
			log.info("Results produced: "+results.size());
			for(ConflictStatementCluster item : results) {
				FusionEnvironment.getInstance().addConflictSet(item);
			}
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}
		
		log.info("");
	}
	
	private IConflictDetectionMethod loadImplementation(FusionMethodWrapper wrapper) throws Exception {
		IConflictDetectionMethod impl;
		try {
			impl = (IConflictDetectionMethod)wrapper.getImplementation();
		} catch(Exception e) {
			throw e;
		}
		return impl;
	}

}
