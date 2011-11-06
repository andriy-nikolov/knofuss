package uk.ac.open.kmi.fusion.learning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;

import com.hp.hpl.jena.graph.Node;

import uk.ac.open.kmi.common.utils.CountMapKeyByValueSizeComparator;
import uk.ac.open.kmi.common.utils.OIComparison;
import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.common.utils.sesame.SesameUtils;
import uk.ac.open.kmi.common.utils.sparql.MySPARQLParser;
import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.ILuceneBlocker;
import uk.ac.open.kmi.fusion.api.IObjectIdentificationMethod;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.AtomicMapping;
import uk.ac.open.kmi.fusion.api.impl.AtomicAttribute;
import uk.ac.open.kmi.fusion.api.impl.AttributeProfileInDataset;
import uk.ac.open.kmi.fusion.api.impl.AttributeType;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.api.impl.FusionMethodWrapper;
import uk.ac.open.kmi.fusion.util.FusionException;
import uk.ac.open.kmi.fusion.learning.cache.CacheEntry;
import uk.ac.open.kmi.fusion.learning.cache.CachedPair;
import uk.ac.open.kmi.fusion.learning.cache.MemoryInstanceCache;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.DefaultFitnessFunction;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.F1Fitness;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.IFitnessFunction;

public class DummyBlockerOnlyObjectIdentificationMethod implements
		IObjectIdentificationMethod {

	private FusionMethodWrapper descriptor;
	private static Logger log = Logger.getLogger(DummyBlockerOnlyObjectIdentificationMethod.class);
	
	private int depth = 1;
	
	private boolean addMissing = false;
	
	public DummyBlockerOnlyObjectIdentificationMethod() {
		
	}

	@Override
	public void setMethodDescriptor(FusionMethodWrapper descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public FusionMethodWrapper getDescriptor() {
		return descriptor;
	}
	
	
	private void initProperties() {
		
		if(descriptor.getProperties().containsKey(FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"depth")) {
			this.depth = Integer.parseInt((descriptor.getProperties().get(FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"depth")));
			if(this.depth>2) {
				log.error("Does not support depth greater than 2");
				depth = 2;
			}
		}
		
	}

	@Override
	public List<AtomicMapping> getAllPossibleMappings(ApplicationContext context) throws FusionException {

		try {
			
			initProperties();
			
			MemoryInstanceCache cache = new MemoryInstanceCache();
			
			List<AtomicAttribute> sourcePropertiesPool = this.initializePropertyPools(context, cache);
			List<AtomicAttribute> targetPropertiesPool = new ArrayList<AtomicAttribute>();
			Map<Integer, String> goldStandardEncoded = new HashMap<Integer, String>();
			this.doBlocking(context, cache, goldStandardEncoded, sourcePropertiesPool, targetPropertiesPool);
			
			Map<Integer, Double> finalResults = this.generateFinalResultsFromBlocker(cache);
			
			
			/*CandidateSolutionPool candidateSolutionPool = new CandidateSolutionPool(context, sourcePropertiesPool, targetPropertiesPool, cache);
			candidateSolutionPool.setMaxIterations(maxIterations);
			candidateSolutionPool.setPopulationSize(populationSize);
			candidateSolutionPool.setUseUnsupervisedFitness(useUnsupervisedFitness);
			candidateSolutionPool.setCrossoverRate(crossoverRate);
			candidateSolutionPool.setMutationRate(mutationRate);
			
			candidateSolutionPool.setGoldStandardSet(goldStandardEncoded.keySet());
			Map<Integer, Double> finalResults = candidateSolutionPool.run();
			CandidateSolution finalSolution = candidateSolutionPool.getFinalSolution();
			doFilteringByClasses(finalResults, cache, goldStandardEncoded.keySet(), finalSolution.getFitness(), context.getRestrictedTypesTarget().get(0)); */
			
			return createAtomicMappings(finalResults, cache);
			
		} catch(Exception e) {
			throw new FusionException("Could not perform coreference resolution: ", e);
		}
		
	}

	private void doBlocking(ApplicationContext context, MemoryInstanceCache cache, Map<Integer, String> goldStandardEncoded, List<AtomicAttribute> sourcePropertiesPool, List<AtomicAttribute> targetPropertiesPool) throws FusionException {
		
		try {
		ILuceneBlocker blocker = context.getBlocker();

		// Set<String> targetPropertiesSet = new HashSet<String>();
		
		Map<AtomicAttribute, Integer> targetAttributeCounts = new HashMap<AtomicAttribute, Integer>();
		
		Map<String, AttributeProfileInDataset> targetAttributes = new HashMap<String, AttributeProfileInDataset>();
		
		int size = context.getLinkSession().getTargetDataset().copyRelevantSubsetToBlocker(blocker, context, targetAttributes);
		double val;
		AtomicAttribute attribute;
		for(String key : targetAttributes.keySet()) {
			val = ((double)targetAttributes.get(key).getMentionedIn())/size;
			attribute = targetAttributes.get(key).createAttribute();
			targetAttributeCounts.put(attribute, targetAttributes.get(key).getMentionedIn());
			if(val>=0.1) {
				if((!key.equals(Utils.FOAF_NS+"name"))/*&&(!key.equals("http://oaei.ontologymatching.org/2010/IIMBTBOX/article"))*/) {
					// Many thanks to the DBPedia bug which assigns the person's birth year as her name !
					targetPropertiesPool.add(attribute);
				}
			}
		}
		
		CountMapKeyByValueSizeComparator<AtomicAttribute> comparator = new CountMapKeyByValueSizeComparator<AtomicAttribute>(targetAttributeCounts, true);
		Collections.sort(targetPropertiesPool, comparator);
		
		Map<String, Document> docs;
		int cacheSize = 0;
		CacheEntry targetEntry;
		
		int tp = 0;
		int i = 0;
		String signature;
		
		boolean goldStandardAvailable = context.isGoldStandardAvailable();
		Map<String, OIComparison> goldStandard;
		Set<String> missedKeys;
		if(goldStandardAvailable) {
			goldStandard = context.getGoldStandard();
			missedKeys = new HashSet<String>(goldStandard.keySet());
		} else {
			goldStandard = new HashMap<String, OIComparison>(0);
			missedKeys = new HashSet<String>(0);
		}
		String type = context.getRestrictedTypesTarget().get(0);
		
		
		String[] targetPropertyArray = new String[targetPropertiesPool.size()];
		// targetPropertyArray = targetPropertiesPool.toArray(targetPropertyArray);
		AtomicAttribute attr;
		for(int j=0;j<targetPropertiesPool.size();j++) {
			attr = targetPropertiesPool.get(j);
			targetPropertyArray[j] = attr.getPropertyPath();
		}
		
		//if(useBlocking) {
		
		log.info("Caching starts");
		i=0;
		
		Map<String, List<String>> searchValues = new HashMap<String, List<String>>();
		CachedPair pair;
		
		List<String> tmpList;
		List<? extends Object> tmpObjectList;
		
		for(CacheEntry sourceEntry : cache.getSourceCachedEntries()) {
			searchValues.clear();
			//searchValues.put(Utils.FOAF_NS+"name", sourceEntry.getValueTable().get(Utils.FOAF_NS+"name"));
			if(sourceEntry.getUri().toString().endsWith("kristin_minter")) {
				log.debug("here");
			}
			
			for(AtomicAttribute sourceAttribute : sourcePropertiesPool) {
				if(!sourceAttribute.getType().equals(AttributeType.LONG_TEXT)&&(sourceEntry.getValueTable().containsKey(sourceAttribute.getPropertyPath()))) {
					tmpList = new LinkedList<String>();
					searchValues.put(sourceAttribute.getPropertyPath(), tmpList);
					tmpObjectList = sourceEntry.getValueTable().get(sourceAttribute);
					for(Object obj : tmpObjectList) {
						if(obj instanceof String) {
							tmpList.add((String)obj);
						}
					}
					
				}
			}

			docs = blocker.findClosestDocuments(searchValues, targetPropertyArray, blocker.getThreshold(), type);
			cacheSize+=docs.size();
			i++;
			for(String key : docs.keySet()) {
				signature = sourceEntry.getUri().toString()+" : "+key;
				
				targetEntry = cache.getTargetCacheEntry(FusionEnvironment.getInstance().getMainKbValueFactory().createURI(key));
				
				targetEntry.readPropertiesFromLuceneDocument(docs.get(key));
				pair = cache.addPairToCache(sourceEntry, targetEntry, false);
				
				if(goldStandardAvailable) { 
					if(goldStandard.containsKey(sourceEntry.getUri().toString()+" : "+key)) {
						tp++;
						pair.setGoldStandard(true);
						goldStandardEncoded.put(pair.getId(), signature);
						missedKeys.remove(signature);
					}
				}
			}
			if((i%100)==0) {
				log.info("Cached "+i+" instances, cache size: "+cacheSize);
			}
				
		}
				
		double recall = ((double)tp)/goldStandard.size();
		String[] uris;
		log.info("Cache filled, size: "+cacheSize+", recall: "+recall);
			
			// Add the pairs which were missed by the blocker to the goldStandard
			if(goldStandardAvailable) {
				CacheEntry sourceEntry;
				Document doc;
				int missed = 0;
				int relevant = 0;
				URI sourceEntryURI;
				for(String sign : missedKeys) {
					uris = sign.split(" : ");
					sourceEntryURI = FusionEnvironment.getInstance().getFusionKbValueFactory().createURI(uris[0].trim());
					if(cache.containsSourceCacheEntry(sourceEntryURI)) {
						relevant++;
						sourceEntry = cache.getSourceCacheEntry(FusionEnvironment.getInstance().getFusionKbValueFactory().createURI(uris[0].trim()));
						doc = blocker.findByURI(uris[1].trim());
						if(doc==null) {
							// System.out.println(uris[1].trim());
							missed ++;
						} else {
							targetEntry = cache.getTargetCacheEntry(FusionEnvironment.getInstance().getMainKbValueFactory().createURI(uris[1].trim()));
							targetEntry.readPropertiesFromLuceneDocument(doc);
							pair = cache.addPairToCache(sourceEntry, targetEntry, true);
							if(!addMissing) {
								pair.setMissing(true);
							}
							goldStandardEncoded.put(pair.getId(), sign);
						}
					}
					
				}
				
				log.info("Missed comparison pairs added: "+(relevant-missed)+", could not find: "+missed);
			}
			
			log.info("Gold standard size: "+goldStandardEncoded.size());
		} catch(Exception e) {
			throw new FusionException("Could not create the cache of instance pairs for comparison: ", e);
		}
	}
	
	
	private Map<Integer, Double> generateFinalResultsFromBlocker(MemoryInstanceCache cache) {
		Map<Integer, Double> results = new HashMap<Integer, Double>();
		
		Iterator<CachedPair> iterator = cache.getComparablePairsIterator(false);
		CachedPair currentPair;
		while(iterator.hasNext()) {
			currentPair = iterator.next();
			results.put(currentPair.getId(), 1.0);
		}
		
		return results;
		
	}

	private List<AtomicAttribute> initializePropertyPools(ApplicationContext context, MemoryInstanceCache cache) throws OpenRDFException {
				
		MySPARQLParser tmpQueryParser = new MySPARQLParser(context.serializeQuerySPARQLSource());
		tmpQueryParser.addTriplePattern(Node.createVariable("uri"), Node.createVariable("property"), Node.createVariable("obj"));
		tmpQueryParser.addOutputVariable("property");
		tmpQueryParser.addOutputVariable("obj");
		
		CacheEntry currentCacheEntry = null;
		String currentUri = "";
		String previousUri = "";
		
		log.info("Initialize source property pool...");
		
		// Retrieve all direct paths
		String tmpQuery = tmpQueryParser.getFilteredQuery();
		log.info("Query: "+tmpQuery);
		TupleQuery query = FusionEnvironment.getInstance().getFusionRepositoryConnection().prepareTupleQuery(QueryLanguage.SPARQL, tmpQuery);
		TupleQueryResult res = query.evaluate();
		Set<AtomicAttribute> sourcePropertiesSet = new HashSet<AtomicAttribute>();
		Map<String, AttributeProfileInDataset> sourceAttributeProfiles = new HashMap<String, AttributeProfileInDataset>();
		AttributeProfileInDataset currentAttributeProfile;
		AtomicAttribute currentAttribute;
		BindingSet bs;
		URI propertyURI;
		Set<String> tmpSet = new HashSet<String>();
		String val;
		List<String> tokens;
		try {
			while(res.hasNext()) {
				bs = res.next();
				if(!(bs.getValue("uri") instanceof URI)) continue;
				currentUri = bs.getValue("uri").toString();
				if(!currentUri.equals(previousUri)) {
					
					currentCacheEntry = cache.getSourceCacheEntry((URI)bs.getValue("uri"));
					tmpSet.clear();
				}
				previousUri = currentUri;
				
				if(bs.getValue("property") instanceof URI) {
					propertyURI = (URI)bs.getValue("property");
					if(propertyURI.equals(RDF.TYPE)) continue;
					// if(propertyURI.equals("http://oaei.ontologymatching.org/2010/IIMBTBOX/article")) continue;
					if(bs.getValue("obj") instanceof Literal) {
						if(sourceAttributeProfiles.containsKey(propertyURI.toString())) {
							currentAttributeProfile = sourceAttributeProfiles.get(propertyURI.toString());
						} else {
							currentAttributeProfile = new AttributeProfileInDataset(propertyURI.toString());
							sourceAttributeProfiles.put(propertyURI.toString(), currentAttributeProfile);
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
		
		// Retrieve all forward paths of length 2
		if(depth==2) {
			tmpQueryParser = new MySPARQLParser(context.serializeQuerySPARQLSource());
			tmpQueryParser.addTriplePattern(Node.createVariable("uri"), Node.createVariable("property1"), Node.createVariable("tmp"));
			tmpQueryParser.addTriplePattern(Node.createVariable("tmp"), Node.createVariable("property2"), Node.createVariable("obj"));
			tmpQueryParser.addOutputVariable("property1");
			tmpQueryParser.addOutputVariable("property2");
			tmpQueryParser.addOutputVariable("obj");
			
			tmpQuery = tmpQueryParser.getFilteredQuery();
			log.info(tmpQuery);
			query = FusionEnvironment.getInstance().getFusionRepositoryConnection().prepareTupleQuery(QueryLanguage.SPARQL, tmpQuery);
			res = query.evaluate();
			
			try {
				
				Set<String> paths = new HashSet<String>();
				String path;
				currentUri = "";
				previousUri = "";
				
				while(res.hasNext()) {
					bs = res.next();
					
					if(!(bs.getValue("uri") instanceof URI)) continue;
					currentUri = bs.getValue("uri").toString();
					if(!currentUri.equals(previousUri)) {
						
						currentCacheEntry = cache.getSourceCacheEntry((URI)bs.getValue("uri"));
						tmpSet.clear();
					}
					previousUri = currentUri;
					
					if((bs.getValue("property1") instanceof URI)
							&&(bs.getValue("property2") instanceof URI)
							&&(bs.getValue("obj") instanceof Literal)) {
						if(bs.getValue("property1").equals(RDF.TYPE)
								||bs.getValue("property2").equals(RDF.TYPE)) continue;
						
						path = "<"+bs.getValue("property1").toString()+">/<"+bs.getValue("property2").toString()+">";
						paths.add(path);
						// paths.add();
						if(sourceAttributeProfiles.containsKey(path)) {
							currentAttributeProfile = sourceAttributeProfiles.get(path);
						} else {
							currentAttributeProfile = new AttributeProfileInDataset(path);
							sourceAttributeProfiles.put(path, currentAttributeProfile);
						}
						if(!tmpSet.contains(path)) {
							currentAttributeProfile.increaseMentionedIn();
							tmpSet.add(path);
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
						
						
						// sourcePropertiesSet.add(path);
						currentCacheEntry.addValue(path, val);
						
						// log.info(bs.getValue("uri")+" : "+bs.getValue("property1")+" : "+bs.getValue("property2")+" : "+bs.getValue("obj"));
						
					}
					
				}
				log.info(paths.size());
				
				for(String tmp : paths) {
					log.info(tmp);
				}
				
			} finally {
				res.close();
			}
			
			
		}
		
		for(String key : sourceAttributeProfiles.keySet()) {
			currentAttributeProfile = sourceAttributeProfiles.get(key);
			currentAttributeProfile.summarize();
			currentAttribute = currentAttributeProfile.createAttribute();
			sourcePropertiesSet.add(currentAttribute);
		}
		
		log.info("Initialize source property pool... finished");
		log.info("Source dataset size: "+cache.getSourceCachedEntries().size());
		
		return new ArrayList<AtomicAttribute>(sourcePropertiesSet);

		
	}
	
	private List<AtomicMapping> createAtomicMappings(Map<Integer, Double> resultsEncoded, MemoryInstanceCache cache) {
		List<AtomicMapping> mappings = new ArrayList<AtomicMapping>(resultsEncoded.size());
		
		CachedPair pair;
		for(Integer id : resultsEncoded.keySet()) {
			pair = cache.getCachedPairById(id);
			mappings.add(pair.convertToAtomicMapping(1.0));
		}
		
		return mappings;
		
	}

}
