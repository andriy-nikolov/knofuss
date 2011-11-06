package uk.ac.open.kmi.fusion.inconsistencyresolution;

import java.util.List;

import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;

public class FunctionalPropertyHandler<IAtomicMapping> {


	public FunctionalPropertyHandler() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public void run(ApplicationContext context, List<IAtomicMapping> mappings) {
		
		String sourcePropertyUri;
		String targetPropertyUri;
		
		context.getProperties().get(FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"sourceProperty");
		context.getProperties().get(FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"targetProperty");
		
		
		
	}

}
