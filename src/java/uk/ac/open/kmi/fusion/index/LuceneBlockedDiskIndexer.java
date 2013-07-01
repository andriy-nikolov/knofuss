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

import org.apache.log4j.Logger;
import org.apache.lucene.store.Directory;
import org.openrdf.model.Resource;

import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.IPersistentStore;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.index.search.ILuceneSearchStrategy;
import uk.ac.open.kmi.fusion.index.search.LuceneBlockedSearchStrategy;
import uk.ac.open.kmi.fusion.index.store.ILuceneStore;
import uk.ac.open.kmi.fusion.util.FusionException;

public class LuceneBlockedDiskIndexer extends LuceneEnhancedDiskIndexer implements IPersistentStore {

	public static final String TYPE_URI = FusionMetaVocabulary.LUCENE_BLOCKED_DISK_BLOCKER;
	
	private static Logger log = Logger.getLogger(LuceneBlockedDiskIndexer.class);
	
	boolean refresh = false;
	
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
	protected ILuceneSearchStrategy initSearchStrategy() throws FusionException {
		Directory directory = this.storeStrategy.getDirectory();
		this.searchStrategy = new LuceneBlockedSearchStrategy(directory);
		return this.searchStrategy;
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

}
