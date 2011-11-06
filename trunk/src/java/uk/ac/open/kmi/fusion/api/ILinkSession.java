package uk.ac.open.kmi.fusion.api;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.semanticweb.owl.align.Alignment;


//import uk.ac.open.kmi.fusion.MultiOntologyUtil;
import uk.ac.open.kmi.fusion.api.impl.AtomicMapping;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.util.FusionException;

public interface ILinkSession {

	public Alignment getSchemaAlignment();

	public void setSchemaAlignment(Alignment schemaAlignment);

	public boolean isMultiOntology();

	public void setMultiOntology(boolean multiOntology);

//	public MultiOntologyUtil getMultiOntologyUtil();

//	public Set<URI> getContentIndividualUris();

	public FusionEnvironment getFusionEnvironment();

	public abstract List<AtomicMapping> getAtomicMappings();

//	public abstract Alignment getDataAlignment();

//	public abstract void close() throws RepositoryException;

	public abstract void run() throws FusionException;

//	public abstract void setLuceneIndexDir(String indexDir);

//	public abstract void setSourceKB(Repository repository);

//	public abstract void setTargetKB(Repository repository);

//	public abstract void init();


}
