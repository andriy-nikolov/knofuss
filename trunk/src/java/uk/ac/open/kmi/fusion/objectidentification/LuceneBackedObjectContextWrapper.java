package uk.ac.open.kmi.fusion.objectidentification;

import java.util.*;


import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import uk.ac.open.kmi.fusion.api.*;
import uk.ac.open.kmi.fusion.api.impl.*;
import uk.ac.open.kmi.fusion.index.LuceneDiskIndexer;


public class LuceneBackedObjectContextWrapper extends ObjectContextWrapper {
	
	public LuceneBackedObjectContextWrapper() {
		super();
	}
	
	public LuceneBackedObjectContextWrapper(ObjectContextModel model) {
		super(model);
	}
	
	public boolean getPropertiesFromLuceneIndexedDocument(Document doc, URI individual) {
		
		this.individual = individual; 
		Map<String, IAttribute> variablePropertyMap = model.getTargetAttributesByVarName();
		this.individual = FusionEnvironment.getInstance().getMainKbValueFactory().createURI(doc.get("uri"));
		IAttribute attribute;
		for(String var : variablePropertyMap.keySet()) {
			attribute = ((AtomicAttribute) variablePropertyMap.get(var));
				
			String[] values = doc.getValues(((AtomicAttribute)attribute).getPropertyPath());
				
			if(values==null) continue;
			if(values.length==0) continue;
			for(String val : values) {
				if(val.trim().startsWith("\""))
					val = val.trim().substring(1);
				if(val.trim().endsWith("\"")) {
					val = val.trim().substring(0, val.length()-1);
				}
				if(val.contains("^^")) {
					val = val.substring(0, val.indexOf("^^"));
				}
				if(val.contains("\"@en")) {
					val = val.substring(0, val.indexOf("\"@en"));
				}
				if(val.trim().endsWith("\"")) {
					val = val.trim().substring(0, val.length()-1);
				}
				this.addValue(attribute, val);
			}
			
		}
		return true;
		
	}
	
}
