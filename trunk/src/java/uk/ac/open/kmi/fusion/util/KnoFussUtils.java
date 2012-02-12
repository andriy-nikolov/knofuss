package uk.ac.open.kmi.fusion.util;

import java.io.File;
import java.io.FileInputStream;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Cell;

import uk.ac.open.kmi.common.utils.OIComparison;
import uk.ac.open.kmi.common.utils.OIComparisonUtils;
import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.common.utils.sparql.SPARQLUtils;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.learning.genetic.mutation.DefaultMutationOperator;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

public class KnoFussUtils {
	
	private static Logger log = Logger.getLogger(KnoFussUtils.class);
	
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
		} else if(filePath.endsWith(".rdf")) {
			try {
				return KnoFussUtils.loadGoldStandardFromAlignAPIRDFFile(filePath);
			} catch(Exception e) {
				log.error("Does not seem to be in the AlignAPI format: ", e);
				return KnoFussUtils.loadGoldStandardFromRDFFile(filePath, RDFFormat.RDFXML);
			}
		} else if(filePath.endsWith(".nt")) {
			return KnoFussUtils.loadGoldStandardFromRDFFile(filePath, RDFFormat.NTRIPLES);
		} else if(filePath.endsWith(".n3")) {
			return KnoFussUtils.loadGoldStandardFromRDFFile(filePath, RDFFormat.N3);
		} else {
			throw new FusionException("Cannot determine the format of the gold standard mappings file: "+filePath);
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
	
	public static Map<String, OIComparison> loadGoldStandardFromRDFFile(String file, RDFFormat format) throws FusionException {
		
		Map<String, OIComparison> goldStandard = new HashMap<String, OIComparison>();
		
		Repository repository;
		RepositoryConnection con = null;
		try {
			repository = new SailRepository(new MemoryStore());
			repository.initialize();
			con = repository.getConnection();
			
			con.add(new FileInputStream(file), "", format);
			
			String sQuery = "SELECT ?x ?y WHERE { " +
					"?x <http://www.w3.org/2002/07/owl#sameAs> ?y . " +
					"}";
			
			TupleQuery query = con.prepareTupleQuery(QueryLanguage.SPARQL, sQuery);
			
			TupleQueryResult res = query.evaluate();
			BindingSet bs;
			
			OIComparison comp;
			
			URI candidateURI, targetURI;
			String itemkey;
			try {
				while(res.hasNext()) {
					bs = res.next();
					candidateURI = (URI)bs.getValue("x");
					targetURI = (URI)bs.getValue("y");
					comp = new OIComparison();
					comp.setCandidateURI(candidateURI.toString());
					comp.setTargetURI(targetURI.toString());
					itemkey = candidateURI.toString()+" : "+targetURI.toString();
					comp.setCorrect(true);
					goldStandard.put(itemkey, comp);					
				}
			} finally {
				res.close();
			}
			return goldStandard;
		} catch(Exception e) {
			throw new FusionException("Could not load gold standard mappings from an RDF file: ", e);
		} finally {
			try {
				if(con!=null) {
					if(con.isOpen()) {
						con.close();
					}
				}
			} catch(OpenRDFException e) {
				throw new FusionException("Could not close temporary repository: ", e);
			}
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
	
	public static String generateQuery(List<String> restrictions, List<IAttribute> attributes, Map<String, String> namespaceMap) {
		StringBuffer queryBuffer = new StringBuffer();
		
		List<String> variables = new ArrayList<String>();
		
		variables.add("uri");
		for(IAttribute attribute : attributes) {
			variables.addAll(attribute.getVariableNames());
		}
		
		queryBuffer.append("SELECT DISTINCT ");
		for(int i=0;i<variables.size();i++) {
			queryBuffer.append("?");
			queryBuffer.append(variables.get(i));
			queryBuffer.append(" ");
		}
		queryBuffer.append(" WHERE { \n");
		for(String restriction : restrictions) {
			queryBuffer.append(SPARQLUtils.expandRestriction(restriction, namespaceMap));
			queryBuffer.append("\n");
		}
		String expandedPath;
		int tmpIndex = 0;
		for(IAttribute attribute : attributes) {
			expandedPath = attribute.writeSPARQLWhereClause(namespaceMap);
			queryBuffer.append(expandedPath);
		}
		
		queryBuffer.append("} \n");
		if(variables.get(0).equals("uri")) {
			queryBuffer.append(" ORDER BY ?uri");
		}
		
		return queryBuffer.toString();
		
	}


}
