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

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.fusion.*;
import uk.ac.open.kmi.fusion.util.SesameUtils;


public class MappingSet extends FusionSet {
	
	List<ConflictStatementCluster> conflicts;
	List<AtomicMapping> atomicMappings;
	Hashtable<URI, List<AtomicMapping>> mappingsMap;
	URI mergedIndividual;
	Set<URI> mainKbIndividuals;
	Set<URI> candidateKbIndividuals;
	
	private static Logger log = Logger.getLogger(MappingSet.class);

	private void init() {
		conflicts = new ArrayList<ConflictStatementCluster>();
		atomicMappings = new ArrayList<AtomicMapping>();
		mappingsMap = new Hashtable<URI, List<AtomicMapping>>();
		mergedIndividual = null;
		mainKbIndividuals = new HashSet<URI>();
		candidateKbIndividuals = new HashSet<URI>();
	}
	
	public MappingSet(FusionMethodWrapper producedBy) {
		super(producedBy);
		init();
	}

	public MappingSet() {
		super();
		init();
	}

	public List<ConflictStatementCluster> getConflicts() {
		return conflicts;
	}
	
	public void addConflict(ConflictStatementCluster conflict) {
		conflicts.add(conflict);
	}

	public List<AtomicMapping> getMappings() {
		return atomicMappings;
	}

	public Map<String, String> mergeIndividuals() throws RepositoryException {
		URI res = null;
		Map<String, String> mappings = new HashMap<String, String>();
		
		int maxprop = 0;
		// Find an individual from the main KB with the maximum number of properties
		res = findTheMostDescriptiveIndividual(
				this.getMainKBIndividuals(), 
				FusionEnvironment.getInstance().getMainKbRepositoryConnection() );
		if(res==null) {
			res = findTheMostDescriptiveIndividual(
					this.getCandidateIndividuals(), 
					FusionEnvironment.getInstance().getFusionRepositoryConnection());
			if(res==null) {
				return null;
			}
		} else {
			res = FusionEnvironment.getInstance().getFusionKbValueFactory().createURI(res.toString());
		}
				
		for(URI ind : this.getMainKBIndividuals()) {
			if(ind.toString().equals(res.toString())) {
				continue;
			}
			mappings.put(ind.toString(), res.toString());			
			/*try {
				mergeIndividuals(res, ind);
			} catch(BOInsertionException e) {
				e.printStackTrace();
			}*/
		}
		
		for(URI ind : this.getCandidateIndividuals()) {
			if(ind.toString().equals(res.toString())) {
				continue;
			}
			mappings.put(ind.toString(), res.toString());
			/*try {
				mergeIndividuals(res, ind);
				//removeIndividual(ind);
			} catch(BOInsertionException e) {
				e.printStackTrace();
			}*/
		}
		this.mergedIndividual = res;
		return mappings;
	}

	public void addMapping(AtomicMapping mapping) {
		
		List<AtomicMapping> mappings;
		for(URI ind : mapping.getMainKBIndividuals()) {
			if(mappingsMap.get(ind)==null) {
				mappings = new ArrayList<AtomicMapping>();
				mappingsMap.put(ind, mappings);
			} else {
				mappings = mappingsMap.get(ind);
			}
			mappings.add(mapping);
			mainKbIndividuals.add(ind);
		}
		for(URI ind : mapping.getCandidateIndividuals()) {
			if(mappingsMap.get(ind)==null) {
				mappings = new ArrayList<AtomicMapping>();
				mappingsMap.put(ind, mappings);
			} else {
				mappings = mappingsMap.get(ind);
			}
			mappings.add(mapping);
			candidateKbIndividuals.add(ind);
		}
		atomicMappings.add(mapping);
	}
	
	public void removeMapping(AtomicMapping mapping) {
		atomicMappings.remove(mapping);
		List<AtomicMapping> mappings;
		for(URI ind : mapping.getMainKBIndividuals()) {
			mappings = this.mappingsMap.get(ind);
			mappings.remove(mapping);
			if(mappings.size()==0) {
				this.mainKbIndividuals.remove(ind);
			}
		}
		for(URI ind : mapping.getCandidateIndividuals()) {
			mappings = this.mappingsMap.get(ind);
			mappings.remove(mapping);
			if(mappings.size()==0) {
				this.candidateKbIndividuals.remove(ind);
			}
		}
	}

	private URI findTheMostDescriptiveIndividual(Set<URI> from, RepositoryConnection con) throws RepositoryException {
		int maxprop = 0;
		URI tmp = null;
		
		// Find an individual from the KB with the maximum number of properties
		Set<Value> propertyVals;
		for(URI ind : from) {
			propertyVals = SesameUtils.getPropertyValues(ind, null, con); 
			if(propertyVals.size()>=maxprop) {
				tmp = ind;
				maxprop = propertyVals.size();
			}
		}
		
		return tmp;
	}
	
	public void removeIndividual(URI ind, RepositoryConnection con) throws RepositoryException {
		Calendar calendarBefore, calendarAfter; 
		calendarBefore = new GregorianCalendar();
		con.remove(ind, null, null);
		
		calendarAfter = new GregorianCalendar();
		log.info("Time cost (deletion): "+(calendarAfter.getTimeInMillis()-calendarBefore.getTimeInMillis()));
	}
	
	public void removeObsoleteIndividuals() {
		if(this.mergedIndividual==null) return;
		for(URI ind : this.getCandidateIndividuals()) {
			if(ind.toString().equals(this.mergedIndividual.toString())) {
				continue;
			}
			try {
				removeIndividual(ind, FusionEnvironment.getInstance().getFusionRepositoryConnection());
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	public URI getMergedIndividual() {
		return mergedIndividual;
	}

	public void setMergedIndividual(URI mergedIndividual) {
		this.mergedIndividual = mergedIndividual;
	}
	
	public URI selectClusterCenter() {
		URI clusterCenter = null;
		
		int maxsize = 0;
		String maxuri = null;
		Map<URI, Integer> paths = new HashMap<URI, Integer>();
		for(URI ind1 : this.getIndividuals()) {
			paths.put(ind1, 0);
			for(URI ind2 : this.getIndividuals()) {
				if(ind1.equals(ind2)) continue;
				List<AtomicMapping> path = new ArrayList<AtomicMapping>();
				if(this.getPath(ind1, ind2, path)) {
					paths.put(ind1, paths.get(ind1)+path.size());
				}
			}
		}
		
		int minpath = -1;
		URI minInd = null;
		for(URI ind : this.getMainKBIndividuals()) {
			if((minInd==null)||(paths.get(ind)<minpath)||(minpath==-1)) {
				minInd = ind;
				minpath = paths.get(ind);
			}
		}
		
		for(URI ind : this.getCandidateIndividuals()) {
			if((minInd==null)||(paths.get(ind)<minpath)||(minpath==-1)) {
				minInd = ind;
				minpath = paths.get(ind);
			}
		}
		
		clusterCenter = minInd;
		/*for(String uri : this.targetUris) {
			if((maxuri==null)||(this.getMappingsByUri(uri).size()>maxsize)) { 
				maxuri = uri;
				maxsize = this.getMappingsByUri(uri).size();
			}
		}
		
		this.clusterCenter = maxuri; */
		return clusterCenter;
	}
	
	private boolean getPath(URI ind1, URI ind2, List<AtomicMapping> path) {
		URI otherInd;
		
		for(AtomicMapping edge : this.mappingsMap.get(ind1)) {
			if(path.contains(edge)) {
				continue;
			}
			path.add(edge);
			if(edge.getIndividuals().contains(ind2)) {
				return true;
			}
			otherInd = null;
			for(URI tmp : edge.getIndividuals()) {
				if(!tmp.equals(ind1)) {
					otherInd = tmp;
					break;
				}
			}
			if(getPath(otherInd, ind2, path)) {
				return true;
			} 
			path.remove(edge);
		}
		
		return false;
	}

	public void addIndividual(URI individual, boolean isCandidate) {
		if(isCandidate) {
			if(!candidateKbIndividuals.contains(individual)) {
				candidateKbIndividuals.add(individual);
			}
		} else {
			if(!mainKbIndividuals.contains(individual)) {
				mainKbIndividuals.add(individual);
			}
		}
		
	}

	protected URI mergeIndividuals(URI ind1, URI ind2)
			throws RepositoryException {
				try {
					int i=0;
					
					RepositoryConnection con;
					if(this.mainKbIndividuals.contains(ind2)) {
						con = FusionEnvironment.getInstance().getMainKbRepositoryConnection();
					} else {
						con = FusionEnvironment.getInstance().getFusionRepositoryConnection();
					}
					List<Statement> stmts = SesameUtils.getStatements(ind2, null, null, con);
								
					Statement addedStatement;
					Calendar calendarBefore, calendarAfter;
					
					for(Statement stmt : stmts) {
						calendarBefore = new GregorianCalendar();
						/*try {
							for(IProvenance provenance : stmt.getProvenances()) {
								if(provenance!=null) {
									
									addedStatement = FusionGlobals.fusionPropertyMemberDao.insert(
											ind1, 
											stmt.getProperty(), 
											stmt.getTarget(),
											provenance);
								} else {
									FusionGlobals.fusionPropertyMemberDao.insert(
											ind1, 
											stmt.getProperty(), 
											stmt.getTarget());
								}
							}
						} catch(ProvenanceUnknownException e) {
							log.info("ProvenanceUnknownException");
							FusionGlobals.fusionPropertyMemberDao.insert(
									ind1, 
									stmt.getProperty(), 
									stmt.getTarget());
						}*/
						calendarAfter = new GregorianCalendar();
						log.info("Statement insertion time cost: "+(calendarAfter.getTimeInMillis()-calendarBefore.getTimeInMillis()));
						i++;
					}
					
					stmts = SesameUtils.getStatements(null, null, ind2, con);
								
					for(Statement stmt : stmts) {
						if(stmt.getPredicate().toString().equals(RDF.TYPE.toString())) {
							continue;
						}
						if(stmt.getSubject() instanceof Resource) {
							calendarBefore = new GregorianCalendar();
							
							calendarAfter = new GregorianCalendar();
							log.info("Statement insertion time cost: "+(calendarAfter.getTimeInMillis()-calendarBefore.getTimeInMillis()));
						}
						
						
						i++;
					}
					log.info("Copied "+i+" statements");
				} catch(NullPointerException e) {
					e.printStackTrace();
				}
				return ind1;
			}
	
	@Override
	public Set<URI> getCandidateIndividuals() {
		
		return this.candidateKbIndividuals;
	}
	
	@Override
	public Set<URI> getMainKBIndividuals() {
	
		return this.mainKbIndividuals;
	}

	
	
}
