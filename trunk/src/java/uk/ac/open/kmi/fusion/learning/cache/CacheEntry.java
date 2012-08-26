package uk.ac.open.kmi.fusion.learning.cache;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;


import uk.ac.open.kmi.fusion.api.IObjectContextModel;
import uk.ac.open.kmi.fusion.api.IObjectContextWrapper;
import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;
import uk.ac.open.kmi.fusion.learning.CacheEntryBackedObjectContextWrapper;
import uk.ac.open.kmi.fusion.util.KnoFussUtils;

public class CacheEntry {

	private URI uri;
	private int id;
	protected Map<String, List<? extends Object>> valueTable;
	protected MemoryInstanceCache cache;
	private boolean sampled = false;
	
	/*CacheEntry(MemoryInstanceCache cache) {
		this.valueTable = new HashMap<String, List<String>>();
		this.cache = cache;
	}*/
	
	CacheEntry(MemoryInstanceCache cache, URI uri, int id) {
		//this(cache);
		this.valueTable = new HashMap<String, List<? extends Object>>();
		this.cache = cache;
		this.uri = uri;
		this.id = id;
	}
	
	
	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public Map<String, List<? extends Object>> getValueTable() {
		return valueTable;
	}

	public void readPropertiesFromLuceneDocument(Document doc) {
		for(Fieldable f : doc.getFields()) {
			if(f.name().equals("uri"))continue;
			if(f.name().equals(RDF.TYPE.toString())) {
				cache.addCacheEntryIdToTargetTypeTable(this, f.stringValue());
			} else {
				addValue(f.name(), f.stringValue());
			}
		}
	}
	
	public IObjectContextWrapper getObjectContextWrapper(IObjectContextModel model, boolean isSource) {
		
		/*Map<String, String> variablePropertyMap = 
			(isSource)?model.getVariablePathMapSource():model.getVariablePathMapTarget();*/
		IObjectContextWrapper wrapper = new CacheEntryBackedObjectContextWrapper(this, model);

		return wrapper;
	}
	
	public void addValue(String propertyPath, Object value) {
		List<Object> tmpValueSet;
		if(this.valueTable.containsKey(propertyPath)) {
			tmpValueSet = (List<Object>)this.valueTable.get(propertyPath);
		} else {
			tmpValueSet = new LinkedList<Object>();
			this.valueTable.put(propertyPath, tmpValueSet);
		}
		if(!tmpValueSet.contains(value)) {
			tmpValueSet.add(value);
		}
		
		if(value instanceof String) {
			Set<String> alts = KnoFussUtils.getAlternativeStringValues((String)value);
			for(String alt : alts) {
				if(!tmpValueSet.contains(alt)) {
					tmpValueSet.add(alt);
				}
			}
		}
		
	}

	public int getId() {
		return id;
	}

	public MemoryInstanceCache getCache() {
		return cache;
	}


	public boolean isSampled() {
		return sampled;
	}


	public void setSampled(boolean sampled) {
		this.sampled = sampled;
	}
	
	
	
}
