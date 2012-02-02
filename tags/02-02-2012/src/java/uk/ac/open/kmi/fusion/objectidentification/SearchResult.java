package uk.ac.open.kmi.fusion.objectidentification;

import org.apache.lucene.document.Document;

public class SearchResult {
	String uri;
	Document document;
	double score;
	
	public SearchResult(String uri, Document document, double score) {
		this.uri = uri;
		this.document = document;
		this.score = score;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
	
}
