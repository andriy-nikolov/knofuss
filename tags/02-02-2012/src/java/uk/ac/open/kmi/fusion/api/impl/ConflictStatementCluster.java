package uk.ac.open.kmi.fusion.api.impl;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
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
