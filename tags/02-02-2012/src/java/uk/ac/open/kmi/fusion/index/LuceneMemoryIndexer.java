package uk.ac.open.kmi.fusion.index;

import java.io.File;

import org.apache.lucene.store.Directory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.index.search.ILuceneSearchStrategy;
import uk.ac.open.kmi.fusion.index.search.LuceneAlignedFieldsSearchStrategy;
import uk.ac.open.kmi.fusion.index.search.LuceneAllFieldsSearchStrategy;
import uk.ac.open.kmi.fusion.index.search.LuceneAllFieldsSearchStrategyFuzzy;
import uk.ac.open.kmi.fusion.index.store.ILuceneStore;
import uk.ac.open.kmi.fusion.index.store.LuceneDiskStoreStrategy;
import uk.ac.open.kmi.fusion.index.store.LuceneMemoryStoreStrategy;
import uk.ac.open.kmi.fusion.util.FusionException;

public class LuceneMemoryIndexer extends LuceneIndexer {

	public static final String TYPE_URI = FusionMetaVocabulary.LUCENE_MEMORY_BLOCKER;
	
	public LuceneMemoryIndexer() {
		super();
	}

	public LuceneMemoryIndexer(LuceneMemoryStoreStrategy storeStrategy,
			ILuceneSearchStrategy searchStrategy) {
		super(storeStrategy, searchStrategy);
	}
	
	

	public LuceneMemoryIndexer(Resource rdfIndividual,
			FusionEnvironment environment) {
		super(rdfIndividual, environment);
		
	}

	@Override
	public void readFromRDFIndividual(RepositoryConnection connection)
			throws FusionException {
		super.readFromRDFIndividual(connection);
		this.storeStrategy = new LuceneMemoryStoreStrategy();
		this.storeStrategy.init();
		Directory directory = this.storeStrategy.getDirectory();
		switch(this.searchPolicy) {
		case ALIGNED_FIELDS:
			this.searchStrategy = new LuceneAlignedFieldsSearchStrategy(directory);
		break;
		case ALL_FIELDS:
			this.searchStrategy = new LuceneAllFieldsSearchStrategy(directory);
			break;
		case FUZZY:
			this.searchStrategy = new LuceneAllFieldsSearchStrategyFuzzy(directory);
			break;
		}
		this.searchStrategy.setThreshold(threshold);
		this.storeStrategy.setPropertyPathDepth(propertyPathDepth);
		this.searchStrategy.setCutOff(cutOff);
	}

	@Override
	protected void readFromPropertyMember(Statement statement)
			throws RepositoryException {
		super.readFromPropertyMember(statement);
	}
	
	

}
