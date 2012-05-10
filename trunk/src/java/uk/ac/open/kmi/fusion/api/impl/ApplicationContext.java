package uk.ac.open.kmi.fusion.api.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.common.utils.OIComparison;
import uk.ac.open.kmi.common.utils.sparql.MySPARQLParser;
import uk.ac.open.kmi.common.utils.sparql.SPARQLUtils;
import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.ILuceneBlocker;
import uk.ac.open.kmi.fusion.util.FusionException;
import uk.ac.open.kmi.fusion.util.KnoFussUtils;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;


public class ApplicationContext extends FusionConfigurationObject {
	
	Query querySPARQLSource = null;
	Query querySPARQLTarget = null;
	
	String restrictionSource = null;
	String restrictionTarget = null;
	
	double reliability;
	FusionMethodWrapper method;

	List<IAttribute> additionalSourceAttributes;
	List<IAttribute> additionalTargetAttributes;
	
	Map<Resource, ObjectContextModel> objectModels;
	
	Map<String, String> properties;
	List<String> affectedIndividuals;
	
	protected List<String> restrictedTypesSource;
	protected List<String> restrictedTypesTarget;
	
	private Map<String, OIComparison> goldStandard = null;
	
	private String goldStandardPath = null;
	
	public static final String TYPE_URI = FusionMetaVocabulary.APPLICATION_CONTEXT;
	
	private LinkSession linkSession;
	
	private ILuceneBlocker blocker;
	
	public ApplicationContext() {
		super();
		init();
	}
	

	public ApplicationContext(Resource rdfIndividual, FusionEnvironment environment) {
		super(rdfIndividual, environment);
		init();
	}
	
	private void init() {
		objectModels = new HashMap<Resource, ObjectContextModel>();
		properties = new HashMap<String, String>();
		this.restrictedTypesSource = new ArrayList<String>(1);
		this.restrictedTypesTarget = new ArrayList<String>(1);
		this.additionalSourceAttributes = new ArrayList<IAttribute>();
		this.additionalTargetAttributes = new ArrayList<IAttribute>();
	}

	public Resource addToOntology(RepositoryConnection con,
			String descriptorURI) throws RepositoryException {
		return this.rdfIndividual;
	}
	
	public void prepare() {
		for(Resource key : this.objectModels.keySet()) {
			this.objectModels.get(key).prepare();
		}
		
		if(this.goldStandardPath!=null) {
			try {
				this.goldStandard = KnoFussUtils.loadGoldStandardFromFile(this.goldStandardPath);
			} catch(FusionException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public boolean isGoldStandardAvailable() {
		return (goldStandard!=null);
	}
	
	@Override
	public void readFromRDFIndividual(RepositoryConnection connection) throws FusionException {
		super.readFromRDFIndividual(connection);
	}

	@Override
	protected void readFromPropertyMember(Statement statement) throws RepositoryException {
		super.readFromPropertyMember(statement);
		if(statement.getPredicate().toString().equals(FusionMetaVocabulary.SOURCE_RESTRICTION)) {
			this.restrictionSource = ((Literal)statement.getObject()).stringValue().trim();
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.TARGET_RESTRICTION)) {
			this.restrictionTarget = ((Literal)statement.getObject()).stringValue().trim();
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.HAS_OBJECT_MODEL)) {
			if(statement.getObject() instanceof Resource) {
				Resource linkIndividual = (Resource) statement.getObject();
				ObjectContextModel model = (ObjectContextModel)environment.findConfigurationObjectByID(linkIndividual);
				model.setApplicationContext(this);
				this.objectModels.put((Resource)statement.getObject(), model);
			}
			
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.HAS_METHOD)) {
			Resource linkIndividual = (Resource) statement.getObject();
			FusionMethodWrapper methodWrapper = (FusionMethodWrapper)environment.findConfigurationObjectByID(linkIndividual);
			this.method = methodWrapper;
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.HAS_BLOCKER)) {
			Resource linkIndividual = (Resource) statement.getObject();
			this.blocker = (ILuceneBlocker)environment.findConfigurationObjectByID(linkIndividual);
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.GOLD_STANDARD)) {
			this.goldStandardPath = ((Literal)statement.getObject()).stringValue().trim();
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.SOURCE_ATTRIBUTE)) {
			if(statement.getObject() instanceof Resource) {
				Resource res = (Resource)statement.getObject();
				IAttribute attr = (IAttribute)FusionEnvironment.getInstance().findConfigurationObjectByID(res);
				this.additionalSourceAttributes.add(attr);
			}
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.TARGET_ATTRIBUTE)) {
			if(statement.getObject() instanceof Resource) {
				Resource res = (Resource)statement.getObject();
				IAttribute attr = (IAttribute)FusionEnvironment.getInstance().findConfigurationObjectByID(res);
				this.additionalTargetAttributes.add(attr);
			}
		}else {
			String key = statement.getPredicate().toString();
			if(!(key.startsWith(RDF.NAMESPACE)||key.startsWith(RDFS.NAMESPACE))) {
				Value res = statement.getObject();
				
				if(res instanceof Literal) {
					properties.put(key, ((Literal)res).stringValue());
				} else if(res instanceof Resource) {
					properties.put(key, ((Resource)res).toString());
				} 
			}
		}
	}

	public double getReliability() {
		return reliability;
	}

	public void setReliability(double reliability) {
		this.reliability = reliability;
	}


	public FusionMethodWrapper getMethod() {
		return method;
	}



	public void setMethod(FusionMethodWrapper method) {
		this.method = method;
	}


	public void addObjectModel(ObjectContextModel obj) {
		objectModels.put(obj.getRDFIndividual(), obj);
	}

	public List<ObjectContextModel> getObjectModels() {
		List<ObjectContextModel> res = new LinkedList<ObjectContextModel>();
		for(Resource key : this.objectModels.keySet()) {
			res.add(this.objectModels.get(key));
		}
		return res;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperty(String property, String value) {
		properties.put(property, value);
	}

	public List<String> getAffectedIndividuals() {
		return affectedIndividuals;
	}

	public void setAffectedIndividuals(List<String> affectedIndividuals) {
		this.affectedIndividuals = affectedIndividuals;
	}

	public String serializeQuerySPARQLSource() {
		if(querySPARQLSource==null) {
			getQuerySPARQLSourceARQ();
		}
		return querySPARQLSource.serialize();
	}

	public String serializeQuerySPARQLTarget() {
		if(querySPARQLTarget==null) {
			getQuerySPARQLTargetARQ();
		}
		return querySPARQLTarget.serialize();
	}
	
	public LinkSession getLinkSession() {
		return linkSession;
	}

	public void setLinkSession(LinkSession linkSession) {
		this.linkSession = linkSession;
	}

	public Query getQuerySPARQLTargetARQ() {
		if(querySPARQLTarget==null) {
			if(restrictionTarget==null) {
				if(restrictionSource!=null) {
					querySPARQLTarget = getQuerySPARQLSourceARQ();
				}
			} else {
				List<String> variables = new ArrayList<String>();
				variables.add("uri");
				List<String> restrictions = new ArrayList<String>();
				restrictions.add(restrictionTarget);
				querySPARQLTarget = QueryFactory.create(SPARQLUtils.generateQuery(variables, restrictions, FusionEnvironment.getInstance().getNamespaceURITable()));
			}
			MySPARQLParser parser = new MySPARQLParser(querySPARQLTarget);
			this.restrictedTypesTarget.addAll(parser.getRestrictedTypes());
		}
		return querySPARQLTarget;
	}
	
	public Query getQuerySPARQLSourceARQ() {
		if(querySPARQLSource==null) {
			if(restrictionSource==null) {
				if(restrictionTarget!=null) {
					querySPARQLSource = getQuerySPARQLTargetARQ();
				}
			} else {
				List<String> variables = new ArrayList<String>();
				variables.add("uri");
				List<String> restrictions = new ArrayList<String>();
				restrictions.add(restrictionSource);
				querySPARQLSource = QueryFactory.create(SPARQLUtils.generateQuery(variables, restrictions, FusionEnvironment.getInstance().getNamespaceURITable()));
			}
			MySPARQLParser parser = new MySPARQLParser(querySPARQLSource);
			this.restrictedTypesSource.addAll(parser.getRestrictedTypes());
		}
		return querySPARQLSource;
	}

	public String getRestrictionSource() {
		return restrictionSource;
	}

	public void setRestrictionSource(String restrictionSource) {
		this.restrictionSource = restrictionSource;
	}

	public String getRestrictionTarget() {
		return restrictionTarget;
	}

	public void setRestrictionTarget(String restrictionTarget) {
		this.restrictionTarget = restrictionTarget;
	}

	public List<String> getRestrictedTypesSource() {
		if(this.querySPARQLSource==null) {
			this.getQuerySPARQLSourceARQ();
		}
		return restrictedTypesSource;
	}

	public List<String> getRestrictedTypesTarget() {
		if(this.querySPARQLTarget==null) {
			this.getQuerySPARQLTargetARQ();
		}
		return restrictedTypesTarget;
	}
	
	public void setBlocker(ILuceneBlocker blocker) {
		this.blocker = blocker;
	}

	public ILuceneBlocker getBlocker() {
		return blocker;
	}

	public Map<String, OIComparison> getGoldStandard() {
		return goldStandard;
	}

	public void setGoldStandard(Map<String, OIComparison> goldStandard) {
		this.goldStandard = goldStandard;
	}


	public List<IAttribute> getAdditionalSourceAttributes() {
		return additionalSourceAttributes;
	}


	public List<IAttribute> getAdditionalTargetAttributes() {
		return additionalTargetAttributes;
	}
	
	
	
	
}
