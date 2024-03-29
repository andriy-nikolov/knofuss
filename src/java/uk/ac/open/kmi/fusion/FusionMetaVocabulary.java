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
package uk.ac.open.kmi.fusion;

import java.util.HashMap;
import java.util.Map;

import uk.ac.open.kmi.fusion.api.impl.AttributeType;

/**
 * 
 * @author andriy.nikolov
 *
 */
public class FusionMetaVocabulary {
	
	private FusionMetaVocabulary() {
		
	}
	
	public static String FUSION_ONTOLOGY_NS = "http://kmi.open.ac.uk/fusion/fusion#";
	
	public static String FUSION_ONTOLOGY_URI = "http://kmi.open.ac.uk/fusion/fusion";
	
	
	public static final String ABBREVIATIONS_FILE = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"abbreviationsFile";
	public static final String AGGREGATION_FUNCTION = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"aggregationFunction";
	
	public static final String APPLICATION_CONTEXT = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"ApplicationContext";
	public static final String ATOMIC_ATTRIBUTE = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"AtomicAttribute";
	public static final String ATOMIC_MAPPING = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"AtomicMapping";
	public static final String ATTRIBUTE_TYPE = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"attributeType";
	public static final String BLOCK_FOR = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"blockFor";
	public static final String COMPOSITE_ATTRIBUTE = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"CompositeAttribute";
	public static final String CONFIGURATION_FILE = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"configurationFile";
	public static final String CONFLICT_DETECTION = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"ConflictDetection";
	public static final String CONFLICT_SET = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"ConflictSet";
	public static final String CONFLICT_STATEMENT_CLUSTER = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"ConflictStatementCluster";
	public static final String CONTAINS_CANDIDATE_KB_INSTANCE = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"containsCandidateKbInstance";
	public static final String CONTAINS_MAIN_KB_INSTANCE = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"containsMainKbInstance";
	public static final String CUT_OFF = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"cutOff";
	public static final String DATA_FILE = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"dataFile";
	public static final String DATASET_MATCHING_SPEC = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"datasetMatchingSpec";
	public static final String DESCRIBES = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"describes";
	public static final String FEATURE_SELECTION = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"FeatureSelection";
	public static final String FILE_DUMP = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"FileDump";
	public static final String FUSION_CONFIGURATION_OBJECT = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"FusionConfigurationObject";
	public static final String FUSION_METHOD = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"FusionMethod";

	public static final String FUZZY_THRESHOLD = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"fuzzyThreshold";
	public static final String GOLD_STANDARD = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"goldStandard";
	public static final String HAS_APPLICATION_CONTEXT = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"hasApplicationContext";
	public static final String HAS_ATTRIBUTE = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"hasAttribute";
	public static final String HAS_BLOCKER = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"hasBlocker";
	public static final String HAS_CAPABILITY = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"hasCapability";
	public static final String HAS_CONFIDENCE = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"hasConfidence";
	public static final String HAS_CONFLICT_DESCRIPTION = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"hasConflictDescription";
	public static final String HAS_CONSTRUCT_QUERY = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"hasConstructQuery";
	
	public static final String HAS_ERRORS = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"hasErrors";
	public static final String HAS_IMPLEMENTING_CLASS = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"hasImplementingClass";
	public static final String HAS_METHOD = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"hasMethod";
	public static final String HAS_NAMESPACE_URI = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"hasNamespaceURI";
	
	public static final String HAS_OBJECT_MODEL = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"hasObjectModel";
	public static final String HAS_RELIABILITY = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"hasReliability";
	public static final String HAS_SELECT_QUERY = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"hasSelectQuery";
	public static final String HAS_TOTAL_RESOURCES_PROCESSED = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"hasTotalResourcesProcessed";
	public static final String HAS_TRANSFORMATION_FUNCTION = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"hasTransformationFunction";
	public static final String ID_FIELD_NAME = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"idFieldName";
	public static final String INCONSISTENCY_RESOLUTION = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"InconsistencyResolution";
	public static final String INSTANCE_MATCHING_SPEC = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"instanceMatchingSpec";
	public static final String INTERMEDIATE_STORE = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"intermediateStore";
	public static final String LANG = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"lang";
	public static final String LINK_SESSION = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"LinkSession";
	
	public static final String LOAD_FROM = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"loadFrom";
	public static final String LUCENE_BIGRAM_DISK_BLOCKER = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"LuceneBigramDiskBlocker";
	
	public static final String LUCENE_BLOCKED_DISK_BLOCKER = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"LuceneBlockedDiskBlocker";
	public static final String LUCENE_BLOCKER = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"LuceneBlocker";
	public static final String LUCENE_DISK_BLOCKER = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"LuceneDiskBlocker";
	public static final String LUCENE_DISK_BLOCKER_ALL_FIELDS = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"LuceneDiskBlockerAllFields";
	public static final String LUCENE_ENHANCED_DISK_BLOCKER = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"LuceneEnhancedDiskBlocker";
	public static final String LUCENE_MEMORY_BLOCKER = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"LuceneMemoryBlocker";
	
	public static final String LUCENE_MEMORY_BLOCKER_ALL_FIELDS = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"LuceneMemoryBlockerAllFields";
	public static final String LUCENE_MEMORY_BLOCKER_NGRAMS = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"LuceneMemoryBlockerNGrams";
	public static final String METRIC = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"metric";
	public static final String NAME = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"name";
	public static final String NGRAM = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"ngram";
	public static final String OBJECT_CONTEXT_MODEL = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"ObjectContextModel";
	public static final String OBJECT_CONTEXT_MODEL_TREE = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"ObjectContextModelTree";
	public static final String OBJECT_IDENTIFICATION = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"ObjectIdentification";
	public static final String ONTOLOGY_MATCHING = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"OntologyMatching";
	public static final String OPTIONAL = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"optional";
	public static final String PAGE_SIZE = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"pageSize";
	public static final String PATH = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"path";
	public static final String PRODUCED_BY = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"producedBy";
	public static final String PROPERTY_PATH = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"propertyPath";
	public static final String PROXY_HOST = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"proxyHost";
	public static final String PROXY_PORT = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"proxyPort";
	public static final String RDFS_REASONING = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"rdfsReasoning";
	public static final String REFRESH = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"refresh";
	public static final String REMOTE_SPARQL_DATA_SOURCE = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"RemoteSPARQLDataSource";
	public static final String RESULTS_FILE = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"resultsFile";
	public static final String RESULTS_FORMAT = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"resultsFormat";
	public static final String SCHEMA_FILE = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"schemaFile";
	public static final String SEARCH_STRATEGY = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"searchStrategy";
	public static final String SESAME_DATA_SOURCE = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"SesameDataSource";
	public static final String SESAME_MEMORY_DATA_SOURCE = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"SesameMemoryDataSource";
	public static final String SESAME_NATIVE_DATA_SOURCE = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"SesameNativeDataSource";
	public static final String SOURCE_ATTRIBUTE = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"sourceAttribute";
	public static final String SOURCE_DATASET = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"sourceDataset";
	public static final String SOURCE_PATH = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"sourcePath";
	public static final String SOURCE_RESTRICTION = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"sourceRestriction";
	public static final String TARGET_ATTRIBUTE = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"targetAttribute";
	public static final String TARGET_DATASET = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"targetDataset";
	public static final String TARGET_PATH = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"targetPath";
	public static final String TARGET_RESTRICTION = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"targetRestriction";
	public static final String THRESHOLD = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"threshold";
	public static final String TRANSFORMATION_ATTRIBUTE = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"TransformationAttribute";
	public static final String TRANSFORMATION_FUNCTION = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"TransformationFunction";
	public static final String URL = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"url";
	
	public static final String VALUE_MATCHING_FUNCTION = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"ValueMatchingFunction";
	//	public static final String HAS_VARIABLE_WEIGHT = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"hasVariableWeight";
	public static final String VARIABLE = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"variable";
	public static final String VARIABLE_COMPARISON = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"variableComparison";
	public static final String VARIABLE_COMPARISON_SPEC = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"VariableComparisonSpec";
	public static final String WEIGHT = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"weight";
	
	public static Map<String, AttributeType> mapAttributeTypeByName;
	
	static {
		mapAttributeTypeByName = new HashMap<String, AttributeType>();
		
		
	}
	
}
