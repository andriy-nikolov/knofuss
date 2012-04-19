package uk.ac.open.kmi.fusion.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

public class KnoFussDateUtils {
	
	public static final String[] dateFormats = {
		"yyyy-MM-dd'T'HH:mm:ss",
		"yyyy-MM-dd",
		"MMMM dd, yyyy",
		"M/d/yy",
		"MMM d, yyyy",
		"yyyy"
	};
	
	
	private KnoFussDateUtils() {
		
	}
	
	public static Date parseDate(String value) throws ParseException {
		
		return DateUtils.parseDate(value, dateFormats);
		
	}
	
	public static boolean isDate(String value) {
		try {
			parseDate(value);
			return true;
		} catch(ParseException e) {
			return false;
		}
	}

}
