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
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.fusion.util.FusionException;
import uk.ac.open.kmi.fusion.util.KnoFussUtils;
import uk.ac.open.kmi.fusion.util.OAEIUtils;
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
					
					/*if(((Literal)stmt.getObject()).stringValue().toLowerCase().contains("r2-d2")) {
						f = new Field(stmt.getPredicate().toString(), "r2 d2", Field.Store.YES, Field.Index.ANALYZED);
						doc.add(f);
					}*/
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
						if((stmt.getObject() instanceof BNode)
								||((stmt.getObject() instanceof URI)&&(stmt.getObject().toString().startsWith(OAEIUtils.IIMB_ADDONS_NS)))) {
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
							
						}
					}
				}
			} 
		}
		
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
	
	
	
	

}
