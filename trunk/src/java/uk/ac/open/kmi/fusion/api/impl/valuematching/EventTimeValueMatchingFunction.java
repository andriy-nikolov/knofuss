package uk.ac.open.kmi.fusion.api.impl.valuematching;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.wcohen.ss.MongeElkan;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.ICustomValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.AttributeType;
import uk.ac.open.kmi.fusion.api.impl.CompositeAttribute;
import uk.ac.open.kmi.fusion.api.impl.CompositeAttributeValue;
import uk.ac.open.kmi.fusion.api.impl.ValueMatchingFunctionWrapper;
import uk.ac.open.kmi.fusion.objectidentification.standard.SimMetricsObjectIdentificationUtils;
import uk.ac.open.kmi.fusion.util.KnoFussDateUtils;
import uk.ac.open.kmi.fusion.util.KnoFussUtils;

public class EventTimeValueMatchingFunction implements ICustomValueMatchingFunction<CompositeAttributeValue> {

	private static final EventTimeValueMatchingFunction INSTANCE = new EventTimeValueMatchingFunction();
	//private static Logger log = Logger.getLogger(TokenWiseValueMatchingFunction.class);
	private static AttributeType[][] supportedTypes = {
		{ AttributeType.COMPOSITE, AttributeType.COMPOSITE }

	};

	ValueMatchingFunctionWrapper descriptor;

	public EventTimeValueMatchingFunction() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public double getSimilarity(IAttribute attr1, IAttribute attr2, CompositeAttributeValue value1, CompositeAttributeValue value2) {

		@SuppressWarnings("unchecked")
		List<String> beginList1 = (List<String>)value1.getAttributeValues().get(value1.getAttribute().getAtomicAttributesByPropertyPath().get("<http://linkedevents.org/ontology/atTime>/<http://www.w3.org/2006/time#hasBeginning>/<http://www.w3.org/2006/time#inXSDDateTime>"));
		@SuppressWarnings("unchecked")
		List<String> endList1 = (List<String>)value1.getAttributeValues().get(value1.getAttribute().getAtomicAttributesByPropertyPath().get("<http://linkedevents.org/ontology/atTime>/<http://www.w3.org/2006/time#hasEnd>/<http://www.w3.org/2006/time#inXSDDateTime>"));

		@SuppressWarnings("unchecked")
		List<String> beginList2 = (List<String>)value2.getAttributeValues().get(value2.getAttribute().getAtomicAttributesByPropertyPath().get("<http://linkedevents.org/ontology/atTime>/<http://www.w3.org/2006/time#hasBeginning>/<http://www.w3.org/2006/time#inXSDDateTime>"));
		@SuppressWarnings("unchecked")
		List<String> endList2 = (List<String>)value2.getAttributeValues().get(value2.getAttribute().getAtomicAttributesByPropertyPath().get("<http://linkedevents.org/ontology/atTime>/<http://www.w3.org/2006/time#hasEnd>/<http://www.w3.org/2006/time#inXSDDateTime>"));

		if((beginList1==null)||(endList1==null)||(beginList2==null)||(endList2==null))return 0;

		if((beginList1.isEmpty())||(endList1.isEmpty())||(beginList2.isEmpty())||(endList2.isEmpty()))return 0;
		try {
			Date begin1 = KnoFussDateUtils.parseDate(beginList1.get(0));
			Date begin2 = KnoFussDateUtils.parseDate(beginList2.get(0));
			//KnoFussUtils.log.info("begin1 date :"+begin1+"/n");
			//KnoFussUtils.log.info("begin2 date :"+begin2+"/n");
			Date end1 = KnoFussDateUtils.parseDate(endList1.get(0));
			Date end2 = KnoFussDateUtils.parseDate(endList2.get(0));
			//KnoFussUtils.log.info("end1 date :"+end1+"/n");
			//KnoFussUtils.log.info("end2 date :"+end2+"/n");
			if(Math.abs(begin1.getTime() - begin2.getTime() ) < 86400)
			{
				//KnoFussUtils.log.info("time true");
				return 1.0 ;
			}

			else if(Math.min(end1.getTime(), end2.getTime())-Math.max(begin1.getTime(), begin2.getTime())>=0) {
				return 1.0;
			} else {
				return 0.0;
			}

		} catch(ParseException e) {
			return 0.0;
		}

	}

	static EventTimeValueMatchingFunction getInstance() {
		return INSTANCE;
	}

	@Override
	public String toString() {
		return "event-time";
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


		if(!(attribute1.getPropertyPaths().contains("<http://linkedevents.org/ontology/atTime>/<http://www.w3.org/2006/time#hasBeginning>/<http://www.w3.org/2006/time#inXSDDateTime>")
				&&(attribute1.getPropertyPaths().contains("<http://linkedevents.org/ontology/atTime>/<http://www.w3.org/2006/time#hasEnd>/<http://www.w3.org/2006/time#inXSDDateTime>")))) {
			return false;
		}

		if(!(attribute2.getPropertyPaths().contains("<http://linkedevents.org/ontology/atTime>/<http://www.w3.org/2006/time#hasBeginning>/<http://www.w3.org/2006/time#inXSDDateTime>")
				&&(attribute2.getPropertyPaths().contains("<http://linkedevents.org/ontology/atTime>/<http://www.w3.org/2006/time#hasEnd>/<http://www.w3.org/2006/time#inXSDDateTime>")))) {
			return false;
		}
		return true;

	}

	@Override
	public AttributeType[][] suitableForTypes() {
		return supportedTypes;
	}

	@Override
	public void setAttributeFeatures(IAttribute attr1, IAttribute attr2) {

	}

	@Override
	public void setFunctionDescriptor(ValueMatchingFunctionWrapper descriptor) {

		this.descriptor = descriptor;
	}



}

