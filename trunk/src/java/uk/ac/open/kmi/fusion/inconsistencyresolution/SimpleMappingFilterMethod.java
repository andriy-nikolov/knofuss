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
public class SimpleMappingFilterMethod implements
		IInconsistencyResolutionMethod {

	double precisionWeight = 0.6;
	ApplicationContext context = null;
	FusionMethodWrapper descriptor = null;
	
	String outputFile = "filtered-because-of-conflict.xml";
	
	public SimpleMappingFilterMethod() {
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
		double worstconfidence = 1.1;
		double worstsimilarity = 1.1;
		
		List<AtomicMapping> filteredOut = new ArrayList<AtomicMapping>(); 
		
		AtomicMapping worstMapping = null;
		for(AtomicMapping mapping : mappings) {
			if(mapping.getConfidence()<worstconfidence) {
				worstconfidence = mapping.getConfidence();
				worstsimilarity = mapping.getSimilarity();
				worstMapping = mapping;
			} else if(Math.abs(mapping.getConfidence()-worstconfidence)<0.001) {
				if(mapping.getSimilarity()<worstsimilarity) {
					worstsimilarity = mapping.getSimilarity();
					worstMapping = mapping;
				}
			}
		}
		
		// Check if there other equally bad mappings
		
		if(worstMapping!=null) {
			List<AtomicMapping> equallyBad = new ArrayList<AtomicMapping>();
			for(AtomicMapping mapping : mappings) {
				if(mapping!=worstMapping) {
					if((Math.abs(mapping.getConfidence()-worstconfidence)<0.0001)&&(Math.abs(mapping.getSimilarity()-worstsimilarity)<0.000001)) {
						equallyBad.add(mapping);
					}
				}
			}
			if(equallyBad.size()==0) {
				// easy - just delete the worst mapping in the set
				conflict.getMappingSet().removeMapping(worstMapping);
			} else {
				double ratio = 1.0/(1.0+(double)equallyBad.size());		
				if(ratio<precisionWeight) {
					// precision is more important - when in doubt, kill all
					conflict.getMappingSet().removeMapping(worstMapping);
					filteredOut.add(worstMapping);
					for(AtomicMapping mapping : equallyBad) {
						conflict.getMappingSet().removeMapping(mapping);
						filteredOut.add(mapping);
					}
				} 
			}
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
