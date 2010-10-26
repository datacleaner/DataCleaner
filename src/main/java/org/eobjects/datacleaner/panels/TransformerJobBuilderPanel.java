package org.eobjects.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerChangeListener;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.widgets.properties.ChangeRequirementButton;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindow;

public class TransformerJobBuilderPanel extends AbstractJobBuilderPanel implements TransformerChangeListener {

	private static final long serialVersionUID = 1L;

	private final TransformerJobBuilder<?> _transformerJobBuilder;
	private final AnalyzerBeansConfiguration _configuration;
	private final ColumnListTable _outputColumnsTable;

	public TransformerJobBuilderPanel(AnalysisJobBuilderWindow parentWindow, AnalysisJobBuilder analysisJobBuilder,
			TransformerJobBuilder<?> transformerJobBuilder, AnalyzerBeansConfiguration configuration) {
		super(parentWindow, "images/window/transformer-tab-background.png", analysisJobBuilder);
		getAnalysisJobBuilder().getTransformerChangeListeners().add(this);
		_transformerJobBuilder = transformerJobBuilder;
		_configuration = configuration;

		TransformerBeanDescriptor<?> descriptor = transformerJobBuilder.getDescriptor();

		init(descriptor, transformerJobBuilder);

		List<MutableInputColumn<?>> outputColumns = transformerJobBuilder.getOutputColumns();

		_outputColumnsTable = new ColumnListTable(outputColumns, _configuration, getAnalysisJobBuilder());

		ImageManager imageManager = ImageManager.getInstance();

		addTaskPane(imageManager.getImageIcon("images/model/source.png", IconUtils.ICON_SIZE_SMALL), "Output columns",
				_outputColumnsTable);

		ChangeRequirementButton requirementButton = new ChangeRequirementButton(analysisJobBuilder, transformerJobBuilder);
		DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(requirementButton);
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

	@Override
	public void onAdd(TransformerJobBuilder<?> transformerJobBuilder) {
	}

	@Override
	public void onRemove(TransformerJobBuilder<?> transformerJobBuilder) {
	}

	@Override
	public void onOutputChanged(TransformerJobBuilder<?> transformerJobBuilder, List<MutableInputColumn<?>> outputColumns) {
		if (transformerJobBuilder == _transformerJobBuilder) {
			_outputColumnsTable.setColumns(outputColumns);
		}
	}
}
