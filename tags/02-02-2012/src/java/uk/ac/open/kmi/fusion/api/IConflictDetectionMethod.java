package uk.ac.open.kmi.fusion.api;

import java.util.List;

import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.fusion.api.impl.ConflictStatementCluster;
import uk.ac.open.kmi.fusion.api.impl.FusionEnvironment;
import uk.ac.open.kmi.fusion.api.impl.MappingSet;
/**
 * 
 * <p>
 * An interface for a conflict detection method
 * </p>
 * 
 * @author an2548
 *
 */
public interface IConflictDetectionMethod extends IAbstractFusionMethod {
	public List<ConflictStatementCluster> discoverConflicts(MappingSet mapping) throws RepositoryException;
	public List<ConflictStatementCluster> discoverAllConflicts(FusionEnvironment onto) throws RepositoryException;
}
