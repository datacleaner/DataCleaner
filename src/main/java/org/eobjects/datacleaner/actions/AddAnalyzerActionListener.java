package org.eobjects.datacleaner.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.eobjects.analyzer.beans.api.ExploringAnalyzer;
import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.datacleaner.output.beans.HiddenFromMenu;
import org.eobjects.datacleaner.util.DisplayNameComparator;
import org.eobjects.datacleaner.widgets.tooltip.DescriptorMenuItem;

public final class AddAnalyzerActionListener implements ActionListener {

	private final AnalyzerBeansConfiguration _configuration;
	private final AnalysisJobBuilder _analysisJobBuilder;

	public AddAnalyzerActionListener(AnalyzerBeansConfiguration configuration, AnalysisJobBuilder analysisJobBuilder) {
		_configuration = configuration;
		_analysisJobBuilder = analysisJobBuilder;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JPopupMenu popup = new JPopupMenu();

		Collection<AnalyzerBeanDescriptor<?>> descriptors = _configuration.getDescriptorProvider()
				.getAnalyzerBeanDescriptors();
		descriptors = CollectionUtils.sorted(descriptors, new DisplayNameComparator());
		for (final AnalyzerBeanDescriptor<?> descriptor : descriptors) {
			if (descriptor.getAnnotation(HiddenFromMenu.class) == null) {
				if (descriptor.isRowProcessingAnalyzer()) {
					JMenuItem menuItem = new DescriptorMenuItem(descriptor);
					menuItem.addActionListener(new ActionListener() {
						@SuppressWarnings("unchecked")
						@Override
						public void actionPerformed(ActionEvent e) {
							Class<?> analyzerClass = descriptor.getBeanClass();
							if (descriptor.isExploringAnalyzer()) {
								_analysisJobBuilder.addExploringAnalyzer((Class<? extends ExploringAnalyzer<?>>) analyzerClass);
							} else {
								_analysisJobBuilder
								.addRowProcessingAnalyzer((Class<? extends RowProcessingAnalyzer<?>>) analyzerClass);
							}
						}
					});
					
					popup.add(menuItem);
				}
			}
		}

		Component source = (Component) e.getSource();
		popup.show(source, 0, source.getHeight());
	}
}
