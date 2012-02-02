package uk.ac.open.kmi.fusion.api.impl;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.ICustomTransformationFunction;

public class TransformationFunctionWrapper extends FusionConfigurationObject {

	public static final String TYPE_URI = FusionMetaVocabulary.TRANSFORMATION_FUNCTION;
	
	ICustomTransformationFunction<? extends Object> impl = null;
	String implementingClass;
	Map<String, String> properties;
	
	
	public TransformationFunctionWrapper() {
		init();
	}

	public TransformationFunctionWrapper(Resource rdfIndividual,
			FusionEnvironment environment) {
		super(rdfIndividual, environment);
		init();
	}

	
	private void init() {
		properties = new HashMap<String, String>();
	}



	@Override
	protected void readFromPropertyMember(Statement statement)
			throws RepositoryException {
		super.readFromPropertyMember(statement);
		
		if(statement.getPredicate().toString().equals(FusionMetaVocabulary.HAS_IMPLEMENTING_CLASS)) {
			this.implementingClass = ((Literal)statement.getObject()).stringValue();
		} else {
			String key = statement.getPredicate().toString();
			if(!(key.startsWith(RDF.NAMESPACE)||key.startsWith(RDFS.NAMESPACE))) { 
				Value res = statement.getObject();
				if(res instanceof Literal) {
					properties.put(key, ((Literal)res).stringValue());
				} else if(res instanceof URI) {
					properties.put(key, ((URI)res).toString());
				} 
			}
		}
	}
	
	public boolean isIdenticalTo(FusionConfigurationObject dataInstance) {
		if(dataInstance instanceof ValueMatchingFunctionWrapper) {
			if(this.implementingClass.equals(((ValueMatchingFunctionWrapper)dataInstance).getImplementingClass())) 
					return true;
		}
		return false;
	}

	public String getImplementingClass() {
		return implementingClass;
	}

	public void setImplementingClass(String implementingClass) {
		this.implementingClass = implementingClass;
	}
	
	@SuppressWarnings("unchecked")
	public ICustomTransformationFunction<? extends Object> getImplementation() {
		try {
			if(impl==null) {
				impl = (ICustomTransformationFunction<? extends Object>)Class.forName(implementingClass).getConstructor().newInstance();
				impl.setFunctionDescriptor(this);
			}
			return impl;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Map<String, String> getProperties() {
		return properties;
	}
}
