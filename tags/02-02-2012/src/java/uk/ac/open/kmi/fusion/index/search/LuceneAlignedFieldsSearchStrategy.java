package uk.ac.open.kmi.fusion.index.search;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermsFilter;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.openrdf.model.vocabulary.RDF;

import uk.ac.open.kmi.common.utils.LuceneUtils;
import uk.ac.open.kmi.fusion.index.AbstractLuceneIndexer;
import uk.ac.open.kmi.fusion.objectidentification.SearchResult;

public class LuceneAlignedFieldsSearchStrategy extends
		AbstractLuceneSearchStrategy {

	public LuceneAlignedFieldsSearchStrategy(Directory directory) {
		super(directory);
	}
	
	

	@Override
	public Map<String, Document> findClosestDocuments(
			Map<String, List<String>> searchFieldValues, String[] searchFields,
			double threshold, String type) throws IOException {
		
		return findClosestDocuments(searchFieldValues, threshold, type);
	}



	@Override
	public Map<String, Document> findClosestDocuments(Map<String, List<String>> fields, double threshold, String type) throws IOException {
		Map<String, Document> docs = new HashMap<String, Document>();
	    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
    
	   
	    
	    try {
	    	for(String key : fields.keySet()) {

	    		if(fields.get(key).isEmpty()) continue;
	    		String queryString = LuceneUtils.getTransducedQuery(getConcatenatedString(fields.get(key)));
	    		if(queryString.equals("")) continue;
	    		Query query = null;

	    		QueryParser queryParser = new QueryParser(Version.LUCENE_30, key, analyzer);
	    		
	    		query = queryParser.parse(queryString);
	    		
	    		TopDocs hits;
	    		
	    		if(type==null) {
	    			hits = indexSearcher.search(query, this.getCutOff());
	    		} else {
	    			TermsFilter filter = new TermsFilter();
	    			filter.addTerm(new Term(RDF.TYPE.toString(), type));
	    		
	    			hits = indexSearcher.search(query, filter, this.getCutOff());
	    		}
			    
	    		
	    			    		
	    		if(hits.totalHits==0) {
	    			continue;
	    		}
      
	    		Document doc;
	    		
	    		for(int i=0;i<hits.scoreDocs.length;i++) {
	    			if((hits.scoreDocs[i].score>=threshold)) {
	    				doc = indexSearcher.doc(hits.scoreDocs[i].doc);
	    				docs.put(doc.get("uri"), doc);

	    			} else {
	    				break;
	    			}
	    		}
	    	}

	    	
	    } catch(ParseException e) {
	    	e.printStackTrace();
	    }
			
		return docs;
	}

	@Override
	public Map<String, SearchResult> findClosestDocumentsWithScores(Map<String, String> fields, double threshold, PrintWriter writer) throws IOException {
		Map<String, SearchResult> docs = new HashMap<String, SearchResult>();

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
	    	//try {
    		//	query = queryParser.parse(getTransduced(queryString));
    		//} catch(ParseException e) {
    		query = queryParser.parse(LuceneUtils.getTransducedQuery(queryString));
    		//}
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

}
