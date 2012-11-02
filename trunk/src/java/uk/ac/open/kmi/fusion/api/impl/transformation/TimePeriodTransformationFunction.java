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
package uk.ac.open.kmi.fusion.api.impl.transformation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.ICustomTransformationFunction;
import uk.ac.open.kmi.fusion.api.ITransformationFunction;
import uk.ac.open.kmi.fusion.api.impl.AtomicAttribute;
import uk.ac.open.kmi.fusion.api.impl.AttributeType;
import uk.ac.open.kmi.fusion.api.impl.CompositeAttribute;
import uk.ac.open.kmi.fusion.api.impl.CompositeAttributeValue;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.api.impl.TransformationAttribute;
import uk.ac.open.kmi.fusion.api.impl.TransformationFunctionWrapper;
import uk.ac.open.kmi.fusion.util.KnoFussDateUtils;
import uk.ac.open.kmi.fusion.util.KnoFussUtils;

public class TimePeriodTransformationFunction implements
		ICustomTransformationFunction<CompositeAttributeValue> {

	private static TimePeriodTransformationFunction INSTANCE = new TimePeriodTransformationFunction();
	private static Logger log = Logger.getLogger(TimePeriodTransformationFunction.class);
	
	// private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
	
	private String dateFormattingString = "yyyy-MM-dd'T'HH:mm:ss";
	
	TransformationFunctionWrapper descriptor;
	
	public TimePeriodTransformationFunction() {
		
	}
	
	public static TimePeriodTransformationFunction getInstance() {
		return INSTANCE;
	}

	@Override
	public synchronized List<CompositeAttributeValue> getTransformationResult(TransformationAttribute top, Map<IAttribute, List<? extends Object>> operands) {
		StringBuffer result = new StringBuffer();
		
		List<CompositeAttributeValue> res = new ArrayList<CompositeAttributeValue>(1);
		CompositeAttributeValue val;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormattingString); 
		
		List<IAttribute> attrs = top.getAttributes();
		
		CompositeAttribute dateRangeAttribute = null;
		List<? extends Object> operandList;
		
		AtomicAttribute hasBeginning = null, hasEnd = null;
		
		for(IAttribute attr : attrs) {
			if(attr instanceof CompositeAttribute) {
				dateRangeAttribute = (CompositeAttribute)attr;
				hasBeginning = (AtomicAttribute)dateRangeAttribute.getAtomicAttributesByPropertyPath().get("<http://linkedevents.org/ontology/atTime>/<http://www.w3.org/2006/time#hasBeginning>/<http://www.w3.org/2006/time#inXSDDateTime>");
				hasEnd = (AtomicAttribute)dateRangeAttribute.getAtomicAttributesByPropertyPath().get("<http://linkedevents.org/ontology/atTime>/<http://www.w3.org/2006/time#hasEnd>/<http://www.w3.org/2006/time#inXSDDateTime>");
				break;
			}
		}
		
		Date originalValue, beginValue, endValue;
		List<String> valueList;
		
		Calendar calendar = GregorianCalendar.getInstance();
		
		if((dateRangeAttribute!=null)&&(hasBeginning!=null)&&(hasEnd!=null)) {
			for(IAttribute attr : attrs) {
				operandList = operands.get(attr);
				if(operandList!=null) {
					if(!operandList.isEmpty()) {
						if(operandList.get(0) instanceof CompositeAttributeValue) {
							res.add((CompositeAttributeValue)operandList.get(0));
							break;
						} else if(operandList.get(0) instanceof String) {
							val = new CompositeAttributeValue(dateRangeAttribute);
							try {
								originalValue = KnoFussDateUtils.parseDate((String)operandList.get(0));
							
								calendar.setTime(originalValue);
								/*calendar.set(Calendar.HOUR_OF_DAY, 0);
								calendar.set(Calendar.MINUTE, 0);
								calendar.set(Calendar.SECOND, 0);
								calendar.set(Calendar.MILLISECOND, 0);*/
								calendar.add(Calendar.HOUR_OF_DAY, -24);
								
								// beginValue = new Date(originalValue.getTime()-86400000);
								beginValue = calendar.getTime();
								try {
									valueList = new ArrayList<String>(1);
									valueList.add(dateFormat.format(beginValue));
									val.getAttributeValues().put(hasBeginning, valueList);
								} catch(ArrayIndexOutOfBoundsException e) {
									e.printStackTrace();
									log.error("Date: "+beginValue.toString());
									throw e;
								}
							
								calendar.setTime(originalValue);
								calendar.add(Calendar.HOUR_OF_DAY, 24);
								
								/*calendar.set(Calendar.HOUR_OF_DAY, 23);
								calendar.set(Calendar.MINUTE, 59);
								calendar.set(Calendar.SECOND, 59);
								calendar.set(Calendar.MILLISECOND, 999);*/
								// endValue = new Date(originalValue.getTime()+86400000);
								endValue = calendar.getTime();
								
								try {
									valueList = new ArrayList<String>(1);
									valueList.add(dateFormat.format(endValue));
									val.getAttributeValues().put(hasEnd, valueList);
							
									res.add(val);
								} catch(ArrayIndexOutOfBoundsException e) {
									e.printStackTrace();
									log.error("Date: "+endValue.toString());
									throw e;
								}
								break;
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		
		return res;
	}

	@Override
	public boolean isSuitableForAttributes(List<IAttribute> attributes) {
		CompositeAttribute compositeAttr;
		for(IAttribute attr : attributes) {
			if(attr instanceof CompositeAttribute) {
				compositeAttr = (CompositeAttribute)attr;
				if(!(compositeAttr.getPropertyPaths().contains("<http://linkedevents.org/ontology/atTime>/<http://www.w3.org/2006/time#hasBeginning>")
						&&(compositeAttr.getPropertyPaths().contains("<http://linkedevents.org/ontology/atTime>/<http://www.w3.org/2006/time#hasEnd>")))) {
					return false;
				}
			} else if(!attr.getType().equals(AttributeType.DATE)) {
				return false;
			}
		}	
		return true;
	}

	@Override
	public String toString() {
		return "time-period";
	}

	@Override
	public void setFunctionDescriptor(TransformationFunctionWrapper descriptor) {
		this.descriptor = descriptor;
	}

	
	
}
