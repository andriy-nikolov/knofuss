/* Copyright (c) 2012, Knowledge Media Institute
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   * Neither the name of the <organization> nor the
 *     names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.open.kmi.fusion.index.search;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermsFilter;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;

import uk.ac.open.kmi.common.utils.LuceneUtils;
import uk.ac.open.kmi.fusion.objectidentification.SearchResult;
import uk.ac.open.kmi.fusion.util.OAEIUtils;

public class LuceneAlignedFieldsEnhancedSearchStrategy extends
		AbstractLuceneSearchStrategy {
	
	boolean deletedClasses = false;
	boolean abbreviatedNames = true;
	

	public LuceneAlignedFieldsEnhancedSearchStrategy(Directory directory) {
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
	    	
 	    	BooleanQuery query = new BooleanQuery();
 	    	String val;
	    	for(String key : fields.keySet()) {
	    		if(key.equals(OAEIUtils.IIMB_TBOX_NS+"gender")) continue;
	    		if(fields.get(key).isEmpty()) continue;
	    		String queryString = LuceneUtils.getTransducedQuery(getConcatenatedString(fields.get(key)));
	    		if(queryString.equals("")) continue;
	    		Query tmpquery = null;

	    		QueryParser queryParser = new QueryParser(Version.LUCENE_30, key, analyzer);
	    		
	    		tmpquery = queryParser.parse(queryString);
	    		
	    		query.add(tmpquery, Occur.SHOULD);
	    		
	    		if(key.equals(OAEIUtils.IIMB_TBOX_NS+"name")) {
	    			queryParser = new QueryParser(Version.LUCENE_30, OAEIUtils.IIMB_TBOX_NS+"article", analyzer);
		    		
		    		tmpquery = queryParser.parse(queryString);
		    		
		    		query.add(tmpquery, Occur.SHOULD);
	    			
	    			if(abbreviatedNames) {
		    			val = LuceneUtils.getAbbreviation(fields.get(key).get(0));
		    			// tmpquery = queryParser.parse(val);
		    			tmpquery = new TermQuery(new Term(key, val));
		    			query.add(tmpquery, Occur.SHOULD);
	    			}
	    		}
	    	}

	    	TopDocs hits;
    		String selectedType = type;
    		if((deletedClasses)&&(type.contains("IIMB"))) {
    			selectedType = OWL.NAMESPACE+"NamedIndividual";
    		}
    		
    		if(selectedType==null) {
    			hits = indexSearcher.search(query, this.getCutOff());
    		} else {
    			TermsFilter filter = new TermsFilter();
    			filter.addTerm(new Term(RDF.TYPE.toString(), selectedType));
    		
    			hits = indexSearcher.search(query, filter, this.getCutOff());
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
	    	
	    	String queryString = formQueryString(fields);
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

	public void setDeletedClasses(boolean deletedClasses) {
		this.deletedClasses = deletedClasses;
	}

	public void setAbbreviatedNames(boolean abbreviatedNames) {
		this.abbreviatedNames = abbreviatedNames;
	}

}
