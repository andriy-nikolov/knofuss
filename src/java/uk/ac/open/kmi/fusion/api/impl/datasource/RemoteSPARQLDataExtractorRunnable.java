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
package uk.ac.open.kmi.fusion.api.impl.datasource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;

import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;

public class RemoteSPARQLDataExtractorRunnable implements Callable<List<BindingSet>> {

	RepositoryConnection connection;
	ApplicationContext context;
	URI predicate;
	int limit;
	
	int currentOffset = 0;
	
	public RemoteSPARQLDataExtractorRunnable(RepositoryConnection conn, ApplicationContext context, URI predicate, int limit, int offset) {
		this.connection = conn;
		this.context = context;
		this.predicate = predicate;
		this.limit = limit;
		this.currentOffset = offset;
	}
	
	public URI getPredicate() {
		return predicate;
	}

	@Override
	public List<BindingSet> call() {
		List<BindingSet> response = new ArrayList<BindingSet>(this.limit);
		
		try {
			TupleQueryResult partialResult;
			
			BindingSet bs;
			
			partialResult = executeQuery();
				
			try {
				while(partialResult.hasNext()) {
					bs = partialResult.next();
					response.add(bs);
				}
			} finally {
				partialResult.close();
			}
				
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

			}
						
		} catch (OpenRDFException e) {
			e.printStackTrace();
		} 
		return response;
	}
	
	private TupleQueryResult executeQuery() throws OpenRDFException {
		
		String sQuery = formNextPropertyRetrievalQuery();
		
		TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, sQuery);
		
		TupleQueryResult result = query.evaluate();
		
		return result;
	}
	
	private String formNextPropertyRetrievalQuery() {
		
		String query = "SELECT ?uri (<"+this.predicate.stringValue()+"> AS ?predicate) ?object WHERE { \n"
				+ "?uri <" + predicate.stringValue() + "> ?object . "
				+ context.getRestrictionTarget() + " . }";
		
		if(limit>0) {
			query += " LIMIT ";
			query += limit;
			if(currentOffset>0) {
				query += " OFFSET ";
				query += currentOffset;
			}
		}

		return query;
	}

}
