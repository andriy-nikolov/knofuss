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
package uk.ac.open.kmi.fusion.conflictdetection.disjoint;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Map;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Cell;

import uk.ac.open.kmi.fusion.*;
import uk.ac.open.kmi.fusion.api.*;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.ConflictStatementCluster;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.api.impl.FusionMethodWrapper;
import uk.ac.open.kmi.fusion.api.impl.MappingSet;
import uk.ac.open.kmi.fusion.util.SesameUtils;

/**
 * @author an2548
 *
 */
public class DisjointConflictDetector implements IConflictDetectionMethod {

	
	FusionEnvironment environment;
	String descriptorURI;
	Set<URI> restrictedProperties;
	ApplicationContext context;
	
	/**
	 * 
	 */
	public DisjointConflictDetector() {
		// TODO Auto-generated constructor stub
	}

	public List<ConflictStatementCluster> discoverAllConflicts(FusionEnvironment onto) throws RepositoryException {
		List<ConflictStatementCluster> res = new ArrayList<ConflictStatementCluster>();
		for(MappingSet mapping : onto.getMappingSets()) {
			res.addAll(this.discoverConflicts(mapping));
		}
		return res;
	}

	private Map<URI, Set<URI>> fillInd2ConceptMap(Set<URI> inds, RepositoryConnection con) throws RepositoryException {
		Map<URI, Set<URI>> ind2conceptMap = new HashMap<URI, Set<URI>>();
		
		Set<URI> types;
		Set<URI> conceptTypes;
		for(URI ind : inds) {
			types = SesameUtils.getNamedTypes(ind, con, false);
			conceptTypes = new HashSet<URI>();
			for(URI type : types) {
				conceptTypes.add(type);
			}
			ind2conceptMap.put(ind, conceptTypes);
		}
		return ind2conceptMap;
		
	}
	
	public List<ConflictStatementCluster> discoverConflicts(MappingSet mapping) throws RepositoryException {
		List<ConflictStatementCluster> resSet = new ArrayList<ConflictStatementCluster>();
		//Map<IProperty, List<IPropertyMember>> restrictedProperties = new HashMap<IProperty, List<IPropertyMember>>();
		List<Statement> conflictingSet;
		
		RepositoryConnection con;
		
		/*if(FusionEnvironment.isMultiOntologyCase) {
			Alignment disjointAlignment = FusionEnvironment.multiOntologyUtil.getDisjointAlignment();
			
			Map<URI, Set<URI>> sourceInd2ConceptMap = 
				fillInd2ConceptMap(
					mapping.getCandidateIndividuals(), 
					FusionEnvironment.getInstance().getFusionRepositoryConnection());
			
			Map<URI, Set<URI>> targetInd2ConceptMap = fillInd2ConceptMap(
					mapping.getMainKBIndividuals(), 
					FusionEnvironment.getInstance().getMainKbRepositoryConnection());
			
			
			for(URI targetInd : targetInd2ConceptMap.keySet()) {
				Set<URI> targetClassUris = targetInd2ConceptMap.get(targetInd);
				for(URI sourceInd : sourceInd2ConceptMap.keySet()) {
					Set<URI> sourceClassUris = sourceInd2ConceptMap.get(sourceInd);
					for(URI targetClassUri : targetClassUris) {
						try {
							Set<Cell> cells = disjointAlignment.getAlignCells2(new java.net.URI(targetClassUri.toString()));
							for(Cell cell : cells) {
								if(sourceClassUris.contains(cell.getObject1AsURI().toString())) {
									// Conflict!!!
															
								}
							}
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				}
				
			}
			
		}*/
				
		ConflictStatementCluster conflictSet;
		URI propertyInd;
		
		
		return resSet;
	}
	
	private void addToIndividualHashMap(URI key, URI value, HashMap<URI, List<URI>> map) {
		List<URI> list;
		if(map.containsKey(key)) {
			list = map.get(key);
			
		} else {
			list = new ArrayList<URI>();
			map.put(key, list);
		}
		list.add(value);
	}
	
	private void addToConceptHashMap(URI key, URI value, HashMap<URI, List<URI>> map) {
		List<URI> list;
		if(map.containsKey(key)) {
			list = map.get(key);
			
		} else {
			list = new ArrayList<URI>();
			map.put(key, list);
		}
		list.add(value);
	}
	
	private ConflictStatementCluster createConflictSet(URI ind1, URI ind2, URI concept1, URI concept2) {
		ConflictStatementCluster conflictSet = new ConflictStatementCluster();
		
		
		return conflictSet;
	}
	
	private void findConceptChain(URI ind, URI concept, RepositoryConnection con) {
		boolean found = false;
		List<URI> queue = new ArrayList<URI>();
		queue.add(concept);
		int pos = 0;
		URI current;
		while(pos<queue.size()) {
			current = queue.get(pos);
			pos++;
		}
	}
	
	private boolean findPathTo(List<URI> path, URI ind, RepositoryConnection con) throws RepositoryException {
		URI concept = path.get(path.size()-1);
		if(SesameUtils.getNamedTypes(ind, con).contains(concept)) {
			return true;
		}
				
		
		
		path.remove(path.size()-1);
		return false;
	}
	
	public void setApplicationContext(ApplicationContext context) {
		this.context = context;
	}

	public FusionMethodWrapper getDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setMethodDescriptor(FusionMethodWrapper descriptor) {
		// TODO Auto-generated method stub
		
	}

}
 