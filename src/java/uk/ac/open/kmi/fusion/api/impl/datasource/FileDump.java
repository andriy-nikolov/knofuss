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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.IDataSource;
import uk.ac.open.kmi.fusion.api.IDump;
import uk.ac.open.kmi.fusion.api.impl.FusionConfigurationObject;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.util.SesameUtils;

public class FileDump extends FusionConfigurationObject implements IDump {

	Set<String> ontologyFilePaths;
	Set<String> dataFilePaths;
	
	private static Logger log = Logger.getLogger(FileDump.class);
	
	public FileDump(Resource rdfIndividual, FusionEnvironment environment) {
		super(rdfIndividual, environment);
		ontologyFilePaths = new HashSet<String>();
		dataFilePaths = new HashSet<String>();
	}

	@Override
	protected void readFromPropertyMember(Statement statement)
			throws RepositoryException {
		
		super.readFromPropertyMember(statement);
		
		if(statement.getPredicate().toString().equals(FusionMetaVocabulary.DATA_FILE)) {
			dataFilePaths.add(((Literal)statement.getObject()).stringValue().trim());
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.SCHEMA_FILE)) {
			ontologyFilePaths.add(((Literal)statement.getObject()).stringValue().trim());
		}
	}

	@Override
	public void loadToRepository(IDataSource dataSource) throws OpenRDFException, IOException {
		
		RepositoryConnection con = dataSource.getConnection();
		ValueFactory vf = dataSource.getValueFactory();
		
		for(String filePath : ontologyFilePaths) {
			log.info("Loading ontology from: "+filePath);
			con.add(new File(filePath), 
					"",
					SesameUtils.getRDFFormatForFileName(filePath),
					vf.createURI(FusionEnvironment.DOMAIN_CONTEXT_URI));
		}
		
		for(String filePath : dataFilePaths) {
			log.info("Loading data from: "+filePath);
			con.add(new File(filePath), 
					"",
					SesameUtils.getRDFFormatForFileName(filePath));
		}
		
	}

	
	
}
