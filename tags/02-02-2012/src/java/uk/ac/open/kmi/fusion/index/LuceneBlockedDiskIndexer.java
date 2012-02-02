package uk.ac.open.kmi.fusion.index;

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
import org.apache.lucene.store.Directory;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.IDataSource;
import uk.ac.open.kmi.fusion.api.ILuceneBlocker;
import uk.ac.open.kmi.fusion.api.IPersistentStore;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.AttributeProfileInDataset;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.index.search.ILuceneSearchStrategy;
import uk.ac.open.kmi.fusion.index.search.LuceneAlignedFieldsEnhancedSearchStrategy;
import uk.ac.open.kmi.fusion.index.search.LuceneAlignedFieldsSearchStrategy;
import uk.ac.open.kmi.fusion.index.search.LuceneAllFieldsSearchStrategy;
import uk.ac.open.kmi.fusion.index.search.LuceneAllFieldsSearchStrategyFuzzy;
import uk.ac.open.kmi.fusion.index.search.LuceneBlockedSearchStrategy;
import uk.ac.open.kmi.fusion.index.store.ILuceneStore;
import uk.ac.open.kmi.fusion.index.store.LuceneDiskStoreStrategy;
import uk.ac.open.kmi.fusion.index.store.LuceneEnhancedStoreStrategy;
import uk.ac.open.kmi.fusion.util.FusionException;
import uk.ac.open.kmi.fusion.util.SesameUtils;

public class LuceneBlockedDiskIndexer extends LuceneIndexer implements IPersistentStore {

	public static final String TYPE_URI = FusionMetaVocabulary.LUCENE_BLOCKED_DISK_BLOCKER;
	
	private static Logger log = Logger.getLogger(LuceneBlockedDiskIndexer.class);
	
	String indexDirectory = null;
	boolean refresh = false;
	IDataSource embeddingDataSource;
	
	boolean abbreviatedNames = false;
	boolean deletedClasses = false;
	
	protected LuceneBlockedDiskIndexer() {
		this.storePolicy = LuceneStorePolicy.DISK;
	}
	
	public LuceneBlockedDiskIndexer(ILuceneStore storeStrategy,
			ILuceneSearchStrategy searchStrategy) {
		super(storeStrategy, searchStrategy);
	}
	
	public LuceneBlockedDiskIndexer(Resource rdfIndividual,
			FusionEnvironment environment) {
		super(rdfIndividual, environment);
		this.storePolicy = LuceneStorePolicy.DISK;
	}

	@Override
	public void readFromRDFIndividual(RepositoryConnection connection)
			throws FusionException {
		super.readFromRDFIndividual(connection);
		
		if(this.indexDirectory!=null) {
			this.storeStrategy = new LuceneEnhancedStoreStrategy(new File(indexDirectory));
			try {
				this.storeStrategy.init();
				Directory directory = this.storeStrategy.getDirectory();
				this.searchStrategy = new LuceneBlockedSearchStrategy(directory);
				
			} catch(FusionException e) {
				
			}
			this.searchStrategy.setThreshold(threshold);
			this.storeStrategy.setPropertyPathDepth(propertyPathDepth);
			this.searchStrategy.setCutOff(cutOff);
		}
	}


	@Override
	protected void readFromPropertyMember(Statement statement)
			throws RepositoryException {
		super.readFromPropertyMember(statement);
		if(statement.getPredicate().toString().equals(FusionMetaVocabulary.PATH)) {
			this.indexDirectory = ((Literal)statement.getObject()).stringValue().trim();
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.REFRESH)) {
			this.refresh = ((Literal)statement.getObject()).booleanValue();
		}
	}


	@Override
	public void prepare() throws FusionException {
		try {
			if(this.refresh&&(this.embeddingDataSource!=null)) {
				this.storeStrategy.prepare(this.embeddingDataSource);
				this.refresh = false;
				abbreviatedNames = ((LuceneEnhancedStoreStrategy)storeStrategy).isAbbreviatedNames();
				deletedClasses = ((LuceneEnhancedStoreStrategy)storeStrategy).isDeletedClasses();
				
				((LuceneAlignedFieldsEnhancedSearchStrategy)searchStrategy).setAbbreviatedNames(abbreviatedNames);
				((LuceneAlignedFieldsEnhancedSearchStrategy)searchStrategy).setDeletedClasses(deletedClasses);
			}
			refreshSearcher();
		} catch(Exception e) {
			throw new FusionException("Could not prepare the intermediate data source", e);
		}
	}

	@Override
	public int copyRelevantSubsetToBlocker(ILuceneBlocker blocker,
			ApplicationContext context,
			Map<String, AttributeProfileInDataset> targetAttributes)
			throws FusionException {
		return searchStrategy.copyRelevantSubsetToBlocker(blocker, context, targetAttributes);
	}

	@Override
	public IDataSource getEmbeddingDataSource() {		
		return embeddingDataSource;
	}

	@Override
	public void setEmbeddingDataSource(IDataSource embeddingDataSource) {
		this.embeddingDataSource = embeddingDataSource;
	}

	@Override
	public void close() throws FusionException {
		
		this.storeStrategy.close();
		
	}
	
	

}
