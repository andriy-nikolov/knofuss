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
package uk.ac.open.kmi.fusion.api.impl.datasource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import com.hp.hpl.jena.graph.Node;

import uk.ac.open.kmi.common.utils.sesame.http.SesameRemoteConnectionProfile;
import uk.ac.open.kmi.common.utils.sesame.http.SesameSPARQLConnectionProfile;
import uk.ac.open.kmi.common.utils.sparql.MySPARQLParser;
import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.IDataSource;
import uk.ac.open.kmi.fusion.api.ILuceneBlocker;
import uk.ac.open.kmi.fusion.api.IPersistentStore;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.AttributeProfileInDataset;
import uk.ac.open.kmi.fusion.api.impl.FusionConfigurationObject;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.learning.GeneticAlgorithmObjectIdentificationMethod;
import uk.ac.open.kmi.fusion.util.FusionException;

public class RemoteSPARQLDataSource extends FusionConfigurationObject implements IDataSource {
	
	public static final String TYPE_URI = FusionMetaVocabulary.REMOTE_SPARQL_DATA_SOURCE;
	public static final int DEFAULT_PAGE_SIZE = 1000;
	
	SesameSPARQLConnectionProfile connectionProfile;
	
	String repositoryURL;
	
	String proxyHost = null;
	int proxyPort = 0;
	
	IDataSource embeddingDataSource;
	
	IPersistentStore intermediateStore = null;
	
	int pageSize = DEFAULT_PAGE_SIZE;
	
	int N_THREADS = 4;
	
	static Logger log = Logger.getLogger(RemoteSPARQLDataSource.class);
	

	public RemoteSPARQLDataSource(Resource rdfIndividual, FusionEnvironment environment) {
		super(rdfIndividual, environment);
	}
	
	public RemoteSPARQLDataSource(String repositoryURL) {
		this.repositoryURL = repositoryURL;
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
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.PAGE_SIZE)) {
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
			connectionProfile = SesameSPARQLConnectionProfile.openConnection(repositoryURL, proxyHost, proxyPort);
			
			
		} catch(OpenRDFException e) {
			e.printStackTrace();
		}
		
		if(this.intermediateStore!=null) {
			intermediateStore.prepare();
		}
	}

	
	private int copyRelevantSubsetToBlockerFromRemoteSource(ILuceneBlocker blocker,
			ApplicationContext context, Map<String, AttributeProfileInDataset> targetAttributes)
			throws FusionException {
		
		try {
			MySPARQLParser tmpQueryParser = new MySPARQLParser(context.serializeQuerySPARQLTarget());
			tmpQueryParser.addTriplePattern(Node.createVariable("uri"), Node.createVariable("property"), Node.createVariable("obj"));
			tmpQueryParser.addOutputVariable("property");
			tmpQueryParser.addOutputVariable("obj");

			// Retrieve instance counts 
			String sQuery = formUriCountRetrievalQuery(context);
			
			TupleQuery query = connectionProfile.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, sQuery);
			
			TupleQueryResult tqr = query.evaluate();
			
			int instanceCount = 0;
			try {
				if(tqr.hasNext()) {
					instanceCount = ((Literal)(tqr.next().getBinding("count").getValue())).intValue();
				}
			} finally {
				tqr.close();
			}
			
			if(instanceCount==0)
				return 0;
			
			// Retrieve relevant properties
			sQuery = formPropertyRetrievalQuery(context);
			
			query = connectionProfile.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, sQuery);
			
			tqr = query.evaluate();
			
			BindingSet bs;
			int count;
			URI propertyURI;
			
			Map<URI, Integer> countsMap = new HashMap<URI, Integer>();
			
			try {
				while(tqr.hasNext()) {
					bs = tqr.next();
					
					count = ((Literal)bs.getValue("count")).intValue();
					propertyURI = (URI)bs.getValue("property");
					
					countsMap.put(propertyURI, count);
				}
			} finally {
				tqr.close();
			}
			
			Set<URI> propertiesToCopy = new HashSet<URI>();
			
			double coverage;
			
			for(Entry<URI, Integer> countEntry : countsMap.entrySet()) {
				
				coverage = ((double)countEntry.getValue())/instanceCount;
			
				if(coverage >= GeneticAlgorithmObjectIdentificationMethod.PROPERTY_COVERAGE_REQUIRED) {
					propertiesToCopy.add(countEntry.getKey());
					log.info("\t"+countEntry.getKey()+": "+countEntry.getValue());
				}
				
			}
			
			ExecutorService threadPool = Executors.newFixedThreadPool(Math.min(propertiesToCopy.size(), N_THREADS));
			
			RemoteSPARQLDataExtractorRunnable task;
			
			Future<List<BindingSet>> futureResult;
			
			Set<URI> finishedProperties = new HashSet<URI>();
			Map<URI, Integer> offsets = new HashMap<URI, Integer>();
			
			for(URI property : propertiesToCopy) {
				offsets.put(property, 0);
			}
			
			List<BindingSet> result;
			Map<URI, BindingSet> firstResults = new HashMap<URI, BindingSet>();
			BindingSet firstResult = null;
			
			Map<URI, Future<List<BindingSet>>> futureResults = new HashMap<URI, Future<List<BindingSet>>>();
			
			Set<URI> distinctUris = new HashSet<URI>();
			
			int numstmts = 0;
			while(!propertiesToCopy.isEmpty()) {
				finishedProperties.clear();
				futureResults.clear();
				for(URI property : propertiesToCopy) {
					task = new RemoteSPARQLDataExtractorRunnable(connectionProfile.getConnection(), context, property, pageSize, offsets.get(property));
					
					log.info("Submitted: "+property.stringValue()+", "+offsets.get(property));
					futureResult = threadPool.submit(task);
					
					futureResults.put(property, futureResult);
					
					Thread.sleep(1000);
				}	
				
				for(Entry<URI, Future<List<BindingSet>>> entry : futureResults.entrySet()) {
					futureResult = entry.getValue();
					result = futureResult.get();
					log.info("Received: "+entry.getKey().stringValue()+", "+offsets.get(entry.getKey()));
					offsets.put(entry.getKey(), offsets.get(entry.getKey()) + result.size());
					
					if(result.size()<pageSize) {
						finishedProperties.add(entry.getKey());
						log.info("Finished: "+entry.getKey().stringValue()+", total: "+offsets.get(entry.getKey()));
					}
					
					if(result.size()>0) {
						if(firstResults.containsKey(entry.getKey()) && firstResults.get(entry.getKey()).equals(result.get(0))) {
							System.err.println("Equal results for different searches: "+entry.getKey());
							finishedProperties.add(entry.getKey());
							continue;
						}
					}
					
					firstResults.put(entry.getKey(), result.get(0));
					
					for(BindingSet tmp : result) {
						if(tmp.getValue("uri") instanceof URI)
							distinctUris.add((URI)tmp.getValue("uri"));
					}
					
					numstmts += result.size();
					
					blocker.indexBindingSets(result, context, context.getRestrictedTypesTarget().get(0));
				}
				
				propertiesToCopy.removeAll(finishedProperties);
			}
			
			System.out.println("Statements retrieved: "+numstmts);
			System.out.println("Instances retrieved: "+distinctUris.size());
			System.out.println("Instances should have been retrieved: "+instanceCount);
			
			
			return distinctUris.size();
		} catch(Exception e) {
			throw new FusionException("Error when copying relevant instances to the indexer", e);
		}
		
		
	}
	
	
	private String formUriCountRetrievalQuery(ApplicationContext context) {
		String restriction = context.getRestrictionTarget();
		
		String query = "SELECT (count(distinct ?uri) as ?count) WHERE { \n"
				+ restriction+" . }";
		
		return query;
		
	}
	
	private String formPropertyRetrievalQuery(ApplicationContext context) {
		
		String restriction = context.getRestrictionTarget();
		
		String query = "SELECT DISTINCT ?property (COUNT(?val) AS ?count) WHERE { \n"
				+ restriction+" . ?uri ?property ?val . " 
				+ " FILTER(isLiteral(?val)) } GROUP BY ?property";
		
		return query;
	}
	
	@Override
	public int copyRelevantSubsetToBlocker(ILuceneBlocker blocker,
			ApplicationContext context, Map<String, AttributeProfileInDataset> targetAttributes)
			throws FusionException {
		
		if(intermediateStore!=null && intermediateStore!=blocker) {
			return intermediateStore.copyRelevantSubsetToBlocker(blocker, context, targetAttributes);
			
		} else {
			return copyRelevantSubsetToBlockerFromRemoteSource(blocker, context, targetAttributes);
		}
		
		/*Set<String> tmpSet = new HashSet<String>();
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
		}*/
		
			
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

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	
}
