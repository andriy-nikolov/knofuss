package uk.ac.open.kmi.fusion.util;

import java.io.File;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Cell;

import uk.ac.open.kmi.common.utils.OIComparison;
import uk.ac.open.kmi.common.utils.OIComparisonUtils;
import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

public class KnoFussUtils {
	
	static String stopWords[] = {"the", "a"};
	static Set<String> stopWordsSet = new HashSet<String>();
	
	static {
		for(String s : stopWords) {
			stopWordsSet.add(s);
		}
	}
	
	private KnoFussUtils() {
		
	}
	
	
	
	public static Map<String, OIComparison> loadGoldStandardFromFile(String filePath) throws FusionException {
		if(filePath.endsWith("xml")) {
			return KnoFussUtils.loadGoldStandardFromXMLFile(filePath);
		} else {
			return KnoFussUtils.loadGoldStandardFromAlignAPIRDFFile(filePath);
		}
	}
	
	public static Map<String, OIComparison> loadGoldStandardFromAlignAPIRDFFile(String file) throws FusionException {
		
		Map<String, OIComparison> goldStandard = new HashMap<String, OIComparison>();
		Alignment goldStandardAlignment = new BasicAlignment();
		try {
			AlignmentParser parser = new AlignmentParser(0);
			File fileFrom = new File(file);
			goldStandardAlignment = parser.parse(fileFrom.toURI().toURL().toString());
			String itemkey;
			Iterator<Cell> cells = goldStandardAlignment.iterator();
			OIComparison comp;
			Cell cell;
			while(cells.hasNext()) {
				cell = cells.next();
				comp = new OIComparison();
				comp.setCandidateURI(cell.getObject1AsURI().toString());
				comp.setTargetURI(cell.getObject2AsURI().toString());
				itemkey = comp.getCandidateURI()+" : "+comp.getTargetURI();
				comp.setCorrect(true);
				goldStandard.put(itemkey, comp);
			}
			return goldStandard;
		} catch(Exception e) {
			throw new FusionException("Could not load gold standard mappings from file (OAEI Alignment format): ", e);
		}
	}
	
	public static Map<String, OIComparison> loadGoldStandardFromXMLFile(String file) throws FusionException {
		try {
			
			Map<String, OIComparison> goldStandard = new HashMap<String, OIComparison>();
			Map<String, List<OIComparison>> compById = OIComparisonUtils.loadComparisonsFromFile(new File(file));
			List<OIComparison> comparisons;
			StringBuffer signature;
			
			String itemkey;
			
			for(String key : compById.keySet()) {
				comparisons = compById.get(key);
				for(OIComparison comp : comparisons) {
					itemkey = comp.getCandidateURI()+" : "+comp.getTargetURI();
					goldStandard.put(itemkey, comp);
				
				}
			}
			return goldStandard;
		} catch(Exception e) {
			throw new FusionException("Could not load gold standard mappings from file (XML format): ", e);
		}
	}
	
	public static Set<String> getAlternativeStringValues(String initialValue) {
		Set<String> result = new HashSet<String>();
		// Remove non-english characters
		
		String cleaned = removeDiacriticalMarks(initialValue);
		if(!cleaned.equals(initialValue)) {
			result.add(cleaned);
		}
		
		String strippedBrackets = null;
		
		if(cleaned.contains("(")&&cleaned.endsWith(")")) {
			strippedBrackets = cleaned.substring(0, cleaned.indexOf('(')).trim();
			result.add(strippedBrackets.toLowerCase());
		}
		
		cleaned = cleaned.replace("(", "");
		cleaned = cleaned.replace(")", "");
		
		Map<String, String> abbreviations = FusionEnvironment.getInstance().getAbbreviations();
		List<String> tokens = Utils.tokenizeLine(cleaned.toLowerCase(), " ");
		
		StringBuffer buffer = new StringBuffer();
		boolean changed = false;
		for(String token : tokens) {
			if(abbreviations.containsKey(token)) {
				changed = true;
				buffer.append(abbreviations.get(token));
				buffer.append(" ");
			} else if (stopWordsSet.contains(token)) { 
				changed = true;
			} else if(token.startsWith("(")||token.endsWith(")")) {
				if(token.startsWith("(")&&token.endsWith(")")) {
					token = token.substring(1, token.length()-1);
					if(abbreviations.containsKey(token)) {
						buffer.append(abbreviations.get(token));
						buffer.append(" ");
					}

				}
				changed = true;
				// buffer.append(" ");
			} else {
				buffer.append(token);
				buffer.append(" ");
			}
			
		}
		String unified = buffer.toString().trim();
		if(changed) {
			result.add(unified);
		}
		
		return result;
		// return new HashSet<String>();
		
	}
	
	public static String removeDiacriticalMarks(String string) {
	    return Normalizer.normalize(string, Form.NFD)
	        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}


}
