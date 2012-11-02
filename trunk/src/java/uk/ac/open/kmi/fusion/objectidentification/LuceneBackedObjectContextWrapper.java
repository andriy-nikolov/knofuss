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
package uk.ac.open.kmi.fusion.objectidentification;

import java.util.Map;

import org.apache.lucene.document.Document;
import org.openrdf.model.URI;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.impl.AtomicAttribute;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;
import uk.ac.open.kmi.fusion.api.impl.ObjectContextWrapper;


public class LuceneBackedObjectContextWrapper extends ObjectContextWrapper {
	
	public LuceneBackedObjectContextWrapper() {
		super();
	}
	
	public LuceneBackedObjectContextWrapper(ObjectContextModel model) {
		super(model);
	}
	
	public boolean getPropertiesFromLuceneIndexedDocument(Document doc, URI individual) {
		
		this.individual = individual; 
		Map<String, IAttribute> variablePropertyMap = model.getTargetAttributesByVarName();
		this.individual = FusionEnvironment.getInstance().getMainKbValueFactory().createURI(doc.get("uri"));
		IAttribute attribute;
		for(String var : variablePropertyMap.keySet()) {
			attribute = ((AtomicAttribute) variablePropertyMap.get(var));
				
			String[] values = doc.getValues(((AtomicAttribute)attribute).getPropertyPath());
				
			if(values==null) continue;
			if(values.length==0) continue;
			for(String val : values) {
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
				this.addValue(attribute, val);
			}
			
		}
		return true;
		
	}
	
}
