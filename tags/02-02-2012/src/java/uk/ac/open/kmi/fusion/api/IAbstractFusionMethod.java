package uk.ac.open.kmi.fusion.api;

import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.FusionMethodWrapper;

/**
 * <p>
 * Abstract interface of all fusion methods
 * <p>
 * 
 * @author an2548
 *
 */
public interface IAbstractFusionMethod {
	// public void setApplicationContext(ApplicationContext context);
	public void setMethodDescriptor(FusionMethodWrapper descriptor);
	public FusionMethodWrapper getDescriptor();
}
