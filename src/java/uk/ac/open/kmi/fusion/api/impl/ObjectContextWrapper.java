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
package uk.ac.open.kmi.fusion.api.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IObjectContextModel;
import uk.ac.open.kmi.fusion.api.IObjectContextWrapper;
import uk.ac.open.kmi.fusion.util.KnoFussUtils;

public class ObjectContextWrapper implements IObjectContextWrapper {
	
	protected URI individual;
	protected IObjectContextModel model;

	public static final String CLASS_URI = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"ObjectContextWrapper";
	
	protected Map<IAttribute, List<? extends Object>> valueTable;
	
	public ObjectContextWrapper() {
		super();
		valueTable = new HashMap<IAttribute, List<? extends Object>>();
	}
	
	public ObjectContextWrapper(ObjectContextModel model) {
		this();
		this.model = model;
		model.addInstance(this);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.fusion.api.impl.IObjectContextWrapper#getModel()
	 */
	@Override
	public IObjectContextModel getModel() {
		return model;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.fusion.api.impl.IObjectContextWrapper#setModel(uk.ac.open.kmi.fusion.api.impl.ObjectContextModel)
	 */
	@Override
	public void setModel(IObjectContextModel model) {
		this.model = model;
		model.addInstance(this);
	}

		
	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.fusion.api.impl.IObjectContextWrapper#getIndividual()
	 */
	@Override
	public URI getIndividual() {
		return individual;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.fusion.api.impl.IObjectContextWrapper#setIndividual(org.openrdf.model.URI)
	 */
	@Override
	public void setIndividual(URI individual) {
		this.individual = individual;
	}

	public boolean getPropertiesFromQueryResult(BindingSet queryResult, URI individual) {
		this.individual = individual;
		return getPropertiesFromQueryResult(queryResult);
	}

	public boolean getPropertiesFromQueryResult(BindingSet queryResult) {

		if(this.individual==null) {
			this.individual = (URI)queryResult.getValue("uri");
		} else if(!this.individual.toString().equals(((URI)queryResult.getValue("uri")).toString())) {
			return false;
		}
		
		String val;
		
		IAttribute attribute;
		
		Value rdfValue;
		for(String curVar : queryResult.getBindingNames()) {
			
			rdfValue = queryResult.getValue(curVar);
			
			if(rdfValue instanceof Literal) {
			
				val = rdfValue.stringValue();
				
				if(val.trim().startsWith("\""))
					val = val.trim().substring(1);
				if(val.trim().endsWith("\"")) {
					val = val.trim().substring(0, val.length()-1);
				}
				if(val.contains("^^")) {
					val = val.substring(0, val.indexOf("^^"));
				}
				if(val.contains("\"@en")) {
					val = val.substring(0, val.indexOf("\"@en"));
				}
				if(val.trim().endsWith("\"")) {
					val = val.trim().substring(0, val.length()-1);
				}
				
				attribute = model.getSourceAttributeByVarName(curVar);
				if(attribute!=null) {
					addValue(attribute, val);
				}
			}
			
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addValue(IAttribute attribute, Object value) {
		List<Object> tmpValueSet;
		if(this.valueTable.containsKey(attribute)) {
			tmpValueSet = (List<Object>)this.valueTable.get(attribute);
		} else {
			tmpValueSet = new LinkedList<Object>();
			this.valueTable.put(attribute, tmpValueSet);
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

	public Map<IAttribute, List<? extends Object>> getValues() {
		return valueTable;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.fusion.api.impl.IObjectContextWrapper#getValuesById(java.lang.String)
	 */
	@Override
	public List<? extends Object> getValuesByAttribute(IAttribute attribute) {
		
		return attribute.getValuesHavingAttributes(valueTable);

	}
	
	
}
