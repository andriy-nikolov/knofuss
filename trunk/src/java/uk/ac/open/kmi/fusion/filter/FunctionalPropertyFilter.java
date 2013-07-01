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
package uk.ac.open.kmi.fusion.filter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;

import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.fusion.api.IDatasetMatchingMethod;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.AtomicMapping;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.api.impl.FusionMethodWrapper;

public class FunctionalPropertyFilter implements IDatasetMatchingMethod {

	private FusionMethodWrapper descriptor;
	
	private Map<String, Map<String, Set<String>>> linkedUrisByType;
	private Map<String, Map<String, Set<String>>> linkedUrisByProperty;
	
	private Set<String> mappedSourceUris;
	private Set<String> mappedTargetUris;
	private Set<String> mappingSignatures;
	
	
	Logger log = Logger.getLogger(FunctionalPropertyFilter.class);
	
	public FunctionalPropertyFilter() {
		linkedUrisByType = new HashMap<String, Map<String, Set<String>>>();
		linkedUrisByProperty = new HashMap<String, Map<String, Set<String>>>();
		mappedSourceUris = new HashSet<String>();
		mappedTargetUris = new HashSet<String>();
		mappingSignatures = new HashSet<String>();
	}

	@Override
	public void setMethodDescriptor(FusionMethodWrapper descriptor) {
		this.descriptor = descriptor;

	}

	@Override
	public FusionMethodWrapper getDescriptor() {
		return descriptor;
	}

	@Override
	public List<AtomicMapping> refineMappings(ApplicationContext context,
			List<AtomicMapping> existingMappings) {
		log.info("Collecting mentioned uris");
		this.collectMentionedUris(existingMappings);
		
		log.info("Collecting linked individuals from the source dataset");
		this.collectProfiles(true);
		log.info("Collecting linked individuals from the target dataset");
		this.collectProfiles(false);
		
		Map<String, Set<String>> map1, map2;
		
		Map<String, String> mappingsToAdd = new HashMap<String, String>();
		
		Set<String> set1, set2;
		
		Map<String, Integer> addedMappingsByProperties = new HashMap<String, Integer>();
		Map<String, Integer> addedMappingsByTypes = new HashMap<String, Integer>();
		
		for(AtomicMapping mapping : existingMappings) {
			map1 = this.linkedUrisByProperty.get(mapping.getSourceIndividual().toString());
			map2 = this.linkedUrisByProperty.get(mapping.getTargetIndividual().toString());
			for(Entry<String, Set<String>> entry : map1.entrySet()) {
				if(entry.getValue().size()==1) {
					set1 = entry.getValue();
					for(String uri1 : set1) {
						if(map2.containsKey(entry.getKey())) {
							if(map2.get(entry.getKey()).size()==1) {
								set2 = map2.get(entry.getKey());
								for(String uri2 : set2) {
									if(!this.mappingSignatures.contains(uri1+" : "+uri2)) {
										mappingsToAdd.put(uri1, uri2);
										this.mappingSignatures.add(uri1+" : "+uri2);
										Utils.increaseCounter(entry.getKey(), addedMappingsByProperties);
									}
								}
								
							}
						}
					}
				}
			}
			
			map1 = this.linkedUrisByType.get(mapping.getSourceIndividual().toString());
			map2 = this.linkedUrisByType.get(mapping.getTargetIndividual().toString());
			for(Entry<String, Set<String>> entry : map1.entrySet()) {
				if(entry.getValue().size()==1) {
					set1 = entry.getValue();
					for(String uri1 : set1) {
						if(map2.containsKey(entry.getKey())) {
							if(map2.get(entry.getKey()).size()==1) {
								set2 = map2.get(entry.getKey());
								for(String uri2 : set2) {
									if(!this.mappingSignatures.contains(uri1+" : "+uri2)) {
										mappingsToAdd.put(uri1, uri2);
										this.mappingSignatures.add(uri1+" : "+uri2);
										Utils.increaseCounter(entry.getKey(), addedMappingsByTypes);
									}
								}
								
							}
						}
					}
				}
			}
		}
		
		for(Entry<String, String> entry : mappingsToAdd.entrySet()) {
			AtomicMapping mapping = new AtomicMapping(this.descriptor);
			
			mapping.setSourceIndividual(FusionEnvironment.getInstance().getFusionKbValueFactory().createURI(entry.getKey()));
			mapping.setTargetIndividual(FusionEnvironment.getInstance().getMainKbValueFactory().createURI(entry.getValue()));
			
			mapping.setSimilarity(1.0);
			mapping.setConfidence(1.0);
			
			existingMappings.add(mapping);
			
		}
		
		for(Entry<String, Integer> entry : addedMappingsByProperties.entrySet()) {
			log.info("Added "+entry.getValue()+" by property "+entry.getKey());
		}
		
		for(Entry<String, Integer> entry : addedMappingsByTypes.entrySet()) {
			log.info("Added "+entry.getValue()+" by type "+entry.getKey());
		}
		
		log.info("Added "+mappingsToAdd.size()+" in total");
		
		
		return existingMappings;
	}
	
	private void collectMentionedUris(List<AtomicMapping> mappings) {
		for(AtomicMapping mapping : mappings) {
			mappedSourceUris.add(mapping.getSourceIndividual().toString());
			mappedTargetUris.add(mapping.getTargetIndividual().toString());
			mappingSignatures.add(mapping.getSourceIndividual().toString()+" : "+mapping.getTargetIndividual().toString());
		}
	}
	
	private void collectProfiles(boolean isSource) {
		String sQuery = "SELECT ?uri1 ?type1 ?property1 ?uri2 ?type2 WHERE { \n" +
		"?uri1 ?property1 ?uri2 . \n " +
		"?uri1 a ?type1 . \n" +
		"?uri2 a ?type2 . \n" +
		"}";
		
		String sQuery2 = "SELECT ?uri1 ?type1 ?property1 ?tmp ?uri2 ?type2 WHERE { \n" +
		"{ ?uri1 ?property1 ?tmp . \n " +
		"?tmp ?property2 ?uri2 . " +
		"?uri1 a ?type1 . \n" +
		"?uri2 a ?type2 . \n" +
		"}";
		
		RepositoryConnection connection;
		Set<String> uris;
		
		if(isSource) {
			connection = FusionEnvironment.getInstance().getFusionRepositoryConnection();
			uris = this.mappedSourceUris;
		} else {
			connection = FusionEnvironment.getInstance().getMainKbRepositoryConnection();
			uris = this.mappedTargetUris;
		}
		
		try {
			TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, sQuery);
			
			TupleQueryResult result = query.evaluate();
			BindingSet bs;
			
			String uri1, uri2, property, type;
			
			try {
				while(result.hasNext()) {
					bs = result.next();
					uri1 = bs.getValue("uri1").toString();
					uri2 = bs.getValue("uri2").toString();
					if(uris.contains(uri1)) {
						 if(!uris.contains(uri2)) {
							 property = bs.getValue("property1").toString();
							 type = bs.getValue("type2").toString();
							 
							 addToMap(uri1, property, uri2, this.linkedUrisByProperty);
							 addToMap(uri1, type, uri2, this.linkedUrisByType);
						 }
					}
					
					if(uris.contains(uri2)) {
						 if(!uris.contains(uri1)) {
							 property = bs.getValue("property1").toString();
							 type = bs.getValue("type1").toString();
							 
							 addToMap(uri2, property, uri1, this.linkedUrisByProperty);
							 addToMap(uri2, type, uri1, this.linkedUrisByType);
						 }
					}
				} 
			} finally {
				result.close();
			}
			
			/*query = connection.prepareTupleQuery(QueryLanguage.SPARQL, sQuery2);
			
			result = query.evaluate();

			try {
				Value tmp;
				
				while(result.hasNext()) {
					bs = result.next();
					uri1 = bs.getValue("uri1").toString();
					uri2 = bs.getValue("uri2").toString();
					
					tmp = bs.getValue("tmp");
					
					if((tmp instanceof BNode)||
							((tmp instanceof URI)&&(tmp.toString().startsWith(OAEIUtils.IIMB_ADDONS_NS)))) {
						
					}
					if(uris.contains(uri1)) {
						 if(!uris.contains(uri2)) {
							 property = bs.getValue("property1").toString();
							 type = bs.getValue("type2").toString();
							 
							 addToMap(uri1, property, uri2, this.linkedUrisByProperty);
							 addToMap(uri1, type, uri2, this.linkedUrisByType);
						 }
					}
					
					if(uris.contains(uri2)) {
						 if(!uris.contains(uri1)) {
							 property = bs.getValue("property1").toString();
							 type = bs.getValue("type1").toString();
							 
							 addToMap(uri2, property, uri1, this.linkedUrisByProperty);
							 addToMap(uri2, type, uri1, this.linkedUrisByType);
						 }
					}
				} 
			} finally {
				result.close();
			}*/
			
		} catch(Exception e) {
			log.error("Error with query "+sQuery, e);
		}
	}
	
	
	
	private void addToMap(String uri, String typeOrProperty, String linkedUri, Map<String, Map<String, Set<String>>> map) {
		
		Map<String, Set<String>> internalMap;
		if(map.containsKey(uri)) {
			internalMap = map.get(uri);
		} else {
			internalMap = new HashMap<String, Set<String>>();
			map.put(uri, internalMap);
		}
		
		Set<String> internalSet;
		if(internalMap.containsKey(typeOrProperty)) {
			internalSet = internalMap.get(typeOrProperty);
		} else {
			internalSet = new HashSet<String>();
			internalMap.put(typeOrProperty, internalSet);
		}
		
		internalSet.add(linkedUri);
	}

}
