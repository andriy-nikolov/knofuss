package uk.ac.open.kmi.fusion.api;

import java.util.List;

import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.AtomicMapping;

public interface IDatasetMatchingMethod extends IAbstractFusionMethod {

	List<AtomicMapping> refineMappings(ApplicationContext context, List<AtomicMapping> existingMappings);
	
}
