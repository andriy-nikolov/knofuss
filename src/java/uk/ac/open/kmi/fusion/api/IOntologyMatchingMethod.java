package uk.ac.open.kmi.fusion.api;

import java.util.Map;

import org.semanticweb.owl.align.Alignment;

public interface IOntologyMatchingMethod extends IAbstractFusionMethod {

	public Alignment getAlignment();
	
	public Map<String, String> getQueryMapping();
	
}
