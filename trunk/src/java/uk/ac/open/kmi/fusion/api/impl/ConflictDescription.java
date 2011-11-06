/**
 * 
 */
package uk.ac.open.kmi.fusion.api.impl;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;


import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.util.SesameUtils;


/**
 * @author an2548
 *
 */
public class ConflictDescription extends FusionConfigurationObject {
	
	public static final String CARDINALITY_CONFLICT = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"CardinalityConflict";
	public static final String ATOMIC_CONFLICT = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"AtomicConflict";
	public static final String OTHER_CONFLICT = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"OtherConflict";
	public static final String FUNCTIONAL_CARDINALITY_CONFLICT = FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"FunctionalCardinalityConflict";
	String conflictType;


	public ConflictDescription() {
		super();
		conflictType = OTHER_CONFLICT;
	}

	public ConflictDescription(String conflictType) {
		super();
		this.conflictType = conflictType;
	}
	
	public String getConflictType() {
		return conflictType;
	}
	public void setConflictType(String conflictType) {
		this.conflictType = conflictType;
	}
	
	public boolean isIdenticalTo(FusionConfigurationObject dataInstance) {
		ConflictDescription conflictDescription;
		try {
			conflictDescription = (ConflictDescription)dataInstance;
		} catch(ClassCastException e) {
			return false;
		}
		if(!conflictType.equals(conflictDescription.getConflictType()))
			return false;
		return true;
	}

	@Override
	protected void readFromPropertyMember(Statement statement) throws RepositoryException {
		super.readFromPropertyMember(statement);
		if(statement.getPredicate().toString().equals(FusionMetaVocabulary.HAS_CONFLICT_DESCRIPTION)) {
			this.conflictType = statement.getObject().toString();
		}
	}
	
}
