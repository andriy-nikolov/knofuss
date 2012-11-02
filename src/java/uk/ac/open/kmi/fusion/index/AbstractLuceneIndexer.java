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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.IDataSource;
import uk.ac.open.kmi.fusion.api.ILuceneBlocker;
import uk.ac.open.kmi.fusion.api.impl.FusionConfigurationObject;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.util.FusionException;
import uk.ac.open.kmi.fusion.util.SesameUtils;

public abstract class AbstractLuceneIndexer extends FusionConfigurationObject implements ILuceneBlocker {

	private int cutOff = 300;
	private double threshold = 0.5;
	
	IndexWriter indexWriter=null;
	IndexSearcher indexSearcher = null;
	IndexReader indexReader = null;
	
	Directory directory = null;
	
	private Logger log = Logger.getLogger(AbstractLuceneIndexer.class);
	
	static char[] special = {':', '*', '+', '-', '[', ']', '\"', '\\', '?', '(', ')', '{', '}', '!'};
	
	protected AbstractLuceneIndexer() {
		// TODO Auto-generated constructor stub
	}
	
	protected AbstractLuceneIndexer(Resource rdfIndividual,
			FusionEnvironment environment) {
		super(rdfIndividual, environment);
		// TODO Auto-generated constructor stub
	}

	/*@Override
	public IndexWriter getIndexWriter() {
		return indexWriter;
	}

	@Override
	public IndexSearcher getIndexSearcher() {
		return indexSearcher;
	}*/

	/*public IndexReader getIndexReader() {
		return indexReader;
	}

	public Directory getDirectory() {
		return directory;
	}*/

	@Override
	protected void readFromPropertyMember(Statement statement)
			throws RepositoryException {
		// TODO Auto-generated method stub
		super.readFromPropertyMember(statement);
		if(statement.getPredicate().toString().equals(FusionMetaVocabulary.THRESHOLD)) {
			this.threshold = ((Literal)statement.getObject()).doubleValue();
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.CUT_OFF)) {
			this.cutOff = ((Literal)statement.getObject()).intValue();
		}
	}

	@Override
	public Set<String> findClosest(Map<String, List<String>> fields, double threshold) throws IOException {
		
		return findClosestDocuments(fields, threshold, null).keySet();
	}
	
	@Override
	public Document findByURI(String uri) {
	    try {
		    //Analyzer analyzer = new StandardAnalyzer();
		    Term term = new Term("uri", uri);
	    	Query query = new TermQuery(term);
	    	
	    	TopDocs topDocs = indexSearcher.search(query, 1);
	    	
	    	//if(Math.round(topDocs.scoreDocs[0].score)==1) {
	    	if(topDocs.totalHits>0) {
		    	Document doc = indexSearcher.doc(topDocs.scoreDocs[0].doc);
		    	if(doc.get("uri").equals(uri)) {
		    		return indexSearcher.doc(topDocs.scoreDocs[0].doc);
		    	}
	    	}
	    	
	    } catch(Exception e) {
	    	//FusionGlobals.log.error("Could not open lucene index");
	    	e.printStackTrace();
	    } 
	    return null;
	}

	@Override
	public void clearIndex() throws FusionException {
		try {
			this.indexWriter.deleteAll();
			this.indexWriter.commit();
		} catch(IOException e) {
			throw new FusionException("Could not clear a Lucene indexer: ", e);
		}
	}

	@Override
	public void closeIndex() throws FusionException
	{
		try {
			if(this.indexWriter!=null) {
				this.indexWriter.optimize();
				this.indexWriter.close();
			}
			if(this.directory!=null) {
				this.directory.close();
			}
		} catch(IOException e) {
			throw new FusionException("Could not close a Lucene index writer: ", e);
		}
	}
		
	public static String getTransduced(String val) {
		String res = val;
		
		while(res.indexOf(':')!=-1) {
			res = res.substring(0, res.indexOf(':'))+
			res.substring(res.indexOf(':')+1);
		}
		return res;
	}
	
	public static String getTransducedQuery(String val) {
		String res = val;
		for(int i=0;i<special.length;i++) {
			while(res.indexOf(special[i])!=-1) {
				res = res.substring(0, res.indexOf(special[i]))+" "+
					res.substring(res.indexOf(special[i])+1);
			}
		}
		  
		return res;
	}

	protected static String getConcatenatedString(List<String> strs) {
		StringBuffer buffer = new StringBuffer();
		for(String str : strs) {
			buffer.append(str);
			buffer.append(' ');
		}
		return buffer.toString().trim();
	}
	
	
	public void reloadDataSource(IDataSource dataSource) throws FusionException {
		try {
			openIndex();
			clearIndex();
			log.info("Indexing started");
			Set<Resource> res = SesameUtils.findAllIndividuals(dataSource.getConnection());
			int i = 0;
			for(Resource ind : res) {
				log.info((i++)+" out of "+res.size());
				if(ind instanceof URI) {
					addIndividual((URI)ind, dataSource.getConnection());
				}
				
			}
			closeIndex();
			refreshSearcher();
			log.info("Indexing finished");
		} catch(Exception e) {
			throw new FusionException("Could not index data from a SPARQL data source");
		}
	}
	
	public abstract void addIndividual(URI ind, RepositoryConnection con) throws RepositoryException;

	protected Document indexIndividual(URI ind, RepositoryConnection con)
			throws RepositoryException {
				Document doc = new Document();
			
				doc.add(new Field("uri", ind.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
				
				addTypesList(ind, con, doc);
				return doc;
			}

	public void updateIndividual(URI ind, RepositoryConnection con)
			throws FusionException, RepositoryException {
				if(ind.toString()==null) {
					return;
				}
				Term term = new Term("uri", ind.toString());
				try {
					this.indexWriter.deleteDocuments(term);
					addIndividual(ind, con);
				} catch(IOException e) {
					String message = "Could not update lucene index for the individual "+ind.toString();
					log.error(message);
					throw new FusionException(message, e);
				}
			}

	private void addTypesList(URI ind, RepositoryConnection con, Document doc)
			throws RepositoryException {
				String conceptLine = "";
				
				for(URI concept : SesameUtils.getNamedTypes(ind, con)) {
						
						doc.add(new Field(RDF.TYPE.toString(), concept.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
						
						for(URI supertype : SesameUtils.getNamedSuperconcepts(concept, con)) {
							
							doc.add(new Field(RDF.TYPE.toString(), supertype.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
						
						}
					
				}
			}

	@Override
	public void addDocument(Document doc, String type) throws CorruptIndexException, IOException {
		
		Document internalDoc = new Document();
		
		List<Fieldable> fields = doc.getFields();
		
		Field f;
		String val;
		Set<String> tokens;
		for(Fieldable field : fields) {
			if((field.name().equals("uri"))||(field.name().equals(RDF.TYPE.toString()))) {
				internalDoc.add(new Field(field.name(), field.stringValue(), Field.Store.YES, Field.Index.NOT_ANALYZED));
			} else {
				internalDoc.add(new Field(field.name(), field.stringValue(), Field.Store.YES, Field.Index.ANALYZED));
			}
			internalDoc.add(field);
		}
		internalDoc.add(new Field(RDF.TYPE.toString(), type, Field.Store.YES, Field.Index.NOT_ANALYZED));
		this.indexWriter.addDocument(internalDoc);
		
	}

	
	@Override
	public int getCutOff() {
		return cutOff;
	}

	@Override
	public void setCutOff(int cutOff) {
		this.cutOff = cutOff;
	}

	@Override
	public double getThreshold() {
		return threshold;
	}

	@Override
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	@Override
	public void commit() throws IOException {
		this.indexWriter.commit();
		
	}
	
	
	
}
