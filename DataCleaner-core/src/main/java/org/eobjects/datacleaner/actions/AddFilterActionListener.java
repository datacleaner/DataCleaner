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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.panels.FilterListPanel;
import org.eobjects.datacleaner.user.UsageLogger;
import org.eobjects.datacleaner.widgets.DescriptorMenuItem;
import org.eobjects.datacleaner.widgets.DescriptorPopupMenu;

public final class AddFilterActionListener implements ActionListener {

	private final AnalyzerBeansConfiguration _configuration;
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final FilterListPanel _filterListPanel;
	private final UsageLogger _usageLogger;

	public AddFilterActionListener(AnalyzerBeansConfiguration configuration, AnalysisJobBuilder analysisJobBuilder,
			FilterListPanel filterListPanel, UsageLogger usageLogger) {
		_configuration = configuration;
		_analysisJobBuilder = analysisJobBuilder;
		_filterListPanel = filterListPanel;
		_usageLogger = usageLogger;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Collection<FilterBeanDescriptor<?, ?>> descriptors = _configuration.getDescriptorProvider()
				.getFilterBeanDescriptors();

		final JPopupMenu popup = new DescriptorPopupMenu<FilterBeanDescriptor<?, ?>>(descriptors) {

			private static final long serialVersionUID = 1L;

			@Override
			protected JMenuItem createMenuItem(final FilterBeanDescriptor<?, ?> descriptor) {
				JMenuItem menuItem = new DescriptorMenuItem(descriptor);
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						_analysisJobBuilder.addFilter(descriptor);

						_usageLogger.log("Add filter: " + descriptor.getDisplayName());

						_filterListPanel.updateUI();
					}
				});
				return menuItem;
			}
		};

		final Component source = (Component) e.getSource();
		popup.show(source, 0, source.getHeight());
	}
}
