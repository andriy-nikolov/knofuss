package uk.ac.open.kmi.fusion.api.impl;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import uk.ac.open.kmi.fusion.api.impl.datasource.FileDump;
import uk.ac.open.kmi.fusion.api.impl.datasource.SesameDataSource;
import uk.ac.open.kmi.fusion.api.impl.datasource.RemoteSPARQLDataSource;
import uk.ac.open.kmi.fusion.api.impl.datasource.SesameMemoryDataSource;
import uk.ac.open.kmi.fusion.api.impl.datasource.SesameNativeDataSource;
import uk.ac.open.kmi.fusion.index.LuceneBlockedDiskIndexer;
import uk.ac.open.kmi.fusion.index.LuceneDiskIndexer;
import uk.ac.open.kmi.fusion.index.LuceneEnhancedDiskIndexer;
import uk.ac.open.kmi.fusion.index.LuceneMemoryIndexer;
import uk.ac.open.kmi.fusion.util.FusionException;

public class FusionConfigurationObjectFactory {

	private FusionConfigurationObjectFactory() {
		// TODO Auto-generated constructor stub
	}
	
	public static FusionConfigurationObject createFromResource(Resource resource, FusionEnvironment environment, RepositoryConnection connection) throws FusionException {
		try {
			RepositoryResult<Statement> types = connection.getStatements(resource, RDF.TYPE, null, true);
			Statement stmt;
			String typeUri;
			try {
				while(types.hasNext()) {
					stmt = types.next();
					if(stmt.getObject() instanceof URI) {
						typeUri = stmt.getObject().toString();
						
						if(typeUri.equals(ApplicationContext.TYPE_URI)) {
							return new ApplicationContext(resource, environment);
						} else if(typeUri.equals(FusionMethodWrapper.TYPE_URI)) {
							return new FusionMethodWrapper(resource, environment);
						} else if(typeUri.equals(ValueMatchingFunctionWrapper.TYPE_URI)) {
							return new ValueMatchingFunctionWrapper(resource, environment);
						} else if(typeUri.equals(SesameDataSource.TYPE_URI)) {
							return new SesameDataSource(resource, environment);
						} else if(typeUri.equals(FileDump.TYPE_URI)) {
							return new FileDump(resource, environment);
						} else if(typeUri.equals(LuceneDiskIndexer.TYPE_URI)) {
							return new LuceneDiskIndexer(resource, environment);
						} else if(typeUri.equals(LuceneMemoryIndexer.TYPE_URI)) {
							return new LuceneMemoryIndexer(resource, environment);
						} else if(typeUri.equals(LuceneEnhancedDiskIndexer.TYPE_URI)) {
							return new LuceneEnhancedDiskIndexer(resource, environment);
						} else if(typeUri.equals(LuceneBlockedDiskIndexer.TYPE_URI)) {
							return new LuceneBlockedDiskIndexer(resource, environment);
						} 
						/*else if(typeUri.equals(LuceneDiskIndexerAllFields.TYPE_URI)) {
							return new LuceneDiskIndexerAllFields(resource, environment);
						} else if(typeUri.equals(LuceneMemoryIndexerAllFields.TYPE_URI)) {
							return new LuceneMemoryIndexerAllFields(resource, environment);
						}*/ else if(typeUri.equals(LinkSession.TYPE_URI)) {
							return new LinkSession(resource, environment);
						} else if(typeUri.equals(VariableComparisonSpecification.TYPE_URI)) {
							return new VariableComparisonSpecification(resource, environment);
						} else if(typeUri.equals(ObjectContextModel.TYPE_URI)) {
							return new ObjectContextModel(resource, environment);
						} else if(typeUri.equals(RemoteSPARQLDataSource.TYPE_URI)) {
							return new RemoteSPARQLDataSource(resource, environment);
						} else if(typeUri.equals(SesameNativeDataSource.TYPE_URI)) {
							return new SesameNativeDataSource(resource, environment);
						} else if(typeUri.equals(SesameMemoryDataSource.TYPE_URI)) {
							return new SesameMemoryDataSource(resource, environment);
						}
						
					}
					
				}
			} finally {
				types.close();
			}
		} catch(Exception e) {
			throw new FusionException("Could not create the fusion configuration object from resource: "+resource.stringValue(), e);
		}
		throw new FusionException("Resource does not denote a valid fusion configuration object: "+resource.toString());
	}

}
