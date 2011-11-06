package uk.ac.open.kmi.fusion.objectidentification.standard;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;


import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Jaro;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;
//import uk.ac.shef.wit.simmetrics.similaritymetrics.MongeElkan;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWaterman;
import com.wcohen.ss.MongeElkan;

public abstract class SimMetricsObjectIdentificationUtils {

	public static double default_threshold = 1.0;
	// public static double lucene_threshold = 0.1;
	
	public static final String SIMMETRICS_OBJECT_IDENTIFICATION_NS = "http://kmi.open.ac.uk/fusion/simmetrics#";
	public static final String SIMMETRICS_THRESHOLD = SIMMETRICS_OBJECT_IDENTIFICATION_NS+"threshold";
	public static final String SIMMETRICS_SIMILARITY_METRICS = SIMMETRICS_OBJECT_IDENTIFICATION_NS+"similarityMetrics";
	
	public static final int SIMMETRICS_JARO = 0;
	public static final int SIMMETRICS_L2_JARO = 1;
	public static final int SIMMETRICS_JARO_WINKLER = 2;
	public static final int SIMMETRICS_L2_JARO_WINKLER = 3;
	public static final int SIMMETRICS_LEVENSHTEIN = 4;
	public static final int SIMMETRICS_L2_LEVENSHTEIN = 5;
	public static final int SIMMETRICS_SMITH_WATERMAN = 6;
	public static final int SIMMETRICS_L2_SMITH_WATERMAN = 7;
	public static final int SIMMETRICS_MONGE_ELKAN = 8;
	public static final int SIMMETRICS_L2_MONGE_ELKAN = 9;
	public static final int SIMMETRICS_AVERAGE_JARO_WINKLER = 10;
	public static final int SIMMETRICS_PERSON_NAME_COMPARISON = 11;
	public static final int SIMMETRICS_I_SUB = 12;
	
	public static int default_metrics = SIMMETRICS_JARO;
	
	public static int metrics = default_metrics;
	
	private static MongeElkan dist;
	
	static char[] special = {':', '*', '+', '[', ']', '\"', '\\', '?', '(', ')', '{', '}'};
	
	static String[] personNameStopWords = {
		"Sr", "Jr", "II", "III", "IV", "Prof", "Dr"
	};
	
	static Set<String> personStopWordSet;
	
	static {
		personStopWordSet = new HashSet<String>();
		for(String stop : personNameStopWords) personStopWordSet.add(stop.toLowerCase());
	}
	
	static public List<String> tokenize(String val) {
		return tokenize(val, " \t\n\r\f:(),-.");
		
	}
	
	static public List<String> tokenize(String val, String chars) {
		List<String> res = new ArrayList<String>();
		String token;
		StringTokenizer tokenizer = new StringTokenizer(val, chars);
		while(tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken();
			/*if(token.toLowerCase().equals("prof"))continue;
			if(token.toLowerCase().equals("dr"))continue;
			if(token.toLowerCase().equals("miss"))continue;
			if(token.toLowerCase().equals("ms"))continue;*/
			res.add(token);
		}
		return res;
	}
	
	static {
		dist = new MongeElkan();
	}

/*	static public double getDirectStringDistance(String s1, String s2) { 
		AbstractStringMetric matcher = new Jaro();
		
		return matcher.getSimilarity(s1.toLowerCase(), s2.toLowerCase());
	} */
	
	static public double getDirectStringDistance(String s1, String s2, AbstractStringMetric matcher) { 
		return matcher.getSimilarity(s1.toLowerCase(), s2.toLowerCase());
	}
	
	
/*	static public double getBestScore(List<String> list1, List<String> list2) {
		// list1 is shorter than list2
		List<String> small, big;
		AbstractStringMetric matcher = new Jaro();
		List<List<Double>>scores = new ArrayList<List<Double>>();
		if(list1.size()>list2.size()) {
			big = list1;
			small = list2;
		} else {
			big = list2;
			small = list1;
		}
		for(int i=0;i<small.size();i++) {
			List<Double> row = new ArrayList<Double>(big.size());
			for(int j=0;j<big.size();j++) {
				row.add((double)matcher.getSimilarity(small.get(i).toLowerCase(), big.get(j).toLowerCase()));
			}
			scores.add(row);
		}
		
		double bestscore = 0;
		int maxi, maxj;
		double res = 0;
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
			res+=bestscore;
		}
		res/=small.size();
		
		return res;
	} */
	
	// Similar to getBestScore but preserves the order
	// The first tokens in two strings must be matchable
	// Number of tokens must be the same +/-1
	static public double getAvgPerWordScore(List<String> list1, List<String> list2, AbstractStringMetric matcher) {
		double res = 0;
		List<String> small, big;
		if(Math.abs(list1.size()-list2.size())>1) return 0.0;
		if(list1.size()>list2.size()) {
			big = list1;
			small = list2;
		} else {
			big = list2;
			small = list1;
		}
		List<Double> scores = new ArrayList<Double>();
		//double bestscore = 0;
		double score = 0;
		//int maxj = 0;
		//int pos = 0;
		for(int i=0;i<small.size();i++) {
			//bestscore = -1;
			//maxj = pos;
			if((i<small.size()-1)||(i==0)) {
				score = (small.get(i).toLowerCase().equals(big.get(i).toLowerCase()))?1.0:0.0;
			} else {
				score = (double)matcher.getSimilarity(small.get(i).toLowerCase(), big.get(i).toLowerCase());
			}
			/*for(int j=pos;j<big.size();j++) {
				if((i==0)&&(j>0))continue;
				score = (double)matcher.getSimilarity(small.get(i).toLowerCase(), big.get(j).toLowerCase());
				if(score>bestscore) {
					bestscore = score;
					maxj = j;
				}
			}*/
			scores.add(score);
			//pos = maxj+1;
		}
		
		for(Double cScore : scores) {
			res+=cScore;
		}
		res/=small.size();
		return res;
	}
	
	static public double getBestScore(List<String> list1, List<String> list2, AbstractStringMetric matcher) {
		List<String> small, big;
		List<List<Double>>scores = new ArrayList<List<Double>>();
		if(list1.size()>list2.size()) {
			big = list1;
			small = list2;
		} else {
			big = list2;
			small = list1;
		}
		for(int i=0;i<small.size();i++) {
			List<Double> row = new ArrayList<Double>(big.size());
			for(int j=0;j<big.size();j++) {
				row.add((double)matcher.getSimilarity(small.get(i).toLowerCase(), big.get(j).toLowerCase()));
			}
			scores.add(row);
		}
		
		double bestscore = 0;
		int maxi, maxj;
		double res = 0;
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
			res+=bestscore;
		}
		res/=small.size();
		
		return res;
	}
	
	static public double getBestScoreMongeElkan(List<String> list1, List<String> list2) {
		List<String> small, big;
		List<List<Double>>scores = new ArrayList<List<Double>>();
		if(list1.size()>list2.size()) {
			big = list1;
			small = list2;
		} else {
			big = list2;
			small = list1;
		}
		for(int i=0;i<small.size();i++) {
			List<Double> row = new ArrayList<Double>(big.size());
			for(int j=0;j<big.size();j++) {
				row.add((double)dist.score(small.get(i).toLowerCase(), big.get(j).toLowerCase()));
			}
			scores.add(row);
		}
		
		double bestscore = 0;
		int maxi, maxj;
		double res = 0;
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
			res+=bestscore;
		}
		res/=small.size();
		
		return res;
	}
	
	static public double getBestScore(String s1, String s2, AbstractStringMetric matcher) {
		List<String> list1 = tokenize(s1);
		List<String> list2 = tokenize(s2);
		return getBestScore(list1, list2, matcher);
	}
	
	static public double getAvgPerWordScore(String s1, String s2, AbstractStringMetric matcher) {
		List<String> list1 = tokenize(s1);
		List<String> list2 = tokenize(s2);
		return getAvgPerWordScore(list1, list2, matcher);
	}
	
	static public double getL2JaroWinkler(String s1, String s2) {
		return getBestScore(s1, s2, new JaroWinkler());
	}
	
	static public double getL2Jaro(String s1, String s2) {
		return getBestScore(s1, s2, new Jaro());
	}
	
	static public double getL2Levenshtein(String s1, String s2) {
		return getBestScore(s1, s2, new Levenshtein());
	}
	
	static public double getL2MongeElkan(String s1, String s2) {
		List<String> list1 = tokenize(s1);
		List<String> list2 = tokenize(s2);
		return getBestScoreMongeElkan(list1, list2);
	}
	
	static public double getDirectMongeElkan(String s1, String s2) {
		return Math.max(dist.score(s1, s2), dist.score(s2, s1));
		//getDirectStringDistance(s1, s2, new MongeElkan());
	}
	
	static public double getDirectJaro(String s1, String s2) {
		return Math.max(getDirectStringDistance(s1, s2, new Jaro()), getDirectStringDistance(getCleaned(s1), getCleaned(s2), new Jaro()));
	}
	
	static public double getDirectJaroWinkler(String s1, String s2) {
		return getDirectStringDistance(s1, s2, new JaroWinkler());
	}
	
	static public double getDirectLevenshtein(String s1, String s2) {
		return getDirectStringDistance(s1, s2, new Levenshtein());
	}
	
	static public double getDirectISub(String s1, String s2) {
	
		return getDirectStringDistance(s1, s2, new ISubDistance());
	}
	
	static public double getDirectSmithWaterman(String s1, String s2) {
		return getDirectStringDistance(s1, s2, new SmithWaterman());
	}
	
	static public double getL2SmithWaterman(String s1, String s2) {
		return getBestScore(s1, s2, new SmithWaterman());
	}
	
	static public double getAvgJaroWinkler(String s1, String s2) {
		return getAvgPerWordScore(s1, s2, new JaroWinkler());
	}
	
	static public double getPersonNameComparison(String s1, String s2) {
		String s1_ = s1;
		String s2_ = s2;
		if(s1.indexOf('(')!=-1) {
			s1_ = s1.substring(0, s1.indexOf('(')); 
		}
		if(s2.indexOf('(')!=-1) {
			s2_ = s2.substring(0, s2.indexOf('(')); 
		}
		if(s1_.equalsIgnoreCase(s2_)) return 1.0;
		List<String> list1 = tokenize(s1_);
		List<String> list2 = tokenize(s2_);
		return getPersonNameComparison(list1, list2, new Jaro());
	}
	
	static public double getPersonNameComparison(List<String> list1, List<String> list2, AbstractStringMetric matcher) {
		
		double res = 0;
		List<String> small, big;
		if(Math.abs(list1.size()-list2.size())>1) return 0.0;
		if(list1.size()>list2.size()) {
			big = getTransducedList(list1);
			small = getTransducedList(list2);
		} else {
			big = getTransducedList(list2);
			small = getTransducedList(list1);
		}
		
		// List<List<Double>>scores = new ArrayList<List<Double>>();
		// last names must be equal
		
		int lastNameIndex1 = getLastNameIndex(small);
		int lastNameIndex2 = getLastNameIndex(big);
		
		if((lastNameIndex1==-1)||(lastNameIndex2==-1)) {
			return 0.0;
		}
		
		int firstNameIndex1 = getFirstNameIndex(small);
		int firstNameIndex2 = getFirstNameIndex(big);
		
		if((firstNameIndex1==-1)||(firstNameIndex2==-1)) {
			return 0.0;
		}
		
		if((firstNameIndex1==lastNameIndex1)||(firstNameIndex2==lastNameIndex2)) {
			return 0.0;
		}
		double firstNameSimilarity = matcher.getSimilarity(small.get(firstNameIndex1), big.get(firstNameIndex2));
		double lastNameSimilarity = matcher.getSimilarity(small.get(lastNameIndex1), big.get(lastNameIndex2)); 
		
		if(lastNameSimilarity<0.98) {
			return 0.0;
		}
		
		double scores[][] = new double[lastNameIndex1][lastNameIndex2];
		
		for(int i=firstNameIndex1+1;i<lastNameIndex1;i++) {
			for(int j=firstNameIndex2+1;j<lastNameIndex2;j++) {
				if(small.get(i).length()>1) {
					if(big.get(j).length()>1) {
						scores[i][j] = matcher.getSimilarity(small.get(i), big.get(j));
					} else {
						scores[i][j] = (small.get(i).startsWith(big.get(j)))?0.99:0.0;
					}
				} else {
					if(big.get(j).length()>1) {
						scores[i][j] = (big.get(j).startsWith(small.get(i)))?0.99:0.0;
					} else {
						scores[i][j] = (big.get(j).equals(small.get(i)))?1.0:0.0;
					}
				}
			}
		}
		
		double bestScore = 0.0;
		int bestAligned = firstNameIndex2+1;
		for(int i=firstNameIndex1+1;i<lastNameIndex1;i++) {
			double max = 0;
			int best = bestAligned;
			for(int j = bestAligned;j<lastNameIndex2;j++) {
				if(scores[i][j]>max) {
					max = scores[i][j];
					best = j;
				}
			}
			bestScore+=max;
			bestAligned = best+1;
		}
		
		bestScore = (bestScore+lastNameSimilarity+firstNameSimilarity+0.99*(big.size()-small.size()))/big.size();
		
		return bestScore;
	}
	
	private static int getLastNameIndex(List<String> list) {
		for(int i=list.size()-1;i>=0;i--) {
			if((list.get(i).length()>1)&&(!personStopWordSet.contains(list.get(i)))) {
				return i;
			}
		}
		return -1;
	}
	
	private static int getFirstNameIndex(List<String> list) {
		for(int i=0;i<list.size();i++) {
			if((list.get(i).length()>1)&&(!personStopWordSet.contains(list.get(i)))) {
				return i;
			}
		}
		return -1;
	}
	
	public static String getLastName(String str) {
		String str_ = str;
		if(str.indexOf("(")!=-1) {
			str_ = str.substring(0, str.indexOf("("));
		}
	
		List<String> list1 = getTransducedList(tokenize(str_, " \t\n\r\f:(),."));
		int index = getLastNameIndex(list1);
		if(index!=-1) {
			return list1.get(index);
		}
		return null;
	}
	
	public static String getCleaned(String str) {
		String str_ = str;
		if(str.indexOf("(")!=-1) {
			str_ = str.substring(0, str.indexOf("("));
		}
		return str_;
	}
	
	public static List<String> getTransducedList(List<String> initial) {
		List<String> res = new ArrayList<String>();
		for(String s : initial) {
			res.add(getTransducedLine(s.toLowerCase()));
		}
		return res;
	}
	
	public static String getTransducedLine(String val) {
		String res = val;
		for(int i=0;i<special.length;i++) {
			while(res.indexOf(special[i])!=-1) {
				res = res.substring(0, res.indexOf(special[i]))+
				res.substring(res.indexOf(special[i])+1);
			}
		}
		  
		return res;
	}
	
	public static int longestSubstr(String s, String t) {
        if (s.isEmpty() || t.isEmpty()) {
                return 0;
        }

        int m = s.length();
        int n = t.length();
        int cost = 0;
        int maxLen = 0;
        int[] p = new int[n];
        int[] d = new int[n];

        for (int i = 0; i < m; ++i) {
                for (int j = 0; j < n; ++j) {
                        // calculate cost/score
                        if (s.charAt(i) != t.charAt(j)) {
                                cost = 0;
                        } else {
                                if ((i == 0) || (j == 0)) {
                                        cost = 1;
                                } else {
                                        cost = p[j - 1] + 1;
                                }
                        }
                        d[j] = cost;

                        if (cost > maxLen) {
                                maxLen = cost;
                        }
                } // for {}

                int[] swap = p;
                p = d;
                d = swap;
        }

        return maxLen;
	}
	
}
