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

import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;

import org.apache.metamodel.schema.Column;
import org.datacleaner.api.InputColumn;
import org.datacleaner.job.AnalysisJobMetadata;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.SourceColumnChangeListener;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.table.DCTable;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;

import com.google.common.base.Strings;

public class MetadataPanel extends DCPanel implements SourceColumnChangeListener {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMN_NAMES = new String[] { "Table", "Column", "Type", "Native type", "Size",
            "Nullable?", "Indexed?" };

    private final AnalysisJobBuilder _analysisJobBuilder;
    private final DCTable _table;
    private final JXTextField _jobNameTextField;
    private final JXTextField _jobDescriptionTextField;
    private final JXTextField _authorTextField;

    public MetadataPanel(AnalysisJobBuilder analysisJobBuilder) {
        super(ImageManager.get().getImage("images/window/metadata-tab-background.png"), 95, 95,
                WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        _analysisJobBuilder = analysisJobBuilder;
        _analysisJobBuilder.getSourceColumnListeners().add(this);

        _table = new DCTable(COLUMN_NAMES);
        _table.setAlignment(4, Alignment.RIGHT);
        _table.setAlignment(5, Alignment.CENTER);
        _table.setAlignment(6, Alignment.CENTER);

        setLayout(new VerticalLayout(40));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        _jobNameTextField = WidgetFactory.createTextField("Job name", 30);
        _jobNameTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                String text = _jobNameTextField.getText();
                _analysisJobBuilder.getAnalysisJobMetadata().setJobName(text);
            }
        });
        _jobDescriptionTextField = WidgetFactory.createTextField("Job description", 30);
        _jobDescriptionTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                String text = _jobDescriptionTextField.getText();
                _analysisJobBuilder.getAnalysisJobMetadata().setJobDescription(text);
            }
        });
        _authorTextField = WidgetFactory.createTextField("Author", 30);
        _authorTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                String text = _authorTextField.getText();
                _analysisJobBuilder.getAnalysisJobMetadata().setAuthor(text);
            }
        });

        final DCPanel jobMetadataPanel = new DCPanel();
        jobMetadataPanel.setLayout(new VerticalLayout(4));

        final DCLabel jobMetadataHeaderLabel = DCLabel.dark("Job metadata");
        jobMetadataHeaderLabel.setFont(WidgetUtils.FONT_HEADER1);
        jobMetadataPanel.add(jobMetadataHeaderLabel);

        jobMetadataPanel.add(_jobNameTextField);
        jobMetadataPanel.add(_jobDescriptionTextField);
        jobMetadataPanel.add(_authorTextField);

        final DCPanel tablePanel = _table.toPanel();
        tablePanel.setBorder(new CompoundBorder(WidgetUtils.BORDER_SHADOW, WidgetUtils.BORDER_THIN));

        add(jobMetadataPanel);
        add(tablePanel);

        updateComponents();
    }

    private void updateComponents() {
        final AnalysisJobMetadata metadata = _analysisJobBuilder.getAnalysisJobMetadata();
        if (metadata != null) {

            _jobNameTextField.setText(Strings.nullToEmpty(metadata.getJobName()));
            _jobDescriptionTextField.setText(Strings.nullToEmpty(metadata.getJobDescription()));
            _authorTextField.setText(Strings.nullToEmpty(metadata.getAuthor()));
        }

        final SortedSet<InputColumn<?>> sourceColumns = new TreeSet<InputColumn<?>>(
                _analysisJobBuilder.getSourceColumns());

        final Icon validIcon = ImageManager.get().getImageIcon(IconUtils.STATUS_VALID, IconUtils.ICON_SIZE_SMALL);

        final DefaultTableModel model = new DefaultTableModel(COLUMN_NAMES, sourceColumns.size());

        int i = 0;
        for (InputColumn<?> inputColumn : sourceColumns) {
            Column column = inputColumn.getPhysicalColumn();
            model.setValueAt(column.getTable().getName(), i, 0);
            model.setValueAt(column.getName(), i, 1);
            model.setValueAt(column.getType(), i, 2);
            model.setValueAt(column.getNativeType(), i, 3);
            model.setValueAt(column.getColumnSize(), i, 4);

            Boolean nullable = column.isNullable();
            if (nullable != null && nullable.booleanValue()) {
                model.setValueAt(validIcon, i, 5);
            } else {
                model.setValueAt("", i, 5);
            }

            boolean indexed = column.isIndexed();
            if (indexed) {
                model.setValueAt(validIcon, i, 6);
            } else {
                model.setValueAt("", i, 6);
            }

            i++;
        }

        _table.setModel(model);
    }

    @Override
    public void onAdd(InputColumn<?> sourceColumn) {
        updateComponents();
    }

    @Override
    public void onRemove(InputColumn<?> sourceColumn) {
        updateComponents();
    }

}
