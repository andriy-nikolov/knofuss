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
