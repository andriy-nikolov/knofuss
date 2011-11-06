package uk.ac.open.kmi.fusion.index.store;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.Directory;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.fusion.api.IDataSource;
import uk.ac.open.kmi.fusion.util.FusionException;

public interface ILuceneStore {

	public void openIndexWriter() throws FusionException;
	
	public void clearIndex() throws FusionException;
	
	public void addIndividual(URI ind, RepositoryConnection con) throws RepositoryException;
		
	public void closeIndexWriter() throws FusionException;

	void addDocument(Document doc, String type) throws CorruptIndexException,
			IOException;
	
	public void commit() throws FusionException;
	
	public void init() throws FusionException;
	
	public Directory getDirectory();

	int getPropertyPathDepth();

	void setPropertyPathDepth(int pathDepth);
	
	public void prepare(IDataSource embeddingDataSource) throws FusionException;
	
	public void close() throws FusionException;
	
}
