/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.datacleaner.actions.DisplayOutputWritersForTransformedDataActionListener;
import org.eobjects.datacleaner.actions.PreviewTransformedDataActionListener;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * Specialization of {@link AbstractJobBuilderPanel} for {@link Transformer}s.
 * 
 * This panel will show the transformers configuration properties as well as
 * output columns, a "write data" button, a preview button and a context
 * visualization.
 */
public class TransformerJobBuilderPanel extends AbstractJobBuilderPanel implements TransformerJobBuilderPresenter {

    private static final long serialVersionUID = 1L;

    private static final ImageManager imageManager = ImageManager.get();
    private static final Image WATERMARK_IMAGE = imageManager.getImage("images/window/transformer-tab-background.png");

    private final TransformerJobBuilder<?> _transformerJobBuilder;
    private final ColumnListTable _outputColumnsTable;
    private final JButton _previewButton;
    private final JButton _writeDataButton;
    private final WindowContext _windowContext;

    public TransformerJobBuilderPanel(TransformerJobBuilder<?> transformerJobBuilder, WindowContext windowContext,
            PropertyWidgetFactory propertyWidgetFactory, AnalyzerBeansConfiguration configuration) {
        this(WATERMARK_IMAGE, 95, 95, transformerJobBuilder, windowContext, propertyWidgetFactory, configuration);
    }

    protected TransformerJobBuilderPanel(Image watermarkImage, int watermarkHorizontalPosition,
            int watermarkVerticalPosition, TransformerJobBuilder<?> transformerJobBuilder, WindowContext windowContext,
            PropertyWidgetFactory propertyWidgetFactory, AnalyzerBeansConfiguration configuration) {
        super(watermarkImage, watermarkHorizontalPosition, watermarkVerticalPosition, transformerJobBuilder,
                propertyWidgetFactory);
        _transformerJobBuilder = transformerJobBuilder;
        _windowContext = windowContext;

        final List<MutableInputColumn<?>> outputColumns;
        if (_transformerJobBuilder.isConfigured()) {
            outputColumns = _transformerJobBuilder.getOutputColumns();
        } else {
            outputColumns = new ArrayList<MutableInputColumn<?>>(0);
        }
        _outputColumnsTable = new ColumnListTable(outputColumns, getAnalysisJobBuilder(), false, _windowContext);

        _writeDataButton = new JButton("Write data",
                imageManager.getImageIcon("images/component-types/type_output_writer.png"));
        _writeDataButton.addActionListener(new DisplayOutputWritersForTransformedDataActionListener(
                _transformerJobBuilder));

        _previewButton = new JButton("Preview data", imageManager.getImageIcon("images/actions/preview_data.png"));
        int previewRows = getPreviewRows();
        _previewButton.addActionListener(new PreviewTransformedDataActionListener(_windowContext, this,
                getAnalysisJobBuilder(), _transformerJobBuilder, configuration, previewRows));

        final DCPanel bottomButtonPanel = new DCPanel();
        bottomButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        bottomButtonPanel.add(_writeDataButton);
        bottomButtonPanel.add(_previewButton);
        _outputColumnsTable.add(bottomButtonPanel, BorderLayout.SOUTH);
    }

    protected int getPreviewRows() {
        return PreviewTransformedDataActionListener.DEFAULT_PREVIEW_ROWS;
    }

    public WindowContext getWindowContext() {
        return _windowContext;
    }

    @Override
    protected JComponent decorate(DCPanel panel) {
        JComponent result = super.decorate(panel);
        addTaskPane(imageManager.getImageIcon("images/model/source.png", IconUtils.ICON_SIZE_SMALL), "Output columns",
                _outputColumnsTable);
        return result;
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
}