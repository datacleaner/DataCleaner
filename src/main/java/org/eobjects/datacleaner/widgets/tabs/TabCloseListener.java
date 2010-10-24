package org.eobjects.datacleaner.widgets.tabs;

/**
 * This is a slightly rewritten/modified version of swingutil's
 * ClosableTabbedPane
 */
public interface TabCloseListener {

	/**
	 * Called when a tabs close button has been pressed. The policy for when/how
	 * to close tabs is left as an implementation detail of the listeners.
	 */
	public void tabClosing(TabCloseEvent ev);
}