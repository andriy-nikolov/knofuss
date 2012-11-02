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
