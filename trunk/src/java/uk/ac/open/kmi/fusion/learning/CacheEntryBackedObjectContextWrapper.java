package uk.ac.open.kmi.fusion.learning;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IObjectContextWrapper;
import uk.ac.open.kmi.fusion.api.impl.AtomicAttribute;
import uk.ac.open.kmi.fusion.api.impl.CompositeAttribute;
import uk.ac.open.kmi.fusion.api.impl.CompositeAttributeValue;
import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;
import uk.ac.open.kmi.fusion.learning.cache.CacheEntry;

public class CacheEntryBackedObjectContextWrapper implements
		IObjectContextWrapper {

	CacheEntry entry;
	ObjectContextModel model;
	// Map<String, String> variablePropertyMap;
	
	public CacheEntryBackedObjectContextWrapper(CacheEntry entry, ObjectContextModel model) {
		this.entry = entry;
		this.model = model;
		// this.variablePropertyMap = variablePropertyMap;
	}

	@Override
	public ObjectContextModel getModel() {
		// TODO Auto-generated method stub
		return model;
	}

	@Override
	public void setModel(ObjectContextModel model) {
		this.model = model;

	}

	@Override
	public URI getIndividual() {
		
		return entry.getUri();
	}

	@Override
	public void setIndividual(URI individual) {
		entry.setUri(individual);

	}

	@Override
	public void addValue(IAttribute attribute, Object value) {
		//if(variablePropertyMap.containsKey(varName)) {
		if(attribute instanceof AtomicAttribute) {
			entry.addValue(((AtomicAttribute) attribute).getPropertyPath(), value);
		}
		//}
		
	}

	@Override
	public List<? extends Object> getValuesByAttribute(IAttribute attribute) {
		if(attribute instanceof AtomicAttribute) {
			return entry.getValueTable().get(((AtomicAttribute) attribute).getPropertyPath());
		} else {
			List<CompositeAttributeValue> tmpList = new ArrayList<CompositeAttributeValue>(1);
			tmpList.add(CompositeAttributeValue.createCompositeAttributeValueHavingPropertyPaths((CompositeAttribute)attribute, this.entry.getValueTable()));
			return tmpList;
		}
	}

}
