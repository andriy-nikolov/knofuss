package uk.ac.open.kmi.fusion.objectidentification.standard;

import java.util.*;
import java.io.*;


import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.*;
import org.openrdf.model.*;
import org.openrdf.model.vocabulary.RDF;


import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.common.utils.sparql.MySPARQLParser;
import uk.ac.open.kmi.fusion.api.*;
import uk.ac.open.kmi.fusion.api.impl.*;
// import uk.ac.open.kmi.fusion.index.LuceneDiskIndexer;
// import uk.ac.open.kmi.fusion.index.LuceneDiskIndexerAllFields;
import uk.ac.open.kmi.fusion.objectidentification.*;
import uk.ac.open.kmi.fusion.objectidentification.standard.*;
import uk.ac.open.kmi.fusion.util.*;

public class ObjectContextModelMatcher {
	ObjectContextModel instanceModel;
	ApplicationContext applicationContext;
	Set<LuceneBackedObjectContextWrapper> sourceResources;
	Map<String, LuceneBackedObjectContextWrapper> sourceResourcesTable;
	List<ComparisonPair> selectedPairs;
	Map<LuceneBackedObjectContextWrapper, List<ComparisonPair>> candidatePairs;
	ILuceneBlocker indexer;
	double threshold;
	List<AtomicMapping> mappings;
	boolean multiOntologyCase = false;
	
	String sourceQuery = null;
	
	String notFoundFile = "not-found.txt";
	
	long totalTimeSearch = 0;
	long totalTimeComparison = 0;
	long totalTimeRetrieval = 0;
	
	Map<String, Set<String>> targetUrisByClass;
	
	private static Logger log = Logger.getLogger(ObjectContextModelMatcher.class); 
	
	//RDF2txtTranslator translator;
	
	private void init() {
		sourceResources = new HashSet<LuceneBackedObjectContextWrapper>();
		sourceResourcesTable = new HashMap<String, LuceneBackedObjectContextWrapper>();
		candidatePairs = new HashMap<LuceneBackedObjectContextWrapper, List<ComparisonPair>>();
		selectedPairs = new ArrayList<ComparisonPair>();
		//indexer = FusionEnvironment.getIndexer();
		mappings = new ArrayList<AtomicMapping>();
		targetUrisByClass = new HashMap<String, Set<String>>();
	}
	
	public ObjectContextModelMatcher() {
		init();
	}
	
	private void fillList(Set<LuceneBackedObjectContextWrapper> resList, Map<String, LuceneBackedObjectContextWrapper> resTable, RepositoryConnection connection) {
		LuceneBackedObjectContextWrapper resource;
		
		String sQuery = instanceModel.serializeQuerySPARQLSource();
				
		try {
			TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, sQuery);
		
			TupleQueryResult rsSource = query.evaluate();
			
			try {
				String curUri;
				resource = new LuceneBackedObjectContextWrapper(instanceModel);
				
				int tuples = 0;
				int resources = 0;
				
				BindingSet tuple;
				while(rsSource.hasNext()) {
					tuple = rsSource.next();
					tuples++;
					if(resList==this.sourceResources) {
						if(!(tuple.getValue("uri") instanceof URI)) {
							continue;
						} 
						/*if(!FusionEnvironment.getInstance().getContentIndividualUris().contains(((URI)tuple.getValue("uri")))) {
							continue;
						}*/
					}
					if(!resource.getPropertiesFromQueryResult(tuple)) {
						
						resList.add(resource);
						resources++;
						resTable.put(resource.getIndividual().toString(), resource);
						resource = new LuceneBackedObjectContextWrapper(instanceModel);
						
						resource.getPropertiesFromQueryResult(tuple);
					} 
				}
			 
				resList.add(resource);
				log.info("Resources: "+resources);
			} finally {
				rsSource.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	protected int calculateSimilarities() {
		int i, j;
		ObjectContextWrapper resTarget;
		
		List<ComparisonPair> candList;
		double similarity;
		Set<String> candidateURIs;
		Map<String, SearchResult> searchResults;
		Map<String, Document> candidateDocs;
		Map<String, List<String>> valuesByPropertyPath = new HashMap<String, List<String>>();

		int comparisons = 0;
		ComparisonPair pair;
		List<LuceneBackedObjectContextWrapper> notFound = new ArrayList<LuceneBackedObjectContextWrapper>();
		
		Map<String, Set<String>> mapSimilarities = new HashMap<String, Set<String>>();
		
		try {
			
			PrintWriter writerTest = new PrintWriter(System.out);
			PrintWriter writerTestAccepted = null;
			String type = null;
			if(!instanceModel.getRestrictedTypesTarget().isEmpty()) {
				type = instanceModel.getRestrictedTypesTarget().get(0).toString();
			}
			
			i=0;
			j=0;
			long currentTime;
			long initTime = System.currentTimeMillis();
			
			log.info("Concept type : "+type);
			int size = sourceResources.size();
			log.info("SourceResources: " + size);
			
			Set<String> targetTypes = new HashSet<String>();
			
			for(LuceneBackedObjectContextWrapper resSource : sourceResources) {

				j++;
				
				log.info(i + " out of "+size);
			
					Map<IAttribute, List<? extends Object>> values = resSource.getValues();
					valuesByPropertyPath.clear();
					List<Object> tmpList;
					List<String> tmpStringList;
					for(IAttribute attribute : values.keySet()) {
						//if(instanceModel.getVariablePathMapTarget().containsKey(key)) {
						tmpList = (List<Object>)values.get(attribute);
						tmpStringList = new ArrayList<String>();
						valuesByPropertyPath.put(((AtomicAttribute)attribute).getPropertyPath(), tmpStringList);
						for(Object tmp : tmpList) {
							if(tmp instanceof String) {
								tmpStringList.add((String)tmp);
							}
						}
						//}
					}
					
					try {
						
						currentTime = System.currentTimeMillis(); 
						candidateDocs = indexer.findClosestDocuments(valuesByPropertyPath, indexer.getThreshold(), type);
						totalTimeSearch+=(System.currentTimeMillis()-currentTime);
						
					} catch(Exception e) {
						e.printStackTrace();
						candidateDocs = new HashMap<String, Document>();
					}

					comparisons+=candidateDocs.size();

					if(candidateDocs.size()==0) {
						log.debug("Not found: "+resSource.getIndividual().toString());
					}

					for(String tmp : candidateDocs.keySet()) {

						Document doc = candidateDocs.get(tmp);
						
						targetTypes.clear();
						
						
						List<String> types = this.getTypesFromLuceneDocument(doc);
						
						if(!types.contains(type)) continue;
						
						
						resTarget = getTargetFromLuceneDocument(tmp, doc);
						
						for(String tmpType : types) {
							if(tmpType.startsWith(Utils.DBPEDIA_ONTOLOGY_NS)) {
								Utils.addToSetMap(tmpType, resTarget.getIndividual().toString(), this.targetUrisByClass);
							}
						}
						
						if(resTarget==null) {
							continue;
						}
						
						
						j++;
						if(resTarget.getIndividual().toString().equals(resSource.getIndividual().toString())) continue;
						
						pair = new ComparisonPair(resSource, resTarget);
						
						/*if(resTarget.getIndividual().toString().equals("http://dbpedia.org/resource/William_Shakespeare")&&
								((List<String>)resSource.getValuesByAttribute("name").get(0)).contains("Shakespeare")) {
							log.info("here");
						}*/
						
						currentTime = System.currentTimeMillis();
						similarity = instanceModel.getSimilarity(pair);
						pair.setSimilarity(similarity);
						totalTimeComparison+=(System.currentTimeMillis()-currentTime);

						/*if(FusionEnvironment.debug) {
							
							this.writeComparison(writerTest, resSource, resTarget, similarity);
							
						}*/
					
						if(similarity>=threshold) {
							/*if(FusionEnvironment.debug) {
								
								this.writeComparison(writerTestAccepted, resSource, resTarget, similarity);
								
							}*/
					
							if(!candidatePairs.containsKey(resSource)) {
								//log.info(sourceLabel + " : " + targetLabel);
								candList = new ArrayList<ComparisonPair>();
								candidatePairs.put(resSource, candList);
							} else {
								candList = candidatePairs.get(resSource);
							}
							candList.add(pair);
						}
					}
					
					
				i++;
			}

			List<ComparisonPair> tmpList;
			double minmax = 100;
			double top;
			for(LuceneBackedObjectContextWrapper wrapper : candidatePairs.keySet()) {
				tmpList = candidatePairs.get(wrapper);
				top = 0;
				for(ComparisonPair tmpPair :  tmpList) {
					if(tmpPair.getSimilarity()>top) {
						top = tmpPair.getSimilarity();
					}
				}
				if(top<minmax) {
					minmax = top;
				}
			}
			
			log.info("Minimal reasonable threshold: "+minmax);
			log.info("Total time: "+(System.currentTimeMillis()-initTime));
			log.info("Total search time: "+totalTimeSearch);
			log.info("Total comparison time: "+totalTimeComparison);
			
			
		} catch(Exception e) {
			e.printStackTrace();
			return -1;
		}
		return comparisons;
	}
	
	
	private void calculateUnsupervised() {
		// Calculate pseudo precision & recall
		double averageSimilarity = 0;
		double average = 0;
		List<ComparisonPair> pairs;
		double solutionResults = 0.0;
		for(ObjectContextWrapper wrapper : candidatePairs.keySet()) {
			average += candidatePairs.get(wrapper).size();
			pairs = candidatePairs.get(wrapper);
			
			for(ComparisonPair pair : pairs) {
				if(pair.getSimilarity() >= threshold) {
					average ++;
					solutionResults ++;
					averageSimilarity += pair.getSimilarity();
				}
			}
		}
		
		averageSimilarity = averageSimilarity / solutionResults;
		average = average / candidatePairs.size();
		
		double pseudoPrecision = 1 / average;
		double pseudoRecall = ((double)candidatePairs.size()) / sourceResources.size();
		
		log.info("Pseudo precision: "+pseudoPrecision+", pseudo recall: "+pseudoRecall);
		
	}
	
	private void calculateUnsupervisedForClass(String targetType) {
		// Calculate pseudo precision & recall
		double averageSimilarity = 0;
		double average = 0;
		List<ComparisonPair> pairs;
		
		Set<String> validIndividuals = this.targetUrisByClass.get(targetType);
		
		Set<String> coveredSourceIndividuals = new HashSet<String>();
		
		int solutionResults = 0;
		for(ObjectContextWrapper wrapper : candidatePairs.keySet()) {
			pairs = candidatePairs.get(wrapper);
			
			for(ComparisonPair pair : pairs) {
				if(validIndividuals.contains(pair.getTargetInstance().getIndividual().toString())) {
					coveredSourceIndividuals.add(wrapper.getIndividual().toString());
					solutionResults += 1;
					average ++;
					averageSimilarity += pair.getSimilarity();
				}
			}
		}
		
		if(solutionResults>0) {
			averageSimilarity = averageSimilarity / solutionResults;
			average = average / coveredSourceIndividuals.size();
			
			double pseudoPrecision = 1 / average;
			double pseudoRecall = ((double)coveredSourceIndividuals.size()) / sourceResources.size();
		
			log.info("Class: "+targetType+" Pseudo precision: "+pseudoPrecision+", pseudo recall: "+pseudoRecall);
		}
		
	}
	
	/*private IObjectContextWrapper getTargetByUri(String uri) {
		ObjectContextWrapper resource = new LuceneBackedObjectContextWrapper(instanceModel);
		URI ind = FusionEnvironment.getInstance().getMainKbValueFactory().createURI(uri);
		resource.setIndividual(ind);

		String sQuery = instanceModel.serializeQuerySPARQLTarget();
		MySPARQLParser parser = new MySPARQLParser(sQuery);
		parser.bindURI(uri);
		sQuery = parser.getFilteredQuery();
		
		
		try {
			TupleQuery query = FusionEnvironment.getInstance().getMainKbRepositoryConnection().prepareTupleQuery(QueryLanguage.SPARQL, sQuery);
			Calendar before = new GregorianCalendar();
			TupleQueryResult rsSource = query.evaluate();
			Calendar after = new GregorianCalendar();

			try {
				BindingSet tmp;
				while(rsSource.hasNext()) {
					tmp = rsSource.next();
					resource.getPropertiesFromQueryResult(tmp);
				}
			} finally {
				rsSource.close();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		return resource;
	}*/
	
	ObjectContextWrapper getTargetFromLuceneDocument(String uri, Document document) {
		LuceneBackedObjectContextWrapper resource = new LuceneBackedObjectContextWrapper(instanceModel);
		URI ind = FusionEnvironment.getInstance().getMainKbValueFactory().createURI(uri);
		resource.setIndividual(ind);
		resource.getPropertiesFromLuceneIndexedDocument(document, ind);
		return resource;
	}
	
	
	private List<String> getTypesFromLuceneDocument(Document doc) {
		List<String> answer = new ArrayList<String>();
		
		Field[] typeFields = doc.getFields(RDF.TYPE.toString());
		for(int i=0;i<typeFields.length;i++) {
			Field typeField = typeFields[i];
			String tmp = typeField.stringValue().trim();
			if(tmp.indexOf(' ')>=0) {
				StringTokenizer tokenizer = new StringTokenizer(tmp, " ");
				while(tokenizer.hasMoreTokens()) {
					answer.add(tokenizer.nextToken());
				}
			} else {
				answer.add(typeField.stringValue());
			}
		}
		return answer;
	}
	
		
	public List<AtomicMapping> execute(double threshold, ILuceneBlocker indexer) {
		this.threshold = threshold;
		this.indexer = indexer;
		
		sourceResources.clear();
		selectedPairs.clear();
		candidatePairs.clear();
		
		this.sourceQuery = instanceModel.serializeQuerySPARQLSource();
		
		log.info("Loading sources... "+this.instanceModel.getRestrictedTypesSource().get(0));
		fillList(sourceResources, this.sourceResourcesTable, FusionEnvironment.getInstance().getFusionRepositoryConnection());
		log.info("Loading targets... "+this.instanceModel.getRestrictedTypesTarget().get(0));
		log.info("Indexing... "+this.instanceModel.getRestrictedTypesTarget().get(0));
		log.info("Candidate individuals: "+sourceResources.size());
		//((LuceneDiskIndexerAllFields)indexer).collectPropertyNames(this.instanceModel.getRestrictedTypesTarget().get(0));
		log.info("Calculating similarities... "+this.instanceModel.getRestrictedTypesTarget().get(0));
		Calendar calendarBefore = new GregorianCalendar();
		int comparisons = calculateSimilarities();
		Calendar calendarAfter = new GregorianCalendar();
		log.info("Comparisons: "+comparisons);
		log.info("Time cost: "+(calendarAfter.getTimeInMillis()-calendarBefore.getTimeInMillis()));
		
		calculateUnsupervised();
		
		/*for(String type : targetUrisByClass.keySet()) {
			calculateUnsupervisedForClass(type);
		}*/
		
		addToModel();
		writeNotFoundOnes();
		
		return mappings; 
	}
	
	private void writeNotFoundOnes() {
		Map<IAttribute, List<? extends Object>> values = new HashMap<IAttribute, List<? extends Object>>();
		List<? extends Object> valList;
		
		String fileOut = "not-found.xml";
		
		try {
			PrintWriter writer = Utils.openPrintFileWriter(fileOut);
			try {
				writer.println("<?xml version=\"1.0\" encoding=\"windows-1251\"?>");
				writer.println("<DocElement>");
				writer.println("<ResultSet=\"default\">");
				
				String label;
				for(ObjectContextWrapper wrapper : sourceResources) {
					
					
					
					if(!candidatePairs.containsKey(wrapper)) {
						writer.println("<comparison>");
						
						writer.println("\t<instance1>");
						writer.print("\t<uri>");
						writer.print(StringEscapeUtils.escapeXml(wrapper.getIndividual().toString()));
						writer.println("</uri>");
						label = "";
						
						values = wrapper.getValues();
						for(IAttribute key : values.keySet()) {
							if(key.equals("uri")) continue;
							valList = values.get(key);
							for(Object val : valList) {
								label = label+" "+val.toString();
							}
						}
						label = label.trim();
						writer.print("\t<label>");
						writer.print(StringEscapeUtils.escapeXml(label));
						writer.println("</label>");
						writer.println("\t</instance1>");
						
						writer.println("\t<instance2>");
						writer.print("\t<uri>http://dbpedia.org/resource/");
						writer.println("</uri>");
						writer.print("\t<label>");
						writer.println("</label>");
						writer.println("</comparison>");
					}
				}
				writer.println("</ResultSet>");
				writer.println("</DocElement>");
			} finally {
				writer.close();
			}
		
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	

	protected void addToModel() {
		int i;
		ComparisonPair comparator;
		AtomicMapping curAtomicMapping;
		for(IObjectContextWrapper source : candidatePairs.keySet()) {
			selectedPairs = candidatePairs.get(source);
			for(i=0;i<selectedPairs.size();i++) {
				comparator = selectedPairs.get(i);
				curAtomicMapping = createAtomicMappingFromInstanceComparator(comparator);
				if(curAtomicMapping!=null) {
					mappings.add(curAtomicMapping);
					if(comparator.getSimilarity()>=threshold) {
						curAtomicMapping.setAccepted(true);
					}
				}
				
			}
		}
	}
	
	protected AtomicMapping createAtomicMappingFromInstanceComparator(ComparisonPair comparator) {
		AtomicMapping res;
		ObjectContextWrapper resource;
		res = new AtomicMapping();
		String sourceLabel, targetLabel;
		resource = (ObjectContextWrapper)comparator.getSourceInstance();
		res.addIndividual(resource.getIndividual(), true);
		sourceLabel = "";
		for(IAttribute key : resource.getValues().keySet()) {
			if(key instanceof AtomicAttribute) {
				if(((AtomicAttribute) key).getVariableName().equals("uri")) continue;
				if(((AtomicAttribute) key).getPropertyPath().endsWith("name")||((AtomicAttribute) key).getPropertyPath().endsWith("label")||((AtomicAttribute) key).getPropertyPath().endsWith("title")) {
					sourceLabel = resource.getValuesByAttribute(key).get(0).toString();
				}
			}
		}
		res.setSourceLabel(sourceLabel);
		//res.addLabel(resource.getIndividual(), sourceLabel);
		
		resource = (ObjectContextWrapper)comparator.getTargetInstance();
		res.addIndividual(resource.getIndividual(), false);
		targetLabel = "";
		for(IAttribute key : resource.getValues().keySet()) {
			if(key instanceof AtomicAttribute) {
				if(((AtomicAttribute) key).getVariableName().equals("uri")) continue;
				if(((AtomicAttribute) key).getPropertyPath().endsWith("name")||((AtomicAttribute) key).getPropertyPath().endsWith("label")||((AtomicAttribute) key).getPropertyPath().endsWith("title")) {
					targetLabel = resource.getValuesByAttribute(key).get(0).toString();
				}
			}
		}
		
		//res.addLabel(resource.getIndividual(), targetLabel);
		res.setTargetLabel(targetLabel);
		
		res.setSimilarity(comparator.getSimilarity());
		
		return res;
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

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	
	
}
