package uk.ac.open.kmi.fusion.learning;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IObjectContextModel;
import uk.ac.open.kmi.fusion.api.IObjectContextWrapper;
import uk.ac.open.kmi.fusion.api.impl.AtomicAttribute;
import uk.ac.open.kmi.fusion.api.impl.CompositeAttribute;
import uk.ac.open.kmi.fusion.api.impl.CompositeAttributeValue;
import uk.ac.open.kmi.fusion.learning.cache.CacheEntry;

public class CacheEntryBackedObjectContextWrapper implements
		IObjectContextWrapper {

	CacheEntry entry;
	IObjectContextModel model;
	
	public CacheEntryBackedObjectContextWrapper(CacheEntry entry, IObjectContextModel model) {
		this.entry = entry;
		this.model = model;
	}

	@Override
	public IObjectContextModel getModel() {
		return model;
	}

	@Override
	public void setModel(IObjectContextModel model) {
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
		if(attribute instanceof AtomicAttribute) {
			entry.addValue(((AtomicAttribute) attribute).getPropertyPath(), value);
		}
	}

	@Override
	public List<? extends Object> getValuesByAttribute(IAttribute attribute) {
		return attribute.getValuesHavingPropertyPaths(entry.getValueTable()); 
	}

}
