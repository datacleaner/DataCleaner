package org.eobjects.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.eobjects.analyzer.job.builder.AbstractBeanWithInputColumnsBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;

public final class AddFilterMappingActionListener implements ActionListener {

	private final JButton _button;
	private final FilterJobBuilder<?, ?> _filterJobBuilder;
	private final Enum<?> _category;
	private final AnalysisJobBuilder _analysisJobBuilder;

	public AddFilterMappingActionListener(JButton button, FilterJobBuilder<?, ?> filterJobBuilder, Enum<?> category,
			AnalysisJobBuilder analysisJobBuilder) {
		_button = button;
		_filterJobBuilder = filterJobBuilder;
		_category = category;
		_analysisJobBuilder = analysisJobBuilder;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		List<AbstractBeanWithInputColumnsBuilder<?, ?, ?>> availableBeans = _analysisJobBuilder
				.getAvailableUnfilteredBeans(_filterJobBuilder);
		JPopupMenu popupMenu = new JPopupMenu("Target component");
		for (final AbstractBeanWithInputColumnsBuilder<?, ?, ?> bean : availableBeans) {
			JMenuItem menuItem = new JMenuItem(bean.toString());
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					bean.setRequirement(_filterJobBuilder, _category);
				}
			});
			popupMenu.add(menuItem);
		}
		popupMenu.show(_button, 0, _button.getHeight());

	}

}
