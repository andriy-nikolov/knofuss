/* Copyright (c) 2012, Knowledge Media Institute
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   * Neither the name of the <organization> nor the
 *     names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.open.kmi.fusion.index;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.store.Directory;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.ILuceneBlocker;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.FusionConfigurationObject;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.index.search.ILuceneSearchStrategy;
import uk.ac.open.kmi.fusion.index.search.LuceneAlignedFieldsEnhancedSearchStrategy;
import uk.ac.open.kmi.fusion.index.search.LuceneAlignedFieldsSearchStrategy;
import uk.ac.open.kmi.fusion.index.search.LuceneAllFieldsSearchStrategy;
import uk.ac.open.kmi.fusion.index.search.LuceneAllFieldsSearchStrategyFuzzy;
import uk.ac.open.kmi.fusion.index.store.ILuceneStore;
import uk.ac.open.kmi.fusion.util.FusionException;

public abstract class LuceneIndexer extends FusionConfigurationObject implements ILuceneBlocker {

	public static final String TYPE_URI = FusionMetaVocabulary.LUCENE_BLOCKER;
			
	protected LuceneSearchPolicy searchPolicy = LuceneSearchPolicy.ALL_FIELDS;
	protected LuceneStorePolicy storePolicy = LuceneStorePolicy.MEMORY;
	
	protected ILuceneSearchStrategy searchStrategy;
	protected ILuceneStore storeStrategy;
	
	protected double threshold = 0.5;
	protected double fuzzyThreshold = 0.6;
	protected int cutOff = 5;
	
	public static String ID_FIELD_NAME = "uri";
	
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
		
		initStoreStrategy();
		this.storeStrategy.setPropertyPathDepth(propertyPathDepth);
		
		initSearchStrategy();
		this.searchStrategy.setThreshold(threshold);
		this.searchStrategy.setCutOff(cutOff);
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
			if(this.propertyPathDepth>3) {
				log.error("Does not support depth greater than 3");
				this.propertyPathDepth = 3;
			}
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.THRESHOLD)) {
			if(statement.getObject() instanceof Literal) {
				this.threshold = ((Literal)statement.getObject()).doubleValue();
			}
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.CUT_OFF)) {
			if(statement.getObject() instanceof Literal) {
				this.cutOff = ((Literal)statement.getObject()).intValue();
			}
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.FUZZY_THRESHOLD)) {
			if(statement.getObject() instanceof Literal) {
				this.fuzzyThreshold = ((Literal)statement.getObject()).doubleValue();
			}
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.ID_FIELD_NAME)) {
			if(statement.getObject() instanceof Literal) {
				ID_FIELD_NAME = ((Literal)statement.getObject()).stringValue();
			}
		}
	}

	
	protected abstract ILuceneStore initStoreStrategy() throws FusionException;
	
	protected ILuceneSearchStrategy initSearchStrategy() throws FusionException {
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
			((LuceneAllFieldsSearchStrategyFuzzy)this.searchStrategy).setFuzzyThreshold(fuzzyThreshold);
			break;
		case ENHANCED:
			this.searchStrategy = new LuceneAlignedFieldsEnhancedSearchStrategy(directory);
			break;
		case BIGRAM:
			break;
		default:
			throw new FusionException("Cannot determine the search policy: " + this.searchPolicy);
		}
		
		return this.searchStrategy; 
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
	public synchronized void indexBindingSets(List<BindingSet> bindingSets, ApplicationContext context, String type) throws FusionException {
		storeStrategy.indexBindingSets(bindingSets, context, type);
		searchStrategy.refreshSearcher();				
	}

	@Override
	public synchronized void indexStatements(List<Statement> statements, ApplicationContext context, String type) throws FusionException {
		storeStrategy.indexStatements(statements, context, type);
		searchStrategy.refreshSearcher();				
	}
	
	public static Document getDocumentByURI(IndexReader reader, URI uri) throws FusionException {
		if(reader == null)
			throw new IllegalArgumentException("IndexReader is null");
		
		if(uri == null)
			throw new IllegalArgumentException("URI to find is null");
		
		return getDocumentByURI(reader, uri.stringValue());
	}
	
	public static Document getDocumentByURI(IndexReader reader, String uri) throws FusionException {
		if(reader == null)
			throw new IllegalArgumentException("IndexReader is null");
		
		if(uri == null)
			throw new IllegalArgumentException("URI to find is null");
		
		Term term = new Term(ID_FIELD_NAME, uri);
		
		Document doc = null;
		try {
			TermDocs docs = reader.termDocs(term);
			try {
				if(docs.next()) {
					int docNr = docs.doc();
					doc = reader.document(docNr);
					if(docs.next())
						throw new FusionException("More than one document for uri: "+uri);
				}
			} finally {
				docs.close();
			}
			
		} catch(IOException e) {
			throw new FusionException(e);
		}
		
		return doc;
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
