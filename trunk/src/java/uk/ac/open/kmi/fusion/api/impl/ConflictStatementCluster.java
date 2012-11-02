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

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryException;


public class ConflictStatementCluster extends FusionConfigurationObject {

	List<String> sourceStatementDescriptors;
	List<String> targetStatementDescriptors;
	
	List<Statement> conflictingABoxStatements;
	List<Statement> tboxAxioms;
	
	MappingSet mappingSet;
	List<AtomicMapping> atomicMappings;
	
	List<Object> auxiliaryObjects;

	private void init() {
		this.sourceStatementDescriptors = new ArrayList<String>();
		this.targetStatementDescriptors = new ArrayList<String>();
		
		this.conflictingABoxStatements = new ArrayList<Statement>();
		this.tboxAxioms = new ArrayList<Statement>();
		this.atomicMappings = new ArrayList<AtomicMapping>();
		this.auxiliaryObjects = new ArrayList<Object>();
	}
	
	public ConflictStatementCluster() {
		super();
		this.init();
	}
		
	public void addSourceStatementDescriptor(String descriptor) {
		this.addStatementDescriptor(descriptor, this.sourceStatementDescriptors);
	}
	
	public void addTargetStatementDescriptor(String descriptor) {
		this.addStatementDescriptor(descriptor, this.targetStatementDescriptors);
	}
	
	private void addStatementDescriptor(String res, List<String> list) {
		if(!list.contains(res)) list.add(res);
	}

	public List<String> getSourceStatementDescriptors() {
		return sourceStatementDescriptors;
	}

	public void setSourceStatementDescriptors(
			List<String> sourceStatementDescriptors) {
		this.sourceStatementDescriptors = sourceStatementDescriptors;
	}

	public List<String> getTargetStatementDescriptors() {
		return targetStatementDescriptors;
	}

	public void setTargetStatementDescriptors(
			List<String> targetStatementDescriptors) {
		this.targetStatementDescriptors = targetStatementDescriptors;
	}
	
	@Override
	protected void readFromPropertyMember(Statement statement) throws RepositoryException {
		super.readFromPropertyMember(statement);
	}
	
	public double getConflictSignificance() {
		return 0.5;
	}

	public int getConflictType() {
		return 0;
	}
	
	public List<Statement> getConflictingStatements() {
		return this.conflictingABoxStatements;
	}

	public List<Statement> getCandidateConflictingStatements() {
		List<Statement> res = new ArrayList<Statement>();
		for(Statement axiom : this.conflictingABoxStatements) {
			res.add(axiom);
		}
		return res;
	}

	public List<Statement> getMainKBConflictingStatements() {
		List<Statement> res = new ArrayList<Statement>();
		for(Statement axiom : this.conflictingABoxStatements) {
			res.add(axiom);
		}
		return res;
	}
	
	public void addConfictingABoxStatement(Statement axiom) {
		if(!this.conflictingABoxStatements.contains(axiom)) {
			this.conflictingABoxStatements.add(axiom);
		}
	}
	
	
	
	public void addAtomicMapping(AtomicMapping mapping) {
		this.atomicMappings.add(mapping);
		
	}

	public void addConflictingStatement(Statement statement) {
		this.addConfictingABoxStatement(statement);		
	}

	public List<AtomicMapping> getAtomicMappings() {
		return this.atomicMappings;
	}

	public void addTBoxAxiom(Statement axiom) {
		this.tboxAxioms.add(axiom);
	}

	public List<Statement> getTBoxStatements() {
		return this.tboxAxioms;
	}

	public List<? extends Object> getAuxiliaryObjects() {
		return auxiliaryObjects;
	}

	public void addAuxiliaryObject(Object auxiliaryObject) {
		this.auxiliaryObjects.add(auxiliaryObject);
	}

	public MappingSet getMappingSet() {
		return mappingSet;
	}

	public void setMappingSet(MappingSet mapping) {
		this.mappingSet = mapping;
	}

	public List<Statement> getConflictingABoxStatements() {
		return conflictingABoxStatements;
	}

	public List<Statement> getTboxAxioms() {
		return tboxAxioms;
	}

	
}
