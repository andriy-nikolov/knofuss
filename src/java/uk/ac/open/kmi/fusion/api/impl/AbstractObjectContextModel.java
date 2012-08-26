package uk.ac.open.kmi.fusion.api.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;

import uk.ac.open.kmi.common.utils.sparql.MySPARQLParser;
import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IObjectContextModel;
import uk.ac.open.kmi.fusion.api.IObjectContextWrapper;
import uk.ac.open.kmi.fusion.api.impl.aggregation.AggregationFunctionFactory;
import uk.ac.open.kmi.fusion.util.FusionException;
import uk.ac.open.kmi.fusion.util.KnoFussUtils;

public abstract class AbstractObjectContextModel extends
		FusionConfigurationObject implements IObjectContextModel {

	protected Query querySPARQLSource = null;
	protected Query querySPARQLTarget = null;
	protected Map<String, IAttribute> sourceAttributesByVarName;
	protected Map<String, IAttribute> targetAttributesByVarName;
	protected Map<String, IAttribute> sourceAttributesByPath;
	protected Map<String, IAttribute> targetAttributesByPath;
	protected ApplicationContext applicationContext;
	protected List<IAttribute> sourceAttributes;
	protected List<IAttribute> targetAttributes;
	protected List<String> restrictedTypesSource;
	protected List<String> restrictedTypesTarget;
	protected List<IObjectContextWrapper> instances;
	protected Map<String, IObjectContextWrapper> instanceTable;

	public AbstractObjectContextModel() {
		super();
		init();
	}

	public AbstractObjectContextModel(Resource rdfIndividual,
			FusionEnvironment environment) {
		super(rdfIndividual, environment);
		init();
	}
	
	private void init() {
		restrictedTypesSource = new ArrayList<String>(1);
		restrictedTypesTarget = new ArrayList<String>(1);
		
		sourceAttributes = new ArrayList<IAttribute>();
		targetAttributes = new ArrayList<IAttribute>();
		
		sourceAttributesByVarName = new HashMap<String, IAttribute>();
		targetAttributesByVarName = new HashMap<String, IAttribute>();
		
		sourceAttributesByPath = new HashMap<String, IAttribute>();
		targetAttributesByPath = new HashMap<String, IAttribute>();
		
		instances = new ArrayList<IObjectContextWrapper>();
		instanceTable = new HashMap<String, IObjectContextWrapper>();
		
	}
	
	protected abstract void fillAttributeMaps();

	@Override
	public void prepare() {

		
		fillAttributeMaps();
		
		constructQuerySPARQLTarget();
		constructQuerySPARQLSource();
	}

	
	@Override
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	protected Query getQuerySPARQLSource() {
		
		return querySPARQLSource;
	}

	protected Query getQuerySPARQLTarget() {
		
		return querySPARQLTarget;
	}

	public void initializeQueries() {
		if((this.querySPARQLSource==null)||(this.querySPARQLTarget==null)) {
			getQuerySPARQLTarget();
			getQuerySPARQLSource();
		}
	}

	protected abstract Query generateQuery(boolean isTarget);

	@Override
	public String serializeQuerySPARQLSource() {
		if(this.querySPARQLSource==null) {
			this.getQuerySPARQLSource();
		}
		return this.querySPARQLSource.serialize();
	}

	@Override
	public String serializeQuerySPARQLTarget() {
		if(this.querySPARQLTarget==null) {
			this.getQuerySPARQLTarget();
		}
		return this.querySPARQLTarget.serialize();
	}

	@Override
	public List<IAttribute> getSourceAttributes() {
		return sourceAttributes;
	}

	@Override
	public List<IAttribute> getTargetAttributes() {
		return targetAttributes;
	}

	@Override
	public List<String> getRestrictedTypesSource() {
		if(this.querySPARQLSource==null) {
			this.getQuerySPARQLSource();
		}
		return restrictedTypesSource;
	}

	@Override
	public List<String> getRestrictedTypesTarget() {
		if(this.querySPARQLTarget==null) {
			this.getQuerySPARQLTarget();
		}
		return restrictedTypesTarget;
	}

	@Override
	public IAttribute getSourceAttributeByVarName(String varName) {
		
		return this.sourceAttributesByVarName.get(varName);
		
	}

	@Override
	public IAttribute getTargetAttributeByVarName(String varName) {
		
		return this.targetAttributesByVarName.get(varName);
		
	}

	@Override
	public Map<String, IAttribute> getSourceAttributesByVarName() {
		return sourceAttributesByVarName;
	}

	@Override
	public Map<String, IAttribute> getTargetAttributesByVarName() {
		return targetAttributesByVarName;
	}

	@Override
	public Map<String, IAttribute> getSourceAttributesByPath() {
		return sourceAttributesByPath;
	}

	@Override
	public Map<String, IAttribute> getTargetAttributesByPath() {
		return targetAttributesByPath;
	}

	@Override
	public IAttribute getTargetAttributeByPath(String path) {
		return this.targetAttributesByPath.get(path);
	}

	@Override
	public IAttribute getSourceAttributeByPath(String path) {
		return this.sourceAttributesByPath.get(path);
	}

	public void setQuerySPARQLSource(Query querySPARQLSource) {
		this.querySPARQLSource = querySPARQLSource;
	}

	public void setQuerySPARQLTarget(Query querySPARQLTarget) {
		this.querySPARQLTarget = querySPARQLTarget;
	}

	protected void constructQuerySPARQLTarget() {
		if(querySPARQLTarget==null) {
			if(this.getApplicationContext().getRestrictionTarget()!=null) {
				this.querySPARQLTarget = generateQuery(true);
			} else {
				querySPARQLTarget = getQuerySPARQLSource();
			}
			MySPARQLParser parser = new MySPARQLParser(querySPARQLTarget);
			this.restrictedTypesTarget.addAll(parser.getRestrictedTypes());
			// this.variablePathMapTarget = parser.getVariablePathMap();
			
			Map<String, String> variablePathMapTarget = parser.getVariablePathMap();
			
			AtomicAttribute attribute;
			for(String var : variablePathMapTarget.keySet()) {
				attribute = (AtomicAttribute)this.getTargetAttributeByVarName(var);
				attribute.setPropertyPath(variablePathMapTarget.get(var));
			}
		}
	}

	protected void constructQuerySPARQLSource() {
		if(querySPARQLSource==null) {
			if(this.getApplicationContext().getRestrictionSource()!=null) {
				this.querySPARQLSource = generateQuery(false);
			} else {
				querySPARQLSource = getQuerySPARQLTarget();
			}
			MySPARQLParser parser = new MySPARQLParser(querySPARQLSource);
			this.restrictedTypesSource.addAll(parser.getRestrictedTypes());
			// this.variablePathMapTarget = parser.getVariablePathMap();
			
			Map<String, String> variablePathMapSource = parser.getVariablePathMap();
			
			AtomicAttribute attribute;
			for(String var : variablePathMapSource.keySet()) {
				attribute = (AtomicAttribute)this.getSourceAttributeByVarName(var);
				attribute.setPropertyPath(variablePathMapSource.get(var));
			}
		}
		
	}

	@Override
	public List<IObjectContextWrapper> getInstances() {
		return instances;
	}

	@Override
	public IObjectContextWrapper getInstance(int index) {
		return instances.get(index);
	}

	@Override
	public IObjectContextWrapper getInstance(String uri) {
		if(instanceTable.containsKey(uri)) {
			return instanceTable.get(uri);
		}
		return null;
	}

	@Override
	public void addInstance(IObjectContextWrapper instance) {
		if(!instances.contains(instance)) {
			instances.add(instance);
			instance.setModel(this);
		}
	}

}