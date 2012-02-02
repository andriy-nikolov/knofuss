package uk.ac.open.kmi.fusion.index.store;

import java.io.IOException;

import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;

import uk.ac.open.kmi.fusion.api.IDataSource;
import uk.ac.open.kmi.fusion.util.FusionException;

public class LuceneMemoryStoreStrategy extends AbstractLuceneStore {

	public LuceneMemoryStoreStrategy() {
		super();
	}

	@Override
	public void init() throws FusionException {
		directory = new RAMDirectory();
	}

	@Override
	public void prepare(IDataSource embeddingDataSource) throws FusionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() throws FusionException {
		// TODO Auto-generated method stub
		
	}
	
	
}
