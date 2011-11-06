package uk.ac.open.kmi.fusion.index.store;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.SimpleFSDirectory;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.fusion.api.IDataSource;
import uk.ac.open.kmi.fusion.api.ILuceneBlocker;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.AttributeProfileInDataset;
import uk.ac.open.kmi.fusion.index.LuceneDiskIndexer;
import uk.ac.open.kmi.fusion.util.FusionException;
import uk.ac.open.kmi.fusion.util.SesameUtils;

public class LuceneDiskStoreStrategy extends AbstractLuceneStore {

	File indexDirFile;
	
	private static Logger log = Logger.getLogger(LuceneDiskStoreStrategy.class);
	
	public LuceneDiskStoreStrategy(File indexDirFile) {
		super();
		this.indexDirFile = indexDirFile;
	}

	@Override
	public void init() throws FusionException {
		
		if(!indexDirFile.isDirectory()){
			  throw new IllegalArgumentException("The index directory set: "
						+ indexDirFile.getPath() + " isn't a directory.");
		}
				
		try {
			directory = new SimpleFSDirectory(indexDirFile, null);
		} catch(IOException e) {
			throw new FusionException("Could not create a Lucene disk index directory: ", e); 
		}
		
	}
	
	public void prepare(IDataSource embeddingDataSource) throws FusionException {
		try {
			openIndexWriter();
			this.indexWriter.deleteAll();
			int i = 0;
			log.info("Indexing started");
			Set<Resource> res = SesameUtils.findAllIndividuals(embeddingDataSource.getConnection());
			
			for(Resource ind : res) {
				//log.info((i++)+" out of "+res.size());
				if(ind instanceof URI) {
					addIndividual((URI)ind, embeddingDataSource.getConnection());
				}
				
			}
			closeIndexWriter();
			
			log.info("Indexing finished, "+res.size()+" individuals indexed");
		} catch(IOException e) {
			throw new FusionException(e);
		} catch(RepositoryException e) {
			throw new FusionException(e);
		}
	}	
	

	public void close() throws FusionException {
		this.closeIndexWriter();		
	}

	
	
}
