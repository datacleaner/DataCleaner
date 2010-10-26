package org.eobjects.datacleaner.util;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A simple abstract class that implements DocumentListener. Using this class
 * will make it easier to create DocumentListeners on the fly that delegate to
 * the same method, so you only have to override a single method
 * 
 * @author Kasper Sørensen
 */
public abstract class DCDocumentListener implements DocumentListener {

	@Override
	public void insertUpdate(DocumentEvent e) {
		onChange(e);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		onChange(e);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		onChange(e);
	}

	protected abstract void onChange(DocumentEvent e);
}
