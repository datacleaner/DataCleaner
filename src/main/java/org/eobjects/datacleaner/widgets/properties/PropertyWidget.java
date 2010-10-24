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

	public boolean isSet();

	public E getValue();
}
