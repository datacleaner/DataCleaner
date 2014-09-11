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
package org.eobjects.datacleaner.widgets.result;

import java.util.List;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableModel;

import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.result.renderer.AbstractRenderer;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.datacleaner.actions.SaveDataSetActionListener;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCComboBox;
import org.eobjects.datacleaner.widgets.DCComboBox.Listener;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.table.ColumnHighlighter;
import org.eobjects.datacleaner.widgets.table.DCTable;
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
                final DCComboBox<String> comboBox = new DCComboBox<String>(VIEWS);
                comboBox.addListener(new Listener<String>() {
                    @Override
                    public void onItemSelected(String item) {
                        if (item == VIEWS[0]) {
                            applyDetailedView();
                        } else {
                            applyDistinctValuesView();
                        }
                    }
                });
                comboBox.setSelectedItem(VIEWS[0]);
                comboBox.notifyListeners();

                buttonToolBar.add(comboBox);
            } else {
                applyDetailedView();
            }

            final JButton saveToFileButton = new JButton("Save dataset", ImageManager.get().getImageIcon(
                    "images/actions/save.png", IconUtils.ICON_SIZE_MEDIUM));
            saveToFileButton.addActionListener(new SaveDataSetActionListener(result.getInputColumns(),
                    result.getRows(), _userPreferences, _datastoreCatalog));
            buttonToolBar.add(saveToFileButton);

            add(buttonToolBar);

            if (annotatedRowCount == 0) {
                final DCLabel noRecordsLabel = DCLabel.dark("No records to display.");
                noRecordsLabel.setBorder(new EmptyBorder(0, 4, 0, 0));
                add(noRecordsLabel);
            } else {
                add(_table.toPanel());
            }
        }

        public void applyDistinctValuesView() {
            InputColumn<?>[] highlightedColumns = _result.getHighlightedColumns();
            TableModel tableModel = _result.toDistinctValuesTableModel(highlightedColumns[0]);
            _table.setModel(tableModel);
            _table.autoSetHorizontalScrollEnabled();
            _table.setHighlighters(new Highlighter[0]);
        }

        public void applyDetailedView() {
            _table.setModel(_result.toTableModel());
            _table.autoSetHorizontalScrollEnabled();
            InputColumn<?>[] highlightedColumns = _result.getHighlightedColumns();
            List<InputColumn<?>> inputColumns = _result.getInputColumns();

            if (inputColumns.size() > highlightedColumns.length) {
                // if there's context information available (columns
                // besides the actual columns of interest) then highlight the
                // columns of interest.
                if (highlightedColumns.length > 0) {
                    int[] highligthedColumnIndexes = new int[highlightedColumns.length];
                    for (int i = 0; i < highligthedColumnIndexes.length; i++) {
                        highligthedColumnIndexes[i] = _result.getColumnIndex(highlightedColumns[i]);
                    }

                    _table.addHighlighter(new ColumnHighlighter(highligthedColumnIndexes));
                }
            }
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
        AnnotatedRowResultPanel panel = new AnnotatedRowResultPanel(result, userPreferences, datastoreCatalog);
        return panel;
    }
}
