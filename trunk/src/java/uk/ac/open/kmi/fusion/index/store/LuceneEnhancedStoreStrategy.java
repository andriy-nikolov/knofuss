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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.common.utils.LuceneUtils;
import uk.ac.open.kmi.fusion.api.IDataSource;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.index.LuceneIndexer;
import uk.ac.open.kmi.fusion.util.FusionException;
import uk.ac.open.kmi.fusion.util.OAEIUtils;
import uk.ac.open.kmi.fusion.util.SesameUtils;

public class LuceneEnhancedStoreStrategy implements
		ILuceneStore {
	
	File indexDirFile;
	
	Directory directory = null;
	protected IndexWriter indexWriter = null;
	
	boolean writerOpen = false;
	
	int propertyPathDepth = 1;
	
	boolean abbreviatedNames = false;
	boolean deletedClasses = false;
	
	int abbreviations;
	int classDeletions;
	
	private static Logger log = Logger.getLogger(LuceneEnhancedStoreStrategy.class);
	
	public LuceneEnhancedStoreStrategy(File indexDirFile) {
		super();
		this.indexDirFile = indexDirFile;
	}
	
	protected Document indexIndividual(URI ind, RepositoryConnection con) throws RepositoryException {
		Document doc = new Document();
		
		doc.add(new Field(LuceneIndexer.ID_FIELD_NAME, ind.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		
		addTypesList(ind, con, doc);
		return doc;
	}
	
	private void addTypesList(URI ind, RepositoryConnection con, Document doc) throws RepositoryException {
		
		boolean hasSpecificTypes = false;
		
		Set<URI> types = new HashSet<URI>();
		for(URI concept : SesameUtils.getNamedTypes(ind, con)) {
				if(concept.toString().startsWith(OAEIUtils.IIMB_TBOX_NS)) {
					hasSpecificTypes = true;
				}
				types.add(concept);
				doc.add(new Field(RDF.TYPE.toString(), concept.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
				
				for(URI supertype : SesameUtils.getNamedSuperconcepts(concept, con)) {
					types.add(supertype);
					doc.add(new Field(RDF.TYPE.toString(), supertype.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
				}
			
		}
		
		for(URI concept : types) {
			doc.add(new Field(RDF.TYPE.toString(), concept.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
			// log.info(concept.toString());
		}
		
		if(!hasSpecificTypes) {
			classDeletions++;
		}
		
	}

	@Override
	public void addDocument(Document doc, String type) throws CorruptIndexException, IOException {
		if(this.indexWriter==null) {
			throw new IllegalStateException("Index writer not open!");
		}
		
		Document internalDoc = new Document();
		
		List<Fieldable> fields = doc.getFields();
		
		for(Fieldable field : fields) {
			if((field.name().equals(LuceneIndexer.ID_FIELD_NAME))||(field.name().equals(RDF.TYPE.toString()))) {
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
	public Directory getDirectory() {
		if(this.directory==null) {
			throw new IllegalStateException("Index directory is not initialised");
		}
		return directory;
		
	}

	@Override
	public void openIndexWriter() throws FusionException {
		if(!this.writerOpen) {
			try {
				try {
					this.indexWriter = new IndexWriter(getDirectory(), new StandardAnalyzer(Version.LUCENE_30), false, IndexWriter.MaxFieldLength.UNLIMITED);
				} catch(FileNotFoundException e1) {
					log.error(e1.getMessage(), e1);
					log.error("Trying to fix the index writer error");
					this.indexWriter = new IndexWriter(getDirectory(), new StandardAnalyzer(Version.LUCENE_30), true, IndexWriter.MaxFieldLength.UNLIMITED);
				}
				this.writerOpen = true;
			} catch(Exception e) {
				throw new FusionException("Could not open a Lucene indexer: ", e);
			}
		}
	}

	@Override
	public void clearIndex() throws FusionException {
		try {
			this.indexWriter.deleteAll();
			this.indexWriter.commit();
		} catch(Exception e) {
			throw new FusionException("Could not clear a Lucene indexer: ", e);
		}
	}



	@Override
	public void addIndividual(URI ind, RepositoryConnection con) throws RepositoryException {
		
		Document doc = indexIndividual(ind, con);
		
		List<Statement> stmts = SesameUtils.getStatements(ind, null, null, con);
		Field f;
		
		String value;
		
		for(Statement stmt : stmts) {
			if(!stmt.getPredicate().equals(RDF.TYPE)) {
				if(stmt.getObject() instanceof Literal) {
					value = ((Literal)stmt.getObject()).stringValue();
					if(value.contains("Kristin Minter")) {
						log.debug("Kristin Minter");
					}
					f = new Field(stmt.getPredicate().toString(), value, Field.Store.YES, Field.Index.ANALYZED);
					doc.add(f);
					/*if(((Literal)stmt.getObject()).stringValue().toLowerCase().contains("r2-d2")) {
						f = new Field(stmt.getPredicate().toString(), "r2 d2", Field.Store.YES, Field.Index.ANALYZED);
						doc.add(f);
					}*/
					// log.debug("Indexed field: "+stmt.getPredicate().toString()+" = "+((Literal)stmt.getObject()).stringValue());
					// skos patch - very clumsy, to be fixed
					if(stmt.getPredicate().toString().equals("http://www.w3.org/2004/02/skos/core#altLabel")||
							stmt.getPredicate().toString().equals("http://www.w3.org/2004/02/skos/core#prefLabel")||
							stmt.getPredicate().toString().equals("http://www.w3.org/2004/02/skos/core#hiddenLabel")) {
						f = new Field(RDFS.LABEL.toString(), value, Field.Store.YES, Field.Index.ANALYZED);
						doc.add(f);
					}
					if(stmt.getPredicate().toString().equals(OAEIUtils.IIMB_TBOX_NS+"name")) {
						if(LuceneUtils.isAbbreviation(value)) {
							this.abbreviations++;
						}
						f = new Field(stmt.getPredicate().toString(), LuceneUtils.getAbbreviation(value), Field.Store.YES, Field.Index.NOT_ANALYZED);
						doc.add(f);
					}
					
				} else {
					if(propertyPathDepth>1) {
						if((stmt.getObject() instanceof BNode)
								||((stmt.getObject() instanceof URI)&&(stmt.getObject().toString().startsWith(OAEIUtils.IIMB_ADDONS_NS)))) {
							String pathPrefix = "<"+stmt.getPredicate().toString()+">";
							String path;
							List<Statement> stmts2 = SesameUtils.getStatements((Resource)stmt.getObject(), null, null, con);
							for(Statement stmt2 : stmts2) {
								if(!stmt2.getPredicate().equals(RDF.TYPE)) {
									if(stmt2.getObject() instanceof Literal) {
										path = pathPrefix + "/<" +stmt2.getPredicate()+">";
										value = ((Literal)stmt2.getObject()).stringValue();
										
										doc.add(new Field(path, value, Field.Store.YES, Field.Index.ANALYZED));
										if(stmt.getPredicate().toString().startsWith(OAEIUtils.IIMB_ADDONS_NS)) {
											String propertyUri = OAEIUtils.IIMB_TBOX_NS+stmt.getPredicate().toString().substring(OAEIUtils.IIMB_ADDONS_NS.length());
											doc.add(new Field(propertyUri, value, Field.Store.YES, Field.Index.ANALYZED));
										}
										
										if(stmt2.getPredicate().toString().equals("http://www.w3.org/2004/02/skos/core#altLabel")||
												stmt2.getPredicate().toString().equals("http://www.w3.org/2004/02/skos/core#prefLabel")||
												stmt2.getPredicate().toString().equals("http://www.w3.org/2004/02/skos/core#hiddenLabel")) {
											path = pathPrefix + "/<" +RDFS.LABEL.toString()+">";
											doc.add(new Field(path, value, Field.Store.YES, Field.Index.ANALYZED));
										}
										
										if(stmt.getPredicate().toString().equals(OAEIUtils.IIMB_TBOX_NS+"name")) {
											if(LuceneUtils.isAbbreviation(value)) {
												this.abbreviations++;
											}
											f = new Field(stmt.getPredicate().toString(), LuceneUtils.getAbbreviation(value), Field.Store.YES, Field.Index.NOT_ANALYZED);
											doc.add(f);
										}
									}
								}
							}
							
						}
					}
				}
			} 
		}
		
		try {
			
			if(ind.toString().endsWith("item1247538242153446734")) {
				log.debug("item1247538242153446734");
			}
			
			this.indexWriter.addDocument(doc);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void closeIndexWriter() throws FusionException
	{
		if(this.writerOpen) {
			try {
		
				if(this.indexWriter!=null) {
					this.indexWriter.optimize();
					this.indexWriter.close();
				}
				this.writerOpen = false;
			} catch(IOException e) {
				throw new FusionException("Could not close a Lucene index writer: ", e);
			}
		}
	}

	@Override
	public void commit() throws FusionException {
		try {
			this.indexWriter.commit();
		} catch(IOException e) {
			throw new FusionException("Could not commit a writing operation to a Lucene index writer: ", e);
		}

	}

	@Override
	public int getPropertyPathDepth() {
		return propertyPathDepth;
	}

	@Override
	public void setPropertyPathDepth(int pathDepth) {
		this.propertyPathDepth = pathDepth;
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
			log.info("Indexing started");
			Set<Resource> res = SesameUtils.findAllIndividuals(embeddingDataSource.getConnection());
			abbreviations = 0;
			classDeletions = 0;
			for(Resource ind : res) {
				//log.info((i++)+" out of "+res.size());
				if(ind instanceof URI) {
					addIndividual((URI)ind, embeddingDataSource.getConnection());
				}
				
			}
			closeIndexWriter();
			
			double abbreviationPercent = ((double)abbreviations)/res.size();
			
			if(abbreviationPercent>=0.05) {
				this.abbreviatedNames = true;
			}
			
			double classDeletionPercent = ((double)classDeletions)/res.size();
			
			if(classDeletionPercent>=0.05) {
				this.deletedClasses = true;
			}
			
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

	public boolean isAbbreviatedNames() {
		return abbreviatedNames;
	}

	public boolean isDeletedClasses() {
		return deletedClasses;
	}

	@Override
	public void indexStatements(List<Statement> statements, ApplicationContext context, String type)
			throws FusionException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("indexStatements not implemented yet");
		
	}

	@Override
	public void indexBindingSets(List<BindingSet> bindingSets, ApplicationContext context, String type)
			throws FusionException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("indexStatements not implemented yet");
	}
}
