package org.eobjects.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JButton;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.datacleaner.actions.PreviewTransformedDataActionListener;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.widgets.properties.ChangeRequirementButton;

public class TransformerJobBuilderPanel extends AbstractJobBuilderPanel {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();

	private final TransformerJobBuilder<?> _transformerJobBuilder;
	private final AnalyzerBeansConfiguration _configuration;
	private final ColumnListTable _outputColumnsTable;
	private final ChangeRequirementButton _requirementButton;

	public TransformerJobBuilderPanel(AnalysisJobBuilder analysisJobBuilder, TransformerJobBuilder<?> transformerJobBuilder,
			AnalyzerBeansConfiguration configuration) {
		super("images/window/transformer-tab-background.png", analysisJobBuilder, transformerJobBuilder);
		_transformerJobBuilder = transformerJobBuilder;
		_configuration = configuration;

		List<MutableInputColumn<?>> outputColumns = _transformerJobBuilder.getOutputColumns();

		_outputColumnsTable = new ColumnListTable(outputColumns, _configuration, analysisJobBuilder);

		init();

		JButton previewButton = new JButton("Preview transformed data",
				imageManager.getImageIcon("images/actions/preview_data.png"));
		previewButton
				.addActionListener(new PreviewTransformedDataActionListener(analysisJobBuilder, _transformerJobBuilder));

		DCPanel previewButtonPanel = new DCPanel();
		previewButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		previewButtonPanel.add(previewButton);
		_outputColumnsTable.add(previewButtonPanel, BorderLayout.SOUTH);

		addTaskPane(imageManager.getImageIcon("images/model/source.png", IconUtils.ICON_SIZE_SMALL), "Output columns",
				_outputColumnsTable);

		_requirementButton = new ChangeRequirementButton(analysisJobBuilder, transformerJobBuilder);
		DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
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