package org.eobjects.datacleaner.user;

import org.eobjects.analyzer.reference.Dictionary;

public interface DictionaryChangeListener {

	public void onAdd(Dictionary dictionary);

	public void onRemove(Dictionary dictionary);
}
