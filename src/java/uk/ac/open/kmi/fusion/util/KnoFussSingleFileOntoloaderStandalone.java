/* Copyright (c) 2012, Knowledge Media Institute
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the <organization> nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package uk.ac.open.kmi.fusion.util;

import java.io.IOException;
import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
//import org.xmedia.simpleimpl.Helper;

public class KnoFussSingleFileOntoloaderStandalone {
	private static final Log log = LogFactory.getLog(KnoFussSingleFileOntoloaderStandalone.class);

	public static final RDFFormat DEFAULTFORMAT = RDFFormat.RDFXML;

	private final RepositoryConnection connection;
	
	
	//private Repository sesameRepository;
	//private RepositoryConnection sesameConnection;
	private ValueFactory valueFactory;

	public KnoFussSingleFileOntoloaderStandalone(
			String path, 
			String resourceCheck,
			RepositoryConnection connection, 
			
			String context) {
		
		this(
				path,
				resourceCheck, 
				DEFAULTFORMAT, 
				connection, 
				context);
	}

	public KnoFussSingleFileOntoloaderStandalone(
			String path, 
			String resourceCheck, 
			RDFFormat rdfFormat,
			RepositoryConnection connection, 
			
			String context) {

		URI sesameContext;
		RDFFormat sesameFormat = rdfFormat;
		if(sesameFormat==null) {
			sesameFormat = RDFFormat.RDFXML;
		}
		
		
		log.info("Processing entries from: " + path + "; resourceCheck: is "
				+ resourceCheck);

		this.connection = connection;
		
		//Helper.checkNull(log, targetOntology, "Ontology", true);
		
		
		try {
			
			valueFactory = connection.getRepository().getValueFactory();
	 
		} catch(Exception e) {
			log.error(e);
			e.printStackTrace();
			return;
		} 
		if(context!=null) {
			sesameContext = valueFactory.createURI(context);
		} else {
			sesameContext = null;
		}

		try {

			if (ontologyIsVoid(this.connection, resourceCheck)) {
				
				File file = new File(path);
				try {
					this.connection.add(
							file, 
							"",
							sesameFormat, 
							sesameContext);
				} catch (RepositoryException e) {
					log.error(e);
					e.printStackTrace();
				} catch (RDFParseException e) {
					log.error(e);
					e.printStackTrace();
				} 
				
				log.info("Ontology " + file.getName()+" loaded.");
			} else
				log.info("Ontology already present.");

		} catch (IOException e) {
			log.error(e);
			e.printStackTrace();
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
		} 
	}

	private boolean ontologyIsVoid(RepositoryConnection ontology, String targetResource)
			throws RepositoryException {

		boolean isVoid = true;

		
			isVoid = 
				(ontology.getStatements(ontology.getRepository().getValueFactory().createURI(targetResource), null, null, false).asList().size()==0);

		

		return isVoid;
	}


	public static void main(String[] args) throws RepositoryException {

		// SesameConnection connection = new SesameConnection();
		// connection.setRepositoryRoot("/xmedia_test/shared_kb");
		// new Ontoloader(
		// "/xmedia/fiatOntology.zip",
		// "http://www.x-media-project.org/fiat#VehicleModel",
		// ".rdfs",
		// connection,
		// new SesameSessionFactory(),
		// "http://www.x-media-project.org/ontologies/test");
	}

}
