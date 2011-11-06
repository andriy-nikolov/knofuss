package uk.ac.open.kmi.fusion.api.impl.valuematching;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.ac.open.kmi.common.utils.LuceneUtils;
import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.AttributeType;
import uk.ac.open.kmi.fusion.objectidentification.standard.SimMetricsObjectIdentificationUtils;

public class DateValueMatchingFunction implements IValueMatchingFunction<String> {

	private static final DateValueMatchingFunction INSTANCE = new DateValueMatchingFunction();
	public static final SimpleDateFormat[] dateFormats = {
		new SimpleDateFormat("yyyy-MM-dd"),
		new SimpleDateFormat("MMMM dd, yyyy"),
		new SimpleDateFormat("M/d/yy"),
		new SimpleDateFormat("MMM d, yyyy"),
		new SimpleDateFormat("yyyy"),
	};
	
	private static AttributeType[][] supportedTypes = {
		{ AttributeType.DATE, AttributeType.DATE }
	};
	
	public DateValueMatchingFunction() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public double getSimilarity(IAttribute attr1, IAttribute attr2, String value1, String value2) {
		Date date1, date2;
		
		date1 = parseDate(value1);
		if(date1!=null) {
			date2 = parseDate(value2);
			if(date2!=null) {
				if(date1.equals(date2)) {
					return 1.0;
				}
			}
		}
			
		return 0.0;
	}
	
	static Date parseDate(String value) {
		Date date;
		for(SimpleDateFormat format : dateFormats) {
			try {
				date = format.parse(value);
				return date;
			} catch(ParseException e) {
				
			}
		}
		return null;
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
