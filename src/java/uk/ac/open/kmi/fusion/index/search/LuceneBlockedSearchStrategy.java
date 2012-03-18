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

public class LuceneBlockedSearchStrategy extends AbstractLuceneSearchStrategy {

	String[] fieldNames = {
			"http://kmi.open.ac.uk/fusion/fusion#blockFor"
	};
	
	Logger log = Logger.getLogger(LuceneBlockedSearchStrategy.class);
	
	public LuceneBlockedSearchStrategy(Directory directory) {
		
		super(directory);
	}
	
	@Override
	public void refreshSearcher() {
		super.refreshSearcher();

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
    
	    String uri = searchFieldValues.get("uri").get(0);
		
		Term term = new Term("http://kmi.open.ac.uk/fusion/fusion#blockFor", uri);
		TermQuery query = new TermQuery(term);
		
		TopDocs hits = indexSearcher.search(query, indexSearcher.maxDoc());
		
		if(hits.totalHits>0) {
			Document doc;
			for(int i=0;i<hits.scoreDocs.length;i++) {
				doc = indexSearcher.doc(hits.scoreDocs[i].doc);
				docs.put(doc.get("uri"), doc);
			}
		}
			
		return docs;

	}

	
}
