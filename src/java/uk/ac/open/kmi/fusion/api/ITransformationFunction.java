package uk.ac.open.kmi.fusion.api;

import java.util.List;
import java.util.Map;

import uk.ac.open.kmi.fusion.api.impl.TransformationAttribute;

public interface ITransformationFunction<T> {
	
	public static final String CONCATENATE = "concat";
	
	public List<T> getTransformationResult(TransformationAttribute attr, Map<IAttribute, List<? extends Object>> operands);

	public boolean isSuitableForAttributes(List<IAttribute> attributes);
	
}
