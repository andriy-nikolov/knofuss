package uk.ac.open.kmi.fusion.core;


import java.util.*;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;


import uk.ac.open.kmi.fusion.FusionMetaVocabulary;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.util.SesameUtils;

public abstract class ResultCombiner {
	
	protected AtomicTaskHandler taskHandler;
	protected Hashtable<String, Integer> producedResultsTable; 
	
	public ResultCombiner() {
		super();
		producedResultsTable = new Hashtable<String, Integer>();
	}

	public abstract void execute() throws RepositoryException;

	public AtomicTaskHandler getTaskHandler() {
		return taskHandler;
	}

	public void setTaskHandler(AtomicTaskHandler taskHandler) {
		this.taskHandler = taskHandler;
	}
	
	protected void countProducedResults(URI concept) throws RepositoryException {
		URI descriptorRes;
				
		producedResultsTable.clear();
		
		if(concept==null) {
			return;
		}
		
		Set<Resource> results = SesameUtils.findClassMembers(concept, FusionEnvironment.getInstance().getFusionRepositoryConnection());
		
		for(Resource curResource : results) {
			if(curResource instanceof URI) {
				List<Statement> properties = 
					SesameUtils.getStatements(
							curResource, 
							FusionEnvironment.getInstance().getFusionKbValueFactory().createURI(FusionMetaVocabulary.PRODUCED_BY), 
							null, 
							FusionEnvironment.getInstance().getFusionRepositoryConnection());

				for(Statement stmt : properties) {
					
						descriptorRes = (URI)stmt.getObject();
						if(producedResultsTable.containsKey(descriptorRes.toString())) {
							producedResultsTable.put(descriptorRes.toString(), producedResultsTable.get(descriptorRes.toString()).intValue()+1);
						} else {
							producedResultsTable.put(descriptorRes.toString(), 1);
						}
					
				}
			}
		}
	}
	
	
	protected void logProducedResults() {

	}
	
	protected void logAcceptedResults() {

	}
	
	private void logResults(String propertyURI) {

	}

}
