package uk.ac.open.kmi.fusion.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;

import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IDatasetMatchingMethod;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.AtomicMapping;
import uk.ac.open.kmi.fusion.api.impl.AtomicAttribute;
import uk.ac.open.kmi.fusion.api.impl.AttributeType;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.api.impl.FusionMethodWrapper;
import uk.ac.open.kmi.fusion.api.impl.valuematching.SetDifferenceValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.valuematching.ValueMatchingFunctionFactory;
import uk.ac.open.kmi.fusion.util.KnoFussUtils;

public class OneToOneFilter implements IDatasetMatchingMethod {

	private static class AtomicMappingSimilarityComparator implements Comparator<AtomicMapping> {

		@Override
		public int compare(AtomicMapping o1, AtomicMapping o2) {
			return (int)Math.signum(o1.getSimilarity()-o2.getSimilarity());
		}
		
	}
	
	private static class DoubtfulSetSimilarityComparator implements Comparator<Set<AtomicMapping>> {

		@Override
		public int compare(Set<AtomicMapping> o1, Set<AtomicMapping> o2) {
			return o2.size() - o1.size();
		}
		
	}
	
	private static final AtomicMappingSimilarityComparator mappingComparator = new AtomicMappingSimilarityComparator();
	// private static final double epsilon = 0.000000000001;
	private static final double epsilon = 0.000000000000001;
	
	private Set<String> almostSameSourceInstances;
	private Set<String> almostSameTargetInstances;
	
	private Set<Set<AtomicMapping>> almostSameMappingSets;
	
	private Map<String, String> profiles;
	private Map<String, Map<String, String>> geoProfiles;
	
	private FusionMethodWrapper descriptor;
	
	private int countAlmostSame = 0;
	private int allWrongCases = 0;
	private int countDiscriminativeCases = 0;
	private int countChooseBestErrors = 0;
	private int countPreferredWorseErrors = 0;
	private int countNonDiscErrors = 0;
	
	private IAttribute composedAttribute;
	
	Logger log = Logger.getLogger(OneToOneFilter.class);
	
	public OneToOneFilter() {
		almostSameMappingSets = new HashSet<Set<AtomicMapping>>();
		almostSameSourceInstances = new HashSet<String>();
		almostSameTargetInstances = new HashSet<String>();
		
		profiles = new HashMap<String, String>();
		geoProfiles = new HashMap<String, Map<String, String>>();
		
		composedAttribute = new AtomicAttribute("", AttributeType.LONG_TEXT);
	}
	
	

	@Override
	public void setMethodDescriptor(FusionMethodWrapper descriptor) {
		this.descriptor = descriptor;		
	}



	@Override
	public FusionMethodWrapper getDescriptor() {
		return this.descriptor;
	}



	@Override
	public List<AtomicMapping> refineMappings(ApplicationContext context, List<AtomicMapping> mappingsToFilter) {
		
		Map<String, Set<AtomicMapping>> mappingsByUri = new HashMap<String, Set<AtomicMapping>>();
		
		for(AtomicMapping mapping : mappingsToFilter) {
			Utils.addToSetMap(mapping.getSourceIndividual().toString(), mapping, mappingsByUri);
		}
		int removedGoodOnes = 0;
		List<Set<AtomicMapping>> doubtfulSetList = new ArrayList<Set<AtomicMapping>>();
		
		Set<AtomicMapping> doubtfulMappings;
		
		for(String sourceUri : mappingsByUri.keySet()) {
			doubtfulMappings = mappingsByUri.get(sourceUri);
			if(doubtfulMappings.size()>1) {
				doubtfulSetList.add(doubtfulMappings);
				/*almostSameMappingSets.add(doubtfulMappings);
				for(AtomicMapping doubtfulMapping : doubtfulMappings) {
					almostSameSourceInstances.add(doubtfulMapping.getSourceIndividual().toString());
					almostSameTargetInstances.add(doubtfulMapping.getTargetIndividual().toString());
				}*/
				// mappingsToRemove.addAll(filterDoubtfulMappings(doubtfulMappings));
			}
		}
		
		/*mappingsByUri.clear();
		
		for(AtomicMapping mapping : mappingsToFilter) {
			Utils.addToSetMap(mapping.getTargetIndividual().toString(), mapping, mappingsByUri);
		}
		
		for(String targetUri : mappingsByUri.keySet()) {
			doubtfulMappings = mappingsByUri.get(targetUri);
			if(doubtfulMappings.size()>1) {
				//
				doubtfulSetList.add(doubtfulMappings);
				almostSameMappingSets.add(doubtfulMappings);
				for(AtomicMapping doubtfulMapping : doubtfulMappings) {
					almostSameSourceInstances.add(doubtfulMapping.getSourceIndividual().toString());
					almostSameTargetInstances.add(doubtfulMapping.getTargetIndividual().toString());
				}
				// mappingsToRemove.addAll(filterDoubtfulMappings(doubtfulMappings));
			}
		}*/
		
		mappingsByUri = null;
		
		int countTmp = 0;
		
		for(Set<AtomicMapping> doubtfulSet : doubtfulSetList) {
			
			for(AtomicMapping mapping : doubtfulSet) {
				if( mapping.getTargetIndividual().toString().equals("http://sws.geonames.org/5282825/")&&
						mapping.getSourceIndividual().toString().equals("http://data.nytimes.com/N71660314463312318041")
						) {
					log.info("here: "+doubtfulSet.size());
					countTmp ++;
				}
			}
		}
			
		log.info("Participated in "+countTmp+" doubtful sets");
		
		Set<AtomicMapping> mappingsToRemove = new HashSet<AtomicMapping>();
		DoubtfulSetSimilarityComparator comparator = new DoubtfulSetSimilarityComparator();
		Collections.sort(doubtfulSetList, comparator);
		removedGoodOnes = 0;
		List<AtomicMapping> filteredDoubtfulMappings;
		
		countTmp = 0;
		int countValid = 0;
		for(Set<AtomicMapping> doubtfulSet : doubtfulSetList) {
			
			for(AtomicMapping mapping : doubtfulSet) {
				if( mapping.getTargetIndividual().toString().equals("http://sws.geonames.org/5282825/")&&
						mapping.getSourceIndividual().toString().equals("http://data.nytimes.com/N71660314463312318041")
						) {
					log.info("here: "+doubtfulSet.size());
					countTmp ++;
				}
			}
			
			for(AtomicMapping mapping : doubtfulSet) {
				if(mapping.isCorrect()) {
					countValid ++;
					break;
				}
			}
			
			doubtfulSet.removeAll(mappingsToRemove);
			if(doubtfulSet.size()>1) {
				filteredDoubtfulMappings = filterDoubtfulMappings(doubtfulSet); 
				mappingsToRemove.addAll(filteredDoubtfulMappings);
				for(AtomicMapping mapping : filteredDoubtfulMappings) {
					
					if(mapping.isCorrect()) {
						removedGoodOnes++;
					}
				}
			}
			
		}
		log.info("Valid "+countValid+" almost same sets out of "+doubtfulSetList.size());
		log.info("Participated in "+countTmp+" doubtful sets out of "+doubtfulSetList.size());
		
		log.info("Removed good ones when processing discriminative cases: "+removedGoodOnes);
		
		log.info("Processed discriminative cases: "+countDiscriminativeCases);
		log.info("Out of them all wrong: "+allWrongCases);
		log.info("Made errors: "+this.countChooseBestErrors);
		
		countTmp = 0;
		countValid = 0;
		for(Set<AtomicMapping> almostSameSet : almostSameMappingSets) {
			for(AtomicMapping mapping : almostSameSet) {
				if(mapping.getSourceIndividual().toString().equals("http://data.nytimes.com/N71660314463312318041")) {
					countTmp ++;
					break;
				}
			}
			
			for(AtomicMapping mapping : almostSameSet) {
				if(mapping.isCorrect()) {
					countValid ++;
					break;
				}
			}
		}
		
		log.info("Participated in "+countTmp+" almost same sets out of "+almostSameMappingSets.size());
		
		log.info("Valid "+countValid+" almost same sets out of "+almostSameMappingSets.size());
		
		if(!almostSameMappingSets.isEmpty()) {
			collectProfiles(true);
			collectProfiles(false);
			
			AtomicMapping best;
			removedGoodOnes = 0;
			for(Set<AtomicMapping> almostSameSet : almostSameMappingSets) {
				best = findTheBestMapping(almostSameSet);
				if(best!=null) {
					almostSameSet.remove(best);
				}
				
				for(AtomicMapping mapping : almostSameSet) {
					
					if(mapping.isCorrect()) {
						removedGoodOnes++;
					}
				}
				
				mappingsToRemove.addAll(almostSameSet);
			}
			
			log.info("Removed good ones when processing non-discriminative cases: "+removedGoodOnes);
			
		}
		
		removedGoodOnes = 0;
		for(AtomicMapping mapping : mappingsToRemove) {
		
			if(mapping.isCorrect()) {
				removedGoodOnes++;
			}
		}
		
		mappingsToFilter.removeAll(mappingsToRemove);
		log.info("Processed non-discriminative cases: "+countAlmostSame);
		log.info("Made errors: "+this.countNonDiscErrors);
		
		log.info("Removed good ones in total: "+removedGoodOnes);
		return mappingsToFilter;
	}
	
	private List<AtomicMapping> filterDoubtfulMappings(Set<AtomicMapping> doubtfulMappings) {
		
		List<AtomicMapping> mappingsToRemove = new ArrayList<AtomicMapping>(doubtfulMappings);
		Collections.sort(mappingsToRemove, mappingComparator);	
		AtomicMapping best = mappingsToRemove.get(mappingsToRemove.size()-1); 
		AtomicMapping secondBest = mappingsToRemove.get(mappingsToRemove.size()-2);
		AtomicMapping actuallyCorrect = null;
		
		int j = 0;
		for(AtomicMapping mapping : mappingsToRemove) {
			if(mapping.isCorrect()) {
				actuallyCorrect = mapping;
				j++;
			}
			
			if(mapping.getTargetIndividual().toString().equals("http://sws.geonames.org/5282825/")&&
					mapping.getSourceIndividual().toString().equals("http://data.nytimes.com/N71660314463312318041")
					) {
				log.info("here");
			}
		}
		if(j>1) {
			log.info(j);
		}
		
		if(best.getSimilarity()-secondBest.getSimilarity()>=epsilon) {
			// Let only the best stay
			mappingsToRemove.remove(best);
			countDiscriminativeCases ++;
			if(!best.isCorrect()) {
				countChooseBestErrors ++;
				if(actuallyCorrect!=null) {
					countPreferredWorseErrors ++;
					
					log.error("Preferred an incorrect mapping over the correct one: ");
					log.error("Best sim: "+best.getSimilarity()+" : "+best.toString());
					log.error("Correct sim: "+actuallyCorrect.getSimilarity()+" : "+actuallyCorrect.toString());
				
				} else {
					this.allWrongCases ++;
				}
			}
			
			/*for(AtomicMapping mapping : mappingsToRemove) {
				if(mapping.isCorrect()) {
					log.info("Error: removing an actually correct mapping");
				}
			}*/
			
		} else {
			
			Set<AtomicMapping> almostTheSame = new HashSet<AtomicMapping>();
			
			if(!mappingsToRemove.get(mappingsToRemove.size()-1).equals(best)) {
				log.error("here");
			}
			
			almostTheSame.add(best);
			mappingsToRemove.remove(best);
			
			int limit = mappingsToRemove.size();
			for(int i=0;i<limit;i++) {
				secondBest = mappingsToRemove.get(mappingsToRemove.size()-1);
				if(best.getSimilarity()-secondBest.getSimilarity()<epsilon) {
					almostTheSame.add(secondBest);
					mappingsToRemove.remove(secondBest);
				} else {
					if(mappingsToRemove.contains(actuallyCorrect)) {
						log.info(mappingsToRemove.indexOf(actuallyCorrect));
						log.info("Best similarity: "+best.getSimilarity());
						log.info("Actually correct: "+actuallyCorrect.getSimilarity());
					}
					break;
					
				}
			}
			
			for(AtomicMapping mapping : mappingsToRemove) {
				if(mapping.isCorrect()) {
					log.info("Error: removing an actually correct mapping");
				}
			}
			
			if(almostTheSame.size()>1) {
				for(AtomicMapping mapping : almostTheSame) {
					this.almostSameSourceInstances.add(mapping.getSourceIndividual().toString());
					this.almostSameTargetInstances.add(mapping.getTargetIndividual().toString());
				}
				this.almostSameMappingSets.add(almostTheSame);
			}
			
			
		}
		
		
		return mappingsToRemove;
	}
	
	private AtomicMapping findTheBestMapping(Set<AtomicMapping> mappings) {
		
		// IValueMatchingFunction<String> function = ValueMatchingFunctionFactory.getInstance("l2 levenshtein");
		IValueMatchingFunction<String> function = ValueMatchingFunctionFactory.getInstance("overlap");
		countAlmostSame++;
		//IValueMatchingFunction<String> function = SetDifferenceValueMatchingFunction.getInstance();
		
		AtomicMapping bestMapping = null;
		double bestSim = -1.0;
		double sim = 0;
		AtomicMapping actuallyCorrect = null;
		double actuallyCorrectSim = 0;
		
		Set<URI> sourceIndividuals = new HashSet<URI>();
		Set<URI> targetIndividuals = new HashSet<URI>();
		Boolean sourceIndividualIsACommonOne = null;
		
		boolean useGeoProfiles = true;
		for(AtomicMapping mapping : mappings) {
			if(mapping.getTargetIndividual().toString().equals("http://sws.geonames.org/5282825/")&&
					mapping.getSourceIndividual().toString().equals("http://data.nytimes.com/N71660314463312318041")
					) {
				log.info("here");
			}
			
			sourceIndividuals.add(mapping.getSourceIndividual());
			targetIndividuals.add(mapping.getTargetIndividual());
			
			if(!(this.geoProfiles.containsKey(mapping.getSourceIndividual().toString())
					&&this.geoProfiles.containsKey(mapping.getTargetIndividual().toString()))) {
				useGeoProfiles = false;
			}
			
		}
		
		if(sourceIndividuals.size()==1) {
			sourceIndividualIsACommonOne = new Boolean(true);
		} else if(targetIndividuals.size()==1) {
			sourceIndividualIsACommonOne = new Boolean(false);
		} 
		
		Set<String> set1 = new HashSet<String>();
		Set<String> set2 = new HashSet<String>();
		Set<String> overlapTokens = new HashSet<String>();
		
		double denominator;
		
		Map<AtomicMapping, Double> sims = new HashMap<AtomicMapping, Double>();
		Map<AtomicMapping, Integer> profileSizes = new HashMap<AtomicMapping, Integer>();
		
		for(AtomicMapping mapping : mappings) {
			set1.clear();
			set2.clear();
			
			if(profiles.containsKey(mapping.getSourceIndividual().toString())
					&&profiles.containsKey(mapping.getTargetIndividual().toString())) {
				
				set1.clear();
				set2.clear();
				overlapTokens.clear();
				
				set1.addAll(tokenize(profiles.get(mapping.getSourceIndividual().toString())));
				set2.addAll(tokenize(profiles.get(mapping.getTargetIndividual().toString())));
				overlapTokens.addAll(set1);
				overlapTokens.retainAll(set2);
				
				if(sourceIndividualIsACommonOne!=null) {
					if(sourceIndividualIsACommonOne) {
						denominator = set1.size();
					} else {
						denominator = set2.size();
					}
				} else {
					denominator = Math.min(set1.size(), set2.size());
				}
				
				// sim = function.getSimilarity(composedAttribute, composedAttribute, 
				//		profiles.get(mapping.getSourceIndividual().toString()), 
				//		profiles.get(mapping.getTargetIndividual().toString()));
				
				sim = ((double)overlapTokens.size())/denominator;
				
				if(useGeoProfiles) {
					sim = sim*0.5 + getGeoSimilarity(geoProfiles.get(mapping.getSourceIndividual().toString()), geoProfiles.get(mapping.getTargetIndividual().toString()))*0.5;
				}
				
				sims.put(mapping, sim);
				
				if(mapping.isCorrect()) {
					actuallyCorrect = mapping;
					actuallyCorrectSim = sim;
				}
				
			} else {
				log.error("error: ");
				if(!profiles.containsKey(mapping.getSourceIndividual().toString())) {
					System.out.println("Source not found: "+mapping.getSourceIndividual().toString());
				} else if(!profiles.containsKey(mapping.getTargetIndividual().toString())) {
					System.out.println("Target not found: "+mapping.getTargetIndividual().toString());
				} 
			}
			
			profileSizes.put(mapping, set1.size()+set2.size());
			
			if(sim>=1.0) {
				System.out.println("here");
			}
			
			if((bestMapping==null)) {
				bestMapping = mapping;
				bestSim = sim;
			} else if(sim-bestSim>epsilon) {
				bestMapping = mapping;
				bestSim = sim;
			} else if(Math.abs(sim-bestSim)<=epsilon) {
				if(profileSizes.get(mapping)>profileSizes.get(bestMapping)) {
					bestMapping = mapping;
					bestSim = sim;
				}
			}
		}
		
		if(bestSim>=1.0) {
			System.out.println("here");
		}
		
		if(actuallyCorrect==null) {
			System.out.println("here");
		}
		
		// Count how many still almost the same
		int count = 0;
		for(AtomicMapping mapping : sims.keySet()) {
			sim = sims.get(mapping);
			if(Math.abs(bestSim-sim)<epsilon) {
				count ++;
			}
			
		}
		/*if(count > 1) {
			return null;
		}*/
		
		if(!bestMapping.isCorrect()) {
			if(actuallyCorrect!=null) {
				this.countNonDiscErrors ++;
			}  
			log.error("Preferred an incorrect mapping over the correct one: ");
			log.error("Best sim: "+bestSim);
			log.error("Correct sim: "+actuallyCorrectSim);
			log.error(profiles.get(
					bestMapping.getSourceIndividual().toString()));
			log.error(profiles.get(
					bestMapping.getTargetIndividual().toString()));
			if(actuallyCorrect!=null) {
				log.error(
						profiles.get(actuallyCorrect.getSourceIndividual().toString()));
				log.error(
						profiles.get(actuallyCorrect.getTargetIndividual().toString()));
			} else {
				log.error("");
			}
			log.error("");
			
		}
		
		return bestMapping;
	}
	
	
	private void collectProfiles(boolean isSource) {
		String sQuery = "SELECT ?uri ?property1 ?value WHERE { \n" +
		"?uri ?property1 ?value .  \n" +
		//"UNION \n" +
		//"{ ?uri ?property1 ?tmp . ?tmp ?property2 ?value . } \n" +
		"}";
		
		RepositoryConnection connection;
		Set<String> uris;
		if(isSource) {
			connection = FusionEnvironment.getInstance().getFusionRepositoryConnection();
			uris = this.almostSameSourceInstances;
		} else {
			connection = FusionEnvironment.getInstance().getMainKbRepositoryConnection();
			uris = this.almostSameTargetInstances;
		}
		
		try {
			TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, sQuery);
			
			TupleQueryResult result = query.evaluate();
			BindingSet bs;
			try {
				while(result.hasNext()) {
					bs = result.next();
					if(uris.contains(bs.getValue("uri").toString())) {
						if(bs.getValue("property1").toString().startsWith(Utils.WGS84_NS)) {
							addToGeoProfile(bs.getValue("uri").toString(), bs.getValue("property1").toString(), bs.getValue("value").stringValue());
						} else {
							if(bs.getValue("value") instanceof Literal) {
								addToProfile(bs.getValue("uri").toString(), bs.getValue("value").stringValue());
							} 
						}
					}
				} 
			} finally {
				result.close();
			}
		} catch(Exception e) {
			log.error("Error with query "+sQuery, e);
		}
		
	}
	
	private void addToProfile(String uri, String value) {
		String newVal;
		
		Set<String> alternatives = KnoFussUtils.getAlternativeStringValues(value);
		
		alternatives.add(value);
		
		newVal = "";
		for(String tmp : alternatives) {
			newVal = newVal + " " +tmp;
		}
		newVal = newVal.trim();
		if(this.profiles.containsKey(uri)) {
			newVal = profiles.get(uri)+" "+newVal;
		} 
		this.profiles.put(uri, newVal);
	}
	
	private double getGeoSimilarity(Map<String, String> geoProfile1, Map<String, String> geoProfile2) {
		
		double sim = 0;
		double lat1, lat2, long1, long2;
		double diff;
		if(geoProfile1.containsKey(Utils.WGS84_NS+"long")&&
				geoProfile2.containsKey(Utils.WGS84_NS+"long")&&
				geoProfile1.containsKey(Utils.WGS84_NS+"lat")&&
				geoProfile2.containsKey(Utils.WGS84_NS+"lat")) {
			try {
				long1 = Double.parseDouble(geoProfile1.get(Utils.WGS84_NS+"long"));
				long2 = Double.parseDouble(geoProfile2.get(Utils.WGS84_NS+"long"));
				lat1 = Double.parseDouble(geoProfile1.get(Utils.WGS84_NS+"lat"));
				lat2 = Double.parseDouble(geoProfile2.get(Utils.WGS84_NS+"lat"));
				
				diff = Math.abs(long1-long2)/360+Math.abs(lat1-lat2)/180;
				sim = 1-diff/2;
				
			} catch(Exception s) {
				
			}
			
			// sim = 
		}
		
		return sim;
	}
	
	private void addToGeoProfile(String uri, String property, String value) {
		Map<String, String> map;
		if(this.geoProfiles.containsKey(uri)) {
			map = geoProfiles.get(uri);
		} else {
			map = new HashMap<String, String>();
			this.geoProfiles.put(uri, map);
		}
		map.put(property, value);
	}

	
	static private List<String> tokenize(String val) {
		return tokenize(val, " \t\n\r\f:(),-.");
		
	}
	
	static private List<String> tokenize(String val, String chars) {
		List<String> res = new ArrayList<String>();
		String token;
		StringTokenizer tokenizer = new StringTokenizer(val.toLowerCase(), chars);
		while(tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken();
			res.add(token);
		}
		return res;
	}
	
}
