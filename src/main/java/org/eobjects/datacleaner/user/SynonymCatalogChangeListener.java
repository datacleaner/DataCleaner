package org.eobjects.datacleaner.user;

import org.eobjects.analyzer.reference.SynonymCatalog;

public interface SynonymCatalogChangeListener {

	public void onAdd(SynonymCatalog synonymCatalog);
	
	public void onRemove(SynonymCatalog synonymCatalog);
}
