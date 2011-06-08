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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.RowProcessingAnalyzerJobBuilder;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.datacleaner.output.beans.OutputWriterAnalyzer;
import org.eobjects.datacleaner.util.DisplayNameComparator;
import org.eobjects.datacleaner.widgets.DescriptorMenuItem;

public abstract class AbstractDisplayOutputWritersActionListener implements ActionListener {

	private final AnalysisJobBuilder _analysisJobBuilder;
	private final AnalyzerBeansConfiguration _configuration;

	public AbstractDisplayOutputWritersActionListener(AnalyzerBeansConfiguration configuration,
			AnalysisJobBuilder analysisJobBuilder) {
		_configuration = configuration;
		_analysisJobBuilder = analysisJobBuilder;
	}
	
	@Override
	public final void actionPerformed(ActionEvent e) {
		JPopupMenu popup = new JPopupMenu();

		for (final AnalyzerBeanDescriptor<?> descriptor : getDescriptors()) {
			JMenuItem outputWriterMenuItem = new DescriptorMenuItem(descriptor);
			outputWriterMenuItem.addActionListener(new ActionListener() {
				@SuppressWarnings("unchecked")
				@Override
				public void actionPerformed(ActionEvent e) {
					Class<? extends RowProcessingAnalyzer<?>> beanClass = (Class<? extends RowProcessingAnalyzer<?>>) descriptor
							.getComponentClass();

					RowProcessingAnalyzerJobBuilder<? extends RowProcessingAnalyzer<?>> ajb = _analysisJobBuilder
							.addRowProcessingAnalyzer(beanClass);

					configure(_analysisJobBuilder, ajb);

					ajb.onConfigurationChanged();
				}
			});
			popup.add(outputWriterMenuItem);
		}

		JComponent component = (JComponent) e.getSource();
		popup.show(component, 0, component.getHeight());
	}

	protected abstract void configure(AnalysisJobBuilder analysisJobBuilder,
			RowProcessingAnalyzerJobBuilder<? extends RowProcessingAnalyzer<?>> analyzerJobBuilder);

	protected List<AnalyzerBeanDescriptor<?>> getDescriptors() {
		Collection<AnalyzerBeanDescriptor<?>> descriptors = _configuration.getDescriptorProvider()
				.getAnalyzerBeanDescriptors();
		List<AnalyzerBeanDescriptor<?>> result = CollectionUtils.sorted(descriptors, new DisplayNameComparator());

		for (Iterator<AnalyzerBeanDescriptor<?>> it = result.iterator(); it.hasNext();) {
			AnalyzerBeanDescriptor<?> descriptor = it.next();
			if (!descriptor.isRowProcessingAnalyzer() || descriptor.getAnnotation(OutputWriterAnalyzer.class) == null) {
				it.remove();
			}
		}

		return result;
	}
}
