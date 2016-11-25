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
package org.datacleaner.widgets.result;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableModel;

import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.RendererBean;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.output.OutputRow;
import org.datacleaner.output.OutputWriter;
import org.datacleaner.output.csv.CsvOutputWriterFactory;
import org.datacleaner.output.datastore.DatastoreCreationDelegate;
import org.datacleaner.output.datastore.DatastoreCreationDelegateImpl;
import org.datacleaner.output.datastore.DatastoreOutputWriterFactory;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.result.AnnotatedRowsResult;
import org.datacleaner.result.renderer.AbstractRenderer;
import org.datacleaner.result.renderer.SwingRenderingFormat;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.FileFilters;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCComboBox;
import org.datacleaner.widgets.DCFileChooser;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.PopupButton;
import org.datacleaner.widgets.table.ColumnHighlighter;
import org.datacleaner.widgets.table.DCTable;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.decorator.Highlighter;

@RendererBean(SwingRenderingFormat.class)
public class AnnotatedRowsResultSwingRenderer extends AbstractRenderer<AnnotatedRowsResult, DCPanel> {

    public static class AnnotatedRowResultPanel extends DCPanel {

        private static final long serialVersionUID = 1L;
        private final AnnotatedRowsResult _result;
        private final UserPreferences _userPreferences;
        private final DatastoreCatalog _datastoreCatalog;
        private final DCTable _table;

        public AnnotatedRowResultPanel(final AnnotatedRowsResult result, final UserPreferences userPreferences,
                final DatastoreCatalog datastoreCatalog) {
            super();
            _result = result;
            _userPreferences = userPreferences;
            _datastoreCatalog = datastoreCatalog;

            setLayout(new VerticalLayout(4));

            _table = new DCTable();
            _table.setColumnControlVisible(false);

            final InputColumn<?>[] highlightedColumns = result.getHighlightedColumns();
            final List<InputColumn<?>> inputColumns = result.getInputColumns();

            final JToolBar buttonToolBar = WidgetFactory.createToolBar();
            buttonToolBar.setBorder(new EmptyBorder(0, 4, 0, 4));

            final Description description = ReflectionUtils.getAnnotation(result.getClass(), Description.class);
            final String descriptionText;
            if (description != null) {
                descriptionText = description.value();
            } else {
                descriptionText = "Records";
            }

            final int annotatedRowCount = result.getAnnotation().getRowCount();
            final DCLabel label = DCLabel.dark(descriptionText + " (" + result.getAnnotatedRowCount() + ")");
            label.setFont(WidgetUtils.FONT_HEADER1);
            buttonToolBar.add(label);
            buttonToolBar.add(WidgetFactory.createToolBarSeparator());

            if (highlightedColumns.length == 1 && inputColumns.size() > 1) {
                final DCComboBox<String> comboBox = new DCComboBox<>(VIEWS);
                comboBox.addListener(item -> {
                    if (item == VIEWS[0]) {
                        applyDetailedView();
                    } else {
                        applyDistinctValuesView();
                    }
                });
                comboBox.setSelectedItem(VIEWS[0]);
                comboBox.notifyListeners();

                buttonToolBar.add(comboBox);
            } else {
                applyDetailedView();
            }

            final PopupButton saveToFileButton = createSaveToFileButton(inputColumns);
            buttonToolBar.add(saveToFileButton);

            add(buttonToolBar);

            if (annotatedRowCount == 0) {
                final DCLabel noRecordsLabel = DCLabel.dark("No records to display.");
                noRecordsLabel.setBorder(new EmptyBorder(0, 4, 0, 0));
                add(noRecordsLabel);
            } else {
                final DCPanel tablePanel = _table.toPanel();
                add(WidgetUtils.decorateWithShadow(tablePanel));
            }
        }

        public void applyDistinctValuesView() {
            final InputColumn<?>[] highlightedColumns = _result.getHighlightedColumns();
            final TableModel tableModel = _result.toDistinctValuesTableModel(highlightedColumns[0]);
            _table.setModel(tableModel);
            _table.autoSetHorizontalScrollEnabled();
            _table.setHighlighters(new Highlighter[0]);
        }

        public void applyDetailedView() {
            _table.setModel(_result.toTableModel());
            _table.autoSetHorizontalScrollEnabled();
            final InputColumn<?>[] highlightedColumns = _result.getHighlightedColumns();
            final List<InputColumn<?>> inputColumns = _result.getInputColumns();

            if (inputColumns.size() > highlightedColumns.length) {
                // if there's context information available (columns
                // besides the actual columns of interest) then highlight the
                // columns of interest.
                if (highlightedColumns.length > 0) {
                    final int[] highligthedColumnIndexes = new int[highlightedColumns.length];
                    for (int i = 0; i < highligthedColumnIndexes.length; i++) {
                        highligthedColumnIndexes[i] = _result.getColumnIndex(highlightedColumns[i]);
                    }

                    _table.addHighlighter(new ColumnHighlighter(highligthedColumnIndexes));
                }
            }
        }

        public PopupButton createSaveToFileButton(final List<InputColumn<?>> inputColumns) {
            final PopupButton saveToFileButton =
                    WidgetFactory.createDefaultPopupButton("Save dataset", IconUtils.ACTION_SAVE_DARK);
            final JPopupMenu menu = saveToFileButton.getMenu();

            final JMenuItem saveAsDatastoreItem =
                    WidgetFactory.createMenuItem("As datastore", IconUtils.GENERIC_DATASTORE_IMAGEPATH);
            saveAsDatastoreItem.addActionListener(e -> {
                final String datastoreName = JOptionPane.showInputDialog("Datastore name");
                final DatastoreCreationDelegate creationDelegate =
                        new DatastoreCreationDelegateImpl((MutableDatastoreCatalog) _datastoreCatalog);

                final OutputWriter writer = DatastoreOutputWriterFactory
                        .getWriter(_userPreferences.getSaveDatastoreDirectory(), creationDelegate, datastoreName,
                                "DATASET", inputColumns.toArray(new InputColumn[0]));
                performWrite(writer);
            });

            final JMenuItem saveAsCsvItem = WidgetFactory.createMenuItem("As CSV file", IconUtils.CSV_IMAGEPATH);
            saveAsCsvItem.addActionListener(e -> {
                final DCFileChooser fileChooser = new DCFileChooser(_userPreferences.getAnalysisJobDirectory());
                fileChooser.addChoosableFileFilter(FileFilters.CSV);
                if (fileChooser.showSaveDialog(saveToFileButton) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    if (selectedFile.getName().indexOf('.') == -1) {
                        selectedFile = new File(selectedFile.getPath() + ".csv");
                    }

                    final OutputWriter writer =
                            CsvOutputWriterFactory.getWriter(selectedFile.getAbsolutePath(), inputColumns);
                    performWrite(writer);

                    final File dir = selectedFile.getParentFile();
                    _userPreferences.setAnalysisJobDirectory(dir);
                }
            });

            menu.add(saveAsCsvItem);
            menu.add(saveAsDatastoreItem);

            return saveToFileButton;
        }

        private void performWrite(final OutputWriter writer) {
            for (final InputRow row : _result.getSampleRows()) {
                final OutputRow outputRow = writer.createRow();
                outputRow.setValues(row);
                outputRow.write();
            }
            writer.close();
        }

        public DCTable getTable() {
            return _table;
        }

        public AnnotatedRowsResult getResult() {
            return _result;
        }
    }

    private static final String[] VIEWS = new String[] { "View detailed rows", "View distinct values" };

    @Inject
    UserPreferences userPreferences;

    @Inject
    DatastoreCatalog datastoreCatalog;

    @Override
    public AnnotatedRowResultPanel render(final AnnotatedRowsResult result) {
        return new AnnotatedRowResultPanel(result, userPreferences, datastoreCatalog);
    }

}
