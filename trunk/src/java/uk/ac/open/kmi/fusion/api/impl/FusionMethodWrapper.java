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
import uk.ac.open.kmi.fusion.api.IAbstractFusionMethod;

public class FusionMethodWrapper extends FusionConfigurationObject {
	
	public static final int FUSION_METHOD_CAPABILITY_FEATURE_SELECTION = 1;
	public static final int FUSION_METHOD_CAPABILITY_OBJECTIDENTIFICATION = 2;
	public static final int FUSION_METHOD_CAPABILITY_CONFLICT_DETECTION = 3;
	public static final int FUSION_METHOD_CAPABILITY_INCONSISTENCY_RESOLUTION = 4;
	public static final int FUSION_METHOD_CAPABILITY_ONTOLOGY_MATCHING = 5;
	
	double reliability;
	int capability;
	
	Map<Resource, ApplicationContext> applicationContexts;
	String implementingClass;
	IAbstractFusionMethod impl = null;
	Map<String, String> properties;
	
	public static final String TYPE_URI = FusionMetaVocabulary.FUSION_METHOD;
	
	public void init() {
		applicationContexts = new HashMap<Resource, ApplicationContext>();
		properties = new HashMap<String, String>();
	}

	/*public FusionMethodWrapper() {
		super();
		init();
	}*/
	
	public FusionMethodWrapper(Resource rdfIndividual, FusionEnvironment environment) {
		super(rdfIndividual, environment);
		init();
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.fusion.api.impl.IFusionMethodWrapper#getReliability()
	 */
	public double getReliability() {
		return reliability;
	}

	public void setReliability(double reliability) {
		this.reliability = reliability;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.fusion.api.impl.IFusionMethodWrapper#getCapability()
	 */
	public int getCapability() {
		return capability;
	}

	public void setCapability(int capability) {
		this.capability = capability;
	}


	@Override
	protected void readFromPropertyMember(Statement statement) throws RepositoryException {
		super.readFromPropertyMember(statement);
		Resource linkIndividual;
		
		if(statement.getPredicate().toString().equals(FusionMetaVocabulary.HAS_RELIABILITY)) {
			try {
				this.reliability = ((Literal)statement.getObject()).doubleValue();
			} catch(Exception e) {
				this.reliability = 1;				
			}
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.HAS_CAPABILITY)) {
			linkIndividual = (Resource)statement.getObject();
			if(linkIndividual.toString().equals(FusionMetaVocabulary.FEATURE_SELECTION)) capability = FUSION_METHOD_CAPABILITY_FEATURE_SELECTION;
			if(linkIndividual.toString().equals(FusionMetaVocabulary.OBJECT_IDENTIFICATION)) capability = FUSION_METHOD_CAPABILITY_OBJECTIDENTIFICATION;
			if(linkIndividual.toString().equals(FusionMetaVocabulary.CONFLICT_DETECTION)) capability = FUSION_METHOD_CAPABILITY_CONFLICT_DETECTION;
			if(linkIndividual.toString().equals(FusionMetaVocabulary.INCONSISTENCY_RESOLUTION)) capability = FUSION_METHOD_CAPABILITY_INCONSISTENCY_RESOLUTION;
		}  else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.HAS_APPLICATION_CONTEXT)) {
			linkIndividual = (Resource)statement.getObject();
			// ApplicationContext appContext = new ApplicationContext();
			//appContext.readFromRDFIndividual(linkIndividual);
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.HAS_IMPLEMENTING_CLASS)) {
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
		if(dataInstance instanceof FusionMethodWrapper) {
			if(this.implementingClass.equals(((FusionMethodWrapper)dataInstance).getImplementingClass())) 
					return true;
		}
		return false;
	}

	public Map<Resource, ApplicationContext> getApplicationContexts() {
		return applicationContexts;
	}

	public IAbstractFusionMethod getImplementation() {
		try {
			if(impl==null) {
				impl = (IAbstractFusionMethod)Class.forName(implementingClass).getConstructor().newInstance();
				impl.setMethodDescriptor(this);
			}
			return impl;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getImplementingClass() {
		return implementingClass;
	}

	public void setImplementingClass(String implementingClass) {
		this.implementingClass = implementingClass;
	}
	
	public void addApplicationContext(ApplicationContext context) {
		applicationContexts.put(context.getRDFIndividual(), context);
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperty(String property, String value) {
		properties.put(property, value);
	}
	
}
