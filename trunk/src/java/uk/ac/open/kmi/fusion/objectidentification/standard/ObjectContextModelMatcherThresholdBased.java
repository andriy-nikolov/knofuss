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
package uk.ac.open.kmi.fusion.objectidentification.standard;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;

import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.ILuceneBlocker;
import uk.ac.open.kmi.fusion.api.IObjectContextWrapper;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.AtomicAttribute;
import uk.ac.open.kmi.fusion.api.impl.AtomicMapping;
import uk.ac.open.kmi.fusion.api.impl.ComparisonPair;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;
import uk.ac.open.kmi.fusion.api.impl.ObjectContextWrapper;
import uk.ac.open.kmi.fusion.objectidentification.LuceneBackedObjectContextWrapper;
import uk.ac.open.kmi.fusion.util.KnoFussDateUtils;
// import uk.ac.open.kmi.fusion.index.LuceneDiskIndexer;
// import uk.ac.open.kmi.fusion.index.LuceneDiskIndexerAllFields;

public class ObjectContextModelMatcherThresholdBased {
	ObjectContextModel instanceModel;
	ApplicationContext applicationContext;
	Set<LuceneBackedObjectContextWrapper> sourceResources;
	Map<String, LuceneBackedObjectContextWrapper> sourceResourcesTable;
	List<ComparisonPair> selectedPairs;
	Map<LuceneBackedObjectContextWrapper, List<ComparisonPair>> candidatePairs;
	ILuceneBlocker indexer;
	double threshold;
	List<AtomicMapping> mappings;
	// boolean multiOntologyCase = false;
	
	String sourceQuery = null;
	
	String notFoundFile = "not-found.txt";
	
	long totalTimeSearch = 0;
	long totalTimeComparison = 0;
	long totalTimeRetrieval = 0;
	
	Map<String, Set<String>> targetUrisByClass;
	
	Set<String> goldStandardPairs = null;
	
	boolean goldStandardAvailable = false;
	
	private static Logger log = Logger.getLogger(ObjectContextModelMatcherThresholdBased.class); 
	
	
	private void init() {
		sourceResources = new HashSet<LuceneBackedObjectContextWrapper>();
		sourceResourcesTable = new HashMap<String, LuceneBackedObjectContextWrapper>();
		candidatePairs = new HashMap<LuceneBackedObjectContextWrapper, List<ComparisonPair>>();
		selectedPairs = new ArrayList<ComparisonPair>();
		mappings = new ArrayList<AtomicMapping>();
		targetUrisByClass = new HashMap<String, Set<String>>();
		
	}
	
	public ObjectContextModelMatcherThresholdBased() {
		init();
	}
	
	public void setGoldStandard(Set<String> goldStandard) {
		this.goldStandardPairs = goldStandard;
		this.goldStandardAvailable = true;
	}
	
	private void fillList(Set<LuceneBackedObjectContextWrapper> resList, Map<String, LuceneBackedObjectContextWrapper> resTable, RepositoryConnection connection) {
		LuceneBackedObjectContextWrapper resource;
		
		String sQuery = instanceModel.serializeQuerySPARQLSource();
				
		try {
			TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, sQuery);
		
			TupleQueryResult rsSource = query.evaluate();
			
			try {
				resource = new LuceneBackedObjectContextWrapper(instanceModel);
				
				int resources = 0;
				
				BindingSet tuple;
				while(rsSource.hasNext()) {
					tuple = rsSource.next();
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
				
				if(resource.getIndividual()!=null) {
					resList.add(resource);
				}
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
		Map<String, Document> candidateDocs;
		Map<String, List<String>> valuesByPropertyPath = new HashMap<String, List<String>>();

		int comparisons = 0;
		ComparisonPair pair;
		
		double tp = 0;
		
		Set<String> missedGoldStandardUris = new HashSet<String>();
		
		try {
			
			if(this.goldStandardAvailable) {
				missedGoldStandardUris.addAll(this.goldStandardPairs);
			}
			
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

				// j++;
				
				log.info(i + " out of "+size);
			
					Map<IAttribute, List<? extends Object>> values = resSource.getValues();
					valuesByPropertyPath.clear();
					
					List<String> tmpStringList;
					for(Entry<IAttribute, List<? extends Object>> entry : values.entrySet()) {

						if(((AtomicAttribute)entry.getKey()).getPropertyPath().startsWith(Utils.WGS84_NS)) {
							continue;
						}
						
						tmpStringList = new ArrayList<String>();
						for(Object tmp : entry.getValue()) {
							if(tmp instanceof String) {
								
								if(KnoFussDateUtils.isDate((String)tmp)) {
									continue;
								}
								
								try {
									Double.parseDouble((String)tmp);
									continue;
								} catch(NumberFormatException e) {
									
								}
								
								tmpStringList.add((String)tmp);
							}
						}
						
						if(!tmpStringList.isEmpty()) {
							valuesByPropertyPath.put(((AtomicAttribute)entry.getKey()).getPropertyPath(), tmpStringList);
						}

					}
					
					try {
						
						currentTime = System.currentTimeMillis();
						
						if(resSource.getIndividual().toString().equals("http://data.linkedevents.org/event/f16352aa-7f1b-473c-8a3d-764757d400cc")) {
							log.debug("");
						}
						
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

					for(Entry<String, Document> entry : candidateDocs.entrySet()) {

						Document doc = entry.getValue();
						
						targetTypes.clear();
						
						j++;
						List<String> types = this.getTypesFromLuceneDocument(doc);
						
						if(!types.contains(type)) {
							// log.info("not comparable: !types.contains(type): "+doc.toString());
							continue;
						}
						
						
						resTarget = getTargetFromLuceneDocument(entry.getKey(), doc);
						
						for(String tmpType : types) {
							if(tmpType.startsWith(Utils.DBPEDIA_ONTOLOGY_NS)) {
								Utils.addToSetMap(tmpType, resTarget.getIndividual().toString(), this.targetUrisByClass);
							}
						}
						
						if(resTarget==null) {
							continue;
						}
						
						
						
						if(resTarget.getIndividual().toString().equals(resSource.getIndividual().toString())) {
							continue;
						}
						
						pair = new ComparisonPair(resSource, resTarget);
						
						if(goldStandardAvailable) {
							if(missedGoldStandardUris.remove(resSource.getIndividual().toString()+" : "+resTarget.getIndividual().toString())) {
								tp++;
							}
						}
						
						currentTime = System.currentTimeMillis();
						similarity = instanceModel.getSimilarity(pair);
						pair.setSimilarity(similarity);
						totalTimeComparison+=(System.currentTimeMillis()-currentTime);
						
				
						if(similarity>=threshold) {
					
							if(!candidatePairs.containsKey(resSource)) {
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
			
			if(this.goldStandardAvailable) {
				double recall = 1-((double)missedGoldStandardUris.size())/goldStandardPairs.size();
				log.info("Blocking recall: "+recall);
				
				recall = tp/goldStandardPairs.size();
				log.info("Blocking recall: "+recall);
			}
			
			log.info("Actually compared: "+j);
			
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
		Map<IAttribute, List<? extends Object>> values;
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
						StringBuilder labelBuilder = new StringBuilder();
						
						values = wrapper.getValues();
						for(Entry<IAttribute, List<? extends Object>> entry : values.entrySet()) {
							
							if(entry.getKey() instanceof AtomicAttribute 
									&& ((AtomicAttribute)entry.getKey()).getVariableName().equals("uri"))
								continue;
							
							valList = entry.getValue();
							for(Object val : valList) {
								labelBuilder.append(" ").append(val.toString());
							}
						}
						label = labelBuilder.toString().trim();
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

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	
	
}
