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
package org.eobjects.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.ComponentCategory;
import org.eobjects.analyzer.beans.writers.WriteDataCategory;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.util.CollectionUtils2;
import org.eobjects.datacleaner.util.DisplayNameComparator;
import org.eobjects.datacleaner.widgets.DescriptorMenuItem;

public class DisplayOutputWritersAction {

	private final AnalysisJobBuilder _analysisJobBuilder;

	public DisplayOutputWritersAction(AnalysisJobBuilder analysisJobBuilder) {
		_analysisJobBuilder = analysisJobBuilder;
	}

	public final void showPopup(JComponent component) {
		JPopupMenu popup = new JPopupMenu();

		List<JMenuItem> menuItems = createMenuItems();
		for (JMenuItem menuItem : menuItems) {
			popup.add(menuItem);
		}

		popup.show(component, 0, component.getHeight());
	}

	public List<JMenuItem> createMenuItems() {
		List<JMenuItem> result = new ArrayList<JMenuItem>();
		for (final AnalyzerBeanDescriptor<?> descriptor : getDescriptors()) {
			JMenuItem outputWriterMenuItem = new DescriptorMenuItem(descriptor);
			outputWriterMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Class<? extends Analyzer<?>> beanClass = descriptor.getComponentClass();

					AnalyzerJobBuilder<?> ajb = _analysisJobBuilder.addAnalyzer(beanClass);

					configure(_analysisJobBuilder, ajb);

					ajb.onConfigurationChanged();
				}
			});
			result.add(outputWriterMenuItem);
		}
		return result;
	}

	protected void configure(AnalysisJobBuilder analysisJobBuilder, AnalyzerJobBuilder<?> analyzerJobBuilder) {
	}

	protected List<AnalyzerBeanDescriptor<?>> getDescriptors() {
		Collection<AnalyzerBeanDescriptor<?>> descriptors = _analysisJobBuilder.getConfiguration().getDescriptorProvider()
				.getAnalyzerBeanDescriptors();
		List<AnalyzerBeanDescriptor<?>> result = CollectionUtils2.sorted(descriptors, new DisplayNameComparator());

		for (Iterator<AnalyzerBeanDescriptor<?>> it = result.iterator(); it.hasNext();) {
			AnalyzerBeanDescriptor<?> descriptor = it.next();
			Set<ComponentCategory> categories = descriptor.getComponentCategories();
			if (!categories.contains(new WriteDataCategory())) {
				it.remove();
			}
		}

		return result;
	}
}
