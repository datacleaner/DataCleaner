package org.eobjects.datacleaner.widgets.properties;

import javax.swing.JComponent;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;

/**
 * Defines an interface for (wrappers of) widgets that are used to edit
 * configurable properties of analyzers, transformers and filters.
 * 
 * @author Kasper Sørensen
 * 
 * @param <E>
 */
public interface PropertyWidget<E> {

	public JComponent getWidget();

	public ConfiguredPropertyDescriptor getPropertyDescriptor();

	/**
	 * Called on a widget if the value it contains is prone to have been changed
	 * by a another party (typically some sort of shortcut in the UI to populate
	 * values or similar).
	 * 
	 * Note that this method will sometimes also be invoked at when the
	 * surrounding environment is not able to determine if it has changed or
	 * not. The property widget should therefore investigate if the incoming
	 * value does in deed differ from the existing.
	 */
	public void onValueTouched(E value);

	public boolean isSet();

	public E getValue();
}
