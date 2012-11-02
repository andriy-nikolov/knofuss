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

import java.util.List;

import uk.ac.open.kmi.fusion.api.IAttribute;
import uk.ac.open.kmi.fusion.api.ICustomValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.AttributeType;
import uk.ac.open.kmi.fusion.api.impl.CompositeAttribute;
import uk.ac.open.kmi.fusion.api.impl.CompositeAttributeValue;
import uk.ac.open.kmi.fusion.api.impl.ValueMatchingFunctionWrapper;

import com.wcohen.ss.MongeElkan;

public class TestEventValueMatchingFunction implements ICustomValueMatchingFunction<CompositeAttributeValue> {

	private static final TestEventValueMatchingFunction INSTANCE = new TestEventValueMatchingFunction(); 
	private static final MongeElkan matcher = new MongeElkan();
	
	private static AttributeType[][] supportedTypes = {
		{ AttributeType.COMPOSITE, AttributeType.COMPOSITE }
		
	};
	
	public TestEventValueMatchingFunction() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public double getSimilarity(IAttribute attr1, IAttribute attr2, CompositeAttributeValue value1, CompositeAttributeValue value2) {
		// return matcher.score(value1.toLowerCase(), value2.toLowerCase());
		
		CompositeAttribute compAttr1 = (CompositeAttribute)attr1;
		CompositeAttribute compAttr2 = (CompositeAttribute)attr2;
		
		compAttr1 = (CompositeAttribute)attr1;
		compAttr2 = (CompositeAttribute)attr2;
		
		List<String> longitudes1 = (List<String>)value1.getAttributeValues().get(compAttr1.getAtomicAttributesByVariable().get("long"));
		List<String> latitudes1 = (List<String>)value1.getAttributeValues().get(compAttr1.getAtomicAttributesByVariable().get("lat"));
		
		List<String> longitudes2 = (List<String>)value2.getAttributeValues().get(compAttr2.getAtomicAttributesByVariable().get("long"));
		List<String> latitudes2 = (List<String>)value2.getAttributeValues().get(compAttr2.getAtomicAttributesByVariable().get("lat"));
		
		if((longitudes1==null)||(latitudes1==null)||(longitudes2==null)||(latitudes2==null)) {
			return 0;
		}
		
		if(longitudes1.isEmpty()||latitudes1.isEmpty()||longitudes2.isEmpty()||latitudes2.isEmpty()) {
			return 0;
		}
		
		double dist = ((Math.abs(Double.parseDouble(longitudes1.get(0))-Double.parseDouble(longitudes2.get(0)))/360)
				+(Math.abs(Double.parseDouble(latitudes1.get(0))-Double.parseDouble(latitudes2.get(0))))/180)/2;
		
		
		return 1-dist;
	}

	static TestEventValueMatchingFunction getInstance() {
		return INSTANCE;
	}

	@Override
	public String toString() {
		return "event-space";
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
		
		if((attribute1 instanceof CompositeAttribute)&&(attribute2 instanceof CompositeAttribute)) {
			return true;
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

	@Override
	public void setFunctionDescriptor(ValueMatchingFunctionWrapper descriptor) {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
