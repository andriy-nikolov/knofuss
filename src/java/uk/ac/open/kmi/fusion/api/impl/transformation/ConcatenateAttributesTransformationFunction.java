package uk.ac.open.kmi.fusion.api.impl.transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.ITransformationFunction;
import uk.ac.open.kmi.fusion.api.impl.CompositeAttributeValue;

public class ConcatenateAttributesTransformationFunction implements
		ITransformationFunction<String> {

	private static ConcatenateAttributesTransformationFunction INSTANCE = new ConcatenateAttributesTransformationFunction();
	
	private ConcatenateAttributesTransformationFunction() {
		
	}
	
	public static ConcatenateAttributesTransformationFunction getInstance() {
		return INSTANCE;
	}

	@Override
	public List<String> getTransformationResult(List<List<? extends Object>> operands) {
		StringBuffer result = new StringBuffer();
		
		List<String> res = new ArrayList<String>(1);
		for(List<? extends Object> operandList : operands) {
			if(operandList!=null) {
				for(Object operand : operandList) {
					if(operand!=null) {
						result.append((operand.toString()));
						result.append(" ");
					}
				}
			}
		}
		res.add(result.toString());
		return res;
	}

	@Override
	public boolean isSuitableForAttributes(List<IAttribute> attributes) {
		return true;
	}

	
	
}
