package uk.ac.open.kmi.fusion.index;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.ILuceneBlocker;
import uk.ac.open.kmi.fusion.api.impl.FusionConfigurationObject;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.index.search.ILuceneSearchStrategy;
import uk.ac.open.kmi.fusion.index.store.ILuceneStore;
import uk.ac.open.kmi.fusion.learning.GeneticAlgorithmObjectIdentificationMethod;
import uk.ac.open.kmi.fusion.util.FusionException;

public abstract class LuceneIndexer extends FusionConfigurationObject implements ILuceneBlocker {

	public static final String TYPE_URI = FusionMetaVocabulary.LUCENE_BLOCKER;
			
	protected LuceneSearchPolicy searchPolicy = LuceneSearchPolicy.ALL_FIELDS;
	protected LuceneStorePolicy storePolicy = LuceneStorePolicy.MEMORY;
	
	protected ILuceneSearchStrategy searchStrategy;
	protected ILuceneStore storeStrategy;
	
	protected double threshold = 0.5;
	protected int cutOff = 5;
	
	int propertyPathDepth = 1;
	
	private static Logger log = Logger.getLogger(LuceneIndexer.class);
	
	protected LuceneIndexer() {
		
	}
	
	LuceneIndexer(ILuceneStore storeStrategy, ILuceneSearchStrategy searchStrategy) {
		this.storeStrategy = storeStrategy;
		this.searchStrategy = searchStrategy;
	}
	
	

	public LuceneIndexer(Resource rdfIndividual, FusionEnvironment environment) {
		super(rdfIndividual, environment);
	}

	@Override
	public void readFromRDFIndividual(RepositoryConnection connection)
			throws FusionException {
		super.readFromRDFIndividual(connection);
		
	}

	@Override
	protected void readFromPropertyMember(Statement statement)
			throws RepositoryException {
		super.readFromPropertyMember(statement);
		if(statement.getPredicate().toString().equals(FusionMetaVocabulary.SEARCH_STRATEGY)) {
			String sSearchPolicy = ((Literal)statement.getObject()).stringValue();
			if(sSearchPolicy.equals("aligned")) {
				this.searchPolicy = LuceneSearchPolicy.ALIGNED_FIELDS;
			} else if(sSearchPolicy.equals("all")) {
				this.searchPolicy = LuceneSearchPolicy.ALL_FIELDS;
			} else if(sSearchPolicy.equals("fuzzy")) {
				this.searchPolicy = LuceneSearchPolicy.FUZZY;
			} else if(sSearchPolicy.equals("enhanced")) {
				this.searchPolicy = LuceneSearchPolicy.ENHANCED;
			}
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"depth")) {
			this.propertyPathDepth = Integer.parseInt(((Literal)statement.getObject()).stringValue());
			if(this.propertyPathDepth>2) {
				log.error("Does not support depth greater than 2");
				this.propertyPathDepth = 2;
			}
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.THRESHOLD)) {
			if(statement.getObject() instanceof Literal) {
				this.threshold = ((Literal)statement.getObject()).doubleValue();
			}
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.CUT_OFF)) {
			if(statement.getObject() instanceof Literal) {
				this.cutOff = ((Literal)statement.getObject()).intValue();
			}
		}
	}



	@Override
	public void setCutOff(int cutOff) {
		this.searchStrategy.setCutOff(cutOff);		
	}

	@Override
	public void setThreshold(double threshold) {
		this.searchStrategy.setThreshold(threshold);		
	}

	@Override
	public int getCutOff() {
		return this.searchStrategy.getCutOff();
	}

	@Override
	public double getThreshold() {
		return this.searchStrategy.getThreshold();
	}
	
	@Override
	public double getDocumentFrequency(String field, String term) {
		return this.searchStrategy.getDocumentFrequency(field, term);
	}

	@Override
	public Set<String> findClosest(Map<String, List<String>> fields,
			double threshold) throws IOException {
		return this.searchStrategy.findClosestDocuments(fields, threshold, null).keySet();
	}

	@Override
	public Map<String, Document> findClosestDocuments(
			Map<String, List<String>> fields, double threshold, String type)
			throws IOException {
		return this.searchStrategy.findClosestDocuments(fields, threshold, type);
	}

	@Override
	public Document findByURI(String uri) {
		return this.searchStrategy.findByURI(uri);
	}

	@Override
	public void closeIndex() throws FusionException {
		this.storeStrategy.closeIndexWriter();		
	}

	@Override
	public void openIndex() throws FusionException {
		this.storeStrategy.openIndexWriter();
	}

	@Override
	public void createIndex() throws FusionException {
		this.storeStrategy.openIndexWriter();
	}

	@Override
	public void refreshSearcher() throws FusionException {
		this.searchStrategy.refreshSearcher();		
	}

	
	@Override
	public void clearIndex() throws FusionException {
		this.storeStrategy.clearIndex();		
	}

	@Override
	public void addDocument(Document doc, String type)
			throws CorruptIndexException, IOException {
		this.storeStrategy.addDocument(doc, type);
	}

	@Override
	public void commit() throws FusionException {
		this.storeStrategy.commit();		
	}

	@Override
	public Map<String, Document> findClosestDocuments(
			Map<String, List<String>> searchFieldValues, String[] searchFields,
			double threshold, String type) throws IOException {
		return this.searchStrategy.findClosestDocuments(searchFieldValues, searchFields, threshold, type);
	}

	@Override
	public ILuceneSearchStrategy getSearchStrategy() {
		return searchStrategy;
	}

	@Override
	public ILuceneStore getStoreStrategy() {
		return storeStrategy;
	}

	

}
