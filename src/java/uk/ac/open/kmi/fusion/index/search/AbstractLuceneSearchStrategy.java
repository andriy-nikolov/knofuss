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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.openrdf.model.vocabulary.RDF;

import uk.ac.open.kmi.common.utils.LuceneUtils;
import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.ILuceneBlocker;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.AttributeProfileInDataset;
import uk.ac.open.kmi.fusion.index.LuceneIndexer;
import uk.ac.open.kmi.fusion.util.FusionException;

public abstract class AbstractLuceneSearchStrategy implements
		ILuceneSearchStrategy {

	private int totalNumberOfTerms = 0;
	
	private int cutOff = 300;
	private double threshold = 0.5;
	
	Directory directory = null;
	IndexReader indexReader = null;
	IndexSearcher indexSearcher = null;
	
	Logger log = Logger.getLogger(AbstractLuceneSearchStrategy.class);
	
	protected AbstractLuceneSearchStrategy(Directory directory) {
		this.directory = directory;
	}

	
	@Override
	public Directory getIndexDirectory() {
		return this.directory;
	}

	@Override
	public void setIndexDirectory(Directory indexDirectory) {
		this.directory = indexDirectory;
	}

	@Override
	public void refreshSearcher() {
		if(directory==null) {
			throw new IllegalStateException("Search directory is null!");
		}
		try {
			this.indexReader = IndexReader.open(directory);
			this.indexSearcher = new IndexSearcher(directory);
			// countStopwords();
			
		} catch(IOException e) {
			e.printStackTrace();
		}

	}

	private void countStopwords() {
		
		Comparator<Term> termComparator = new Comparator<Term>() {

			@Override
			public int compare(Term o1, Term o2) {
				try {
					int df1 = indexReader.docFreq(o1);
					int df2 = indexReader.docFreq(o2);
					
					return df2-df1;
				} catch(Exception e) {
					e.printStackTrace();
					return 0;
				}
				
			}
			
		};
		
		try {
			List<Term> terms = new ArrayList<Term>();			
			TermEnum termEnum = indexReader.terms();
			
			while(termEnum.next()) {
				terms.add(termEnum.term());
			}
			termEnum.close();
			
			Collections.sort(terms, termComparator);
			double idfWeight;
			for(int i=0;i<100;i++) {
				idfWeight = Math.log(1+((double)terms.size())/indexReader.docFreq(terms.get(i)));
				System.out.println(terms.get(i).toString()+" : "+idfWeight);
			}
			
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	protected static String getConcatenatedString(List<String> strs) {
		StringBuffer buffer = new StringBuffer();
		StringTokenizer tokenizer;
		String token;
		for(String str : strs) {
			tokenizer = new StringTokenizer(LuceneUtils.getCleanedString(str), " ");
			while(tokenizer.hasMoreTokens()) {
				token = tokenizer.nextToken();
				if(buffer.toString().contains(token)) continue;
				buffer.append(token);
				buffer.append(' ');
			}
			
		}
		return buffer.toString().trim();
	}


	@Override
	public int getCutOff() {
		return cutOff;
	}


	@Override
	public void setCutOff(int cutOff) {
		this.cutOff = cutOff;
	}


	@Override
	public double getThreshold() {
		return threshold;
	}


	@Override
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	@Override
	public Document findByURI(String uri) {
	    try {
		    //Analyzer analyzer = new StandardAnalyzer();
		    Term term = new Term(LuceneIndexer.ID_FIELD_NAME, uri);
	    	Query query = new TermQuery(term);
	    	
	    	TopDocs topDocs = indexSearcher.search(query, 1);
	    	
	    	//if(Math.round(topDocs.scoreDocs[0].score)==1) {
	    	if(topDocs.totalHits>0) {
		    	Document doc = indexSearcher.doc(topDocs.scoreDocs[0].doc);
		    	if(doc.get(LuceneIndexer.ID_FIELD_NAME).equals(uri)) {
		    		return indexSearcher.doc(topDocs.scoreDocs[0].doc);
		    	}
	    	}
	    	
	    } catch(Exception e) {
	    	//FusionGlobals.log.error("Could not open lucene index");
	    	e.printStackTrace();
	    } 
	    return null;
	}
	
	
	
	@Override
	public double getDocumentFrequency(String field, String value) {
		Term term = new Term(field, value);
		try {
			int docFreq = indexSearcher.docFreq(term);
			return ((double)docFreq)/indexSearcher.getIndexReader().numDocs();
		} catch(IOException e) {
			log.error("Could not find the document frequency for term "+value, e);
		}
		return 0;
	}


	@Override
	public int copyRelevantSubsetToBlocker(ILuceneBlocker blocker,
			ApplicationContext context,
			Map<String, AttributeProfileInDataset> targetAttributes)
			throws FusionException {
		
		try {
			
			Set<String> tmpSet = new HashSet<String>();
			// Map<String, AttributeProfileInDataset> tmpMap = new HashMap<String, AttributeProfileInDataset>();
			String type = context.getRestrictedTypesTarget().get(0);
			if(this!=blocker.getSearchStrategy()) {
				blocker.openIndex();
				log.info("Started blocking");
			}
			
			long time = System.currentTimeMillis();
			
			Term term = new Term(RDF.TYPE.toString(), type);
			TermQuery query = new TermQuery(term);
			
			TopDocs topDocs = indexSearcher.search(query, Math.max(1, indexSearcher.maxDoc()));
				
				
			Document doc;
			int total = 0;
			AttributeProfileInDataset currentProfile;
			List<String> tokens;
			String val;
			for(int i=0;i<topDocs.scoreDocs.length;i++) {
					//if((topDocs.scoreDocs[i].score+0.01)>1.0) {
					tmpSet.clear();
					doc = indexSearcher.doc(topDocs.scoreDocs[i].doc);
						
					List<Fieldable> fields = doc.getFields();
					for(Fieldable f : fields) {
						if(!(f.name().equals(LuceneIndexer.ID_FIELD_NAME)||f.name().equals(RDF.TYPE.toString())||f.name().equals(FusionMetaVocabulary.BLOCK_FOR))) {
							if(targetAttributes.containsKey(f.name())) {
								currentProfile = targetAttributes.get(f.name());
							} else {
								currentProfile = new AttributeProfileInDataset(f.name());
								targetAttributes.put(f.name(), currentProfile);
							}
							if(!tmpSet.contains(f.name())) {
								currentProfile.increaseMentionedIn();
								
								tmpSet.add(f.name());
							}
							currentProfile.increasePropertyCount();
							val = f.stringValue();
							tokens = Utils.splitByStringTokenizer(val, " \t\n\r\f:(),-.");
							currentProfile.setAverageNumberOfTokens(currentProfile.getAverageNumberOfTokens()+tokens.size());
							currentProfile.doTypeChecking(val);
								
						} 
					}
					
					//for(String tmp : tmpSet) {
					//	Utils.increaseCounter(tmp, targetPropertiesMap);
					//}
					if(this!=blocker.getSearchStrategy()) {
						blocker.addDocument(doc, type);
					}
					if((i%10000)==0) {
						log.info("Counted "+i+" documents");
					}
					total++;
					/*} else {
					break;
				}*/
			}
			
			for(Entry<String, AttributeProfileInDataset> entry : targetAttributes.entrySet()) {
				currentProfile = entry.getValue();
				currentProfile.summarize();
			}
				
			log.info("Pre-selecting individuals complete: "+(System.currentTimeMillis()-time)+" ms");
			log.info(total+" instances");
			if(this!=blocker.getSearchStrategy()) {
				log.info("Committing blocker");
				time = System.currentTimeMillis();
				blocker.commit();
				blocker.refreshSearcher();
				blocker.closeIndex();
				log.info("Blocker committed: "+(System.currentTimeMillis()-time)+" ms");
			}
				
			return total;
		
		} catch(IOException e) {
			throw new FusionException("Error when copying relevant instances to the indexer", e);
		}
	}
	
	protected static String formQueryString(Map<String, String> fields) {
		StringBuilder queryStringBuilder = new StringBuilder();
    	
    	for(Entry<String, String> entry : fields.entrySet()) {
    		queryStringBuilder.append(entry.getValue().trim());
    		queryStringBuilder.append(" "); 
    	}
    	
    	return queryStringBuilder.toString();
	}
	
	
}
