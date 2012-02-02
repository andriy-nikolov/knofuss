package uk.ac.open.kmi.fusion.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.AttributeProfileInDataset;
import uk.ac.open.kmi.fusion.util.FusionException;

public interface IPersistentStore {

	public void prepare() throws FusionException;
	
	public int copyRelevantSubsetToBlocker(ILuceneBlocker blocker, ApplicationContext context, Map<String, AttributeProfileInDataset> targetAttributes) throws FusionException;
	
	public IDataSource getEmbeddingDataSource();
	
	public void setEmbeddingDataSource(IDataSource dataSource);
	
	public void close() throws FusionException;
	
}
