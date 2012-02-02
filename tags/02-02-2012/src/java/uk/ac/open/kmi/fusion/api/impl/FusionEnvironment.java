package uk.ac.open.kmi.fusion.api.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.rio.RDFFormat;

// 
import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
//import uk.ac.open.kmi.fusion.MultiOntologyUtil;
import uk.ac.open.kmi.fusion.api.ILinkSession;
import uk.ac.open.kmi.fusion.api.ILuceneBlocker;
import uk.ac.open.kmi.fusion.api.IPersistentStore;
import uk.ac.open.kmi.fusion.api.impl.datasource.FileDump;
import uk.ac.open.kmi.fusion.api.impl.datasource.SesameDataSource;
import uk.ac.open.kmi.fusion.api.impl.valuematching.ValueMatchingFunctionFactory;
//import uk.ac.open.kmi.fusion.index.LuceneDiskIndexer;
//import uk.ac.open.kmi.fusion.index.LuceneDiskIndexerAllFields;
//import uk.ac.open.kmi.fusion.index.LuceneMemoryIndexerAllFields;
import uk.ac.open.kmi.fusion.learning.cache.MemoryInstanceCache;
//import uk.ac.open.kmi.fusion.index.LuceneMemoryIndexerNGrams;
import uk.ac.open.kmi.fusion.util.FusionException;
import uk.ac.open.kmi.fusion.util.SesameUtils;

public class FusionEnvironment {
	
	private static FusionEnvironment INSTANCE = new FusionEnvironment();
	
	public static final String FUSION_CONTEXT_URI = "http://kmi.open.ac.uk/fusion/fusion#fusion-environment";
	public static final String DOMAIN_CONTEXT_URI = "http://kmi.open.ac.uk/fusion/fusion#domain-environment";
	// public static String TMP_DIR = "D:/Work/Dataset";
	// public static RDFFormat RDF_LANGUAGE = RDFFormat.TRIX;
	// public static String wordNetPropertiesPath = "D:/Work/poweraqua/PowerAquaNetBeans/";
	
	public static String CONFIG_DIR = "config";
	// public static ILuceneBlocker indexer;
	public static boolean debug = false;
	public static boolean isMultiOntologyCase;
	
	//public static MultiOntologyUtil multiOntologyUtil;
	
	public static boolean saveComparisons = false;
	public static String comparisonsLogFile = "comparisons.xml";
	
	
	private RepositoryConnection fusionRepositoryConnection;
	private ValueFactory fusionKbValueFactory;
	private RepositoryConnection mainKbRepositoryConnection;
	private ValueFactory mainKbValueFactory;
	
	private Map<Resource, FusionConfigurationObject> configObjectRegistry;
	
	private List<ILinkSession> linkSessions;
	
	private List<AtomicMapping> atomicMappings;
	private Map<String, AtomicMapping> atomicMappingTable;
	private List<MappingSet> mappingSets;
	private Map<String, MappingSet> mappingSetTable;
	private List<ConflictStatementCluster> conflictSets;	
	private List<ObjectContextModel> contextModels;
	private List<ObjectContextWrapper> contextWrappers;
	private List<ApplicationContext> applicationContexts;
	private Map<String, ObjectContextWrapper> contextWrapperTable;
	private String url;
	private String defaultNamespaceURI;
	private Map<String, String> namespaceURITable;
	private List<FusionMethodWrapper> methodWrappers;
	private List<ValueMatchingFunctionWrapper> valueMatchingFunctionWrappers;
	private Map<String, String> abbreviations; 

	// private Map<String, FusionMethodWrapper> methodWrapperTable;
	// private Map<String, ConflictStatementCluster> conflictSetTable;
	// private RepositoryConnection connection;
	// private Map<String, String> queryMappings;
	// private MemoryInstanceCache instanceCache;
	
	private Set<URI> contentIndividualUris = null;
	private boolean multiOntologyCase = false;
	
	private Map<String, RepositoryManager> repositoryManagerRegistry;
	
	//private int sourceSetSize = 0;
	//private int targetSetSize = 0;
	
	private static Logger log = Logger.getLogger(FusionEnvironment.class);
	
	public void init() {
		contextModels = new ArrayList<ObjectContextModel>();
		// contextModelTable = new HashMap<String, ObjectContextModel>();
		contextWrappers = new ArrayList<ObjectContextWrapper>();
		namespaceURITable = new HashMap<String, String>();
		atomicMappings = new ArrayList<AtomicMapping>();
		conflictSets = new ArrayList<ConflictStatementCluster>();
		mappingSets = new ArrayList<MappingSet>();
		mappingSetTable = new HashMap<String, MappingSet>();
		methodWrappers = new ArrayList<FusionMethodWrapper>();
		valueMatchingFunctionWrappers = new ArrayList<ValueMatchingFunctionWrapper>();
		
		// methodWrapperTable = new HashMap<String, FusionMethodWrapper>();
		atomicMappingTable = new HashMap<String, AtomicMapping>();
		// queryMappings = new HashMap<String, String>();
		// instanceCache = new MemoryInstanceCache();
		
		configObjectRegistry = new HashMap<Resource, FusionConfigurationObject>();
		linkSessions = new LinkedList<ILinkSession>();
		repositoryManagerRegistry = new HashMap<String, RepositoryManager>();
		abbreviations = new HashMap<String, String>();
	}
	
	

	public Map<String, String> getAbbreviations() {
		return abbreviations;
	}

	protected FusionEnvironment() {
		super();
		init();
	}
	
	public static FusionEnvironment getInstance() {
		return INSTANCE;
	}
	
	public void addRepositoryManagerToRegistry(String path, RepositoryManager manager) {
		this.repositoryManagerRegistry.put(path, manager);
	}
	
	public RepositoryManager getRepositoryManagerFromRegistry(String path) {
		return this.repositoryManagerRegistry.get(path);
	}	
	
	public void loadEnvironment(RepositoryConnection connection) {
		// this.connection = connection;
		
		try {
			loadNamespaces(connection);
			loadFusionConfigurationObjects(connection);
		} catch (Exception exc) {
			log.error("Could not load the fusion environment");
			exc.printStackTrace();
		}
	}
	
	private void loadNamespaces(RepositoryConnection connection) throws RepositoryException {
		RepositoryResult<Namespace> namespaces = connection.getNamespaces();
		Namespace namespace;
		try {
			while(namespaces.hasNext()) {
				namespace = namespaces.next();
				this.namespaceURITable.put(namespace.getPrefix(), namespace.getName());
			}
		} finally {
			namespaces.close();
		}
		
	}
	
	private void loadFusionConfigurationObjects(RepositoryConnection connection) throws FusionException {
		Set<Resource> objectResources = new HashSet<Resource>();
		try {
			RepositoryResult<Statement> stmts = connection.getStatements(null, RDF.TYPE, connection.getValueFactory().createURI(FusionConfigurationObject.TYPE_URI), true);
			Statement stmt;
			try {
				while(stmts.hasNext()) {
					stmt = stmts.next();
					objectResources.add(stmt.getSubject());
				}
			} finally {
				stmts.close();
			}
		} catch(RepositoryException e) {
			throw new FusionException("Could not load the configuration", e);
		}
		
		FusionConfigurationObject obj;
		for(Resource res : objectResources) {
			try {
				obj = getFusionConfigurationObjectByResource(res, connection);
				this.configObjectRegistry.put(res, obj);
				if(obj instanceof LinkSession) {
					this.linkSessions.add((LinkSession)obj);
				} else if(obj instanceof ValueMatchingFunctionWrapper) {
					this.valueMatchingFunctionWrappers.add((ValueMatchingFunctionWrapper)obj);
					obj.readFromRDFIndividual(connection);
					ValueMatchingFunctionFactory.addToPool(((ValueMatchingFunctionWrapper) obj).getImplementation());
				}
			} catch(FusionException e) {
				e.printStackTrace();
			}
		}
		
		
		for(Resource res : this.configObjectRegistry.keySet()) {
			obj = this.configObjectRegistry.get(res);
			if(obj instanceof ValueMatchingFunctionWrapper) continue;
			obj.readFromRDFIndividual(connection);
			if(obj.getRDFIndividual().toString().endsWith("EventComparisonFunction")) {
				System.out.println("here");
			}
			/*if(obj instanceof ValueMatchingFunctionWrapper) {
				ValueMatchingFunctionFactory.addToPool(((ValueMatchingFunctionWrapper) obj).getImplementation());
			}*/
		}
		
	}
	
	private FusionConfigurationObject getFusionConfigurationObjectByResource(Resource resource, RepositoryConnection connection) throws FusionException {
		
		if(this.configObjectRegistry.containsKey(resource)) {
			return configObjectRegistry.get(resource);
		} else {
			return FusionConfigurationObjectFactory.createFromResource(resource, this, connection);
		}
		
		
	}
	
	/*private FusionConfigurationObject createFusionConfigurationObjectFromResource(Resource resource, RepositoryConnection connection) throws FusionException, RepositoryException {
		RepositoryResult<Statement> types = connection.getStatements(resource, RDF.TYPE, null, true);
		Statement stmt;
		String typeUri;
		try {
			while(types.hasNext()) {
				stmt = types.next();
				if(stmt.getObject() instanceof URI) {
					typeUri = stmt.getObject().toString();
					
					if(typeUri.equals(ApplicationContext.TYPE_URI)) {
						return new ApplicationContext(resource, this);
					} else if(typeUri.equals(FusionMethodWrapper.TYPE_URI)) {
						return new FusionMethodWrapper(resource, this);
					} else if(typeUri.equals(OWLIMDataSource.TYPE_URI)) {
						return new OWLIMDataSource(resource, this);
					} else if(typeUri.equals(FileDump.TYPE_URI)) {
						return new FileDump(resource, this);
					} else if(typeUri.equals(LuceneDiskIndexer.TYPE_URI)) {
						return new LuceneDiskIndexer(resource, this);
					} else if(typeUri.equals(LuceneDiskIndexerAllFields.TYPE_URI)) {
						return new LuceneDiskIndexerAllFields(resource, this);
					} else if(typeUri.equals(LuceneMemoryIndexerAllFields.TYPE_URI)) {
						return new LuceneMemoryIndexerAllFields(resource, this);
					} else if(typeUri.equals(LinkSession.TYPE_URI)) {
						return new LinkSession(resource, this);
					} else if(typeUri.equals(VariableComparisonSpecification.TYPE_URI)) {
						return new VariableComparisonSpecification(resource, this);
					} else if(typeUri.equals(ObjectContextModel.TYPE_URI)) {
						return new ObjectContextModel(resource, this);
					}
					
				}
				
			}
		} finally {
			types.close();
		}
		
		throw new FusionException("Resource does not denote a valid fusion configuration object: "+resource.toString());
	}*/
	
	
	protected void loadConflictSets() throws FusionException {
	}
	
	public ObjectContextModel getObjectContextModel(int index) {
		return contextModels.get(index);
	}
	
	public List<ConflictStatementCluster> getConflictSets() {
		return conflictSets;
	}
	
	public String getDefaultNamespaceURI() {
		return defaultNamespaceURI;
	}

	public void setDefaultNamespaceURI(String namespaceURI) {
		this.defaultNamespaceURI = namespaceURI;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public List<ApplicationContext> getApplicationContexts() {
		return applicationContexts;
	}

	/*public RepositoryConnection getCandidateKb() {
		return fusionRepositoryConnection;
	}

	public RepositoryConnection getMainKB() {
		// TODO Auto-generated method stub
		return mainKbRepositoryConnection;
	}*/

	/*public List<IObjectContextModel> getObjectContextModelsForConcept(URI concept) {
		List<IObjectContextModel> res = new ArrayList<IObjectContextModel>();
		for(IApplicationContext context : this.applicationContexts) {
			if(context.getConcept().equals(concept)) {
				List<IFusionOntologyObject> connectedObjects = context.getConnectedObjects();
				for(IFusionOntologyObject obj : connectedObjects) {
					if(obj instanceof IObjectContextModel) {
						res.add((IObjectContextModel)obj);
					}
				}
			}
		}
		for(IObjectContextModel model : contextModels) {
			if(((ObjectContextModel)model).getConcept().getUri().equals(concept.getUri())) {
				res.add(model);
			}
		}
		return res;
	}*/

	public List<ConflictStatementCluster> getConflicts() {
		
		
		return getConflictSets();
	}

	public List<FusionMethodWrapper> getFusionMethods() {
		return this.methodWrappers;
	}

	public List<FusionMethodWrapper> getFusionMethods(int capability) {
		List<FusionMethodWrapper> res = new ArrayList<FusionMethodWrapper>();
		for(FusionMethodWrapper method : this.methodWrappers) {
			if(method.getCapability()==capability) {
				res.add(method);
			}
		}
		return res;
	}

	public List<ObjectContextModel> getObjectContextModels() {
		List<ObjectContextModel> res = new ArrayList<ObjectContextModel>();
		res.addAll(this.contextModels);
		return res;
	}

	public List<ObjectContextWrapper> getContextWrappers() {
		return contextWrappers;
	}

	public Map<String, ObjectContextWrapper> getContextWrapperTable() {
		return contextWrapperTable;
	}

	public List<MappingSet> getMappingSets() {
		return mappingSets;
	}

	public List<FusionMethodWrapper> getMethodWrappers() {
		return methodWrappers;
	}
	
	public List<ValueMatchingFunctionWrapper> getValueMatchingFunctionWrappers() {
		return valueMatchingFunctionWrappers;
	}

	public void addAtomicMapping(AtomicMapping obj) {
		if(!this.atomicMappings.contains(obj)) {
			atomicMappings.add(obj);
		}
	}
	
	public void removeAtomicMapping(AtomicMapping obj) {
		this.atomicMappings.remove(obj);
	}
	
	public void addMappingSet(MappingSet obj) {
		if(!this.mappingSets.contains(obj)) {
			mappingSets.add(obj);
		}
	}
	
	public void removeMappingSet(MappingSet obj) {
		this.mappingSets.remove(obj);
	}
	
	public void addConflictSet(ConflictStatementCluster obj) {
		if(!this.conflictSets.contains(obj)) {
			this.conflictSets.add(obj);
		}
	}
	
	public void removeConflictSet(ConflictStatementCluster obj) {
		this.conflictSets.remove(obj);
	}
	

	public List<AtomicMapping> getAtomicMappings() {
		return this.atomicMappings;
	}

	public FusionConfigurationObject findConfigurationObjectByID(Resource resource) {
		if(this.configObjectRegistry.containsKey(resource)) {
			return this.configObjectRegistry.get(resource);
		} else {
			throw new IllegalArgumentException("No object with the ID "+resource.toString()+" was found");
		}
	}

	public List<ILinkSession> getLinkSessions() {
		return linkSessions;
	}

	public Set<URI> getContentIndividualUris() {
		return this.contentIndividualUris;
	}

	public void setContentIndividualUris(Set<URI> uris) {
		this.contentIndividualUris = uris;
	}

	public void cleanEnvironment() {
		this.mappingSets.clear();
		this.mappingSetTable.clear();
		this.atomicMappings.clear();
		this.atomicMappingTable.clear();
		this.conflictSets.clear();
		this.contextWrappers.clear();
	}

	public boolean isMultiOntologyCase() {
		return multiOntologyCase;
	}

	public void setMultiOntologyCase(boolean multiOntologyCase) {
		this.multiOntologyCase = multiOntologyCase;
	}

	public RepositoryConnection getFusionRepositoryConnection() {
		return fusionRepositoryConnection;
	}

	public void setFusionRepositoryConnection(
			RepositoryConnection fusionRepositoryConnection) {
		this.fusionRepositoryConnection = fusionRepositoryConnection;
	}

	public ValueFactory getFusionKbValueFactory() {
		return fusionKbValueFactory;
	}

	public void setFusionKbValueFactory(ValueFactory fusionKbValueFactory) {
		this.fusionKbValueFactory = fusionKbValueFactory;
	}

	public RepositoryConnection getMainKbRepositoryConnection() {
		return mainKbRepositoryConnection;
	}

	public void setMainKbRepositoryConnection(
			RepositoryConnection mainKbRepositoryConnection) {
		this.mainKbRepositoryConnection = mainKbRepositoryConnection;
	}

	public ValueFactory getMainKbValueFactory() {
		return mainKbValueFactory;
	}

	public void setMainKbValueFactory(ValueFactory mainKbValueFactory) {
		this.mainKbValueFactory = mainKbValueFactory;
	}

	public Map<String, String> getNamespaceURITable() {
		return namespaceURITable;
	}
	
	public void shutDown() {
		FusionConfigurationObject obj;
		for(Resource res : configObjectRegistry.keySet()) {
			obj = configObjectRegistry.get(res);
			if(obj instanceof IPersistentStore) {
				try {
					((IPersistentStore)obj).close();
				} catch(FusionException e) {
					e.printStackTrace();
				}
			}
		}
			
		for(String key : repositoryManagerRegistry.keySet()) {
			repositoryManagerRegistry.get(key).shutDown();
		}
	}
	
}
