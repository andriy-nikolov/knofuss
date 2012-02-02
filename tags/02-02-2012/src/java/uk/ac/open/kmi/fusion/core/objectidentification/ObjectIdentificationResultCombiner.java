package uk.ac.open.kmi.fusion.core.objectidentification;


import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.*;
import uk.ac.open.kmi.fusion.api.impl.*;
import uk.ac.open.kmi.fusion.core.*;
import uk.ac.open.kmi.fusion.util.FusionException;
import uk.ac.open.kmi.fusion.util.SesameUtils;

import java.util.*;
import java.io.*;

import org.apache.log4j.Logger;
import org.openrdf.model.BNode;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

public class ObjectIdentificationResultCombiner extends ResultCombiner {

	List<AtomicMapping> atomicMappings;
	List<List<AtomicMapping>> competingGroupsList;
	
	Map<URI, List<AtomicMapping>> edgeTable;
	Map<AtomicMapping, List<URI>> vertexTable;
	List<MappingSet> clusters;
	Map<URI, Set<AtomicMapping>> clustersByUri;
	List<Set<AtomicMapping>> clusterSets;
	
	private static Logger log = Logger.getLogger(ObjectIdentificationResultCombiner.class);

	public ObjectIdentificationResultCombiner() {
		super();
	}

	@Override
	public void execute() {
		init();
		//fillMappingGraph();
		this.atomicMappings.addAll(this.getTaskHandler().getDispatcher().getFusionEnvironment().getAtomicMappings());
		try {
			countProducedResults(FusionEnvironment.getInstance().getFusionKbValueFactory().createURI(FusionMetaVocabulary.FUSION_ONTOLOGY_NS+"AtomicMapping"));
			logProducedResults();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		//checkLinkedMappings();
		
		//selectCompetingResults();
		//resolveConflicts();
		
		//createClusters();
		createClustersLight();
		
		for(MappingSet cluster : clusters) {
			
			FusionEnvironment.getInstance().addMappingSet(cluster);
			
		}
	}
	
	private void init() {
		edgeTable = new HashMap<URI, List<AtomicMapping>>();
		vertexTable = new HashMap<AtomicMapping, List<URI>>();
		competingGroupsList = new ArrayList<List<AtomicMapping>>();
		atomicMappings = new ArrayList<AtomicMapping>();
		clusters = new ArrayList<MappingSet>();
		clustersByUri = new HashMap<URI, Set<AtomicMapping>>();
		clusterSets = new ArrayList<Set<AtomicMapping>>();
	}
	
	private void fillMappingGraph() {
		this.atomicMappings.addAll(this.getTaskHandler().getDispatcher().getFusionEnvironment().getAtomicMappings());
		List<AtomicMapping> edgeList;
		List<URI> vertexList;
		for(AtomicMapping mapping : this.atomicMappings) {
			vertexList = new ArrayList<URI>();
			for(URI ind : mapping.getIndividuals()) {
				if(edgeTable.containsKey(ind)) {
					edgeList = edgeTable.get(ind);
					edgeList.add(mapping);
				} else {
					edgeList = new ArrayList<AtomicMapping>();
					edgeList.add(mapping);
					edgeTable.put(ind, edgeList);
				}
				vertexList.add(ind);
			}
			vertexTable.put(mapping, vertexList);
		}
	}
	
	protected void selectCompetingResults() {
		List<AtomicMapping> conflictingGroup;
		Map<String, AtomicMapping> mappingSets;
		List<URI> allInds = new ArrayList<URI>();
		allInds.addAll(edgeTable.keySet());
		URI ind1, ind2;
		for(int i=0;i<allInds.size();i++) {
			ind1 = allInds.get(i);
			for(int j=0;j<i-1;j++) {
				if(i==j) {
					continue;
				}
				ind2 = allInds.get(j);
				conflictingGroup = getIntersection(edgeTable.get(ind1), edgeTable.get(ind2));
				if(conflictingGroup.size()>1) {
					// save group
					competingGroupsList.add(conflictingGroup);
				}
			}
		}
	}
	
	protected List<AtomicMapping> getIntersection(List<AtomicMapping> list1, List<AtomicMapping> list2) {
		List<AtomicMapping> intersection = new ArrayList<AtomicMapping>();
		intersection.addAll(list1);
		intersection.retainAll(list2);
		return intersection;
	}
	
	protected void resolveConflicts() {
		for(List<AtomicMapping> atomicMappingList : competingGroupsList) {
			processConflictGroup(atomicMappingList);
		}
	}
	
	protected void processConflictGroup(List<AtomicMapping> conflictMappings) {
		int i;
		AtomicMapping atomicMapping;
		// FusionMethodWrapper descriptor = new FusionMethodWrapper();
		double curReliability, maxReliability;
	
		maxReliability = 0;
		curReliability = 0;
		AtomicMapping bestOption = null;
		for(AtomicMapping mapping : conflictMappings) {
			curReliability = mapping.getConfidence();
			if(curReliability>maxReliability) {
				bestOption = mapping;
				maxReliability = curReliability;
			}
		}
		for(AtomicMapping mapping : conflictMappings) {
			if(mapping!=bestOption) {
				this.getTaskHandler().getDispatcher().getFusionEnvironment().removeAtomicMapping(mapping);
			}
		}
	}
	
	private void createClustersLight() {
		MappingSet curCluster;
		AtomicMapping tmp;
		
		for(AtomicMapping mapping : this.atomicMappings) {
			curCluster = new MappingSet();
			curCluster.addMapping(mapping);
			clusters.add(curCluster);
		}
	}
	
	protected void createClusters() {
		MappingSet curCluster;
		AtomicMapping tmp;
		/*List<IAtomicMapping> unprocessedMappings = new ArrayList<IAtomicMapping>();
		unprocessedMappings.addAll(this.atomicMappings);
		while(!unprocessedMappings.isEmpty()) {
			tmp = unprocessedMappings.get(0);
			curCluster = new MappingSet();
			addEdgeToCluster(tmp, unprocessedMappings, curCluster);
			clusters.add(curCluster);
		}*/
		
		List<URI> tmpInds = new ArrayList<URI>();
		Set<AtomicMapping> list1;
		Set<AtomicMapping> list2;
		Set<AtomicMapping> cluster;
		for(AtomicMapping mapping : this.atomicMappings) {
			
			tmpInds.clear();
			
			tmpInds.addAll(mapping.getIndividuals());
			
			
			if(tmpInds.size()!=2) {
				throw new RuntimeException("Atomic mapping covering "+tmpInds.size()+" individuals");
			}
			
			list1 = this.clustersByUri.get(tmpInds.get(0));
			list2 = this.clustersByUri.get(tmpInds.get(1));
			if((list1!=null)&&(list2!=null)) {
				if(list1!=list2) {
					cluster = new HashSet<AtomicMapping>(list1.size()+list2.size()+1);
					cluster.addAll(list1);
					cluster.addAll(list2);
					clustersByUri.remove(tmpInds.get(0));
					clustersByUri.remove(tmpInds.get(1));
					clustersByUri.put(tmpInds.get(0), cluster);
					clustersByUri.put(tmpInds.get(1), cluster);
					//log.debug("passed6");
					//clusters.remove(list1);
					//clusters.remove(list2);
					//clusters.add(cluster);
					clusterSets.remove(list1);
					clusterSets.remove(list2);
					clusterSets.add(cluster);
				} else {
					cluster = list1;
				}
			} else if((list1!=null)&&(list2==null)) {
				cluster = list1;
				clustersByUri.put(tmpInds.get(1), cluster);
			} else if((list2!=null)&&(list1==null)) {
				cluster = list2;
				clustersByUri.put(tmpInds.get(0), cluster);
			} else {
				cluster = new HashSet<AtomicMapping>();
				clustersByUri.put(tmpInds.get(0), cluster);
				clustersByUri.put(tmpInds.get(1), cluster);
				clusterSets.add(cluster);
			}
			cluster.add(mapping);
		}
		
		for(Set<AtomicMapping> clusterSet : clusterSets) {
			curCluster = new MappingSet();
			for(AtomicMapping mapping : clusterSet) {
				curCluster.addMapping(mapping);
			}
			clusters.add(curCluster);
		}
		
		log.info("Clusters formed: "+clusters.size());
		/*int i;
		for(IMappingSet cluster : clusters) {
			i = cluster.getMappings().size();
			if(i>1) {
				for(INamedIndividual ind : cluster.getIndividuals()) {
					if(ind.getOntology().equals(FusionGlobals.fusionSession.getOntology())) {
						PersistenceUtil.setDaoManager(FusionGlobals.fusionSession.getDaoManager());
					} else {
						PersistenceUtil.setDaoManager(FusionGlobals.mainKbSession.getDaoManager());
					}
				}
			}
		} */
	}
	
	private void addEdgeToCluster(AtomicMapping edge, List<AtomicMapping> unprocessedMappings, MappingSet curCluster) {
		unprocessedMappings.remove(edge);
		curCluster.addMapping(edge);
		for(URI ind : edge.getIndividuals()) {
			for(AtomicMapping chained : this.edgeTable.get(ind)) {
				if(unprocessedMappings.contains(chained)) {
					addEdgeToCluster(chained, unprocessedMappings, curCluster);
				}
			}
		}
	}
	
	private Set<URI> getLinkedIndividualsFrom(URI ind, RepositoryConnection connection) {
		//log.debug("In getLinkedIndividualsFrom("+ind.getUri()+")");
		/*if(ind.getOntology().equals(FusionGlobals.mainKbSession.getOntology())) {
			PersistenceUtil.setDaoManager(FusionGlobals.mainKbSession.getDaoManager());
		} else {
			PersistenceUtil.setDaoManager(FusionGlobals.fusionSession.getDaoManager());
		}*/
		
		Set<URI> res = new HashSet<URI>();
		
		
		try {
			List<Statement> stmts = SesameUtils.getObjectPropertyStatements(ind, null, null, connection);
			//List<Statement> stmts = SesameUtils.getObjectPropertyStatements(ind, null, null, connection);
			BNode bnode;
			for(Statement stmt : stmts) {
				if(stmt.getPredicate().equals(RDF.TYPE)) continue;
				if(stmt.getObject() instanceof URI) {
					res.add((URI)stmt.getObject());
				} else if(stmt.getObject() instanceof BNode) {
					bnode = (BNode)stmt.getObject();
					try {
						for(Statement stmt2 : SesameUtils.getObjectPropertyStatements(bnode, null, null, connection)) {
							if(stmt2.getObject() instanceof URI) {
								if(!((URI)stmt2.getObject()).toString().equals(ind.toString())) {
									res.add((URI)stmt2.getObject());
								}
							}
						}
					} catch(RepositoryException e) {
						e.printStackTrace();
					}
				}
			}
			for(Statement stmt : stmts) {
				if(stmt.getPredicate().equals(RDF.TYPE)) continue;
				if(stmt.getObject() instanceof URI) {
					res.add((URI)stmt.getObject());
				} else if(stmt.getObject() instanceof BNode) {
					bnode = (BNode)stmt.getObject();
					try {
						for(Statement stmt2 : SesameUtils.getObjectPropertyStatements(bnode, null, null, connection)) {
							if(stmt2.getObject() instanceof URI) {
								if(!((URI)stmt2.getObject()).toString().equals(ind.toString())) {
									res.add((URI)stmt2.getObject());
								}
							}
						}
					} catch(RepositoryException e) {
						e.printStackTrace();
					}
				}
			}
		} catch(RepositoryException e) {
			e.printStackTrace();
		}
		//PersistenceUtil.setDaoManager(FusionGlobals.fusionSession.getDaoManager());
		//log.debug("Out getLinkedIndividualsFrom("+ind.getUri()+")");
		return res;
	}
	
	private Set<URI> getLinkedIndividualsTo(URI ind, RepositoryConnection con) {
		//log.debug("In getLinkedIndividualsTo("+ind.getUri()+")");
		/*if(ind.getOntology().equals(FusionGlobals.mainKbSession.getOntology())) {
			PersistenceUtil.setDaoManager(FusionGlobals.mainKbSession.getDaoManager());
		} else {
			PersistenceUtil.setDaoManager(FusionGlobals.fusionSession.getDaoManager());
		}*/
		
		Set<URI> res = new HashSet<URI>();
		try {
			List<Statement> stmts = SesameUtils.getStatements(null, null, ind, con); 
			
			BNode bnode;
			
			for(Statement stmt : stmts) {
				if(stmt.getSubject() instanceof URI) {
					res.add((URI)stmt.getSubject());
				} else if (stmt.getSubject() instanceof BNode){
					bnode = (BNode)stmt.getSubject();
					try {
						for(Statement stmt2 : SesameUtils.getStatements(null, null, bnode, con)) {
							if(stmt2.getSubject() instanceof URI) {
								if(!((URI)stmt2.getSubject()).toString().equals(ind.toString())) {
									res.add((URI)stmt2.getSubject());
								}
							}
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		//PersistenceUtil.setDaoManager(FusionGlobals.fusionSession.getDaoManager());
		//log.debug("Out getLinkedIndividualsTo("+ind.getUri()+")");
		return res;
	}
	
	private void checkLinkedMappings() {
		List<URI> listMapped, listMappedToLinked, listOverlapped;;
		Set<URI> listLinked1, listLinked2;
		List<AtomicMapping> listOtherMappings;
		listOverlapped = new ArrayList<URI>();
		listMappedToLinked = new ArrayList<URI>();
		Set<AtomicMapping> checkedMappings = new HashSet<AtomicMapping>();
		Set<AtomicMapping> checkResults;
		
		Set<AtomicMapping> comemberMappings = new HashSet<AtomicMapping>();
		
		Map<URI, Set<URI>> comemberMap = new HashMap<URI, Set<URI>>();
		
		int confirmedMappingsFound = 0;
		int i=0;
		try {
			PrintWriter writer = null;
			/*if(FusionEnvironment.debug) {
				writer = new PrintWriter(new FileWriter("D:\\Work\\Dataset\\confirmed_mappings.xml"));
				writer.println("<?xml version=\"1.0\" encoding=\"windows-1251\"?>");
				writer.println("<DocElement>");
				writer.println("<ResultSet id=\"http://www.w3.org/2002/07/owl#Thing\">");
			}*/
			for(AtomicMapping mapping : this.atomicMappings) {
				log.info((i++) + " out of "+this.atomicMappings.size());
				if(checkedMappings.contains(mapping))continue;
				listMapped = this.vertexTable.get(mapping);
				URI ind1, ind2;
				if(listMapped.size()==2) {
					ind1 = listMapped.get(0);
					ind2 = listMapped.get(1);
					if(mapping.getCandidateIndividuals().contains(ind1)) {
						listLinked1 = getLinkedIndividualsFrom(ind1, FusionEnvironment.getInstance().getFusionRepositoryConnection());
					} else {
						listLinked1 = getLinkedIndividualsFrom(ind1, FusionEnvironment.getInstance().getMainKbRepositoryConnection());
					}
					if(mapping.getCandidateIndividuals().contains(ind2)) {
						listLinked2 = getLinkedIndividualsFrom(ind2, FusionEnvironment.getInstance().getFusionRepositoryConnection());
					} else {
						listLinked2 = getLinkedIndividualsFrom(ind2, FusionEnvironment.getInstance().getMainKbRepositoryConnection());
					}
					fillComemberMap(comemberMap, listLinked1);
					fillComemberMap(comemberMap, listLinked2);
					//listLinked1.addAll(getLinkedIndividualsTo(ind1));
					
					checkResults = isConfirmed(listLinked1, listLinked2);
					if(checkResults.size()>0) {
						confirmedMappingsFound++;
						confirmedMappingsFound+=checkResults.size();
						checkedMappings.addAll(checkResults);
						checkedMappings.add(mapping);
						continue;
					}
					/*listLinked1 = getLinkedIndividualsTo(ind1);
					listLinked2 = getLinkedIndividualsTo(ind2);
					log.debug("linkedTo: "+listLinked1.size()+":"+listLinked2.size());
					checkResults = isConfirmed(listLinked1, listLinked2);
					if(checkResults.size()>0) {
						confirmedMappingsFound++;
						checkedMappings.addAll(checkResults);
						checkedMappings.add(mapping);
						continue;
						//((AtomicMapping)mapping).writeToXML(writer);
					}*/
				}
			}
			comemberMappings = this.getComemberMappings(comemberMap);
			/*if(FusionEnvironment.debug) {
				for(AtomicMapping tmpMapping : checkedMappings) {
					((AtomicMapping)tmpMapping).writeToXML(writer);
				}
				writer.println("</ResultSet>");
				writer.println("</DocElement>");
				writer.close();
				writer = new PrintWriter(new FileWriter("D:\\Work\\Dataset\\unconfirmed_mappings.xml"));
				writer.println("<?xml version=\"1.0\" encoding=\"windows-1251\"?>");
				writer.println("<DocElement>");
				writer.println("<ResultSet id=\"http://www.w3.org/2002/07/owl#Thing\">");
				for(AtomicMapping tmpMapping : this.atomicMappings) {
					if(checkedMappings.contains(tmpMapping))continue;
					((AtomicMapping)tmpMapping).writeToXML(writer);
				}
				writer.println("</ResultSet>");
				writer.println("</DocElement>");
				writer.close();
				writer = new PrintWriter(new FileWriter("D:\\Work\\Dataset\\comember_mappings.xml"));
				writer.println("<?xml version=\"1.0\" encoding=\"windows-1251\"?>");
				writer.println("<DocElement>");
				writer.println("<ResultSet id=\"http://www.w3.org/2002/07/owl#Thing\">");
				for(AtomicMapping tmpMapping : comemberMappings) {
					((AtomicMapping)tmpMapping).writeToXML(writer);
				}
				writer.println("</ResultSet>");
				writer.println("</DocElement>");
				writer.close();
			}*/
		} catch(Exception e) {
			e.printStackTrace();
		}
		log.info("Confirmed mappings found: "+confirmedMappingsFound + " out of total: "+atomicMappings.size());
		log.info("Co-member mappings found: "+comemberMappings.size() + " out of total: "+atomicMappings.size());		
	}
	
	private Set<AtomicMapping> isConfirmed(Set<URI> listLinked1, Set<URI> listLinked2) {
		//log.debug("In isConfirmed");
		List<URI> listOverlapped = new ArrayList<URI>();
		List<AtomicMapping> listOtherMappings;
		List<URI> listMappedToLinked = new ArrayList<URI>();
		Set<AtomicMapping> confirmedMaps = new HashSet<AtomicMapping>();
		
		for(URI linkedInd1 : listLinked1) {
			listOverlapped.clear();
			if(this.edgeTable.containsKey(linkedInd1)) {
				listOtherMappings = this.edgeTable.get(linkedInd1);
				listMappedToLinked.clear();
				for(AtomicMapping mapping2 : listOtherMappings) {
					listMappedToLinked.clear();
					listMappedToLinked.addAll(mapping2.getOtherIndividuals(linkedInd1));
					listMappedToLinked.retainAll(listLinked2);
					if(listMappedToLinked.size()>0) {
						confirmedMaps.add(mapping2);
					//	log.debug("Confirmed!");
					}
				}
				
				/*listOverlapped.addAll(listMappedToLinked);
				listOverlapped.retainAll(listLinked2);
				if(listOverlapped.size()>0) {
					return true;
				}*/
			}
		}
		//log.debug("Out isConfirmed");
		return confirmedMaps;
	}
	
	private Set<AtomicMapping> getComemberMappings(Map<URI, Set<URI>> map) {
		Set<AtomicMapping> answerSet = new HashSet<AtomicMapping>();
		
		Set<URI> comemberInds1, comemberInds2;
		List<AtomicMapping> mappings;
		Set<AtomicMapping> comemberMappings;
		
		for(URI ind1 : map.keySet()) {
			mappings = this.edgeTable.get(ind1);
			if(!this.edgeTable.containsKey(ind1)) {
				continue;
			}
			comemberInds1 = map.get(ind1);
			for(AtomicMapping mapping : mappings) {
				for(URI ind2 : mapping.getOtherIndividuals(ind1)) {
					if(map.containsKey(ind2)) {
						comemberInds2 = map.get(ind2);
						comemberMappings = this.isComember(comemberInds1, comemberInds2);
						answerSet.addAll(comemberMappings);
					}
				}
			}
		}
		
		return answerSet;
	}
	
	private Set<AtomicMapping> isComember(Set<URI> listLinked1, Set<URI> listLinked2) {
		//log.debug("In isConfirmed");
		List<URI> listOverlapped = new ArrayList<URI>();
		List<AtomicMapping> listOtherMappings;
		List<URI> listMappedToLinked = new ArrayList<URI>();
		Set<AtomicMapping> confirmedMaps = new HashSet<AtomicMapping>();
		
		for(URI linkedInd1 : listLinked1) {
			listOverlapped.clear();
			if(this.edgeTable.containsKey(linkedInd1)) {
				listOtherMappings = this.edgeTable.get(linkedInd1);
				listMappedToLinked.clear();
				for(AtomicMapping mapping2 : listOtherMappings) {
					listMappedToLinked.clear();
					listMappedToLinked.addAll(mapping2.getOtherIndividuals(linkedInd1));
					listMappedToLinked.retainAll(listLinked2);
					if(listMappedToLinked.size()>0) {
						confirmedMaps.add(mapping2);
					//	log.debug("Confirmed!");
					}
				}
				
				/*listOverlapped.addAll(listMappedToLinked);
				listOverlapped.retainAll(listLinked2);
				if(listOverlapped.size()>0) {
					return true;
				}*/
			}
		}
		//log.debug("Out isConfirmed");
		return confirmedMaps;
	}
	
	private void fillComemberMap(Map<URI, Set<URI>> map, Set<URI> memberList) {
		Set<URI> tmpSet;
		for(URI ind : memberList) {
			if(map.containsKey(ind)) {
				tmpSet = map.get(ind);
			} else {
				tmpSet = new HashSet<URI>();
				map.put(ind, tmpSet);
			}
			for(URI ind2 : memberList) {
				if(ind.equals(ind2)) continue;
				tmpSet.add(ind2);				
			}
		}
	}
	
}
