package uk.ac.open.kmi.fusion.api;

import java.util.List;

import org.openrdf.model.URI;

import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.AtomicMapping;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.learning.cache.MemoryInstanceCache;
import uk.ac.open.kmi.fusion.util.FusionException;

/**
 * 
 * <p>
 * Represents an algorithm able to identify a potential mapping between two individuals 
 * </p>
 * @author Andriy Nikolov
 *
 */

public interface IObjectIdentificationMethod extends IAbstractFusionMethod {

	/**
	 * 
	 * <p>
	 * Returns a list of all possible mappings for the environment being processed.
	 * </p>
	 * @param onto
	 * @return 
	 * @throws FusionException 
	 */
	public List<AtomicMapping> getAllPossibleMappings(ApplicationContext context) throws FusionException;

	
	
}
