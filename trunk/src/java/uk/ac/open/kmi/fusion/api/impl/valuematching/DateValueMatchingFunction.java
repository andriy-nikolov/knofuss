package uk.ac.open.kmi.fusion.api.impl.valuematching;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.ac.open.kmi.common.utils.LuceneUtils;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.AttributeType;
import uk.ac.open.kmi.fusion.objectidentification.standard.SimMetricsObjectIdentificationUtils;
import uk.ac.open.kmi.fusion.util.KnoFussDateUtils;

public class DateValueMatchingFunction implements IValueMatchingFunction<String> {

	private static final DateValueMatchingFunction INSTANCE = new DateValueMatchingFunction();
		
	private static AttributeType[][] supportedTypes = {
		{ AttributeType.DATE, AttributeType.DATE }
	};
	
	public DateValueMatchingFunction() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public double getSimilarity(IAttribute attr1, IAttribute attr2, String value1, String value2) {
		Date date1, date2;
		
		try {
			date1 = KnoFussDateUtils.parseDate(value1);
			date2 = KnoFussDateUtils.parseDate(value2);
			if(date1.equals(date2)) {
				return 1.0;
			}
		} catch(ParseException e) {
			return 0.0;
		}
			
		return 0.0;
	}
	
	static DateValueMatchingFunction getInstance() {
		return INSTANCE;
	}

	@Override
	public String toString() {
		return IValueMatchingFunction.DATE;
	}
	
	@Override
	public boolean isSuitableForAttributeTypes(AttributeType attributeType1, AttributeType attributeType2) {
		for(AttributeType[] types : supportedTypes) {
			if(types[0].equals(attributeType1)&&types[1].equals(attributeType2)
					||types[1].equals(attributeType1)&&types[0].equals(attributeType2)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean isSuitableForAttributes(IAttribute attribute1, IAttribute attribute2) {
		for(AttributeType[] types : supportedTypes) {
			if(types[0].equals(attribute1.getType())&&types[1].equals(attribute2.getType())
					||types[1].equals(attribute1.getType())&&types[0].equals(attribute2.getType())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public AttributeType[][] suitableForTypes() {
		return supportedTypes;
	}

	@Override
	public void setAttributeFeatures(IAttribute attr1, IAttribute attr2) {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
