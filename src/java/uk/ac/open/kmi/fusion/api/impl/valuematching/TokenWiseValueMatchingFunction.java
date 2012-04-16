package uk.ac.open.kmi.fusion.api.impl.valuematching;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.ILuceneBlocker;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.AttributeType;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;


public class TokenWiseValueMatchingFunction extends TokenBasedSimilarityValueMatchingFunction {

	private Set<String> stopWords;
	//private static Logger log = Logger.getLogger(TokenWiseValueMatchingFunction.class);
	private static AttributeType[][] supportedTypes = {
		{ AttributeType.NOMINAL, AttributeType.NOMINAL },
		{ AttributeType.NOMINAL, AttributeType.NOMINAL_MULTI_TOKEN },
		{ AttributeType.NOMINAL_MULTI_TOKEN, AttributeType.NOMINAL_MULTI_TOKEN }
		//{ AttributeType.NOMINAL_MULTI_TOKEN, AttributeType.LONG_TEXT },
	};

	IValueMatchingFunction<String> embeddedFunction;

	private TokenWiseValueMatchingFunction(IValueMatchingFunction<String> embeddedFunction) {
		super();
		this.embeddedFunction = embeddedFunction;
		this.stopWords = new HashSet<String>();
		loadStopWords();
	}

	private void loadStopWords() {
		String path = "resources/stopWords.txt";
		try {

			BufferedReader reader = Utils.openBufferedFileReader(path);
			String line;
			while((line=reader.readLine())!=null) {
				stopWords.add(line.trim().toLowerCase());
			}

			reader.close();
		} catch(IOException e) {
			e.printStackTrace();
		}

	}

	public IValueMatchingFunction<String> getEmbeddedFunction() {
		return embeddedFunction;
	}

	public void setEmbeddedFunction(IValueMatchingFunction<String> embeddedFunction) {
		this.embeddedFunction = embeddedFunction;
	}

	public static TokenWiseValueMatchingFunction getInstance(IValueMatchingFunction<String> embeddedFunction) {
		return new TokenWiseValueMatchingFunction(embeddedFunction);
	}

	private double getWeight(String term) {
	double weight = 1;

		if(this.stopWords.contains(term.toLowerCase())) {
			weight = 0;
		}
		return weight;
	}

	protected double getScore(IAttribute attr1, IAttribute attr2, List<String> list1, List<String> list2) {

		List<String> small, big;
		List<List<Double>>scores = new ArrayList<List<Double>>();
		if(list1.size()>list2.size()) {
			big = list1;
			small = list2;
		} else {
			big = list2;
			small = list1;
		}
		if(small.size()==0) return 0;
		for(int i=0;i<small.size();i++) {
			List<Double> row = new ArrayList<Double>(big.size());
			for(int j=0;j<big.size();j++) {
				row.add(embeddedFunction.getSimilarity(attr1, attr2, small.get(i).toLowerCase(), big.get(j).toLowerCase()));
			}
			scores.add(row);
		}

		double bestscore = 0;
		int maxi, maxj;
		double res = 0;
		double sumweights = 0;
		double unionscoreForMatched = 0 ;
		double interesectionscore =0;
		double unionScoreForUnmatched = 0 ;
		List<Double> matchedTokens = new ArrayList<Double>() ;

		for(int k=0;k<small.size();k++) {
			bestscore = 0;
			maxi = 0;
			maxj = 0;
			for(int i=0;i<scores.size();i++) {
				for(int j=0;j<scores.get(i).size();j++) {
					if(scores.get(i).get(j)>=bestscore) {
						bestscore = scores.get(i).get(j);
						maxi = i;
						maxj = j;
					}
				}
			}
			for(List<Double>tmp : scores) {
				tmp.remove(maxj);
			}
			scores.remove(maxi);
			//log.info("maxj:"+maxj+"\n");
			matchedTokens.add((double) maxj);
			//res+=bestscore;
			double weight1 =getWeight(small.get(maxi));
			double weight2 = 	getWeight(big.get(maxj)) ;
			double tmpscore = weight1 *weight2 * bestscore;
			interesectionscore += tmpscore ;
			unionscoreForMatched+= tmpscore + (Math.pow(weight1,2) + Math.pow(weight2,2)) * (1.0 - bestscore) ;

		}

		for(int k=0;k<big.size();k++) {
			if(!matchedTokens.contains((double)k)) {
				double Weight_unmatched = Math.pow(getWeight(big.get(k)),2) ;
				unionScoreForUnmatched+= Weight_unmatched;
			}
		}

		/*log.info("list1:"+list1.get(0)+"\n");
		log.info("list2:"+list2.get(0)+"/n");
		log.info("interesectionscore:"+interesectionscore+"/n");
		log.info("unionscoreForMatched:"+unionscoreForMatched+"/n");
		log.info("unionScoreForUnmatched:"+unionScoreForUnmatched+"/n");*/
		res=interesectionscore/(unionscoreForMatched+unionScoreForUnmatched);

		return res;
	}

	@Override
	public String toString() {
		return "tokenwise " + embeddedFunction.toString();
	}

	@Override
	public boolean isSuitableForAttributeTypes(AttributeType attributeType1, AttributeType attributeType2) {
		for(AttributeType[] types : supportedTypes) {
			if(types[0].equals(attributeType1)&&types[1].equals(attributeType2)
					||types[1].equals(attributeType1)&&types[0].equals(attributeType2)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isSuitableForAttributes(IAttribute attribute1, IAttribute attribute2) {
		for(AttributeType[] types : supportedTypes) {
			if(types[0].equals(attribute1.getType())&&types[1].equals(attribute2.getType())
					||types[1].equals(attribute1.getType())&&types[0].equals(attribute2.getType())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public AttributeType[][] suitableForTypes() {
		return supportedTypes;
	}

	@Override
	public void setAttributeFeatures(IAttribute attr1, IAttribute attr2) {
		// TODO Auto-generated method stub

	}



}
