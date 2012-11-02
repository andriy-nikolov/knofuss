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
import java.util.List;
import java.util.Map;

import uk.ac.open.kmi.fusion.api.IAttribute;

public class CompositeAttributeValue {

	Map<IAttribute, List<? extends Object>> attributeValues;
	CompositeAttribute attribute;
	
	public CompositeAttributeValue(CompositeAttribute attribute) {
		this.attribute = attribute;
		attributeValues = new HashMap<IAttribute, List<? extends Object>>();
	}
	
	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();
		List<? extends Object> valueList;
		// Object obj;
		for(IAttribute attr : attribute.getAttributes()) {
			if(attributeValues.containsKey(attr)) {
				valueList = attributeValues.get(attr);
				if(valueList!=null) {
					for(Object obj : valueList) {
						str.append(obj.toString());
						str.append(" ");
					}
				}
			}
		}
		
		return str.toString().trim();
	}

	public CompositeAttribute getAttribute() {
		return attribute;
	}

	public void setAttribute(CompositeAttribute attribute) {
		this.attribute = attribute;
	}

	public Map<IAttribute, List<? extends Object>> getAttributeValues() {
		return attributeValues;
	}

	
	
}
