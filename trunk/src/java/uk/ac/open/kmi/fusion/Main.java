package uk.ac.open.kmi.fusion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.sail.Sail;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.nativerdf.NativeStore;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;

import uk.ac.open.kmi.fusion.api.ILinkSession;
import uk.ac.open.kmi.fusion.api.impl.AtomicMapping;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.api.impl.LinkSession;
import uk.ac.open.kmi.fusion.index.AbstractLuceneIndexer;
import uk.ac.open.kmi.fusion.index.LuceneDiskIndexer;
import uk.ac.open.kmi.fusion.objectidentification.standard.SimMetricsObjectIdentificationUtils;
import uk.ac.open.kmi.fusion.util.FusionException;
import uk.ac.open.kmi.fusion.util.SesameUtils;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

public class Main {


	private Logger log = Logger.getLogger(Main.class);
	
	public Main() {
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		PropertyConfigurator.configure("log4j.properties");
		
		Main obj = new Main();
		String propFile = "config.rdf";
		
		if(args.length>=2) {
			if(args[0].equals("-c")) {
				propFile = args[1].trim();
			}
		}
		
		try {
			obj.readPropertiesFromRDF(propFile);
			obj.setUp();
			obj.testRun();
		} catch (OpenRDFException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
	
	public void readPropertiesFromRDF(String fileName) throws OpenRDFException, IOException {
		RDFParser parser;
		RDFFormat configFileFormat = SesameUtils.getRDFFormatForFileName(fileName);
		
		SailRepository repoConfig = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
		repoConfig.initialize();
		RepositoryConnection configConnection = repoConfig.getConnection();
		try {
			configConnection.add(
					new File(FusionEnvironment.CONFIG_DIR+"/fusion.owl"), 
					"", 
					RDFFormat.RDFXML);
			
			configConnection.add(
					new File(fileName), 
					"", 
					SesameUtils.getRDFFormatForFileName(fileName));
			
			FusionEnvironment.getInstance().loadEnvironment(configConnection);
		} finally {
				
			configConnection.close();
			repoConfig.shutDown();
		}
		
	}
	
		
	public void setUp() throws Exception {
		
	}
	
	public void testRun() throws FusionException {
		
		try {
			List<ILinkSession> sessions = FusionEnvironment.getInstance().getLinkSessions();
			
			for(ILinkSession session : sessions) {
				session.run();
			}
		} catch(Exception e) {
			e.printStackTrace();
		} catch(Error e) {
			e.printStackTrace();
		} finally {
	
			FusionEnvironment.getInstance().shutDown();
		}
	}		
	
}
