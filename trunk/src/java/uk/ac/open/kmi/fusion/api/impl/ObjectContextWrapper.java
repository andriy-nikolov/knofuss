package uk.ac.open.kmi.fusion.api.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;


import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IObjectContextWrapper;
import uk.ac.open.kmi.fusion.util.KnoFussUtils;

public class ObjectContextWrapper implements IObjectContextWrapper {
	
	protected URI individual;
	protected ObjectContextModel model;
	// protected FusionEnvironment ontology;
	public static final String CLASS_URI = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"ObjectContextWrapper";
	
	protected Map<IAttribute, List<? extends Object>> valueTable;
	
	public ObjectContextWrapper() {
		super();
		valueTable = new HashMap<IAttribute, List<? extends Object>>();
	}
	
	public ObjectContextWrapper(ObjectContextModel model) {
		this();
		this.model = model;
		model.addInstance(this);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.fusion.api.impl.IObjectContextWrapper#getModel()
	 */
	@Override
	public ObjectContextModel getModel() {
		return model;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.fusion.api.impl.IObjectContextWrapper#setModel(uk.ac.open.kmi.fusion.api.impl.ObjectContextModel)
	 */
	@Override
	public void setModel(ObjectContextModel model) {
		this.model = model;
		model.addInstance(this);
	}

		
	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.fusion.api.impl.IObjectContextWrapper#getIndividual()
	 */
	@Override
	public URI getIndividual() {
		return individual;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.fusion.api.impl.IObjectContextWrapper#setIndividual(org.openrdf.model.URI)
	 */
	@Override
	public void setIndividual(URI individual) {
		this.individual = individual;
	}

	public boolean getPropertiesFromQueryResult(BindingSet queryResult, URI individual) {
		this.individual = individual;
		return getPropertiesFromQueryResult(queryResult);
	}

	public boolean getPropertiesFromQueryResult(BindingSet queryResult) {
		String propertyUri;
		
		//Map<String, String> variablePropertyMap = model.getVariablePropertyMapTarget();
		//variablePropertyMap = model.getVariablePropertyMap();
		if(this.individual==null) {
			this.individual = (URI)queryResult.getValue("uri");
		} else if(!this.individual.toString().equals(((URI)queryResult.getValue("uri")).toString())) {
			return false;
		}
		
		String val;
		
		List<String> tmpValueSet;
		
		IAttribute attribute;
		
		for(String curVar : queryResult.getBindingNames()) {
			
			val = queryResult.getValue(curVar).stringValue();
			
			if(val.trim().startsWith("\""))
				val = val.trim().substring(1);
			if(val.trim().endsWith("\"")) {
				val = val.trim().substring(0, val.length()-1);
			}
			if(val.contains("^^")) {
				val = val.substring(0, val.indexOf("^^"));
			}
			if(val.contains("\"@en")) {
				val = val.substring(0, val.indexOf("\"@en"));
			}
			if(val.trim().endsWith("\"")) {
				val = val.trim().substring(0, val.length()-1);
			}
			
			attribute = model.getSourceAttributeByVarName(curVar);
			if(attribute!=null) {
				addValue(attribute, val);
			}
			
		}
		return true;
	}

	@Override
	public void addValue(IAttribute attribute, Object value) {
		List<Object> tmpValueSet;
		if(this.valueTable.containsKey(attribute)) {
			tmpValueSet = (List<Object>)this.valueTable.get(attribute);
			//tmpValueSet = new LinkedList<String>();
		} else {
			tmpValueSet = new LinkedList<Object>();
			this.valueTable.put(attribute, tmpValueSet);
		}
		
		if(!tmpValueSet.contains(value)) {
			tmpValueSet.add(value);
		}
		
		if(value instanceof String) {
			Set<String> alts = KnoFussUtils.getAlternativeStringValues((String)value);
			for(String alt : alts) {
				if(!tmpValueSet.contains(alt)) {
					tmpValueSet.add(alt);
				}
			}
		}
		
		
	}

	public Map<IAttribute, List<? extends Object>> getValues() {
		return valueTable;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.fusion.api.impl.IObjectContextWrapper#getValuesById(java.lang.String)
	 */
	@Override
	public List<? extends Object> getValuesByAttribute(IAttribute attribute) {
		
		return attribute.getValuesHavingAttributes(valueTable);

	}
	
	
}
