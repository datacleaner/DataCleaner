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
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.datacleaner.actions.DisplayOutputWritersForTransformedDataActionListener;
import org.eobjects.datacleaner.actions.PreviewTransformedDataActionListener;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.widgets.ChangeRequirementButton;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;

public class TransformerJobBuilderPanel extends AbstractJobBuilderPanel implements TransformerJobBuilderPresenter {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();

	private final TransformerJobBuilder<?> _transformerJobBuilder;
	private final ColumnListTable _outputColumnsTable;
	private final ChangeRequirementButton _requirementButton;
	private final JButton _previewButton;
	private final JButton _writeOutputButton;
	private final WindowContext _windowContext;

	public TransformerJobBuilderPanel(TransformerJobBuilder<?> transformerJobBuilder, WindowContext windowContext,
			PropertyWidgetFactory propertyWidgetFactory, AnalyzerBeansConfiguration configuration) {
		super("images/window/transformer-tab-background.png", transformerJobBuilder, propertyWidgetFactory);
		_transformerJobBuilder = transformerJobBuilder;
		_windowContext = windowContext;

		init();

		final List<MutableInputColumn<?>> outputColumns;
		if (_transformerJobBuilder.isConfigured()) {
			outputColumns = _transformerJobBuilder.getOutputColumns();
		} else {
			outputColumns = new ArrayList<MutableInputColumn<?>>(0);
		}
		_outputColumnsTable = new ColumnListTable(outputColumns, getAnalysisJobBuilder(), false, _windowContext);

		_writeOutputButton = new JButton("Write data",
				imageManager.getImageIcon("images/component-types/type_output_writer.png"));
		_writeOutputButton
				.addActionListener(new DisplayOutputWritersForTransformedDataActionListener(_transformerJobBuilder));

		_previewButton = new JButton("Preview data", imageManager.getImageIcon("images/actions/preview_data.png"));
		_previewButton.addActionListener(new PreviewTransformedDataActionListener(_windowContext, this,
				getAnalysisJobBuilder(), _transformerJobBuilder, configuration));

		_requirementButton = new ChangeRequirementButton(transformerJobBuilder);

		final DCPanel bottomButtonPanel = new DCPanel();
		bottomButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		bottomButtonPanel.add(_writeOutputButton);
		bottomButtonPanel.add(_previewButton);
		_outputColumnsTable.add(bottomButtonPanel, BorderLayout.SOUTH);

		addTaskPane(imageManager.getImageIcon("images/model/source.png", IconUtils.ICON_SIZE_SMALL), "Output columns",
				_outputColumnsTable);

		final DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		buttonPanel.add(_requirementButton);
		add(buttonPanel, BorderLayout.NORTH);
	}

	public WindowContext getWindowContext() {
		return _windowContext;
	}

	@Override
	protected void setConfiguredProperty(ConfiguredPropertyDescriptor propertyDescriptor, Object value) {
		_transformerJobBuilder.setConfiguredProperty(propertyDescriptor, value);
	}

	public void setOutputColumns(List<? extends InputColumn<?>> outputColumns) {
		_outputColumnsTable.setColumns(outputColumns);
	}

	@Override
	public TransformerJobBuilder<?> getJobBuilder() {
		return _transformerJobBuilder;
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		getAnalysisJobBuilder().getTransformerChangeListeners().remove(this);
	}

	@Override
	public void onOutputChanged(List<MutableInputColumn<?>> outputColumns) {
		_outputColumnsTable.setColumns(outputColumns);
	}

	@Override
	public void onRequirementChanged() {
		_requirementButton.updateText();
	}

	@Override
	public void applyPropertyValues() {
		super.applyPropertyValues();
	}

	@Override
	public void onConfigurationChanged() {
		getPropertyWidgetFactory().onConfigurationChanged();
	}

	@Override
	public JComponent createJComponent() {
		return this;
	}
}