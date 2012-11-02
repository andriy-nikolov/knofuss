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

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import uk.ac.open.kmi.fusion.api.impl.datasource.FileDump;
import uk.ac.open.kmi.fusion.api.impl.datasource.SesameDataSource;
import uk.ac.open.kmi.fusion.api.impl.datasource.RemoteSPARQLDataSource;
import uk.ac.open.kmi.fusion.api.impl.datasource.SesameMemoryDataSource;
import uk.ac.open.kmi.fusion.api.impl.datasource.SesameNativeDataSource;
import uk.ac.open.kmi.fusion.index.LuceneBlockedDiskIndexer;
import uk.ac.open.kmi.fusion.index.LuceneDiskIndexer;
import uk.ac.open.kmi.fusion.index.LuceneEnhancedDiskIndexer;
import uk.ac.open.kmi.fusion.index.LuceneMemoryIndexer;
import uk.ac.open.kmi.fusion.util.FusionException;

public class FusionConfigurationObjectFactory {

	private FusionConfigurationObjectFactory() {
		// TODO Auto-generated constructor stub
	}
	
	public static FusionConfigurationObject createFromResource(Resource resource, FusionEnvironment environment, RepositoryConnection connection) throws FusionException {
		try {
			RepositoryResult<Statement> types = connection.getStatements(resource, RDF.TYPE, null, true);
			Statement stmt;
			String typeUri;
			try {
				while(types.hasNext()) {
					stmt = types.next();
					if(stmt.getObject() instanceof URI) {
						typeUri = stmt.getObject().toString();
						
						if(typeUri.equals(ApplicationContext.TYPE_URI)) {
							return new ApplicationContext(resource, environment);
						} else if(typeUri.equals(FusionMethodWrapper.TYPE_URI)) {
							return new FusionMethodWrapper(resource, environment);
						} else if(typeUri.equals(ValueMatchingFunctionWrapper.TYPE_URI)) {
							return new ValueMatchingFunctionWrapper(resource, environment);
						} else if(typeUri.equals(TransformationFunctionWrapper.TYPE_URI)) {
							return new TransformationFunctionWrapper(resource, environment);
						} else if(typeUri.equals(SesameDataSource.TYPE_URI)) {
							return new SesameDataSource(resource, environment);
						} else if(typeUri.equals(FileDump.TYPE_URI)) {
							return new FileDump(resource, environment);
						} else if(typeUri.equals(LuceneDiskIndexer.TYPE_URI)) {
							return new LuceneDiskIndexer(resource, environment);
						} else if(typeUri.equals(LuceneMemoryIndexer.TYPE_URI)) {
							return new LuceneMemoryIndexer(resource, environment);
						} else if(typeUri.equals(LuceneEnhancedDiskIndexer.TYPE_URI)) {
							return new LuceneEnhancedDiskIndexer(resource, environment);
						} else if(typeUri.equals(LuceneBlockedDiskIndexer.TYPE_URI)) {
							return new LuceneBlockedDiskIndexer(resource, environment);
						} 
						/*else if(typeUri.equals(LuceneDiskIndexerAllFields.TYPE_URI)) {
							return new LuceneDiskIndexerAllFields(resource, environment);
						} else if(typeUri.equals(LuceneMemoryIndexerAllFields.TYPE_URI)) {
							return new LuceneMemoryIndexerAllFields(resource, environment);
						}*/ else if(typeUri.equals(LinkSession.TYPE_URI)) {
							return new LinkSession(resource, environment);
						} else if(typeUri.equals(VariableComparisonSpecification.TYPE_URI)) {
							return new VariableComparisonSpecification(resource, environment);
						} else if(typeUri.equals(ObjectContextModel.TYPE_URI)) {
							return new ObjectContextModel(resource, environment);
						} else if(typeUri.equals(RemoteSPARQLDataSource.TYPE_URI)) {
							return new RemoteSPARQLDataSource(resource, environment);
						} else if(typeUri.equals(SesameNativeDataSource.TYPE_URI)) {
							return new SesameNativeDataSource(resource, environment);
						} else if(typeUri.equals(SesameMemoryDataSource.TYPE_URI)) {
							return new SesameMemoryDataSource(resource, environment);
						} else if(typeUri.equals(TransformationAttribute.TYPE_URI)) {
							return new TransformationAttribute(resource, environment);
						} if(typeUri.equals(AtomicAttribute.TYPE_URI)) {
							return new AtomicAttribute(resource, environment);
						} if(typeUri.equals(CompositeAttribute.TYPE_URI)) {
							return new CompositeAttribute(resource, environment);
						} 
						
					}
					
				}
			} finally {
				types.close();
			}
		} catch(Exception e) {
			throw new FusionException("Could not create the fusion configuration object from resource: "+resource.stringValue(), e);
		}
		throw new FusionException("Resource does not denote a valid fusion configuration object: "+resource.toString());
	}

}
