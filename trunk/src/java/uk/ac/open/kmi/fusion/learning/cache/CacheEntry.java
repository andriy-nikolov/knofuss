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
