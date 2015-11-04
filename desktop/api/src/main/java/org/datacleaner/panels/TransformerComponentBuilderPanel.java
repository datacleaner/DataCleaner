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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.datacleaner.actions.DisplayOutputWritersForTransformedDataActionListener;
import org.datacleaner.actions.PreviewTransformedDataActionListener;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.Transformer;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.job.builder.TransformerChangeListener;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.ComboButton;
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

    private final TransformerComponentBuilder<?> _componentBuilder;
    private final ColumnListTable _outputColumnsTable;
    private final JButton _previewButton;
    private final JButton _previewAlternativesButton;
    private final JButton _writeDataButton;
    private final WindowContext _windowContext;

    public TransformerComponentBuilderPanel(TransformerComponentBuilder<?> transformerJobBuilder,
            WindowContext windowContext, PropertyWidgetFactory propertyWidgetFactory,
            DataCleanerConfiguration configuration) {
        this(null, 95, 95, transformerJobBuilder, windowContext, propertyWidgetFactory, configuration);
    }

    protected TransformerComponentBuilderPanel(Image watermarkImage, int watermarkHorizontalPosition,
            int watermarkVerticalPosition, TransformerComponentBuilder<?> transformerJobBuilder,
            WindowContext windowContext, PropertyWidgetFactory propertyWidgetFactory,
            DataCleanerConfiguration configuration) {
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

        _writeDataButton = WidgetFactory.createDefaultButton("Write data", IconUtils.COMPONENT_TYPE_WRITE_DATA);
        _writeDataButton.addActionListener(new DisplayOutputWritersForTransformedDataActionListener(_componentBuilder));

        _previewButton = WidgetFactory.createDefaultButton("Preview data", IconUtils.ACTION_PREVIEW);
        _previewButton.setBorder(WidgetUtils.BORDER_EMPTY);
        _previewAlternativesButton = WidgetFactory.createDefaultButton("\uf0d7");
        _previewAlternativesButton.setBorder(WidgetUtils.BORDER_EMPTY);
        _previewAlternativesButton.setFont(WidgetUtils.FONT_FONTAWESOME.deriveFont(12f));
        if (_componentBuilder.getAnalysisJobBuilder().isRootJobBuilder()) {
            final int defaultPreviewRows = getPreviewRows();
            final PreviewTransformedDataActionListener defaultPreviewTransformedDataActionListener = new PreviewTransformedDataActionListener(_windowContext, this,
                    _componentBuilder, defaultPreviewRows);
            final TransformerComponentBuilderPanel transformerComponentBuilderPanel = this;
            _previewButton.addActionListener(defaultPreviewTransformedDataActionListener);
            _previewAlternativesButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    final JMenuItem defaultPreviewMenutItem = WidgetFactory.createMenuItem("Preview " + defaultPreviewRows + " records",
                            IconUtils.ACTION_PREVIEW);
                    defaultPreviewMenutItem.addActionListener(defaultPreviewTransformedDataActionListener);

                    final JMenuItem maxRowsPreviewMenuItem = WidgetFactory.createMenuItem("Preview N records",
                            IconUtils.ACTION_PREVIEW);
                    maxRowsPreviewMenuItem.addActionListener(new ActionListener() {
                        
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Integer maxRows = WidgetFactory.showMaxRowsDialog(defaultPreviewRows);
                            
                            if (maxRows != null) {
                                final PreviewTransformedDataActionListener maxRowsPreviewTransformedDataActionListener = new PreviewTransformedDataActionListener(_windowContext, transformerComponentBuilderPanel,
                                        _componentBuilder, maxRows);
                                maxRowsPreviewTransformedDataActionListener.actionPerformed(e);
                            }
                        }
                    });

                    final JPopupMenu menu = new JPopupMenu();
                    menu.add(defaultPreviewMenutItem);
                    menu.add(maxRowsPreviewMenuItem);

                    final int horizontalPosition = -1 * menu.getPreferredSize().width
                            + _previewAlternativesButton.getWidth();
                    menu.show(_previewAlternativesButton, horizontalPosition, _previewAlternativesButton.getHeight());
                }
            });
        } else {
            // we cannot provide a preview-function for transformers in non-root
            // AnalysisJobBuilders
            _previewButton.setVisible(false);
            _previewAlternativesButton.setVisible(false);
        }
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
    protected JComponent decorateMainPanel(DCPanel panel) {
        JComponent result = super.decorateMainPanel(panel);

        final DCPanel bottomButtonPanel = new DCPanel();
        bottomButtonPanel.setBorder(WidgetUtils.BORDER_EMPTY);
        bottomButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        bottomButtonPanel.add(_writeDataButton);
        
        final ComboButton previewButtonPanel = new ComboButton();
        previewButtonPanel.addButton(_previewButton);
        previewButtonPanel.add(new JLabel("|"));
        previewButtonPanel.addButton(_previewAlternativesButton);
        
        bottomButtonPanel.add(previewButtonPanel);

        if (!_componentBuilder.getDescriptor().isMultiStreamComponent()) {
            final DCPanel outputColumnsPanel = new DCPanel();
            outputColumnsPanel.setLayout(new BorderLayout());
            outputColumnsPanel.add(WidgetUtils.decorateWithShadow(_outputColumnsTable), BorderLayout.CENTER);
            outputColumnsPanel.add(bottomButtonPanel, BorderLayout.SOUTH);
            
            addTaskPane(IconUtils.MODEL_SOURCE, "Output columns", outputColumnsPanel);
        }
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
