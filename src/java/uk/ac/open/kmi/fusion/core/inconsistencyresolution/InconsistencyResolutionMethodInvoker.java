package uk.ac.open.kmi.fusion.core.inconsistencyresolution;

import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.openrdf.model.Statement;


import uk.ac.open.kmi.fusion.api.IInconsistencyResolutionMethod;
import uk.ac.open.kmi.fusion.api.impl.ConflictStatementCluster;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.api.impl.FusionMethodWrapper;
import uk.ac.open.kmi.fusion.core.MethodInvoker;

public class InconsistencyResolutionMethodInvoker extends MethodInvoker {

	private static Logger log = Logger.getLogger(InconsistencyResolutionMethodInvoker.class);
	
	public InconsistencyResolutionMethodInvoker() {
		super();
	}
	
	@Override
	public void invoke() {
		try {
			IInconsistencyResolutionMethod impl = loadImplementation(method);
			log.info("Invoking inconsistency resolution method: "+method.getRDFIndividual().toString());
			//impl.setApplicationContext(context);
			List<Statement> results = new ArrayList<Statement>();
			List<Statement> tmpList = new ArrayList<Statement>();
			for(ConflictStatementCluster conflict : FusionEnvironment.getInstance().getConflicts()) {
				tmpList = impl.processConflict(conflict);
				if(tmpList!=null)
					results.addAll(impl.processConflict(conflict));
			}
			log.info("Results produced: "+results.size());
			for(Statement item : results) {
				//FusionEnvironment.getInstance().addObject(item);
			}
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}
		
		log.info("");
	}
	
	private IInconsistencyResolutionMethod loadImplementation(FusionMethodWrapper wrapper) throws Exception {
		IInconsistencyResolutionMethod impl;
		try {
			impl = (IInconsistencyResolutionMethod)wrapper.getImplementation();
		} catch(Exception e) {
			throw e;
		}
		return impl;
	}

}
