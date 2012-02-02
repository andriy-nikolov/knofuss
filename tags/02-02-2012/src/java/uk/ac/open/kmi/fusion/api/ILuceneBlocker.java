package uk.ac.open.kmi.fusion.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

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

	public ILuceneSearchStrategy getSearchStrategy();
	
	public ILuceneStore getStoreStrategy();
	
	// public IndexWriter getIndexWriter();
	
	public void commit() throws IOException, FusionException;
}
