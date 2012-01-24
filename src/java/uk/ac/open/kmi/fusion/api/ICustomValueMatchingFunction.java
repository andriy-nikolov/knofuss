package uk.ac.open.kmi.fusion.api;

import uk.ac.open.kmi.fusion.api.impl.ValueMatchingFunctionWrapper;

public interface ICustomValueMatchingFunction<T> extends
		IValueMatchingFunction<T> {

	public void setFunctionDescriptor(ValueMatchingFunctionWrapper descriptor);
	
}
