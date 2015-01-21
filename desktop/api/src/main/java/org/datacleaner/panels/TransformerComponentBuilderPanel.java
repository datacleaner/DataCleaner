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

import org.datacleaner.actions.DisplayOutputWritersForTransformedDataActionListener;
import org.datacleaner.actions.PreviewTransformedDataActionListener;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.Transformer;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.job.builder.TransformerChangeListener;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * Specialization of {@link AbstractComponentBuilderPanel} for
 * {@link Transformer}s.
 * 
 * This panel will show the transformers configuration properties as well as
 * output columns, a "write data" button, a preview button and a context
 * visualization.
 */
public class TransformerComponentBuilderPanel extends AbstractComponentBuilderPanel implements
        TransformerComponentBuilderPresenter, TransformerChangeListener {

    private static final long serialVersionUID = 1L;

    private static final ImageManager imageManager = ImageManager.get();
    private static final Image WATERMARK_IMAGE = imageManager.getImage("images/window/transformer-tab-background.png");

    private final TransformerComponentBuilder<?> _componentBuilder;
    private final ColumnListTable _outputColumnsTable;
    private final JButton _previewButton;
    private final JButton _writeDataButton;
    private final WindowContext _windowContext;

    public TransformerComponentBuilderPanel(TransformerComponentBuilder<?> transformerJobBuilder,
            WindowContext windowContext, PropertyWidgetFactory propertyWidgetFactory,
            AnalyzerBeansConfiguration configuration) {
        this(WATERMARK_IMAGE, 95, 95, transformerJobBuilder, windowContext, propertyWidgetFactory, configuration);
    }

    protected TransformerComponentBuilderPanel(Image watermarkImage, int watermarkHorizontalPosition,
            int watermarkVerticalPosition, TransformerComponentBuilder<?> transformerJobBuilder,
            WindowContext windowContext, PropertyWidgetFactory propertyWidgetFactory,
            AnalyzerBeansConfiguration configuration) {
        super(watermarkImage, watermarkHorizontalPosition, watermarkVerticalPosition, transformerJobBuilder,
                propertyWidgetFactory);
        _componentBuilder = transformerJobBuilder;
        _windowContext = windowContext;

        final List<MutableInputColumn<?>> outputColumns;
        if (_componentBuilder.isConfigured()) {
            outputColumns = _componentBuilder.getOutputColumns();
        } else {
            outputColumns = new ArrayList<MutableInputColumn<?>>(0);
        }
        _outputColumnsTable = new ColumnListTable(outputColumns, getAnalysisJobBuilder(), false, _windowContext);

        _writeDataButton = new JButton("Write data", imageManager.getImageIcon(IconUtils.COMPONENT_TYPE_WRITE_DATA,
                IconUtils.ICON_SIZE_SMALL));
        _writeDataButton.addActionListener(new DisplayOutputWritersForTransformedDataActionListener(_componentBuilder));

        _previewButton = new JButton("Preview data", imageManager.getImageIcon(IconUtils.ACTION_PREVIEW,
                IconUtils.ICON_SIZE_SMALL));
        int previewRows = getPreviewRows();
        _previewButton.addActionListener(new PreviewTransformedDataActionListener(_windowContext, this,
                _componentBuilder, previewRows));

        final DCPanel bottomButtonPanel = new DCPanel();
        bottomButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        bottomButtonPanel.add(_writeDataButton);
        bottomButtonPanel.add(_previewButton);
        _outputColumnsTable.add(bottomButtonPanel, BorderLayout.SOUTH);
        _outputColumnsTable.setBorder(WidgetUtils.BORDER_EMPTY);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        _componentBuilder.addChangeListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        _componentBuilder.removeChangeListener(this);
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
    public void onAdd(TransformerComponentBuilder<?> tjb) {
    }

    @Override
    public void onConfigurationChanged(TransformerComponentBuilder<?> tjb) {
        onConfigurationChanged();
    }

    @Override
    public void onOutputChanged(TransformerComponentBuilder<?> tjb, List<MutableInputColumn<?>> outputColumns) {
        _outputColumnsTable.setColumns(outputColumns);
    }

    @Override
    public void onRemove(TransformerComponentBuilder<?> tjb) {
    }

    @Override
    public void onRequirementChanged(TransformerComponentBuilder<?> transformerJobBuilder) {
    }

    @Override
    public TransformerComponentBuilder<?> getComponentBuilder() {
        return _componentBuilder;
    }
}
