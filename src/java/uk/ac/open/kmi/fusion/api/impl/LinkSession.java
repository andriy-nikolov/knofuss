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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.owl.align.Alignment;

import uk.ac.open.kmi.common.utils.AlignmentUtils;
import uk.ac.open.kmi.common.utils.OIComparison;
import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.IDataSource;
import uk.ac.open.kmi.fusion.api.IDatasetMatchingMethod;
import uk.ac.open.kmi.fusion.api.ILinkSession;
import uk.ac.open.kmi.fusion.api.IObjectIdentificationMethod;
import uk.ac.open.kmi.fusion.util.FusionException;
import uk.ac.open.kmi.fusion.util.KnoFussUtils;
import fr.inrialpes.exmo.align.impl.BasicAlignment;

public class LinkSession extends FusionConfigurationObject implements ILinkSession {

	public static final String TYPE_URI = FusionMetaVocabulary.LINK_SESSION;
	
	public static final int RESULTS_FORMAT_OAEI = 0;
	public static final int RESULTS_FORMAT_CUSTOM_XML = 1;
	public static final int RESULTS_FORMAT_SAME_AS = 2;
	
	String luceneIndexDir = null;
	String tempFileDir = null;
	public boolean multiOntology = false;
	
	public FusionEnvironment fusionEnvironment;
	
	private Map<String, OIComparison> goldStandard;
	private String goldStandardPath = null;
	
	private String abbreviationsPath = null;
	
	private Alignment schemaAlignment;
	protected Repository targetKB;
	
	private List<ApplicationContext> instanceMatchingTasks;
	private List<ApplicationContext> datasetMatchingTasks;
	
	private IDataSource sourceDataset;
	private IDataSource targetDataset;
	
	private List<AtomicMapping> resultMappings;
	private String resultsFilePath;
	private int resultsFileFormat;
	
	private static Logger log = Logger.getLogger(LinkSession.class);
	
	public LinkSession() {
		super();
		init();
	}

	public LinkSession(Resource rdfIndividual, FusionEnvironment environment) {
		super(rdfIndividual, environment);
		init();
	}

	@Override
	public void readFromRDFIndividual(RepositoryConnection connection)
			throws FusionException {
		super.readFromRDFIndividual(connection);
	}



	@Override
	protected void readFromPropertyMember(Statement statement)
			throws RepositoryException {
		super.readFromPropertyMember(statement);
		if(statement.getPredicate().toString().equals(FusionMetaVocabulary.SOURCE_DATASET)) {
			this.sourceDataset = (IDataSource)FusionEnvironment.getInstance().findConfigurationObjectByID((Resource)statement.getObject());
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.TARGET_DATASET)) {
			this.targetDataset = (IDataSource)FusionEnvironment.getInstance().findConfigurationObjectByID((Resource)statement.getObject());
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.INSTANCE_MATCHING_SPEC)) {
			ApplicationContext context = (ApplicationContext)environment.findConfigurationObjectByID((Resource)statement.getObject());
			this.instanceMatchingTasks.add(context);
			context.setLinkSession(this);
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.DATASET_MATCHING_SPEC)) {
			ApplicationContext context = (ApplicationContext)environment.findConfigurationObjectByID((Resource)statement.getObject());
			this.datasetMatchingTasks.add(context);
			context.setLinkSession(this);
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.RESULTS_FILE)) {
			this.resultsFilePath = ((Literal)statement.getObject()).stringValue();
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.RESULTS_FORMAT)) {
			this.resultsFileFormat = getResultsFileFormat(((Literal)statement.getObject()).stringValue());
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.GOLD_STANDARD)) {
			this.goldStandardPath = ((Literal)statement.getObject()).stringValue().trim();
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.ABBREVIATIONS_FILE)) {
			this.abbreviationsPath = ((Literal)statement.getObject()).stringValue().trim();
		}
	}



	public IDataSource getSourceDataset() {
		return sourceDataset;
	}


	public void setSourceDataset(IDataSource sourceDataset) {
		this.sourceDataset = sourceDataset;
	}

	public IDataSource getTargetDataset() {
		return targetDataset;
	}

	public void setTargetDataset(IDataSource targetDataset) {
		this.targetDataset = targetDataset;
	}

	public synchronized void run() throws FusionException {
		if(this.goldStandardPath!=null) {
			this.goldStandard = KnoFussUtils.loadGoldStandardFromFile(this.goldStandardPath);
		}
		
		if(this.abbreviationsPath!=null) {
			try {
				this.loadAbbreviations(this.abbreviationsPath);
			} catch(IOException e) {
				throw new FusionException("Cannot load abbreviations from "+this.abbreviationsPath, e);
			}
		}
		
		sourceDataset.prepare();
		FusionEnvironment.getInstance().setFusionRepositoryConnection(sourceDataset.getConnection());
		FusionEnvironment.getInstance().setFusionKbValueFactory(sourceDataset.getConnection().getValueFactory());
			
		targetDataset.prepare();
		FusionEnvironment.getInstance().setMainKbRepositoryConnection(targetDataset.getConnection());
		FusionEnvironment.getInstance().setMainKbValueFactory(targetDataset.getConnection().getValueFactory());
		
		// Run object identification
		for(ApplicationContext context : this.instanceMatchingTasks) {
			
			if(this.goldStandard!=null) {
				context.setGoldStandard(this.goldStandard);
			}
			
			context.prepare();
			
			FusionMethodWrapper methodWrapper = context.getMethod();
			
			IObjectIdentificationMethod method = (IObjectIdentificationMethod)methodWrapper.getImplementation();
			
			this.resultMappings.addAll(method.getAllPossibleMappings(context));
			
		}
		
		log.info("Task handling finished");
		log.info("Total mappings: "+resultMappings.size());
		
		if(this.goldStandard!=null) {
			log.info("Results for the instance matching stage: ");
			this.calculateF1Measure(false);
			}
		log.info("Proceed with dataset matching");
		for(ApplicationContext context : this.datasetMatchingTasks) {
			
			context.prepare();
			FusionMethodWrapper methodWrapper = context.getMethod();
			IDatasetMatchingMethod method = (IDatasetMatchingMethod)methodWrapper.getImplementation();
			
			this.resultMappings = method.refineMappings(context, resultMappings);
			
		}
		
		log.info("Task handling finished");
		log.info("Total mappings: "+resultMappings.size());
		
		switch(this.resultsFileFormat) {
		case RESULTS_FORMAT_OAEI:
			writeResultsAsAlignmentRDF(resultMappings, this.resultsFilePath);
			break;
		case RESULTS_FORMAT_SAME_AS:
			writeResultsAsSameAs(resultMappings, this.resultsFilePath);
			break;
		case RESULTS_FORMAT_CUSTOM_XML:
			writeResultsToXML(resultMappings, this.resultsFilePath);
			break;
		}
		
		if(this.goldStandard!=null) {
			log.info("Overall results for the link session: ");
			this.calculateF1Measure(true);
		}
	
		cleanIntermediateRepository();
		
	}
	
	private void calculateF1Measure(boolean save) {
		if(this.goldStandard==null) throw new IllegalStateException("No gold standard defined");
		if(this.goldStandard.isEmpty()) throw new IllegalStateException("No gold standard defined");
		
		Set<String> missedKeys = new HashSet<String>(this.goldStandard.keySet());
		
		List<AtomicMapping> falsePositives = new LinkedList<AtomicMapping>();
		
		int tp = 0, fp = 0, fn = 0;
		double precision, recall, f1;
		
		String key;
		for(AtomicMapping mapping : resultMappings) {
			key = mapping.getSourceIndividual().toString()+" : "+mapping.getTargetIndividual().toString();
			if(missedKeys.contains(key)) {
				missedKeys.remove(key);
				tp++;
			} else {
				fp++;
				falsePositives.add(mapping);
				mapping.setCorrect(false);
			}
		}
		
		fn = missedKeys.size();
		
		if(tp==0) {
			precision = 0.0;
			recall = 0.0;
			f1 = 0.0;
		} else {
			precision = ((double)tp)/(tp+fp);
			recall = ((double)tp)/(tp+fn);
			f1 = (2*precision*recall)/(precision+recall);
		}
		
		log.info("F1: "+f1+", precision: "+precision+", recall: "+recall);
		if(save) {
			this.writeFalsePositives(falsePositives);
			this.writeFalseNegatives(this.goldStandard, missedKeys);
		}
	}
	
	private void writeFalsePositives(List<AtomicMapping> falsePositives) {
		try {
			PrintWriter writer = Utils.openPrintFileWriter("logs/test-false-positives.xml");	
			
			try {
			
				writer.println("<?xml version=\"1.0\" encoding=\"windows-1251\"?>");
				writer.println("<DocElement>");
				writer.println("<ResultSet id=\"false-positives\">");
				
				for(AtomicMapping mapping : falsePositives) {
					mapping.writeToXML(writer);
				}
				writer.println("</ResultSet>");
				writer.println("</DocElement>");
				
				log.info("Written "+falsePositives.size()+" false positives");
			} finally {
				writer.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void writeFalseNegatives(Map<String, OIComparison> allGoldStandardMappings, Set<String> missedKeys) {
		try {
			PrintWriter writer = Utils.openPrintFileWriter("logs/test-false-negatives.xml");
			
			try {
				int i = 0;
				
				writer.println("<?xml version=\"1.0\" encoding=\"windows-1251\"?>");
				writer.println("<DocElement>");
				writer.println("<ResultSet id=\"false-negatives\">");
				
				OIComparison mapping;
				for(String key : missedKeys) {
					i++;
					mapping = allGoldStandardMappings.get(key);
					
					mapping.writeToXML(writer);
				}
				writer.println("</ResultSet>");
				writer.println("</DocElement>");
				log.info("Written "+i+" false negatives");
			} finally {
				writer.close();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public List<AtomicMapping> getAtomicMappings() {
		List<AtomicMapping> res = new ArrayList<AtomicMapping>();
		for(MappingSet cluster : FusionEnvironment.getInstance().getMappingSets()) {
			List<AtomicMapping> mappings = cluster.getMappings();
			res.addAll(mappings);
		}
		return res;
	}
	
	public void init() {
		
		this.instanceMatchingTasks = new LinkedList<ApplicationContext>();
		this.datasetMatchingTasks = new LinkedList<ApplicationContext>();
		this.resultMappings = new LinkedList<AtomicMapping>();

	}
	
	protected void loadEnvironment() {
		fusionEnvironment = FusionEnvironment.getInstance();
		((FusionEnvironment)fusionEnvironment).loadEnvironment(FusionEnvironment.getInstance().getFusionRepositoryConnection());
		
	}
		
	public FusionEnvironment getFusionEnvironment() {
		return fusionEnvironment;
	}

	public void setFusionEnvironment(FusionEnvironment fusionEnvironment) {
		this.fusionEnvironment = fusionEnvironment;
	}

	private void cleanIntermediateRepository() {
		try {
			
			log.info("Clearing fusion context...");
			// FusionEnvironment.getInstance().getFusionRepositoryConnection().clear(this.fusionContext);
			log.info("Clearing fusion context: done");
			

		} catch(Exception e) {
			e.printStackTrace();
		} 
	}
	
	public String getLuceneIndexDir() {
		return luceneIndexDir;
	}

	public void setLuceneIndexDir(String luceneIndexDir) {
		this.luceneIndexDir = luceneIndexDir;
	}


	public String getTempFileDir() {
		return tempFileDir;
	}


	public void setTempFileDir(String tempFileDir) {
		this.tempFileDir = tempFileDir;
		//FusionEnvironment.TMP_DIR = tempFileDir;
		File tmpFileDir = new File(tempFileDir);
		if(!tmpFileDir.exists()) {
			tmpFileDir.mkdir();
		} else if(!tmpFileDir.isDirectory()) {
			tmpFileDir.delete();
			tmpFileDir.mkdir();
		}
		
		
	}
	
	public Alignment getSchemaAlignment() {
		return schemaAlignment;
	}

	public void setSchemaAlignment(Alignment schemaAlignment) {
		this.schemaAlignment = schemaAlignment;
	}

	public boolean isMultiOntology() {
		return multiOntology;
	}

	public void setMultiOntology(boolean multiOntology) {
		this.multiOntology = multiOntology;
		FusionEnvironment.isMultiOntologyCase = multiOntology;
	}

	/*public MultiOntologyUtil getMultiOntologyUtil() {
		return multiOntologyUtil;
	}*/

	public Repository getSharedKB() {
		return targetKB;
	}

	public void setTargetKB(Repository sharedKB) {
		this.targetKB = sharedKB;
	}

	private int getResultsFileFormat(String value) {
		if(value.equalsIgnoreCase("oaei")) {
			return RESULTS_FORMAT_OAEI;
		} else if(value.equalsIgnoreCase("sameas")) {
			return RESULTS_FORMAT_SAME_AS;
		} else {
			return RESULTS_FORMAT_CUSTOM_XML;
		}
	}
	
	private void writeResultsAsSameAs(List<AtomicMapping> mappings, String filePath) {
		try {
			SailRepository tmpRepo = new SailRepository(new MemoryStore());
			tmpRepo.initialize();
			RepositoryConnection connection = tmpRepo.getConnection();
			try {
				Statement stmt;
				for(AtomicMapping mapping : mappings) {
					stmt = connection.getValueFactory().createStatement(
							connection.getValueFactory().createURI(mapping.getSourceIndividual().toString()), 
							OWL.SAMEAS, 
							connection.getValueFactory().createURI(mapping.getTargetIndividual().toString()));
					connection.add(stmt);
				}
				
				RDFFormat format = Rio.getWriterFormatForFileName(filePath);
				RDFWriter writer = Rio.createWriter(format, new FileOutputStream(filePath));
				connection.export(writer);
			} finally {
				connection.close();
				tmpRepo.shutDown();		
			}
		} catch(Exception e) {
			log.fatal("Could not save results as RDF: ", e);
		} 
		
	}
	
	private void writeResultsAsAlignmentRDF(List<AtomicMapping> mappings, String filePath) {
		try {
			Alignment alignment = new BasicAlignment();
			for(AtomicMapping mapping : mappings) {
				try {
					alignment.addAlignCell(new java.net.URI(mapping.getSourceIndividual().toString()), new java.net.URI(mapping.getTargetIndividual().toString()), "=", mapping.getConfidence());
				} catch(URISyntaxException e) {
					log.error("Wrong URI syntax: "+mapping.getSourceIndividual().toString()+" : "+mapping.getTargetIndividual().toString(), e);
				}
			}
			AlignmentUtils.writeAlignmentToFile(alignment, filePath);
		} catch(Exception e) {
			log.fatal("Could not save results as Alignment API mappings: ", e);
		} 
	}
	
	private void writeResultsToXML(List<AtomicMapping> mappings, String filePath) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(filePath));
			try {
				int i=0;
				String conceptUri;
				Map<String, List<AtomicMapping>> mappingsByContext = new HashMap<String, List<AtomicMapping>>();
				List<AtomicMapping> tmpList;
				for(AtomicMapping mapping : mappings) {
					if(mapping.getContextType()!=null) {
						 conceptUri = mapping.getContextType();
					} else {
						conceptUri = "default";
					}
					 tmpList = null;
					 if(mappingsByContext.containsKey(conceptUri)) {
						 tmpList = mappingsByContext.get(conceptUri);
					 } else {
						 tmpList = new ArrayList<AtomicMapping>();
						 mappingsByContext.put(conceptUri, tmpList);
					 }
					 tmpList.add(mapping);
					
				}
				writer.println("<?xml version=\"1.0\" encoding=\"windows-1251\"?>");
				writer.println("<DocElement>");
				for(String key : mappingsByContext.keySet()) {
					writer.println("<ResultSet id=\""+key+"\">");
					for(AtomicMapping mapping : mappingsByContext.get(key)) {
						try {
							((AtomicMapping)mapping).writeToXML(writer);
							i++;
							if(i%10000==0) {
								log.info(i+" out of "+mappings.size());
							}
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
					writer.println("</ResultSet>");
				}
				writer.println("</DocElement>");
			} finally {
				writer.close();
			}
		} catch(Exception e) {
			log.fatal("Could not save results as XML mappings in the KnoFuss custom format: ", e);
		}
	}
	
	private void loadAbbreviations(String abbreviationsPath) throws IOException {
		if(abbreviationsPath!=null) {
			BufferedReader reader = Utils.openBufferedFileReader(abbreviationsPath);
			String line;
			String[] vals;
			int i;
			Map<String, String> abbreviationsMap = FusionEnvironment.getInstance().getAbbreviations();
			while((line = reader.readLine())!=null) {
				vals = line.split(",");
				if(vals.length>1) {
					for(i=1;i<vals.length;i++) {
						abbreviationsMap.put(vals[i].trim().toLowerCase(), vals[0].trim().toLowerCase());
					}
				}
			}
			reader.close();
		}
	}
	
}
