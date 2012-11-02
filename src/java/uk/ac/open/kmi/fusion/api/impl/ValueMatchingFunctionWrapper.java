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
import java.util.Map;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.ICustomValueMatchingFunction;

public class ValueMatchingFunctionWrapper extends FusionConfigurationObject {

	public static final String TYPE_URI = FusionMetaVocabulary.VALUE_MATCHING_FUNCTION;
	
	
	ICustomValueMatchingFunction<? extends Object> impl = null;
	String implementingClass;
	
	Map<String, String> properties;
	
	public ValueMatchingFunctionWrapper() {
		init();
	}
	
	

	public ValueMatchingFunctionWrapper(Resource rdfIndividual,
			FusionEnvironment environment) {
		super(rdfIndividual, environment);
		init();
	}

	private void init() {
		properties = new HashMap<String, String>();
	}



	@Override
	protected void readFromPropertyMember(Statement statement)
			throws RepositoryException {
		super.readFromPropertyMember(statement);
		
		if(statement.getPredicate().toString().equals(FusionMetaVocabulary.HAS_IMPLEMENTING_CLASS)) {
			this.implementingClass = ((Literal)statement.getObject()).stringValue();
		} else {
			String key = statement.getPredicate().toString();
			if(!(key.startsWith(RDF.NAMESPACE)||key.startsWith(RDFS.NAMESPACE))) { 
				Value res = statement.getObject();
				if(res instanceof Literal) {
					properties.put(key, ((Literal)res).stringValue());
				} else if(res instanceof URI) {
					properties.put(key, ((URI)res).toString());
				} 
			}
		}
	}
	
	public boolean isIdenticalTo(FusionConfigurationObject dataInstance) {
		if(dataInstance instanceof ValueMatchingFunctionWrapper) {
			if(this.implementingClass.equals(((ValueMatchingFunctionWrapper)dataInstance).getImplementingClass())) 
					return true;
		}
		return false;
	}

	public String getImplementingClass() {
		return implementingClass;
	}

	public void setImplementingClass(String implementingClass) {
		this.implementingClass = implementingClass;
	}
	
	public ICustomValueMatchingFunction<? extends Object> getImplementation() {
		try {
			if(impl==null) {
				impl = (ICustomValueMatchingFunction<? extends Object>)Class.forName(implementingClass).getConstructor().newInstance();
				impl.setFunctionDescriptor(this);
			}
			return impl;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Map<String, String> getProperties() {
		return properties;
	}
	
	
	
}
