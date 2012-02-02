package uk.ac.open.kmi.fusion.inconsistencyresolution;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;

import org.openrdf.model.Statement;
import uk.ac.open.kmi.fusion.api.IInconsistencyResolutionMethod;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.AtomicMapping;
import uk.ac.open.kmi.fusion.api.impl.ConflictStatementCluster;
import uk.ac.open.kmi.fusion.api.impl.FusionMethodWrapper;



/**
 * @author an2548
 * A simple inconsistency resolution method which only affects mappings. 
 * Deletes mappings to resolve conflicts. 
 */
public class SimpleMappingFilterOneToOneMethod implements
		IInconsistencyResolutionMethod {

	double precisionWeight = 0.6;
	ApplicationContext context = null;
	FusionMethodWrapper descriptor = null;
	
	String outputFile = "filtered-because-of-conflict.xml";
	
	public SimpleMappingFilterOneToOneMethod() {
		// TODO Auto-generated constructor stub
	}

	public boolean isApplicableTo(ConflictStatementCluster conflict) {
		// TODO Auto-generated method stub
		return false;
	}

	public List<Statement> processConflict(
			ConflictStatementCluster conflict) {
		List<AtomicMapping> mappings = conflict.getAtomicMappings();
		// find the worst mapping and delete it
		double bestconfidence = -0.1;
		double bestsimilarity = -0.1;
		
		List<AtomicMapping> filteredOut = new ArrayList<AtomicMapping>(mappings); 
		
		AtomicMapping bestMapping = null;
		for(AtomicMapping mapping : mappings) {
			if(mapping.getSimilarity()>=bestsimilarity) {
				bestconfidence = mapping.getConfidence();
				bestsimilarity = mapping.getSimilarity();
				bestMapping = mapping;
			} 
		}
		
		// Check if there other equally bad mappings
		
		if(bestMapping!=null) {
			filteredOut.remove(bestMapping);
		} else {
			filteredOut.clear();
		}
		saveFiltered(filteredOut);
		return null;
	}
	
	private void saveFiltered(List<AtomicMapping> mappings) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(this.outputFile, true));
			try {
				for(AtomicMapping mapping : mappings) {
					((AtomicMapping)mapping).writeToXML(writer);
				}
			} finally {
			
				writer.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public FusionMethodWrapper getDescriptor() {
		return descriptor;
	}

	public void setApplicationContext(ApplicationContext context) {
		this.context = context;

	}

	public void setMethodDescriptor(FusionMethodWrapper descriptor) {
		this.descriptor = descriptor;

	}

}
