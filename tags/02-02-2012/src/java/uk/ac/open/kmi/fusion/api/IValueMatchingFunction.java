package uk.ac.open.kmi.fusion.api;

import uk.ac.open.kmi.fusion.api.impl.AttributeType;
import uk.ac.open.kmi.fusion.objectidentification.standard.SimMetricsObjectIdentificationUtils;

public interface IValueMatchingFunction<T> {

	public static final String JARO = "jaro";
	public static final String ABBREVIATION = "abbr";
	public static final String JARO_WINKLER = "jaro-winkler";
	public static final String L2_JARO = "l2 jaro";
	public static final String L2_JARO_WINKLER = "l2 jaro-winkler";
	public static final String AVERAGE_JARO_WINKLER = "average jaro-winkler";
	public static final String LEVENSHTEIN = "levenshtein";
	public static final String L2_LEVENSHTEIN = "l2 levenshtein";
	public static final String MONGE_ELKAN = "monge-elkan";
	public static final String L2_MONGE_ELKAN = "l2 monge-elkan";
	public static final String SMITH_WATERMAN = "smith-waterman";
	public static final String L2_SMITH_WATERMAN = "l2 smith-waterman";
	public static final String PERSON_NAME_JARO = "person name jaro";
	public static final String I_SUB = "i-sub";
	public static final String JACCARD = "jaccard";
	public static final String OVERLAP = "overlap";
	public static final String DICE = "dice";
	public static final String DATE = "date";
	public static final String DOUBLE = "double";

	
	public double getSimilarity(IAttribute attr1, IAttribute attr2, T value1, T value2);
			
	public boolean isSuitableForAttributeTypes(AttributeType attributeType1, AttributeType attributeType2);
	public boolean isSuitableForAttributes(IAttribute attribute1, IAttribute attribute2);
	
	public AttributeType[][] suitableForTypes();
	
	public void setAttributeFeatures(IAttribute attr1, IAttribute attr2);
	
}
