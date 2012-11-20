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

import java.io.FileNotFoundException;
import java.io.IOException;
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
import org.apache.lucene.util.Version;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.fusion.util.FusionException;
import uk.ac.open.kmi.fusion.util.KnoFussUtils;
import uk.ac.open.kmi.fusion.util.SesameUtils;

public abstract class AbstractLuceneStore implements
		ILuceneStore {
	
	Directory directory = null;
	protected IndexWriter indexWriter = null;
	
	boolean writerOpen = false;
	
	int propertyPathDepth = 1;
	
	private static Logger log = Logger.getLogger(AbstractLuceneStore.class);
	
	protected AbstractLuceneStore() {
		
	}
	
	protected Document indexIndividual(URI ind, RepositoryConnection con) throws RepositoryException {
		Document doc = new Document();
		
		doc.add(new Field("uri", ind.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		
		addTypesList(ind, con, doc);
		return doc;
	}
	
	private void addTypesList(URI ind, RepositoryConnection con, Document doc) throws RepositoryException {
		
		for(URI concept : SesameUtils.getNamedTypes(ind, con)) {
				
				doc.add(new Field(RDF.TYPE.toString(), concept.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
				
				for(URI supertype : SesameUtils.getNamedSuperconcepts(concept, con)) {
					
					doc.add(new Field(RDF.TYPE.toString(), supertype.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
				
				}
			
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
		
		Set<String> alternativeValues;
		
		for(int i=1;i<propertyPathDepth+1;i++) {
			String sparqlQuery = getAttributeSelectSPARQLQuery(ind.toString(), i);
			try {
				TupleQueryResult res = SesameUtils.executeSelect(sparqlQuery, con);
				BindingSet bs;
				String currentProperty, path;
				Literal lit;
				try {
					while(res.hasNext()) {
						bs = res.next();
						if(bs.getValue("obj") instanceof Literal) {
							lit = (Literal)bs.getValue("obj");
							
							if(i>1) {
								StringBuilder pathBuilder = new StringBuilder("<"+bs.getValue("prop0").toString()+">");
								for(int j=1;j<i;j++) {
									pathBuilder.append("/");
									pathBuilder.append("<");
									currentProperty = bs.getValue("prop"+j).toString();
									
									pathBuilder.append(currentProperty);
									pathBuilder.append(">");
								}
								path = pathBuilder.toString();
							} else {
								path = bs.getValue("prop0").toString();
							}
							
							doc.add(new Field(path, lit.stringValue(), Field.Store.YES, Field.Index.ANALYZED));
							
							addAlternatives(path, lit.stringValue(), doc);
							
							alternativeValues = KnoFussUtils.getAlternativeStringValues(lit.stringValue());
							for(String val : alternativeValues) {
								doc.add(new Field(path, val, Field.Store.YES, Field.Index.ANALYZED));
								
								addAlternatives(path, val, doc);
							}
							
							
						}
						
					}
				} finally {
					res.close();
				}
				
				
			} catch (OpenRDFException e) {
				e.printStackTrace();
			}
			
		}
		
		
		/*List<Statement> stmts = SesameUtils.getStatements(ind, null, null, con);
		URI obj;
		Field f;
		
		String value, additionalValue;
		
		for(Statement stmt : stmts) {
			if(!stmt.getPredicate().equals(RDF.TYPE)) {
				if(stmt.getObject() instanceof Literal) {
					value = KnoFussUtils.removeDiacriticalMarks(((Literal)stmt.getObject()).stringValue());
					
					f = new Field(stmt.getPredicate().toString(), value, Field.Store.YES, Field.Index.ANALYZED);
					
					doc.add(f);
					
					if(value.contains("'")) {
						additionalValue = value.replace("'", "");
						f = new Field(stmt.getPredicate().toString(), additionalValue, Field.Store.YES, Field.Index.ANALYZED);
					}
					
					if(((Literal)stmt.getObject()).stringValue().toLowerCase().contains("r2-d2")) {
						f = new Field(stmt.getPredicate().toString(), "r2 d2", Field.Store.YES, Field.Index.ANALYZED);
						doc.add(f);
					}
					// log.debug("Indexed field: "+stmt.getPredicate().toString()+" = "+((Literal)stmt.getObject()).stringValue());
					// skos patch - very clumsy, to be fixed
					
					if(stmt.getPredicate().toString().equals("http://www.w3.org/2004/02/skos/core#altLabel")||
							stmt.getPredicate().toString().equals("http://www.w3.org/2004/02/skos/core#prefLabel")||
							stmt.getPredicate().toString().equals("http://www.w3.org/2004/02/skos/core#hiddenLabel")||
							stmt.getPredicate().toString().equals(Utils.FOAF_NS+"name")||
							stmt.getPredicate().toString().equals("http://www.geonames.org/ontology#name")||
							stmt.getPredicate().toString().equals("http://www.geonames.org/ontology#alternateName")) {
						f = new Field(RDFS.LABEL.toString(), value, Field.Store.YES, Field.Index.ANALYZED);
						doc.add(f);
					}
				} else {
					if(propertyPathDepth>1) {
						
						for(int i=1;i<propertyPathDepth;i++) {
							String sparqlQuery = getAttributeSelectSPARQLQuery(ind.toString(), i);
							try {
								TupleQueryResult res = SesameUtils.executeSelect(sparqlQuery, con);
								BindingSet bs;
								String currentProperty, path;
								Literal lit;
								try {
									while(res.hasNext()) {
										bs = res.next();
										if(bs.getValue("obj") instanceof Literal) {
											lit = (Literal)bs.getValue("obj");
											path = "<"+bs.getValue("prop0").toString()+">";
											for(int j=0;j<i;j++) {
												path+="/";
												path+="<";
												currentProperty = bs.getValue("prop"+(j+1)).toString();
												path+=currentProperty;
												path+=">";
											}
											
											doc.add(new Field(path, lit.stringValue(), Field.Store.YES, Field.Index.ANALYZED));
											
											addAlternatives(path, lit, doc);
											
										}
										
									}
								} finally {
									res.close();
								}
								
								
							} catch (OpenRDFException e) {
								e.printStackTrace();
							}
							
						}*/
						
						/*if((stmt.getObject() instanceof Resource)) {
							String pathPrefix = "<"+stmt.getPredicate().toString()+">";
							String path;
							List<Statement> stmts2 = SesameUtils.getStatements((Resource)stmt.getObject(), null, null, con);
							for(Statement stmt2 : stmts2) {
								if(!stmt2.getPredicate().equals(RDF.TYPE)) {
									if(stmt2.getObject() instanceof Literal) {
										path = pathPrefix + "/<" +stmt2.getPredicate()+">";
										doc.add(new Field(path, ((Literal)stmt2.getObject()).stringValue(), Field.Store.YES, Field.Index.ANALYZED));
										
										if(stmt2.getPredicate().toString().equals("http://www.w3.org/2004/02/skos/core#altLabel")||
												stmt2.getPredicate().toString().equals("http://www.w3.org/2004/02/skos/core#prefLabel")||
												stmt2.getPredicate().toString().equals("http://www.w3.org/2004/02/skos/core#hiddenLabel")) {
											path = pathPrefix + "/<" +RDFS.LABEL.toString()+">";
											doc.add(new Field(path, ((Literal)stmt2.getObject()).stringValue(), Field.Store.YES, Field.Index.ANALYZED));
										}
									}
								}
							}
							
						}*/
		//			}
		//		}
		//	} 
		//}
		
		try {
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

	public IndexWriter getIndexWriter() {
		return indexWriter;
	}
	
	
	private String getAttributeSelectSPARQLQuery(String uri, int depth) {
		if((depth<1)||(depth>3)) throw new IllegalArgumentException("Property path depth should be between 1 and 3, is "+depth);
		
		StringBuffer queryBuffer = new StringBuffer("SELECT ");
		for(int i=0;i<depth;i++) {
			queryBuffer.append("?prop"+i);
			queryBuffer.append(" ");
		}
		
		queryBuffer.append("?obj WHERE { \n");
		queryBuffer.append("<");
		queryBuffer.append(uri);
		queryBuffer.append("> ?prop0 ");
		for(int i=1;i<depth;i++) {
			queryBuffer.append("?tmp"+(i-1));
			queryBuffer.append(" . \n ");
			queryBuffer.append("?tmp"+(i-1));
			queryBuffer.append(" ?prop"+i);
			queryBuffer.append(" ");
		}
		queryBuffer.append("?obj . \n");
	
		queryBuffer.append("}");
		
		return queryBuffer.toString();
		
		
	}
	
	private void addAlternatives(String path, String val, Document doc) {
		if(path.contains("http://www.w3.org/2004/02/skos/core#altLabel")) {
			doc.add(new Field(path.replace("http://www.w3.org/2004/02/skos/core#altLabel", RDFS.LABEL.toString()), val, Field.Store.YES, Field.Index.ANALYZED));
		} else
		if(path.contains("http://www.w3.org/2004/02/skos/core#prefLabel")) {
			doc.add(new Field(path.replace("http://www.w3.org/2004/02/skos/core#prefLabel", RDFS.LABEL.toString()), val, Field.Store.YES, Field.Index.ANALYZED));
		} else
		if(path.contains("http://www.w3.org/2004/02/skos/core#hiddenLabel")) {
			doc.add(new Field(path.replace("http://www.w3.org/2004/02/skos/core#hiddenLabel", RDFS.LABEL.toString()), val, Field.Store.YES, Field.Index.ANALYZED));
		} else
		if(path.contains("http://www.w3.org/2004/02/skos/core#hiddenLabel")) {
			doc.add(new Field(path.replace("http://www.w3.org/2004/02/skos/core#hiddenLabel", RDFS.LABEL.toString()), val, Field.Store.YES, Field.Index.ANALYZED));
		} else if(path.contains(Utils.FOAF_NS+"name")) {
			doc.add(new Field(path.replace("http://www.w3.org/2004/02/skos/core#hiddenLabel", RDFS.LABEL.toString()), val, Field.Store.YES, Field.Index.ANALYZED));
		} else if(path.contains(Utils.FOAF_NS+"name")) {
			doc.add(new Field(path.replace(Utils.FOAF_NS+"name", RDFS.LABEL.toString()), val, Field.Store.YES, Field.Index.ANALYZED));
		} else if(path.contains("http://www.geonames.org/ontology#name")) {
			doc.add(new Field(path.replace("http://www.geonames.org/ontology#name", RDFS.LABEL.toString()), val, Field.Store.YES, Field.Index.ANALYZED));
		} else if(path.contains("http://www.geonames.org/ontology#alternateName")) {
			doc.add(new Field(path.replace("http://www.geonames.org/ontology#alternateName", RDFS.LABEL.toString()), val, Field.Store.YES, Field.Index.ANALYZED));
		}				
	}
	

}
