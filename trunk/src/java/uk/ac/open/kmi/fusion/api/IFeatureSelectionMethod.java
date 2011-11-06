package uk.ac.open.kmi.fusion.api;

import java.util.List;

import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;

public interface IFeatureSelectionMethod extends IAbstractFusionMethod {
	
	public List<ObjectContextModel> getAllContextModels() throws RepositoryException;
	
}
