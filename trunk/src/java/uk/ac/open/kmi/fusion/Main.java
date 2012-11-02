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
package uk.ac.open.kmi.fusion;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openrdf.OpenRDFException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

import uk.ac.open.kmi.fusion.api.ILinkSession;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.util.FusionException;
import uk.ac.open.kmi.fusion.util.SesameUtils;

public class Main {

	private Logger log = Logger.getLogger(Main.class);
	
	public Main() {
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		PropertyConfigurator.configure("log4j.properties");
		
		Main obj = new Main();
		String propFile = "config.rdf";
		
		if(args.length>=2) {
			if(args[0].equals("-c")) {
				propFile = args[1].trim();
			}
		}
		
		try {
			obj.readPropertiesFromRDF(propFile);
			obj.setUp();
			obj.testRun();
		} catch (OpenRDFException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
	
	public void readPropertiesFromRDF(String fileName) throws OpenRDFException, IOException {
		SailRepository repoConfig = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
		repoConfig.initialize();
		RepositoryConnection configConnection = repoConfig.getConnection();
		try {
			configConnection.add(
					new File(FusionEnvironment.CONFIG_DIR+"/fusion.owl"), 
					"", 
					RDFFormat.RDFXML);
			
			configConnection.add(
					new File(fileName), 
					"", 
					SesameUtils.getRDFFormatForFileName(fileName));
			
			FusionEnvironment.getInstance().loadEnvironment(configConnection);
		} finally {
				
			configConnection.close();
			repoConfig.shutDown();
		}
		
	}
	
		
	public void setUp() throws Exception {
		
	}
	
	public void testRun() throws FusionException {
		
		try {
			List<ILinkSession> sessions = FusionEnvironment.getInstance().getLinkSessions();
			
			for(ILinkSession session : sessions) {
				session.run();
			}
		} catch(Exception e) {
			e.printStackTrace();
		} catch(Error e) {
			e.printStackTrace();
		} finally {
	
			FusionEnvironment.getInstance().shutDown();
		}
	}		
	
}
