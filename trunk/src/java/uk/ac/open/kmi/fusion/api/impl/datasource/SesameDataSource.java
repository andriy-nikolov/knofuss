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
package uk.ac.open.kmi.fusion.api.impl.datasource;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.manager.RepositoryManager;

import uk.ac.open.kmi.common.utils.sesame.owlim.SesameConnectionProfile;
import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.IDataSource;
import uk.ac.open.kmi.fusion.api.IDump;
import uk.ac.open.kmi.fusion.api.ILuceneBlocker;
import uk.ac.open.kmi.fusion.api.IPersistentStore;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.AttributeProfileInDataset;
import uk.ac.open.kmi.fusion.api.impl.FusionConfigurationObject;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.util.FusionException;

public class SesameDataSource extends FusionConfigurationObject implements IDataSource {
	
	SesameConnectionProfile connectionProfile = null;

	String configurationFile;
	String repositoryPath;
	String repositoryName;
	
	boolean refresh = false;
	IDump dump = null;
	
	IPersistentStore intermediateStore = null;
	
	IDataSource embeddingDataSource = null;
	
	public static final String TYPE_URI = FusionMetaVocabulary.SESAME_DATA_SOURCE;
	
	public SesameDataSource(Resource rdfIndividual, FusionEnvironment environment) {
		super(rdfIndividual, environment);
	}
	
	@Override
	public ValueFactory getValueFactory() {
		return connectionProfile.getValueFactory();
	}

	@Override
	public void prepare() throws FusionException {
		try {
			if(connectionProfile==null) {
				RepositoryManager manager = FusionEnvironment.getInstance().getRepositoryManagerFromRegistry(this.repositoryPath); 
				if(manager==null) {
					connectionProfile = SesameConnectionProfile.openConnection(this.repositoryPath, this.configurationFile, this.repositoryName, true);
					manager = connectionProfile.getRepositoryManager();
					FusionEnvironment.getInstance().addRepositoryManagerToRegistry(this.repositoryPath, manager);
				} else {
					connectionProfile = SesameConnectionProfile.openConnection(manager, this.configurationFile, repositoryName, true);
				}
			}
		}  catch(IOException e) {
			throw new FusionException("Could not open OWLIM repository: ", e);
		} catch (OpenRDFException e) {
			throw new FusionException("Could not open OWLIM repository: ", e);
		}
		// refresh if there is a need ...
		if((dump!=null)&&(refresh)) {
			try {
				getConnection().clear();
				dump.loadToRepository(this);
				refresh = false;
			} catch(OpenRDFException e) {
				e.printStackTrace();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		if(this.intermediateStore!=null) {
			intermediateStore.prepare();
		}
	}

	@Override
	public RepositoryConnection getConnection() {
		return connectionProfile.getConnection();
	}

	@Override
	protected void readFromPropertyMember(Statement statement)
			throws RepositoryException {
		super.readFromPropertyMember(statement);
		
		if(statement.getPredicate().toString().equals(FusionMetaVocabulary.CONFIGURATION_FILE)) {
			this.configurationFile = ((Literal)statement.getObject()).stringValue().trim();
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.PATH)) {
			this.repositoryPath = ((Literal)statement.getObject()).stringValue().trim();
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.NAME)) {
			this.repositoryName = ((Literal)statement.getObject()).stringValue().trim();
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.LOAD_FROM)) {
			this.dump = (IDump)FusionEnvironment.getInstance().findConfigurationObjectByID((Resource)statement.getObject());
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.REFRESH)) {
			this.refresh = ((Literal)statement.getObject()).booleanValue();
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.INTERMEDIATE_STORE)) {
			this.intermediateStore = (IPersistentStore)FusionEnvironment.getInstance().findConfigurationObjectByID((Resource)statement.getObject());
			this.intermediateStore.setEmbeddingDataSource(this);
		}
		
	}

	@Override
	public void readFromRDFIndividual(RepositoryConnection connection)
			throws FusionException {
		
		super.readFromRDFIndividual(connection);
	}

	@Override
	public int copyRelevantSubsetToBlocker(ILuceneBlocker blocker, ApplicationContext context, Map<String, AttributeProfileInDataset> targetPropertiesMap) throws FusionException {
		if(intermediateStore!=null) {
			return intermediateStore.copyRelevantSubsetToBlocker(blocker, context, targetPropertiesMap);
			
		} else {
			return 0;
		}
	}	

	@Override
	public IDataSource getEmbeddingDataSource() {
		return embeddingDataSource;
	}

	@Override
	public void setEmbeddingDataSource(IDataSource embeddingDataSource) {
		this.embeddingDataSource = embeddingDataSource;
	}

	@Override
	public void close() throws FusionException {
		try {
			connectionProfile.close();
		} catch(OpenRDFException e) {
			throw new FusionException(e);
		}
	}
	
	

}
