package uk.ac.open.kmi.fusion.api.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;


import uk.ac.open.kmi.common.utils.sparql.MySPARQLParser;
import uk.ac.open.kmi.common.utils.sparql.SPARQLUtils;
import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.IAggregationFunction;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IDump;
import uk.ac.open.kmi.fusion.api.IObjectContextWrapper;
import uk.ac.open.kmi.fusion.api.impl.aggregation.AggregationFunctionFactory;
import uk.ac.open.kmi.fusion.util.FusionException;
import uk.ac.open.kmi.fusion.util.SesameUtils;

public class ObjectContextModel extends FusionConfigurationObject {

	public static final String TYPE_URI = FusionMetaVocabulary.OBJECT_CONTEXT_MODEL;
	
	protected Query querySPARQLSource = null;
	protected Query querySPARQLTarget = null;
	
	protected List<VariableComparisonSpecification> variableComparisonSpecifications;
	// protected Map<String, VariableComparisonSpecification> variableComparisonSpecificationsByName;
	
	protected Map<String, IAttribute> sourceAttributesByVarName;
	protected Map<String, IAttribute> targetAttributesByVarName;
	
	protected Map<String, IAttribute> sourceAttributesByPath;
	protected Map<String, IAttribute> targetAttributesByPath;
	
	protected List<ObjectContextWrapper> instances;
	protected Map<String, ObjectContextWrapper> instanceTable;
	
	protected FusionEnvironment ontology;
	protected ApplicationContext applicationContext; 
	
	protected IAggregationFunction aggregationFunction;
	
	// protected Map<String, String> variablePathMapSource;
	// protected Map<String, String> variablePathMapTarget;
	
	protected List<IAttribute> sourceAttributes;
	protected List<IAttribute> targetAttributes;
	
	protected List<String> restrictedTypesSource;
	protected List<String> restrictedTypesTarget;
	
	protected double threshold;
	
	Logger log = Logger.getLogger(ObjectContextModel.class);
	
	public void init() {
		instances = new ArrayList<ObjectContextWrapper>();
		instanceTable = new HashMap<String, ObjectContextWrapper>();
		variableComparisonSpecifications = new ArrayList<VariableComparisonSpecification>();
		// variableComparisonSpecificationsByName = new HashMap<String, VariableComparisonSpecification>();
		// variablePathMapSource = new HashMap<String, String>();
		// variablePathMapTarget = new HashMap<String, String>();
		
		restrictedTypesSource = new ArrayList<String>(1);
		restrictedTypesTarget = new ArrayList<String>(1);
		
		sourceAttributes = new ArrayList<IAttribute>();
		targetAttributes = new ArrayList<IAttribute>();
		
		sourceAttributesByVarName = new HashMap<String, IAttribute>();
		targetAttributesByVarName = new HashMap<String, IAttribute>();
		
		sourceAttributesByPath = new HashMap<String, IAttribute>();
		targetAttributesByPath = new HashMap<String, IAttribute>();
	}

	public ObjectContextModel() {
		super();
		init();
	}
	
	public ObjectContextModel(Resource individual, FusionEnvironment ontology) {
		super(individual, ontology);
		init();
	}
		
	public List<VariableComparisonSpecification> getVariableComparisonSpecifications() {
		return variableComparisonSpecifications;
	}
	
	/*public Map<String, VariableComparisonSpecification> getVariableComparisonSpecificationsByName() {
		return variableComparisonSpecificationsByName;
	}*/
	
	public List<ObjectContextWrapper> getInstances() {
		return instances;
	}
	
	public IObjectContextWrapper getInstance(int index) {
		return instances.get(index);
	}
	
	public IObjectContextWrapper getInstance(String uri) {
		if(instanceTable.containsKey(uri)) {
			return instanceTable.get(uri);
		}
		return null;
	}
	
	public void addInstance(ObjectContextWrapper instance) {
		if(!instances.contains(instance)) {
			instances.add(instance);
			instance.setModel(this);
		}
	}

	public FusionEnvironment getOntology() {
		return ontology;
	}

	public void setOntology(FusionEnvironment ontology) {
		this.ontology = ontology;
	}
			
	public IAggregationFunction getAggregationFunction() {
		return aggregationFunction;
	}

	public void setAggregationFunction(IAggregationFunction aggregationFunction) {
		this.aggregationFunction = aggregationFunction;
	}
	
	public void prepare() {
		IAttribute attr;
		String varName;
		
		for(VariableComparisonSpecification spec : this.variableComparisonSpecifications) {
			
			attr = spec.getSourceAttribute();
			if(attr instanceof AtomicAttribute) {
				this.sourceAttributesByVarName.put(((AtomicAttribute) attr).getVariableName(), attr);
				// this.sourceAttributesByPath.put(((AtomicAttribute) attr).getPropertyPath(), attr);
			} else {
				this.sourceAttributesByVarName.putAll(((CompositeAttribute)attr).getAtomicAttributesByVariable());
				// this.sourceAttributesByPath.putAll(((CompositeAttribute)attr).getAtomicAttributesByPropertyPath());
			}
			
			attr = spec.getTargetAttribute();
			if(attr instanceof AtomicAttribute) {
				this.targetAttributesByVarName.put(((AtomicAttribute) attr).getVariableName(), attr);
			//	this.targetAttributesByPath.put(((AtomicAttribute) attr).getPropertyPath(), attr);
			} else {
				this.targetAttributesByVarName.putAll(((CompositeAttribute)attr).getAtomicAttributesByVariable());
			//	this.targetAttributesByPath.putAll(((CompositeAttribute)attr).getAtomicAttributesByPropertyPath());
			}
		}
		
		constructQuerySPARQLTarget();
		constructQuerySPARQLSource();
	}
	
	
	
	private void constructQuerySPARQLTarget() {
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
	
	private void constructQuerySPARQLSource() {
		if(querySPARQLSource==null) {
			if(this.getApplicationContext().getRestrictionSource()!=null) {
				this.querySPARQLSource = generateQuery(false);
			} /*else if(FusionEnvironment.isMultiOntologyCase) {
				querySPARQLSource = FusionEnvironment.multiOntologyUtil.translateSPARQLQuery(getQuerySPARQLTarget());
			}*/ else {
				querySPARQLSource = getQuerySPARQLTarget();
			}
			
			MySPARQLParser parser = new MySPARQLParser(querySPARQLSource);
			this.restrictedTypesSource.addAll(parser.getRestrictedTypes());
			
			Map<String, String> variablePathMapSource = parser.getVariablePathMap();
			
			AtomicAttribute attribute;
			for(String var : variablePathMapSource.keySet()) {
				attribute = (AtomicAttribute)this.getSourceAttributeByVarName(var);
				attribute.setPropertyPath(variablePathMapSource.get(var));
			}
			
		}
	}
	
	@Override
	public void readFromRDFIndividual(RepositoryConnection connection) throws FusionException {
		super.readFromRDFIndividual(connection);
		/*List<Statement> statements = SesameUtils.getStatements(
				this.rdfIndividual, 
				null, 
				null, connection);
		for(Statement statement : statements) {
			readFromPropertyMember(statement);
		}*/
	}

	@Override
	protected void readFromPropertyMember(Statement statement) throws RepositoryException {
		super.readFromPropertyMember(statement);
		if(statement.getPredicate().toString().equals(FusionMetaVocabulary.VARIABLE_COMPARISON)) {
			VariableComparisonSpecification spec = (VariableComparisonSpecification)FusionEnvironment.getInstance().findConfigurationObjectByID((Resource)statement.getObject());
			spec.setObjectContextModel(this);
			addVariableComparisonSpecification(spec);						
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.THRESHOLD)) {
			Literal lit = (Literal)statement.getObject();
			this.threshold = lit.doubleValue();
		} else if(statement.getPredicate().toString().equals(FusionMetaVocabulary.AGGREGATION_FUNCTION)) {
			Literal lit = (Literal)statement.getObject();
			this.aggregationFunction = AggregationFunctionFactory.getInstance(lit.stringValue());
		} 
	}
	
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	private Query getQuerySPARQLSource() {
		
		return querySPARQLSource;
	}
	
	private Query getQuerySPARQLTarget() {
		
		return querySPARQLTarget;
	}
	
	public void initializeQueries() {
		if((this.querySPARQLSource==null)||(this.querySPARQLTarget==null)) {
			getQuerySPARQLTarget();
			getQuerySPARQLSource();
		}
	}
	
	private Query generateQuery(boolean isTarget) {
		List<String> variables = new ArrayList<String>();
		List<String> restrictions = new ArrayList<String>();
		
		variables.add("uri");
		if(isTarget) {
			restrictions.add(this.applicationContext.getRestrictionTarget());
		} else {
			restrictions.add(this.applicationContext.getRestrictionSource());
		}
		
		List<String> propertyPaths;
		
		for(VariableComparisonSpecification varSpec : variableComparisonSpecifications) {
			if(isTarget) {
				variables.addAll(varSpec.getTargetAttribute().getVariableNames());
				restrictions.addAll(varSpec.getTargetAttribute().getPropertyPathsAsQueryTriples());
				// restrictions.add(varSpec.getTargetPath());
			} else {
				variables.addAll(varSpec.getSourceAttribute().getVariableNames());
				restrictions.addAll(varSpec.getSourceAttribute().getPropertyPathsAsQueryTriples());
				// restrictions.add(varSpec.getSourcePath());
			}
		}
		String sQuery = SPARQLUtils.generateQuery(variables, restrictions, FusionEnvironment.getInstance().getNamespaceURITable());
		
		Query result = QueryFactory.create(sQuery);
		return result;

	}
	
	
	public String serializeQuerySPARQLSource() {
		if(this.querySPARQLSource==null) {
			this.getQuerySPARQLSource();
		}
		return this.querySPARQLSource.serialize();
	}

	public String serializeQuerySPARQLTarget() {
		if(this.querySPARQLTarget==null) {
			this.getQuerySPARQLTarget();
		}
		return this.querySPARQLTarget.serialize();
	}

	
	
	/*public Map<String, String> getVariablePathMapSource() {
		if(this.querySPARQLSource==null) {
			this.getQuerySPARQLSource();
		}
		return variablePathMapSource;
	}

	public Map<String, String> getVariablePathMapTarget() {
		if(this.querySPARQLTarget==null) {
			this.getQuerySPARQLTarget();
		}
		return variablePathMapTarget;
	}*/

	public List<IAttribute> getSourceAttributes() {
		return sourceAttributes;
	}

	public List<IAttribute> getTargetAttributes() {
		return targetAttributes;
	}

	public List<String> getRestrictedTypesSource() {
		if(this.querySPARQLSource==null) {
			this.getQuerySPARQLSource();
		}
		return restrictedTypesSource;
	}

	public List<String> getRestrictedTypesTarget() {
		if(this.querySPARQLTarget==null) {
			this.getQuerySPARQLTarget();
		}
		return restrictedTypesTarget;
	}	
	
	public double getSimilarity(ComparisonPair pair) {
		return this.aggregationFunction.calculate(pair.getSourceInstance(), pair.getTargetInstance(), this.variableComparisonSpecifications);
	}
	
	public void addVariableComparisonSpecification(VariableComparisonSpecification spec) {
		this.variableComparisonSpecifications.add(spec);
		/*if(spec.getVariableName()!=null) {
			this.getVariableComparisonSpecificationsByName().put(spec.getVariableName(), spec);
		}*/
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("{ \n");
		//result.append("\t source query: ");
		//result.append(this.serializeQuerySPARQLSource());
		//result.append("\n");
		//result.append("\t target query: ");
		//result.append(this.serializeQuerySPARQLTarget());
		//result.append("\n");
		result.append(aggregationFunction.toString());
		result.append("(");
		for(int i=0;i<variableComparisonSpecifications.size();i++) {
			if(i>0) {
				result.append("+");
			}
			result.append(variableComparisonSpecifications.get(i).toString());
		}
		result.append(")>=");
		result.append(threshold);
		result.append("\n}");
		return result.toString();
	}

	public IAttribute getSourceAttributeByVarName(String varName) {
		
		return this.sourceAttributesByVarName.get(varName);
		
	}
	
	public IAttribute getTargetAttributeByVarName(String varName) {
		
		return this.targetAttributesByVarName.get(varName);
		
	}
	
	public Map<String, IAttribute> getSourceAttributesByVarName() {
		return sourceAttributesByVarName;
	}

	public Map<String, IAttribute> getTargetAttributesByVarName() {
		return targetAttributesByVarName;
	}

	public Map<String, IAttribute> getSourceAttributesByPath() {
		return sourceAttributesByPath;
	}

	public Map<String, IAttribute> getTargetAttributesByPath() {
		return targetAttributesByPath;
	}

	public IAttribute getTargetAttributeByPath(String path) {
		return this.targetAttributesByPath.get(path);
	}
	
	public IAttribute getSourceAttributeByPath(String path) {
		return this.sourceAttributesByPath.get(path);
	}
	
	
}
