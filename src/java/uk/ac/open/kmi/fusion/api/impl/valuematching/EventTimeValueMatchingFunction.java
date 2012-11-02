/* Copyright (c) 2012, Knowledge Media Institute
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   * Neither the name of the <organization> nor the
 *     names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.open.kmi.fusion.api.impl.valuematching;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.ICustomValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.AttributeType;
import uk.ac.open.kmi.fusion.api.impl.CompositeAttributeValue;
import uk.ac.open.kmi.fusion.api.impl.ValueMatchingFunctionWrapper;
import uk.ac.open.kmi.fusion.util.KnoFussDateUtils;

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
			if(Math.abs(begin1.getTime() - begin2.getTime() ) < 86400000)
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

