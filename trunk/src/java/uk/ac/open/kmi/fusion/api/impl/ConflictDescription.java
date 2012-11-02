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
