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
