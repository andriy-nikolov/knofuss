package uk.ac.open.kmi.fusion.api;

import java.util.List;

public interface ITransformationFunction<T> {
	
	public T getTransformationResult(List<? extends Object> operands);

	public boolean isSuitableForAttributes(List<IAttribute> attributes);
	
}
