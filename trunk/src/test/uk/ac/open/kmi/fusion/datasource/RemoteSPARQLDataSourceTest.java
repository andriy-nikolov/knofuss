package uk.ac.open.kmi.fusion.datasource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;

import uk.ac.open.kmi.fusion.api.ILuceneBlocker;
import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.AttributeProfileInDataset;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.api.impl.datasource.RemoteSPARQLDataSource;
import uk.ac.open.kmi.fusion.index.LuceneMemoryIndexer;
import uk.ac.open.kmi.fusion.index.search.LuceneAllFieldsSearchStrategy;
import uk.ac.open.kmi.fusion.index.store.LuceneMemoryStoreStrategy;
import uk.ac.open.kmi.fusion.util.FusionException;

public class RemoteSPARQLDataSourceTest {

	ApplicationContext context;
	LuceneMemoryIndexer blocker;
	
	RemoteSPARQLDataSource dataSource;
	
	ValueFactory vf = ValueFactoryImpl.getInstance();
	
	FusionEnvironment environment = FusionEnvironment.getInstance();
	
	SailRepository configRepository;
	RepositoryConnection configConnection;
	
	@Before
	public void setUp() throws FusionException {
		
		dataSource = new RemoteSPARQLDataSource("http://dbpedia.org/sparql");
		// dataSource = new RemoteSPARQLDataSource("http://10.212.10.29:8089/openrdf-sesame/repositories/stanbol");
		dataSource.prepare();
		
		LuceneMemoryStoreStrategy store = new LuceneMemoryStoreStrategy();
		store.init();
		blocker = new LuceneMemoryIndexer(store, new LuceneAllFieldsSearchStrategy(store.getDirectory()));
	}
	
	@Test
	public void testCopyRelevantSubsetToBlocker() throws Exception {
		ApplicationContext context = new ApplicationContext();
		// context.setRestrictionTarget("?uri a <http://dbpedia.org/ontology/MusicalArtist>");
		// context.setRestrictionTarget("?uri a <http://dbpedia.org/class/yago/RussianDiplomats>");
		context.setRestrictionTarget("?uri a <http://dbpedia.org/class/yago/ForeignMinistersOfRussia>");
		context.addLanguage("en");
		
		int retrieved = dataSource.copyRelevantSubsetToBlocker(blocker, context, new HashMap<String, AttributeProfileInDataset>());
		int indexed = countTotals(blocker);
		
		Assert.assertEquals(retrieved, indexed);
	}
	
	private int countTotals(ILuceneBlocker blocker) throws Exception {
		
		Directory dir = blocker.getStoreStrategy().getDirectory();
		
		IndexReader reader = IndexReader.open(dir);
		
		Set<String> distinctUris = new HashSet<String>();
		int docs = 0;
		try {
			
			docs = reader.numDocs();
			
			Document doc;
			int fieldCount = 0;
			for(int i=0; i<reader.maxDoc();i++) {
				if(reader.isDeleted(i))
					continue;
				
				doc = reader.document(i);
				for(Fieldable f : doc.getFields()) {
					if(f.name().equals("uri")) {
						distinctUris.add(f.stringValue());
						continue;
					}
					if(f.name().equals(RDF.TYPE.stringValue()))
						continue;
//					System.out.println(i+": "+f.name()+": "+f.stringValue());					
					fieldCount++;
				}
			}
			System.out.println("Total fields: " + fieldCount);
			System.out.println("Distinct subjects: " + distinctUris.size());
		} finally {
			reader.close();
		}
		
		
		return docs;
	}

	
	@After
	public void shutDown() throws FusionException {
		dataSource.close();
		
		blocker.clearIndex();
		blocker.closeIndex();
	}
	
}
