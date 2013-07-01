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
package uk.ac.open.kmi.fusion.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.openrdf.model.Statement;
import org.openrdf.query.BindingSet;

import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.index.search.ILuceneSearchStrategy;
import uk.ac.open.kmi.fusion.index.store.ILuceneStore;
import uk.ac.open.kmi.fusion.util.FusionException;

public interface ILuceneBlocker {

	// public static int CUT_OFF = 5;
	// public static double THRESHOLD = 0.7;
	// For BNB: 
	/*public static int CUT_OFF = 25;
	public static double THRESHOLD = 0.3;*/
	
	
	// For OAEI: CUT_OFF = 300, THRESHOLD = 0.05
	
	public void setCutOff(int cutOff);
	public void setThreshold(double threshold);
	
	public int getCutOff();
	public double getThreshold();

	public Set<String> findClosest(Map<String, List<String>> fields, double threshold)	throws IOException;

	public Map<String, Document> findClosestDocuments(Map<String, List<String>> fields, double threshold,
			String type) throws IOException;
	
	public Map<String, Document> findClosestDocuments(
			Map<String, List<String>> searchFieldValues, String[] searchFields, double threshold, 
			String type) throws IOException;
	
	public Document findByURI(String uri);

	public void closeIndex() throws FusionException;

	public void openIndex() throws FusionException;

	public void createIndex() throws FusionException;

	public void refreshSearcher() throws FusionException;
	
	public double getDocumentFrequency(String field, String term);

	// public IndexSearcher getIndexSearcher();
	
	public void clearIndex() throws FusionException;
	
	public void addDocument(Document doc, String type)
			throws CorruptIndexException, IOException;
	
	public void indexStatements(List<Statement> statements, ApplicationContext context, String type) throws FusionException;

	public ILuceneSearchStrategy getSearchStrategy();
	
	public ILuceneStore getStoreStrategy();
	
	// public IndexWriter getIndexWriter();
	
	public void commit() throws IOException, FusionException;
	
	public void indexBindingSets(List<BindingSet> bindingSets, ApplicationContext context, String type)
			throws FusionException;
	
}
