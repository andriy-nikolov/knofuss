package uk.ac.open.kmi.fusion.api.impl;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.fusion.*;

public abstract class FusionMethodResult {

	FusionMethodWrapper producedBy;
	ApplicationContext context;
	
	public FusionMethodResult() {
		this.producedBy = null;
	}
	
	public FusionMethodResult(FusionMethodWrapper producedBy) {
		this.producedBy = producedBy;
	}

	public FusionMethodWrapper producedBy() {
		return producedBy;
	}

	public void setProducedBy(FusionMethodWrapper producedBy) {
		this.producedBy = producedBy;
	}

	public ApplicationContext producedInContext() {
		return this.context;
	}

	public void setProducedInContext(ApplicationContext context) {
		this.context = context;
	}
	
	
	
}
