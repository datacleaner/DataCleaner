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

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.datacleaner.actions.DisplayOutputWritersForTransformedDataActionListener;
import org.eobjects.datacleaner.actions.PreviewTransformedDataActionListener;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.widgets.ChangeRequirementButton;

public class TransformerJobBuilderPanel extends AbstractJobBuilderPanel {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();

	private final TransformerJobBuilder<?> _transformerJobBuilder;
	private final AnalyzerBeansConfiguration _configuration;
	private final ColumnListTable _outputColumnsTable;
	private final ChangeRequirementButton _requirementButton;
	private final JButton _previewButton;
	private final JButton _saveOutputButton;

	public TransformerJobBuilderPanel(AnalysisJobBuilder analysisJobBuilder, TransformerJobBuilder<?> transformerJobBuilder,
			AnalyzerBeansConfiguration configuration) {
		super("images/window/transformer-tab-background.png", analysisJobBuilder, transformerJobBuilder);
		_transformerJobBuilder = transformerJobBuilder;
		_configuration = configuration;

		init();

		final List<MutableInputColumn<?>> outputColumns;
		if (_transformerJobBuilder.isConfigured()) {
			outputColumns = _transformerJobBuilder.getOutputColumns();
		} else {
			outputColumns = new ArrayList<MutableInputColumn<?>>(0);
		}
		_outputColumnsTable = new ColumnListTable(outputColumns, _configuration, analysisJobBuilder, false);

		_saveOutputButton = new JButton("Save transformed data",
				imageManager.getImageIcon("images/component-types/type_output_writer.png"));
		_saveOutputButton.addActionListener(new DisplayOutputWritersForTransformedDataActionListener(_configuration,
				analysisJobBuilder, _transformerJobBuilder));

		_previewButton = new JButton("Preview transformed data",
				imageManager.getImageIcon("images/actions/preview_data.png"));
		_previewButton.addActionListener(new PreviewTransformedDataActionListener(this, analysisJobBuilder,
				_transformerJobBuilder));

		_requirementButton = new ChangeRequirementButton(analysisJobBuilder, transformerJobBuilder);

		final DCPanel bottomButtonPanel = new DCPanel();
		bottomButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		bottomButtonPanel.add(_saveOutputButton);
		bottomButtonPanel.add(_previewButton);
		_outputColumnsTable.add(bottomButtonPanel, BorderLayout.SOUTH);

		addTaskPane(imageManager.getImageIcon("images/model/source.png", IconUtils.ICON_SIZE_SMALL), "Output columns",
				_outputColumnsTable);

		final DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		buttonPanel.add(_requirementButton);
		add(buttonPanel, BorderLayout.NORTH);
	}

	@Override
	protected void setConfiguredProperty(ConfiguredPropertyDescriptor propertyDescriptor, Object value) {
		_transformerJobBuilder.setConfiguredProperty(propertyDescriptor, value);
	}

	public void setOutputColumns(List<? extends InputColumn<?>> outputColumns) {
		_outputColumnsTable.setColumns(outputColumns);
	}

	public TransformerJobBuilder<?> getTransformerJobBuilder() {
		return _transformerJobBuilder;
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		getAnalysisJobBuilder().getTransformerChangeListeners().remove(this);
	}

	public void onOutputChanged(List<MutableInputColumn<?>> outputColumns) {
		_outputColumnsTable.setColumns(outputColumns);
	}

	public void onRequirementChanged() {
		_requirementButton.updateText();
	}
}