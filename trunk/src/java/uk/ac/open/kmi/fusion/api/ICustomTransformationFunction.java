package uk.ac.open.kmi.fusion.api;

import uk.ac.open.kmi.fusion.api.impl.TransformationFunctionWrapper;

public interface ICustomTransformationFunction<T> extends
		ITransformationFunction<T> {

	public void setFunctionDescriptor(TransformationFunctionWrapper descriptor);
	
}
