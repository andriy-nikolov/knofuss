package uk.ac.open.kmi.fusion.api;

import java.util.Date;
import java.util.List;

import org.openrdf.model.URI;

import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;

public interface IObjectContextWrapper {

	public abstract ObjectContextModel getModel();

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.fusion.api.impl.IObjectWrapper#setModel(uk.ac.open.kmi.fusion.api.IObjectContextModel)
	 */
	public abstract void setModel(ObjectContextModel model);

	public abstract URI getIndividual();

	public abstract void setIndividual(URI individual);

	public abstract void addValue(IAttribute attribute, Object value);

	public abstract List<? extends Object> getValuesByAttribute(IAttribute attribute);

}