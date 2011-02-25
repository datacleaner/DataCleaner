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
package org.eobjects.datacleaner.panels;

import java.awt.BorderLayout;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.FilterChangeListener;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.datacleaner.actions.AddFilterActionListener;
import org.eobjects.datacleaner.actions.JobBuilderTaskPaneTextMouseListener;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.LabelUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

public class FilterListPanel extends DCPanel implements FilterChangeListener {

	private static final long serialVersionUID = 1L;

	private final AnalyzerBeansConfiguration _configuration;
	private final Map<FilterJobBuilder<?, ?>, JXTaskPane> _taskPanes;
	private final Map<FilterJobBuilder<?, ?>, FilterJobBuilderPresenter> _presenters;
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final JXTaskPaneContainer _taskPaneContainer;
	private final Set<FilterJobBuilderPresenter> _preconfiguredPresenters;

	public FilterListPanel(AnalyzerBeansConfiguration configuration, AnalysisJobBuilder analysisJobBuilder) {
		super(ImageManager.getInstance().getImage("images/window/filters-tab-background.png"), 95, 95,
				WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		setLayout(new BorderLayout());
		_configuration = configuration;
		_taskPanes = new IdentityHashMap<FilterJobBuilder<?, ?>, JXTaskPane>();
		_presenters = new IdentityHashMap<FilterJobBuilder<?, ?>, FilterJobBuilderPresenter>();
		_analysisJobBuilder = analysisJobBuilder;
		_preconfiguredPresenters = new HashSet<FilterJobBuilderPresenter>();
		_analysisJobBuilder.getFilterChangeListeners().add(this);

		JToolBar toolBar = WidgetFactory.createToolBar();

		// Add filter
		ImageManager imageManager = ImageManager.getInstance();
		JButton addFilterButton = new JButton("Add filter", imageManager.getImageIcon("images/component-types/filter.png"));
		addFilterButton.addActionListener(new AddFilterActionListener(configuration, _analysisJobBuilder, this));
		toolBar.add(addFilterButton);

		add(toolBar, BorderLayout.NORTH);

		_taskPaneContainer = WidgetFactory.createTaskPaneContainer();
		_taskPaneContainer.setOpaque(false);

		add(_taskPaneContainer, BorderLayout.CENTER);
	}

	public void addPreconfiguredPresenter(FilterJobBuilderPresenter presenter) {
		_preconfiguredPresenters.add(presenter);
	}

	public void applyPropertyValues() {
		Set<FilterJobBuilder<?, ?>> filterJobBuilders = _presenters.keySet();
		for (FilterJobBuilder<?, ?> filterJobBuilder : filterJobBuilders) {
			_presenters.get(filterJobBuilder).applyPropertyValues();
		}
	}

	private JXTaskPane createTaskPane(final FilterJobBuilder<?, ?> fjb) {
		String title = LabelUtils.getLabel(fjb);
		Icon icon = IconUtils.getDescriptorIcon(fjb.getDescriptor(), IconUtils.ICON_SIZE_SMALL);
		final JXTaskPane taskPane = WidgetFactory.createTaskPane(title, icon);
		taskPane.add(_presenters.get(fjb).getJComponent());
		return taskPane;
	}

	@Override
	public void removeNotify() {
		_analysisJobBuilder.getFilterChangeListeners().remove(this);
		super.removeNotify();
	}

	@Override
	public void onAdd(FilterJobBuilder<?, ?> fjb) {
		boolean createPresenter = true;
		for (FilterJobBuilderPresenter presenter : _preconfiguredPresenters) {
			final FilterJobBuilder<?, ?> presentedFjb = presenter.getFilterJobBuilder();
			if (presentedFjb == fjb) {
				createPresenter = false;
				_presenters.put(fjb, presenter);
				break;
			}
		}

		if (createPresenter) {
			final FilterJobBuilderPresenter presenter = new FilterJobBuilderPanel(_configuration, _analysisJobBuilder, fjb);
			_presenters.put(fjb, presenter);
		}

		final JXTaskPane taskPane = createTaskPane(fjb);
		taskPane.addMouseListener(new JobBuilderTaskPaneTextMouseListener(_analysisJobBuilder, fjb, taskPane));
		_taskPanes.put(fjb, taskPane);
		_taskPaneContainer.add(taskPane);
	}

	@Override
	public void onRemove(FilterJobBuilder<?, ?> fjb) {
		_presenters.remove(fjb);
		JXTaskPane taskPane = _taskPanes.remove(fjb);
		if (taskPane != null) {
			_taskPaneContainer.remove(taskPane);
			updateUI();
		}
	}

	@Override
	public void onConfigurationChanged(FilterJobBuilder<?, ?> filterJobBuilder) {
		FilterJobBuilderPresenter presenter = _presenters.get(filterJobBuilder);
		if (presenter != null) {
			presenter.onConfigurationChanged();
		}
	}

	@Override
	public void onRequirementChanged(FilterJobBuilder<?, ?> filterJobBuilder) {
		FilterJobBuilderPresenter presenter = _presenters.get(filterJobBuilder);
		if (presenter != null) {
			presenter.onRequirementChanged();
		}
	}

	public void initializeExistingComponents() {
		List<FilterJobBuilder<?, ?>> filterJobBuilders = _analysisJobBuilder.getFilterJobBuilders();
		for (FilterJobBuilder<?, ?> filterJobBuilder : filterJobBuilders) {
			onAdd(filterJobBuilder);
		}
	}
}
