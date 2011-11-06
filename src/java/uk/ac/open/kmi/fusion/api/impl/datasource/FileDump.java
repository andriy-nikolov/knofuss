package uk.ac.open.kmi.fusion.api.impl.datasource;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openjena.atlas.logging.Log;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;

import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.IDataSource;
import uk.ac.open.kmi.fusion.api.IDump;
import uk.ac.open.kmi.fusion.api.impl.FusionConfigurationObject;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.index.store.AbstractLuceneStore;
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
