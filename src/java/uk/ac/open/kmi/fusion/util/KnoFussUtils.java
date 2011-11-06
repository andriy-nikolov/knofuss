package uk.ac.open.kmi.fusion.util;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Cell;

import uk.ac.open.kmi.common.utils.OIComparison;
import uk.ac.open.kmi.common.utils.OIComparisonUtils;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

public class KnoFussUtils {
	
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

}
