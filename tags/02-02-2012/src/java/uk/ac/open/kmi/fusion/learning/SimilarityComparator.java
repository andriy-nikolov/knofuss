package uk.ac.open.kmi.fusion.learning;

import java.util.Comparator;
import java.util.Map;

public class SimilarityComparator implements Comparator<Integer> {

	private Map<Integer, Double> similarityMap;
	
	public SimilarityComparator(Map<Integer, Double> similarityMap) {
		this.similarityMap = similarityMap;
	}

	@Override
	public int compare(Integer o1, Integer o2) {
		double sim1 = similarityMap.get(o1);
		double sim2 = similarityMap.get(o2);
		
		if(sim1<sim2) {
			return 1;
		} else if(sim1>sim2) {
			return -1;
		} else {
			return 0;
		}
		
		
	}

}
