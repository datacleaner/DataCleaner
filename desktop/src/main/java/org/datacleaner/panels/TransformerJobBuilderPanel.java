/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.datacleaner.beans.api.Transformer;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.data.InputColumn;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.job.builder.TransformerChangeListener;
import org.datacleaner.job.builder.TransformerJobBuilder;
import org.datacleaner.actions.DisplayOutputWritersForTransformedDataActionListener;
import org.datacleaner.actions.PreviewTransformedDataActionListener;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * Specialization of {@link AbstractJobBuilderPanel} for {@link Transformer}s.
 * 
 * This panel will show the transformers configuration properties as well as
 * output columns, a "write data" button, a preview button and a context
 * visualization.
 */
public class TransformerJobBuilderPanel extends AbstractJobBuilderPanel implements TransformerJobBuilderPresenter,
        TransformerChangeListener {

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

        _previewButton = new JButton("Preview data", imageManager.getImageIcon(IconUtils.ACTION_PREVIEW));
        int previewRows = getPreviewRows();
        _previewButton.addActionListener(new PreviewTransformedDataActionListener(_windowContext, this,
                _transformerJobBuilder, previewRows));

        final DCPanel bottomButtonPanel = new DCPanel();
        bottomButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        bottomButtonPanel.add(_writeDataButton);
        bottomButtonPanel.add(_previewButton);
        _outputColumnsTable.add(bottomButtonPanel, BorderLayout.SOUTH);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        _transformerJobBuilder.addChangeListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        _transformerJobBuilder.removeChangeListener(this);
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
    public void onAdd(TransformerJobBuilder<?> tjb) {
    }

    @Override
    public void onConfigurationChanged(TransformerJobBuilder<?> tjb) {
        onConfigurationChanged();
    }

    @Override
    public void onOutputChanged(TransformerJobBuilder<?> tjb, List<MutableInputColumn<?>> outputColumns) {
        _outputColumnsTable.setColumns(outputColumns);
    }

    @Override
    public void onRemove(TransformerJobBuilder<?> tjb) {
    }
    
    @Override
    public void onRequirementChanged(TransformerJobBuilder<?> transformerJobBuilder) {
    }
}
