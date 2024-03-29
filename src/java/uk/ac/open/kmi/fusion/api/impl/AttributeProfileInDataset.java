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
package uk.ac.open.kmi.fusion.api.impl;

import java.text.ParseException;

import org.apache.log4j.Logger;

import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.fusion.util.KnoFussDateUtils;

public class AttributeProfileInDataset {
	
	String propertyPath;
	
	double averageNumberOfTokens = 0;
	double averagePerIndividual = 0;
	boolean isDouble = true;
	boolean isInteger = true;
	boolean isDate = false;
	int mentionedIn = 0;
	int propertyCount = 0;
	double percentageOfDates = 0;
	double percentageOfDoubles = 0;
	
	double min = 0;
	double max = 0;
	
	boolean summarized = false;
	
	Logger log = Logger.getLogger(AttributeProfileInDataset.class);
	
	public AttributeProfileInDataset(String propertyPath) {
		this.propertyPath = propertyPath;
	}
	
	public AtomicAttribute createAttribute() {
		AtomicAttribute attribute = new AtomicAttribute(propertyPath);
		if(isInteger) {		
			attribute.setType(AttributeType.INTEGER);
		} else if(isDouble) {
			attribute.setType(AttributeType.CONTINUOUS);
			if(this.propertyPath.contains(Utils.WGS84_NS+"long")) {
				attribute.setMax(180);
				attribute.setMin(-180);
			} else if(this.propertyPath.contains(Utils.WGS84_NS+"lat")) {
				attribute.setMax(90);
				attribute.setMin(-90);
			} else {
				attribute.setMax(max);
				attribute.setMin(min);
			}
		} else if(isDate) {
			attribute.setType(AttributeType.DATE);
		} else if(averageNumberOfTokens>10) {
			attribute.setType(AttributeType.LONG_TEXT);
		} else if(averageNumberOfTokens<=1.01) {
			attribute.setType(AttributeType.NOMINAL);
		} else {
			attribute.setType(AttributeType.NOMINAL_MULTI_TOKEN);
		}
		return attribute;
	}
	
	public void summarize() {
		if(propertyCount>0) {
			this.averageNumberOfTokens = this.averageNumberOfTokens/this.propertyCount;
			this.averagePerIndividual = ((double)this.propertyCount)/this.mentionedIn;
			this.percentageOfDates = this.percentageOfDates/this.propertyCount;
			this.percentageOfDoubles = this.percentageOfDoubles/this.propertyCount;
			if(percentageOfDates>0.4) {
				setDate(true);
			}
			if(percentageOfDoubles>0.4) {
				setDouble(true);
			} else {
				setDouble(false);
			}
		}
		
		summarized = true;
	}

	public String getPropertyPath() {
		return propertyPath;
	}

	public void setPropertyPath(String propertyPath) {
		this.propertyPath = propertyPath;
	}

	public double getAverageNumberOfTokens() {
		return averageNumberOfTokens;
	}

	public void setAverageNumberOfTokens(double averageNumberOfTokens) {
		this.averageNumberOfTokens = averageNumberOfTokens;
	}

	public boolean isDouble() {
		return isDouble;
	}

	public void setDouble(boolean isDouble) {
		this.isDouble = isDouble;
	}

	public boolean isInteger() {
		return isInteger;
	}

	public void setInteger(boolean isInteger) {
		this.isInteger = isInteger;
	}

	public int getPropertyCount() {
		return propertyCount;
	}

	public void setPropertyCount(int propertyCount) {
		this.propertyCount = propertyCount;
	}

	public int getMentionedIn() {
		return mentionedIn;
	}

	public void setMentionedIn(int mentionedIn) {
		this.mentionedIn = mentionedIn;
	}
	
	public void increaseMentionedIn() {
		this.mentionedIn++;
	}
	
	public void increasePropertyCount() {
		this.propertyCount++;
	}

	public boolean isDate() {
		return isDate;
	}

	public void setDate(boolean isDate) {
		this.isDate = isDate;
	}
	
	public void doTypeChecking(String val) {
		
		if(isInteger()) {
			try {
				Integer.parseInt(val);
			} catch(NumberFormatException e) {
				setInteger(false);
			}
		}
		
		try {
			double d = Double.parseDouble(val);
			min = Math.min(d, min);
			max = Math.max(d, max);
			percentageOfDoubles++;
		} catch(NumberFormatException e) {
			//setDouble(false);
				
		}
		
		if(!isDate()) {
			try {
				boolean isDate = false;
				
				try {
					KnoFussDateUtils.parseDate(val);
					isDate = true;
				
				} catch(ParseException e) {
					
				}
				
				if(isDate) {
					
					
					percentageOfDates++;
				}
			} catch(NumberFormatException e) {
				//setDouble(false);
			}
		}
		
	}
	

}
