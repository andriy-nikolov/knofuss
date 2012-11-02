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


import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.fusion.util.SesameUtils;

public class AtomicMapping extends FusionSet {
	
	double confidence;
	double similarity;
	
	String contextType;
	
	URI sourceIndividual;
	URI targetIndividual;
	
	String sourceLabel = null;
	String targetLabel = null;
	
	boolean correct = true;
	boolean accepted = false;
	
	public String getContextType() {
		return contextType;
	}

	public void setContextType(String contextType) {
		this.contextType = contextType;
	}

	private static Logger log = Logger.getLogger(AtomicMapping.class); 

	public AtomicMapping() {
		super();
	}
	
		
	public AtomicMapping(FusionMethodWrapper producedBy) {
		super(producedBy);
	}
	

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public URI getTargetIndividual() {
		return this.targetIndividual;
	}
	
	public URI getSourceIndividual() {
		return this.sourceIndividual;
	}
	
	public void setSourceIndividual(URI individual) {
		this.sourceIndividual = individual;
	}
	
	public void setTargetIndividual(URI individual) {
		this.targetIndividual = individual;
	}
	
	@Override
	public Set<URI> getCandidateIndividuals() {
		Set<URI> answer = new HashSet<URI>();
		answer.add(sourceIndividual);
		return answer;
	}

	@Override
	public Set<URI> getMainKBIndividuals() {
		Set<URI> answer = new HashSet<URI>();
		answer.add(targetIndividual);
		return answer;
	}

	public void writeToXML(PrintWriter writer) {
		
		writer.println("<comparison>");
		
		writer.println("\t<instance1>");
		writer.print("\t<uri>");
		writer.print(StringEscapeUtils.escapeXml(this.sourceIndividual.toString()));
		writer.println("</uri>");
		
		try {
			if(this.sourceLabel==null) {
				sourceLabel = SesameUtils.getLabel(this.sourceIndividual, 
						FusionEnvironment.getInstance().getFusionRepositoryConnection());
			}
			if(sourceLabel!=null) {
				writer.print("\t<label>");
				writer.print(StringEscapeUtils.escapeXml(sourceLabel));
					writer.println("</label>");
				}
		} catch(RepositoryException e) {
			e.printStackTrace();
		}
		
		writer.println("\t</instance1>");
			
		writer.println("\t<instance2>");
		writer.print("\t<uri>");
		writer.print(StringEscapeUtils.escapeXml(this.targetIndividual.toString()));
		writer.println("</uri>");
		try {
			if(this.targetLabel==null) {
				targetLabel =	SesameUtils.getLabel(this.targetIndividual, 
										FusionEnvironment.getInstance().getMainKbRepositoryConnection());
			}
					
			if(targetLabel!=null) {
					writer.print("\t<label>");
					writer.print(StringEscapeUtils.escapeXml(targetLabel));
					writer.println("</label>");
			}
		} catch(RepositoryException e) {
			e.printStackTrace();
		}
		writer.println("\t</instance2>");
		
		writer.print("\t<confidence>");
		writer.print(confidence);
		writer.println("</confidence>");
		writer.print("\t<similarity>");
		writer.print(similarity);
		writer.println("</similarity>");
		writer.println("</comparison>");
		
	}

	public double getSimilarity() {
		return similarity;
	}

	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}

	

	public void addIndividual(URI individual, boolean isCandidate) {
		if(isCandidate) {
			setSourceIndividual(individual);
		} else {
			setTargetIndividual(individual);
		}
		
	}

	protected URI mergeIndividuals(URI ind1, URI ind2) throws RepositoryException {
		try {
			int i=0;
			
			RepositoryConnection con;
			if(ind2.equals(this.targetIndividual)) {
				con = FusionEnvironment.getInstance().getMainKbRepositoryConnection();
			} else {
				con = FusionEnvironment.getInstance().getFusionRepositoryConnection();
			}
			List<Statement> stmts = SesameUtils.getStatements(ind2, null, null, con);
							
			// Calendar calendarBefore, calendarAfter;
				
			/*for(Statement stmt : stmts) {
				calendarBefore = new GregorianCalendar();
					
				calendarAfter = new GregorianCalendar();
				log.info("Statement insertion time cost: "+(calendarAfter.getTimeInMillis()-calendarBefore.getTimeInMillis()));
				i++;
			}*/
					
			stmts = SesameUtils.getStatements(null, null, ind2, con);
								
			for(Statement stmt : stmts) {
				if(stmt.getPredicate().toString().equals(RDF.TYPE.toString())) {
					continue;
				}
				/*if(stmt.getSubject() instanceof Resource) {
					calendarBefore = new GregorianCalendar();
					
					calendarAfter = new GregorianCalendar();
					log.info("Statement insertion time cost: "+(calendarAfter.getTimeInMillis()-calendarBefore.getTimeInMillis()));
				}*/
						
				i++;
			}
			log.info("Copied "+i+" statements");
		} catch(NullPointerException e) {
			e.printStackTrace();
		}
		return ind1;
	}

	public String getSourceLabel() {
		return sourceLabel;
	}

	public void setSourceLabel(String sourceLabel) {
		this.sourceLabel = sourceLabel;
	}

	public String getTargetLabel() {
		return targetLabel;
	}

	public void setTargetLabel(String targetLabel) {
		this.targetLabel = targetLabel;
	}

	public boolean isCorrect() {
		return correct;
	}

	public void setCorrect(boolean correct) {
		this.correct = correct;
	}
	
	

	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	@Override
	public String toString() {
		
		return this.sourceIndividual.toString()+" : "+this.targetIndividual.toString();
	}
	
	
	
}
