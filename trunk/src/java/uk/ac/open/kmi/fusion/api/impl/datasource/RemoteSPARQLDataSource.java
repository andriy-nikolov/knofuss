package uk.ac.open.kmi.fusion.api.impl.datasource;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.manager.RepositoryManager;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;

import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.common.utils.sesame.SesameUtils;
import uk.ac.open.kmi.common.utils.sesame.http.SesameRemoteConnectionProfile;
import uk.ac.open.kmi.common.utils.sesame.owlim.SesameConnectionProfile;
import uk.ac.open.kmi.common.utils.sparql.MySPARQLParser;
import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IDataSource;
import uk.ac.open.kmi.fusion.api.IDump;
import uk.ac.open.kmi.fusion.api.ILuceneBlocker;
import uk.ac.open.kmi.fusion.api.IPersistentStore;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.AtomicAttribute;
import uk.ac.open.kmi.fusion.api.impl.AttributeProfileInDataset;
import uk.ac.open.kmi.fusion.api.impl.FusionConfigurationObject;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.learning.cache.CacheEntry;
import uk.ac.open.kmi.fusion.util.FusionException;

public class RemoteSPARQLDataSource extends FusionConfigurationObject implements IDataSource {
	
	SesameRemoteConnectionProfile connectionProfile;
	
	String repositoryURL;
	
	String proxyHost = null;
	int proxyPort = 0;
	
	IDataSource embeddingDataSource;
	
	IPersistentStore intermediateStore = null;
	
	int pageSize = 1000;
	
	Logger log = Logger.getLogger(RemoteSPARQLDataSource.class);
	
	public static final String TYPE_URI = FusionMetaVocabulary.REMOTE_SPARQL_DATA_SOURCE;

	public RemoteSPARQLDataSource(Resource rdfIndividual, FusionEnvironment environment) {
		super(rdfIndividual, environment);
	}

	@Override
	public RepositoryConnection getConnection() {
		return connectionProfile.getConnection();
	}

	@Override
	public ValueFactory getValueFactory() {
		
		return connectionProfile.getValueFactory();
	}
	
	@Override
	protected void readFromPropertyMember(Statement statement)
			throws RepositoryException {
		super.readFromPropertyMember(statement);
		
		if(statement.getPredicate().toString().equals(FusionMetaVocabulary.URL)) {
			this.repositoryURL = ((Literal)statement.getObject()).stringValue().trim();
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.PROXY_HOST)) {
			this.proxyHost = ((Literal)statement.getObject()).stringValue().trim();
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.PROXY_PORT)) {
			this.proxyPort = ((Literal)statement.getObject()).intValue();
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.INTERMEDIATE_STORE)) {
			this.intermediateStore = (IPersistentStore)FusionEnvironment.getInstance().findConfigurationObjectByID((Resource)statement.getObject());
			this.intermediateStore.setEmbeddingDataSource(this);
		}
		
	}

	@Override
	public void readFromRDFIndividual(RepositoryConnection connection)
			throws FusionException {
		
		super.readFromRDFIndividual(connection);
	}

	@Override
	public void prepare() throws FusionException {
		try {
			connectionProfile = SesameRemoteConnectionProfile.openConnection(repositoryURL, proxyHost, proxyPort);
			
			
		} catch(OpenRDFException e) {
			e.printStackTrace();
		}
		
		if(this.intermediateStore!=null) {
			intermediateStore.prepare();
		}
	}

	@Override
	public int copyRelevantSubsetToBlocker(ILuceneBlocker blocker,
			ApplicationContext context, Map<String, AttributeProfileInDataset> targetAttributes)
			throws FusionException {
		
		Set<String> tmpSet = new HashSet<String>();
		// Map<String, AttributeProfileInDataset> tmpMap = new HashMap<String, AttributeProfileInDataset>();
		String type = context.getRestrictedTypesTarget().get(0);
		if(this!=blocker) {
			blocker.openIndex();
			log.info("Started blocking");
		}
		
		try {
			MySPARQLParser tmpQueryParser = new MySPARQLParser(context.serializeQuerySPARQLTarget());
			tmpQueryParser.addTriplePattern(Node.createVariable("uri"), Node.createVariable("property"), Node.createVariable("obj"));
			tmpQueryParser.addOutputVariable("property");
			tmpQueryParser.addOutputVariable("obj");
			
			CacheEntry currentCacheEntry = null;
			String currentUri = "";
			String previousUri = "";
			
			log.info("Retrieving target instances...");
			
			// Retrieve all direct paths
			String tmpQuery = tmpQueryParser.getFilteredQuery();
			
			// Query query = context.getQuerySPARQLTargetARQ();
			TupleQuery query = this.connectionProfile.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, tmpQuery);
			
			log.info("Query: "+tmpQuery);
	
			TupleQueryResult res = query.evaluate();
			Set<AtomicAttribute> targetPropertiesSet = new HashSet<AtomicAttribute>();
	
			AttributeProfileInDataset currentAttributeProfile;
			IAttribute currentAttribute;
			BindingSet bs;
			URI propertyURI;
			
			String val;
			List<String> tokens;
			long time = System.currentTimeMillis();
			Map<String, Document> docMap = new HashMap<String, Document>();
			Document currentDoc = new Document();
			try {
				while(res.hasNext()) {
					bs = res.next();
					if(!(bs.getValue("uri") instanceof URI)) continue;
					currentUri = bs.getValue("uri").toString();
					if(!currentUri.equals(previousUri)) {
						if(docMap.containsKey(currentUri)) {
							currentDoc = docMap.get(currentUri);
						} else {
							currentDoc = new Document();
							currentDoc.add(new Field("uri", currentUri, Field.Store.YES, Field.Index.NOT_ANALYZED));
							docMap.put(currentUri, currentDoc);
						}
	
						
					}
					previousUri = currentUri;
					
					if(bs.getValue("property") instanceof URI) {
						propertyURI = (URI)bs.getValue("property");
						if(propertyURI.equals(RDF.TYPE)) {
							currentDoc.add(new Field(RDF.TYPE.toString(), bs.getValue("obj").toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
						} else if(bs.getValue("obj") instanceof Literal) {
							if(targetAttributes.containsKey(propertyURI.toString())) {
								currentAttributeProfile = targetAttributes.get(propertyURI.toString());
							} else {
								currentAttributeProfile = new AttributeProfileInDataset(propertyURI.toString());
								targetAttributes.put(propertyURI.toString(), currentAttributeProfile);
							}
							if(!tmpSet.contains(propertyURI.toString())) {
								currentAttributeProfile.increaseMentionedIn();
								tmpSet.add(propertyURI.toString());
							}
							currentAttributeProfile.increasePropertyCount();
							val = SesameUtils.cleanSesameLiteralValue((Literal)bs.getValue("obj"));
							
							tokens = Utils.splitByStringTokenizer(val, " \t\n\r\f:(),-.");
							currentAttributeProfile.setAverageNumberOfTokens(currentAttributeProfile.getAverageNumberOfTokens()+tokens.size());
							if(currentAttributeProfile.isInteger()) {
								try {
									Integer.parseInt(val);
								} catch(NumberFormatException e) {
									currentAttributeProfile.setInteger(false);
								}
							}
							if(currentAttributeProfile.isDouble()) {
								try {
									Double.parseDouble(val);
								} catch(NumberFormatException e) {
									currentAttributeProfile.setDouble(false);
								}
							}
							// sourcePropertiesSet.add(propertyURI.toString());
							currentCacheEntry.addValue(propertyURI.toString(), val);
						}
					}
				}
				log.info("Collected properties ");
			} finally {
				res.close();
			}
			
			
			for(String key : docMap.keySet()) {		
				currentDoc = docMap.get(key);
				blocker.addDocument(currentDoc, type);
	
			}
			
			for(String key : targetAttributes.keySet()) {
				currentAttributeProfile = targetAttributes.get(key);
				currentAttributeProfile.summarize();
			}
				
			log.info("Pre-selecting individuals complete: "+(System.currentTimeMillis()-time)+" ms");
			log.info(docMap.size()+" instances");
			if(this!=blocker) {
				log.info("Committing blocker");
				time = System.currentTimeMillis();
				blocker.commit();
				blocker.refreshSearcher();
				blocker.closeIndex();
				log.info("Blocker committed: "+(System.currentTimeMillis()-time)+" ms");
			}
				
			return docMap.size();
	
		} catch(Exception e) {
			throw new FusionException("Error when copying relevant instances to the indexer", e);
		}
		
			
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
		try {
			connectionProfile.close();
		} catch(OpenRDFException e) {
			throw new FusionException(e);
		}
	}
	
}
