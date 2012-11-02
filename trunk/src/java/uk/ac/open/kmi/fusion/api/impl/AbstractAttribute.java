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
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.IAttribute;

public abstract class AbstractAttribute extends FusionConfigurationObject implements IAttribute {

	protected AttributeType type;
	protected boolean attributeTypeKnown = false;
	boolean optional = false;
	
	protected static Map<String, AttributeType> attributeTypesByName = new HashMap<String, AttributeType>();
	
	static {
		attributeTypesByName.put("string", AttributeType.NOMINAL_MULTI_TOKEN);
		attributeTypesByName.put("nominal", AttributeType.NOMINAL);
		attributeTypesByName.put("double", AttributeType.CONTINUOUS);
		attributeTypesByName.put("composite", AttributeType.COMPOSITE);
		attributeTypesByName.put("text", AttributeType.LONG_TEXT);
		attributeTypesByName.put("date", AttributeType.DATE);
		attributeTypesByName.put("integer", AttributeType.INTEGER);
	}

	public AbstractAttribute() {
		super();
	}

	public AbstractAttribute(Resource rdfIndividual,
			FusionEnvironment environment) {
		super(rdfIndividual, environment);
	}

	@Override
	protected void readFromPropertyMember(Statement statement)
			throws RepositoryException {
				super.readFromPropertyMember(statement);
				Literal lit;
				if(statement.getPredicate().toString().equals(FusionMetaVocabulary.OPTIONAL)) {
					if(statement.getObject() instanceof Literal) {
						lit = (Literal)statement.getObject();
						this.optional = lit.booleanValue();
					}
				} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.ATTRIBUTE_TYPE)) {
					if(statement.getObject() instanceof Literal) {
						lit = (Literal)statement.getObject();
						String attrTypeStringValue = lit.stringValue();
						if(attributeTypesByName.containsKey(attrTypeStringValue)) {
							this.setType(attributeTypesByName.get(attrTypeStringValue));
						}
					}
				}   
				
			}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public boolean isAttributeTypeKnown() {
		return this.attributeTypeKnown;
	}

	@Override
	public AttributeType getType() {
		return type;
	}

	public void setType(AttributeType type) {
		this.type = type;
		this.attributeTypeKnown = true;
	}


}