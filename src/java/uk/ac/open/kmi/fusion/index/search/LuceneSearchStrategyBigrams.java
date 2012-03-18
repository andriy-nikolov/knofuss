package uk.ac.open.kmi.fusion.index.search;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexReader.FieldOption;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermsFilter;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.openrdf.model.vocabulary.RDF;

import uk.ac.open.kmi.common.utils.LuceneUtils;
import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.fusion.api.ILuceneBlocker;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.objectidentification.SearchResult;
import uk.ac.open.kmi.fusion.util.FusionException;

public class LuceneSearchStrategyBigrams extends AbstractLuceneSearchStrategy {

	String[] fieldNames;
	
	Logger log = Logger.getLogger(LuceneSearchStrategyBigrams.class);
	
	public LuceneSearchStrategyBigrams(Directory directory) {
		
		super(directory);
	}
	
	@Override
	public void refreshSearcher() {
		super.refreshSearcher();
		Set<String> fieldNameCollection = new HashSet<String>(this.indexSearcher.getIndexReader().getFieldNames(FieldOption.ALL));
		fieldNameCollection.remove("uri");
		fieldNameCollection.remove(RDF.TYPE.toString());
		this.fieldNames = new String[fieldNameCollection.size()];
		this.fieldNames = fieldNameCollection.toArray(this.fieldNames);
	}
		

	@Override
	public Map<String, Document> findClosestDocuments(Map<String, List<String>> fields, double threshold, String type) throws IOException {
		
		return this.findClosestDocuments(fields, this.fieldNames, threshold, type);
		
	}

	@Override
	public Map<String, SearchResult> findClosestDocumentsWithScores(
			Map<String, String> fields, double threshold, PrintWriter writer)
			throws IOException {
		Map<String, SearchResult> docs = new HashMap<String, SearchResult>();
		//Searcher searcher = new IndexSearcher(indexDirectory);
	    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
    
	    if(writer!=null)
	    	writer.print("Query: ");
	    
	    try {
	    	MultiFieldQueryParser queryParser;
	    	
	    	String[] queryFields = new String[fields.size()];
	    	queryFields = fields.keySet().toArray(queryFields);
	    	
	    	queryParser = new MultiFieldQueryParser(Version.LUCENE_30, queryFields, analyzer);
	    	
	    	
	    	String queryString = "";
	    	
	    	for(String key : fields.keySet()) {
	    		queryString += (fields.get(key).trim()+" "); 
	    	}
	    	Query query = null;

    		query = queryParser.parse(LuceneUtils.getTransducedQuery(queryString));
    		TopDocs hits;
    		hits = indexSearcher.search(query, this.getCutOff());
    		
    		Document doc;
    		SearchResult res;
    		for(int i=0;i<hits.scoreDocs.length;i++) {
    			if((hits.scoreDocs[i].score>=threshold)) {
    				doc = indexSearcher.doc(hits.scoreDocs[i].doc);
    				res = new uk.ac.open.kmi.fusion.objectidentification.SearchResult(doc.get("uri"), doc, (double)hits.scoreDocs[i].score);
    				docs.put(doc.get("uri"), res);
    			} else {
    				break;
    			}
    		}
	    } catch(ParseException e) {
	    	e.printStackTrace();
	    }

		return docs;
	}

	@Override
	public boolean isFuzzySearchUsed() {
		return false;
	}
	
	@Override
	public Map<String, Document> findClosestDocuments(
			Map<String, List<String>> searchFieldValues, String[] searchFields, double threshold, 
			String type) throws IOException {
		
		Map<String, Document> docs = new HashMap<String, Document>();
	    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
    
	    try {
	    	List<String> allFieldValues = new LinkedList<String>();
	    	
	    	String label = null;
	    	
	    	for(String key : searchFieldValues.keySet()) {
	    		allFieldValues.addAll(searchFieldValues.get(key));
	    		if(key.toLowerCase().endsWith("label")&&(searchFieldValues.get(key).size()==1)) {
	    			label = searchFieldValues.get(key).get(0);
	    		}
	    	}
	    	
	    	if(label!=null) {
	    		
	    	}
	    	
	    	String queryString = LuceneUtils.getTransducedQuery(getConcatenatedString(allFieldValues));
	    	/*if(queryString.contains("ukraine")) {
	    		System.out.println("here");
	    	}*/
	    	Query query = null;
    		QueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_30, searchFields, analyzer);
    		if(!queryString.isEmpty()) {
	    		query = queryParser.parse(queryString);
	    				    
	    		TopDocs hits;
	    		
	    		if(type==null) {
	    			hits = indexSearcher.search(query, this.getCutOff());
	    		} else {
	    			TermsFilter filter = new TermsFilter();
	    			filter.addTerm(new Term(RDF.TYPE.toString(), type));
	    			hits = indexSearcher.search(query, filter, this.getCutOff());
	    		}
	    		
	    		double topScore = -1;
	    		
	    		if(hits.totalHits>0) {
		    		Document doc;
			    		
		    		for(int i=0;i<hits.scoreDocs.length;i++) {
		    			if((hits.scoreDocs[i].score>=threshold)) {
		    				if(topScore<0) topScore = hits.scoreDocs[i].score;
		    				doc = indexSearcher.doc(hits.scoreDocs[i].doc);
		    				
		    				doc = indexSearcher.doc(hits.scoreDocs[i].doc);
		    				/*if(Math.abs(hits.scoreDocs[i].score-topScore)>0.1) {
		    					if(label.toLowerCase().equals("bulgaria")) {
		    						System.out.println("here");
		    					}
		    					System.out.println(label+" : "+doc.get("http://www.geonames.org/ontology#name"));
		    					break;
		    				}*/
		    				docs.put(doc.get("uri"), doc);
		    			} else {
		    				break;
		    			}
		    		}
	    		}
    		}
	    } catch(ParseException e) {
	    	e.printStackTrace();
	    }
			
		return docs;

	}

	
}
