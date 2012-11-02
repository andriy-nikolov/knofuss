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

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.store.Directory;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

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
import uk.ac.open.kmi.fusion.index.store.ILuceneStore;
import uk.ac.open.kmi.fusion.index.store.LuceneDiskStoreStrategy;
import uk.ac.open.kmi.fusion.util.FusionException;

public class LuceneDiskIndexer extends LuceneIndexer implements IPersistentStore {

	public static final String TYPE_URI = FusionMetaVocabulary.LUCENE_DISK_BLOCKER;
	
	private static Logger log = Logger.getLogger(LuceneDiskIndexer.class);
	
	String indexDirectory = null;
	boolean refresh = false;
	IDataSource embeddingDataSource;
	
	protected LuceneDiskIndexer() {
		this.storePolicy = LuceneStorePolicy.DISK;
	}
	
	public LuceneDiskIndexer(ILuceneStore storeStrategy,
			ILuceneSearchStrategy searchStrategy) {
		super(storeStrategy, searchStrategy);
	}
	
	public LuceneDiskIndexer(Resource rdfIndividual,
			FusionEnvironment environment) {
		super(rdfIndividual, environment);
		this.storePolicy = LuceneStorePolicy.DISK;
	}

	@Override
	public void readFromRDFIndividual(RepositoryConnection connection)
			throws FusionException {
		super.readFromRDFIndividual(connection);
		
		if(this.indexDirectory!=null) {
			this.storeStrategy = new LuceneDiskStoreStrategy(new File(indexDirectory));
			try {
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
					((LuceneAllFieldsSearchStrategyFuzzy)this.searchStrategy).setFuzzyThreshold(fuzzyThreshold);
					break;
				case ENHANCED:
					this.searchStrategy = new LuceneAlignedFieldsEnhancedSearchStrategy(directory);
					break;
				}
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
