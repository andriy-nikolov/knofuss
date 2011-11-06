package uk.ac.open.kmi.fusion.learning;

import java.util.*;
import java.io.*;


import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.*;
import org.openrdf.model.*;
import org.openrdf.model.vocabulary.RDF;


import uk.ac.open.kmi.common.utils.sparql.MySPARQLParser;
import uk.ac.open.kmi.fusion.api.*;
import uk.ac.open.kmi.fusion.api.impl.*;
// import uk.ac.open.kmi.fusion.index.LuceneDiskIndexer;
// import uk.ac.open.kmi.fusion.index.LuceneMemoryIndexer;
// import uk.ac.open.kmi.fusion.index.LuceneMemoryIndexerAllFields;
import uk.ac.open.kmi.fusion.learning.cache.CachedPair;
import uk.ac.open.kmi.fusion.learning.cache.MemoryInstanceCache;
import uk.ac.open.kmi.fusion.objectidentification.*;
import uk.ac.open.kmi.fusion.objectidentification.standard.*;
import uk.ac.open.kmi.fusion.util.*;

public class ObjectContextModelMatcherForGenetic {
	ObjectContextModel instanceModel;
	//List<OntologyProperty> properties;
	Set<LuceneBackedObjectContextWrapper> sourceResources;
	Map<String, LuceneBackedObjectContextWrapper> sourceResourcesTable;
	List<ComparisonPair> selectedPairs;
	//Map<IObjectContextWrapper, List<ComparisonPair>> candidatePairs;
	//ILuceneBlocker indexer;
	
	//LuceneMemoryIndexerNGrams blockerIndexer;
	// LuceneMemoryIndexerAllFields blockerIndexer;
	MemoryInstanceCache cache;
	
	double threshold;
	//List<AtomicMapping> mappings;
	boolean multiOntologyCase = false;
	
	Map<Integer, Double> results;
	
	String sourceQuery = null;
	
	String notFoundFile = "not-found.txt";
	
	// boolean useSampling = false;
	
	private static Logger log = Logger.getLogger(ObjectContextModelMatcherForGenetic.class); 
	
	private void init() {
		sourceResources = new HashSet<LuceneBackedObjectContextWrapper>();
		sourceResourcesTable = new HashMap<String, LuceneBackedObjectContextWrapper>();
		selectedPairs = new ArrayList<ComparisonPair>();
		//indexer = FusionEnvironment.getIndexer();
		results = new HashMap<Integer, Double>();
		
	}
		
	public ObjectContextModelMatcherForGenetic() {
		init();
	}
	
	/*private void doBlocking(String type) throws IOException, FusionException {
		blockerIndexer = new LuceneMemoryIndexerAllFields();
		long time;
		IndexSearcher indexSearcher = indexer.getIndexSearcher();
		
		Document doc;
		int total = 0;
		
		String[] vals;
		Set<String> tmpSet = new HashSet<String>();
		
		log.info("Started blocking");
		
		time = System.currentTimeMillis();
		
		Term term = new Term(RDF.TYPE.toString(), type);
		TermQuery query = new TermQuery(term);
		
		TopDocs topDocs = indexSearcher.search(query, indexSearcher.maxDoc());
		
		for(int i=0;i<topDocs.scoreDocs.length;i++) {
			if((topDocs.scoreDocs[i].score+0.01)>1.0) {
				tmpSet.clear();
				doc = indexSearcher.doc(topDocs.scoreDocs[i].doc);
				vals = doc.getValues(RDF.TYPE.toString());
				for(String val : vals) {
					tmpSet.add(val);
				}
				
				if(!tmpSet.contains(type)) {
					System.err.println("Does not belong to "+type);
					
						List<Fieldable> fields = doc.getFields();
						for(Fieldable f : fields) {
							System.err.println(f.name()+":"+f.stringValue());
						}
					System.err.println();
				}
				
				blockerIndexer.addDocument(doc, type);
				if((i%10000)==0) {
					log.info("Added "+i+" documents");
				}
				total++;
			} else {
				break;
			}
		}
		
		log.info("Added instances to blocker: "+(System.currentTimeMillis()-time)+" ms");
		log.info("Committing blocker");
		time = System.currentTimeMillis();
		blockerIndexer.getIndexWriter().commit();
		blockerIndexer.refreshSearcher();
		log.info("Blocker committed: "+(System.currentTimeMillis()-time)+" ms");
	}*/
	
	
	private int calculateSimilarities(boolean useSample) {
		int i;//, j;
		IObjectContextWrapper resTarget;
		//List<ComparisonPair> candList;
		double similarity;
		//Set<String> candidateURIs;
		//Map<String, SearchResult> searchResults;
		//Map<String, Document> candidateDocs;
		int comparisons = 0;
		ComparisonPair pair;
		
		//List<LuceneBackedObjectContextWrapper> notFound = new ArrayList<LuceneBackedObjectContextWrapper>();
		
		log.info(instanceModel.toString());
		
		try {

			i=0;
			// j=0;
			long currentTime;
			// long totalTimeSearch = 0;
			long totalTimeComparison = 0;
			long totalTimeRetrieval = 0;
			// long initTime = System.currentTimeMillis();
			// String type = instanceModel.getRestrictedTypesTarget().get(0).toString();
			//log.info("Concept type : "+type);
			
			//log.info("Cache size : "+FusionEnvironment.getInstance().getMemoryInstanceCache().getSize());
			
			// Map<String, List<String>> valuesByPropertyUri = new HashMap<String, List<String>>();

			Iterator<CachedPair> iterator = cache.getComparablePairsIterator(useSample);
			
			IObjectContextWrapper resSource;
			
			double totalSimilarity = 0;
			double averageSimilarity = 0;
			double maxSimilarity = 0;
			
			CachedPair cachedPair;
			
			while(iterator.hasNext()) {
				
				cachedPair = iterator.next();
				currentTime = System.currentTimeMillis();
				resSource = cachedPair.getCandidateInstance().getObjectContextWrapper(instanceModel, true);
				resTarget = cachedPair.getTargetInstance().getObjectContextWrapper(instanceModel, false);
				totalTimeRetrieval+=(System.currentTimeMillis()-currentTime);
			
				pair = new ComparisonPair(resSource, resTarget);
				// System.out.print("Comparison ... ");
				currentTime = System.currentTimeMillis();
				similarity = instanceModel.getSimilarity(pair);
				pair.setSimilarity(similarity);
				// System.out.println("done");
				/*if(resSource.getIndividual().toString().endsWith("Person990")&&
						resTarget.getIndividual().toString().endsWith("Person991")) {
					log.info("Person990 vs Person991 similarity: " + similarity);
				}*/
				
				totalSimilarity+=similarity;
				if(similarity>maxSimilarity) {
					maxSimilarity = similarity;
				}
				
				totalTimeComparison+=(System.currentTimeMillis()-currentTime);
				comparisons++;
				if(similarity>=threshold) {
					results.put(cachedPair.getId(), similarity);	
					/*if(!candidatePairs.containsKey(resSource)) {
						candList = new ArrayList<ComparisonPair>();
						candidatePairs.put(resSource, candList);
					} else {
						candList = candidatePairs.get(resSource);
					}
					candList.add(pair);*/
				}
					
				i++;
				
			}
			
			averageSimilarity = totalSimilarity/comparisons;
			if(Double.toString(averageSimilarity).equals("NaN")) {
				log.error("NaN");
			}
			log.info("similarity: average: "+averageSimilarity+" max: "+maxSimilarity);
			//log.info("Total time: "+(System.currentTimeMillis()-initTime));
			//log.info("Total search time: "+totalTimeSearch);
			//log.info("Total comparison time: "+totalTimeComparison);
			//log.info("Total retrieval time: "+totalTimeRetrieval);
			
		} catch(Exception e) {
			e.printStackTrace();
			return -1;
		}
		return comparisons;
	}
	
	
	
		
	public Map<Integer, Double> execute(double threshold, MemoryInstanceCache cache, boolean useSample) {
		this.cache = cache;
		this.threshold = threshold;
//		properties.clear();
		sourceResources.clear();
		//targetResources.clear();
		selectedPairs.clear();
		//candidatePairs.clear();
		
		
		//this.sourceQuery = instanceModel.serializeQuerySPARQLSource();
		
		/*List<String> searchFieldNames = new ArrayList<String>();
		
		Map<String, String> variablePropertyMap = instanceModel.getVariablePathMapTarget();
		for(String key : variablePropertyMap.keySet()) {
			searchFieldNames.add(variablePropertyMap.get(key));
		}*/
				
		//PersistenceUtil.setDaoManager(FusionGlobals.fusionSession.getDaoManager());
		
//		log.info("Loading sources... "+this.instanceModel.getRestrictedTypesSource().get(0));
//		fillList(sourceResources, this.sourceResourcesTable, FusionEnvironment.getInstance().getFusionRepositoryConnection());
//		log.info("Loading targets... "+this.instanceModel.getRestrictedTypesTarget().get(0));
//		log.info("Indexing... "+this.instanceModel.getRestrictedTypesTarget().get(0));
//		log.info("Candidate individuals: "+sourceResources.size());
		
		//log.info("Calculating similarities... "+this.instanceModel.getRestrictedTypesTarget().get(0));
		Calendar calendarBefore = new GregorianCalendar();
		int comparisons = calculateSimilarities(useSample);
		Calendar calendarAfter = new GregorianCalendar();
		log.info("Comparisons: "+comparisons);
		log.info("Time cost: "+(calendarAfter.getTimeInMillis()-calendarBefore.getTimeInMillis()));
		//addToModel();
	
		return results; 
	}

	
	public ObjectContextModel getObjectContextModel() {
		return instanceModel;
	}

	public void setObjectContextModel(ObjectContextModel instanceModel) {
		this.instanceModel = instanceModel;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public boolean isMultiOntologyCase() {
		return multiOntologyCase;
	}

	public void setMultiOntologyCase(boolean multiOntologyCase) {
		this.multiOntologyCase = multiOntologyCase;
	}

	
	
	
}
