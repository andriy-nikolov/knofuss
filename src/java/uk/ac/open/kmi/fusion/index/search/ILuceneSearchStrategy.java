package uk.ac.open.kmi.fusion.index.search;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.ILuceneBlocker;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.AttributeProfileInDataset;
import uk.ac.open.kmi.fusion.objectidentification.SearchResult;
import uk.ac.open.kmi.fusion.util.FusionException;

public interface ILuceneSearchStrategy {

	public Map<String, Document> findClosestDocuments(
			Map<String, List<String>> fields, double threshold, String type)
			throws IOException;

	public Map<String, SearchResult> findClosestDocumentsWithScores(
			Map<String, String> fields, double threshold, PrintWriter writer)
			throws IOException;
	
	public Map<String, Document> findClosestDocuments(
			Map<String, List<String>> searchFieldValues, String[] searchFields,
			double threshold, String type) throws IOException;

	public Directory getIndexDirectory();

	public void setIndexDirectory(Directory indexDirectory);

	public void refreshSearcher();

	public boolean isFuzzySearchUsed();

	public void setThreshold(double threshold);

	public double getThreshold();

	public void setCutOff(int cutOff);

	public int getCutOff();
	
	public Document findByURI(String uri);

	int copyRelevantSubsetToBlocker(ILuceneBlocker blocker,
			ApplicationContext context,
			Map<String, AttributeProfileInDataset> targetAttributes)
			throws FusionException;

	public double getDocumentFrequency(String field, String term);

}