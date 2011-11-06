package uk.ac.open.kmi.fusion.api;

import java.util.List;

import org.openrdf.model.Statement;

import uk.ac.open.kmi.fusion.api.impl.ConflictStatementCluster;

/**
 * 
 * <p>
 * Represent a method dealing with a set of conflicting statements.
 * </p>
 * @author Andriy Nikolov
 *
 */
public interface IInconsistencyResolutionMethod  extends IAbstractFusionMethod {
	 public boolean isApplicableTo(ConflictStatementCluster conflict);
	 
	 /**
	  * 
	  * @param conflict
	  * @return List of statements to replace the original set of conflicting statements.
	  */
	 public List<Statement> processConflict(ConflictStatementCluster conflict);
}
