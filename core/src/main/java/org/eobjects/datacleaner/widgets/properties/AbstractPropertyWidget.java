/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.datacleaner.widgets.properties;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.LayoutManager;

import javax.swing.JComponent;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.panels.DCPanel;

/**
 * Abstract implementation of the PropertyWidget interface. An implementing
 * class should preferably:
 * 
 * <ul>
 * <li>add(...) a single widget in the constructor.</li>
 * <li>call fireValueChanged() each time the contents/value of the widget has
 * changed.</li>
 * </ul>
 * 
 * @author Kasper Sørensen
 * 
 * @param <E>
 */
public abstract class AbstractPropertyWidget<E> extends MinimalPropertyWidget<E> {

	private final DCPanel _panel;

	public AbstractPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor) {
		super(beanJobBuilder, propertyDescriptor);
		_panel = new DCPanel() {
			private static final long serialVersionUID = 1L;

			public void addNotify() {
				super.addNotify();
				onPanelAdd();
			};

			public void removeNotify() {
				super.removeNotify();
				onPanelRemove();
			};
		};
		setLayout(new GridLayout(1, 1));
	}

	protected void setLayout(LayoutManager layout) {
		_panel.setLayout(layout);
	}

	protected void removeAll() {
		_panel.removeAll();
	}

	/**
	 * Notification method added for backwards compatibility
	 * 
	 * @deprecated use {@link #onPanelRemove()} instead.
	 */
	@Deprecated
	protected void removeNotify() {
	}

	/**
	 * Notification method added for backwards compatibility
	 * 
	 * @deprecated use {@link #onPanelAdd()} instead.
	 */
	@Deprecated
	protected void addNotify() {
	}

	/**
	 * Notification method invoked when the resulting panel is added to the UI.
	 */
	@SuppressWarnings("deprecation")
	protected void onPanelAdd() {
		addNotify();
	}

	/**
	 * Notification method invoked when the resulting panel is removed from the
	 * UI.
	 */
	@SuppressWarnings("deprecation")
	protected void onPanelRemove() {
		removeNotify();
	}

	protected void add(Component component, int index) {
		_panel.add(component, index);
	}

	protected Component[] getComponents() {
		return _panel.getComponents();
	}

	protected void add(Component component) {
		_panel.add(component);
	}

	protected void add(Component component, Object constraints) {
		_panel.add(component, constraints);
	}

	protected void remove(Component component) {
		_panel.remove(component);
	}

	protected void updateUI() {
		_panel.updateUI();
	}

	@Override
	public final JComponent getWidget() {
		return _panel;
	}
}
