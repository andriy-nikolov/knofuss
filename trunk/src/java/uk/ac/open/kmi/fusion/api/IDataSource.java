package uk.ac.open.kmi.fusion.api;

import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;

import uk.ac.open.kmi.fusion.util.FusionException;

public interface IDataSource extends IPersistentStore {

	public RepositoryConnection getConnection();
	public ValueFactory getValueFactory();
		
}
