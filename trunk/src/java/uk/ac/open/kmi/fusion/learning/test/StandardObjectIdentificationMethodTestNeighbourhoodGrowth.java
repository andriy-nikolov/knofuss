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
package uk.ac.open.kmi.fusion.learning.test;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;



import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;


import uk.ac.open.kmi.common.utils.OIComparison;
import uk.ac.open.kmi.common.utils.sparql.MySPARQLParser;
import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.IObjectIdentificationMethod;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.AtomicMapping;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.api.impl.FusionMethodWrapper;
import uk.ac.open.kmi.fusion.api.impl.FusionConfigurationObject;
import uk.ac.open.kmi.fusion.api.impl.ObjectContextModel;
/*import uk.ac.open.kmi.fusion.objectidentification.simmetrics.knowledgebase.*;
import uk.ac.open.kmi.fusion.objectidentification.simmetrics.knowledgebase.distancemeasurement.*;*/
import uk.ac.open.kmi.fusion.learning.GeneticAlgorithmObjectIdentificationMethod;
import uk.ac.open.kmi.fusion.learning.genetic.fitness.UnsupervisedFitness;
import uk.ac.open.kmi.fusion.objectidentification.standard.SimMetricsObjectIdentificationUtils;
import uk.ac.open.kmi.fusion.util.SesameUtils;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;

public class StandardObjectIdentificationMethodTestNeighbourhoodGrowth implements IObjectIdentificationMethod {

	FusionEnvironment fusionEnvironment;
	ObjectContextModel objectContextModel = null;
	ApplicationContext context;
	FusionMethodWrapper descriptor = null;
	
	double threshold;
	
	Logger log = Logger.getLogger(StandardObjectIdentificationMethodTestNeighbourhoodGrowth.class);
	
	String outputFile = null;
	
	boolean selectAll = true;
	
	private String criterion = GeneticAlgorithmObjectIdentificationMethod.CRITERION_NEIGHBOURHOOD_GROWTH;
	
	public StandardObjectIdentificationMethodTestNeighbourhoodGrowth() {
		threshold = SimMetricsObjectIdentificationUtils.default_threshold;
	}
	
	/**
	 * 
	 * <p>
	 * Returns a list of all possible mappings for the environment being processed.
	 * </p>
	 * @param onto
	 * @return 
	 */
	@Override
	public List<AtomicMapping> getAllPossibleMappings(ApplicationContext context) {
		List<AtomicMapping> mappingList = new ArrayList<AtomicMapping>();
		List<AtomicMapping> tmpList;
		
		initProperties();
		
		try {
			ObjectContextModelMatcherTestNeighbourhoodGrowth contextModelMatcher;
			
			contextModelMatcher = new ObjectContextModelMatcherTestNeighbourhoodGrowth();
			
			if(this.outputFile!=null) {
				contextModelMatcher.setOutputFile(outputFile);
			}
			
			contextModelMatcher.setSelectionAll(selectAll);
			
			if(context.isGoldStandardAvailable()) {
				contextModelMatcher.setGoldStandard(context.getGoldStandard().keySet());
			}
			
			contextModelMatcher.setMultiOntologyCase(FusionEnvironment.isMultiOntologyCase);
			for(FusionConfigurationObject object : context.getObjectModels()) {
				if(object instanceof ObjectContextModel) {
					objectContextModel = (ObjectContextModel)object;
					contextModelMatcher.setObjectContextModel(objectContextModel);
					contextModelMatcher.setApplicationContext(context);
					contextModelMatcher.setCriterion(criterion);
					tmpList = contextModelMatcher.execute(objectContextModel.getThreshold(), context.getBlocker());
					for(AtomicMapping mapping : tmpList) {
						mapping.setProducedBy(descriptor);
						mapping.setContextType(objectContextModel.getRestrictedTypesTarget().get(0));
					}
					mappingList.addAll(tmpList);
											
				}
			}
			
			compareWithGoldStandard(context, mappingList);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return mappingList;
	}
	
	private void initProperties() {
		if(descriptor.getProperties().containsKey(FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"outputFile")) {
			this.outputFile = descriptor.getProperties().get(FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"outputFile");
		}
		if(descriptor.getProperties().containsKey(FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"beta")) {
			double beta = Double.parseDouble((descriptor.getProperties().get(FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"beta"))); 
			UnsupervisedFitness.BETA = beta;
		}
		if(descriptor.getProperties().containsKey(FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"selectBy")) {
			String selectBy = descriptor.getProperties().get(FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"selectBy"); 
			if(selectBy.toLowerCase().equals("f-measure")) {
				selectAll = false;
			}
		}
		if(descriptor.getProperties().containsKey(FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"criterion")) {
			this.criterion = descriptor.getProperties().get(FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"criterion");
		}
	}
	
	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public FusionEnvironment getFusionEnvironment() {
		return fusionEnvironment;
	}

	public void setFusionEnvironment(FusionEnvironment pendingOntology) {
		this.fusionEnvironment = pendingOntology;
	}
	
	@Override
	public FusionMethodWrapper getDescriptor() {
		return descriptor;
	}

	@Override
	public void setMethodDescriptor(FusionMethodWrapper descriptor) {
		this.descriptor = descriptor;
	}

	private void compareWithGoldStandard(ApplicationContext context, List<AtomicMapping> resultMappings) {

		if(context.getGoldStandard()!=null) {
		
			Set<String> goldStandardMissed = new HashSet<String>(context.getGoldStandard().keySet());
			
			String key;
			int tp = 0, fp = 0, fn = 0;
			
			for(AtomicMapping mapping : resultMappings) {
				key = mapping.getSourceIndividual().toString()+" : "+mapping.getTargetIndividual().toString();
	
				if(goldStandardMissed.contains(key)) {
					tp++;
					mapping.setCorrect(true);
					goldStandardMissed.remove(key);
				} else {
					fp++;
				}
			}
			
			fn = goldStandardMissed.size();
			
			double precision, recall, f1;
			
			if (tp==0) {
				precision = 0.0;
				recall = 0.0;
				f1 = 0.0;
			} else {
				precision = ((double)tp)/(tp+fp);
				recall = ((double)tp)/(tp+fn);
				f1 = (2*precision*recall)/(precision+recall);
			}
			
			log.info("F1: "+f1+", precision: "+precision+", recall: "+recall);
		}
		
	}
	
	
}
