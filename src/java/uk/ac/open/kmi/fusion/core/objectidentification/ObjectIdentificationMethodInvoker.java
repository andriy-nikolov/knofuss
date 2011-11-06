package uk.ac.open.kmi.fusion.core.objectidentification;


import java.util.*;

import org.apache.log4j.Logger;


import uk.ac.open.kmi.fusion.api.*;
import uk.ac.open.kmi.fusion.api.impl.AtomicMapping;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.api.impl.FusionMethodWrapper;
import uk.ac.open.kmi.fusion.core.MethodInvoker;
import uk.ac.open.kmi.fusion.core.inconsistencyresolution.InconsistencyResolutionMethodInvoker;

public class ObjectIdentificationMethodInvoker extends MethodInvoker {
	
	private static Logger log = Logger.getLogger(ObjectIdentificationMethodInvoker.class);

	public ObjectIdentificationMethodInvoker() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void invoke() {
		try {
			IObjectIdentificationMethod impl = loadImplementation(method);
			log.info("Invoking matching method: "+method.getRDFIndividual().toString());
			List<AtomicMapping> results = impl.getAllPossibleMappings(context);
			log.info("Results produced: "+results.size());
			for(AtomicMapping item : results) {
				item.setConfidence(context.getReliability());
				FusionEnvironment.getInstance().addAtomicMapping(item);
			}
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}
		log.info("");
	}
	
	private IObjectIdentificationMethod loadImplementation(FusionMethodWrapper wrapper) throws Exception {
		IObjectIdentificationMethod impl;
		try {
			impl = (IObjectIdentificationMethod)wrapper.getImplementation();
		} catch(Exception e) {
			throw e;
		}
		return impl;
	}

}
