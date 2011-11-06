package uk.ac.open.kmi.fusion.api;

import java.io.IOException;

import org.openrdf.OpenRDFException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;

public interface IDump {

	public void loadToRepository(IDataSource repository) throws OpenRDFException, IOException;
	
}
