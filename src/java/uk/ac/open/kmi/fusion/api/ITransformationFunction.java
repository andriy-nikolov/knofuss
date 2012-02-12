package uk.ac.open.kmi.fusion.api;

import java.util.List;

public interface ITransformationFunction<T> {
	
	public static final String CONCATENATE = "concat";
	
	public List<T> getTransformationResult(List<List<? extends Object>> operands);

	public boolean isSuitableForAttributes(List<IAttribute> attributes);
	
}
