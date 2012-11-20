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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.common.utils.sparql.SPARQLUtils;
import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.util.FusionException;

public class AtomicAttribute extends AbstractAttribute {

	public static final String TYPE_URI = FusionMetaVocabulary.ATOMIC_ATTRIBUTE;
	
	String propertyPath;
	
	double min = -180.0;
	double max = 180.0;
	String variableName;
	static int attributeNumber = 0;
	
	Map<String, Object> linkedObjects;
	
	private static Logger log = Logger.getLogger(AtomicAttribute.class);

	public AtomicAttribute(String propertyPath) {
		super();
		
		this.setPropertyPath(propertyPath);
		this.variableName = "attr"+(attributeNumber++);
		this.linkedObjects = new HashMap<String, Object>();
	}
	
	public AtomicAttribute(String propertyPath, AttributeType type) {
		this(propertyPath);
		this.setType(type);
		this.attributeTypeKnown = true;
	}
	
	public AtomicAttribute(Resource rdfIndividual, FusionEnvironment environment) {
		super(rdfIndividual, environment);
	}
	
	

	@Override
	public void readFromRDFIndividual(RepositoryConnection connection)
			throws FusionException {
		super.readFromRDFIndividual(connection);
		
		this.propertyPath = SPARQLUtils.presentExpandedTriplesAsPath(this.propertyPath, "uri", this.variableName, FusionEnvironment.getInstance().getNamespaceURITable());
		
	}

	@Override
	protected void readFromPropertyMember(Statement statement)
			throws RepositoryException {
		super.readFromPropertyMember(statement);
		Literal lit;
		if(statement.getPredicate().toString().equals(FusionMetaVocabulary.PROPERTY_PATH)) {
			if(statement.getObject() instanceof Literal) {
				lit = (Literal)statement.getObject();
				this.propertyPath = lit.stringValue();
			}
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.VARIABLE)) {
			if(statement.getObject() instanceof Literal) {
				lit = (Literal)statement.getObject();
				this.variableName = lit.stringValue();
			}
		} 
		
	}

	@Override
	public List<String> getPropertyPaths() {
		List<String> propertyPaths = new ArrayList<String>(1);
		propertyPaths.add(propertyPath);
		return propertyPaths;
	}
	
	@Override
	public List<String> getPropertyPathsAsQueryTriples() {
		List<String> propertyPaths = new ArrayList<String>(1);
		propertyPaths.add(SPARQLUtils.expandPath("uri", propertyPath, this.variableName).replace("(", "").replace(")", ""));
		return propertyPaths;
	}

	public String getPropertyPath() {
		return propertyPath;
	}

	public void setPropertyPath(String propertyPath) {
		this.propertyPath = propertyPath;
	}

	@Override
	public boolean samePropertyPathAs(Object obj) {
		if(obj instanceof AtomicAttribute) {
			return this.getPropertyPath().equals(((AtomicAttribute)obj).getPropertyPath());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.propertyPath.hashCode();
	}

	@Override
	public String toString() {
		return this.getPropertyPath();
	}
	
	
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof AtomicAttribute)
			return this.samePropertyPathAs(obj);
		
		return false;
	}

	public double getMin() {
		return min;
	}
	
	public double getMax() {
		return max;
	}
	
	public void setMin(double min) {
		this.min = min;
	}
	
	public void setMax(double max) {
		this.max = max;
	}

	@Override
	public String writePathAsString() {
		return getPropertyPath();
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	@Override
	public List<String> getVariableNames() {
		List<String> varNames = new ArrayList<String>(1);
		varNames.add(this.variableName);
		return varNames;
	}

	public Map<String, Object> getLinkedObjects() {
		return linkedObjects;
	}
	
	public void addLinkedObject(String key, Object object) {
		this.linkedObjects.put(key, object);
	}

	@Override
	public Map<String, IAttribute> getAtomicAttributesByPropertyPath() {
		Map<String, IAttribute> res = new HashMap<String, IAttribute>();
		res.put(this.propertyPath, this);
		return res;
	}

	@Override
	public Map<String, IAttribute> getAtomicAttributesByVariable() {
		Map<String, IAttribute> res = new HashMap<String, IAttribute>();
		
		res.put(this.variableName, this);
		return res;
	}

	@Override
	public List<? extends Object> getValuesHavingAttributes(
			Map<IAttribute, List<? extends Object>> valueTable) {
		if(valueTable.containsKey(this)) {
			return valueTable.get(this);
		} else {
			return new ArrayList<Object>();
		}
	}

	@Override
	public List<? extends Object> getValuesHavingPropertyPaths(
			Map<String, List<? extends Object>> valueTable) {
		if(valueTable.containsKey(this.getPropertyPath())) {
			return valueTable.get(this.getPropertyPath());
		} else {
			return new ArrayList<Object>();
		}
	}

	@Override
	public String writeSPARQLWhereClause(Map<String, String> namespaceMap) {
		StringBuffer buffer = new StringBuffer();
		if(this.isOptional()) {
			buffer.append("OPTIONAL { ");
		} 
		buffer.append(SPARQLUtils.expandPath("uri", SPARQLUtils.expandRestriction(this.propertyPath, namespaceMap), this.variableName).replace("(", "").replace(")", ""));
		buffer.append(" ");
		
		if(this.isOptional()) {
			buffer.append(" } ");
		} 
		buffer.append("\n");
		return buffer.toString();
	}

	@Override
	public List<IAttribute> dependsOn() {
		return new ArrayList<IAttribute>(0);
	}
	
	
	
}
