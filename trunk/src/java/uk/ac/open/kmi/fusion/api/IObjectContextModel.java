package uk.ac.open.kmi.fusion.api;

import java.util.List;
import java.util.Map;

import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;

public interface IObjectContextModel {

	public abstract void prepare();

	public abstract ApplicationContext getApplicationContext();

	public abstract void setApplicationContext(
			ApplicationContext applicationContext);

	public abstract String serializeQuerySPARQLSource();

	public abstract String serializeQuerySPARQLTarget();

	public abstract List<IAttribute> getSourceAttributes();

	public abstract List<IAttribute> getTargetAttributes();

	public abstract List<String> getRestrictedTypesSource();

	public abstract List<String> getRestrictedTypesTarget();

	public abstract IAttribute getSourceAttributeByVarName(String varName);

	public abstract IAttribute getTargetAttributeByVarName(String varName);

	public abstract Map<String, IAttribute> getSourceAttributesByVarName();

	public abstract Map<String, IAttribute> getTargetAttributesByVarName();

	public abstract Map<String, IAttribute> getSourceAttributesByPath();

	public abstract Map<String, IAttribute> getTargetAttributesByPath();

	public abstract IAttribute getTargetAttributeByPath(String path);

	public abstract IAttribute getSourceAttributeByPath(String path);

	public abstract void addInstance(IObjectContextWrapper instance);

	public abstract IObjectContextWrapper getInstance(String uri);

	public abstract IObjectContextWrapper getInstance(int index);

	public abstract List<IObjectContextWrapper> getInstances();

}