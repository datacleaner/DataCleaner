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
package org.eobjects.datacleaner.widgets.result;

import java.util.List;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.table.TableModel;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.result.renderer.AbstractRenderer;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.actions.SaveDataSetActionListener;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.UsageLogger;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.widgets.DCComboBox;
import org.eobjects.datacleaner.widgets.DCComboBox.Listener;
import org.eobjects.datacleaner.widgets.table.ColumnHighlighter;
import org.eobjects.datacleaner.widgets.table.DCTable;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.decorator.Highlighter;

@RendererBean(SwingRenderingFormat.class)
public class AnnotatedRowsResultSwingRenderer extends AbstractRenderer<AnnotatedRowsResult, DCPanel> {

	public static class AnnotatedRowResultPanel extends DCPanel {

		private static final long serialVersionUID = 1L;
		private final AnnotatedRowsResult _result;
		private final UsageLogger _usageLogger;
		private final UserPreferences _userPreferences;
		private final DatastoreCatalog _datastoreCatalog;
		private final DCTable _table;

		public AnnotatedRowResultPanel(final AnnotatedRowsResult result, final UsageLogger usageLogger,
				final UserPreferences userPreferences, final DatastoreCatalog datastoreCatalog) {
			super();
			_result = result;
			_usageLogger = usageLogger;
			_userPreferences = userPreferences;
			_datastoreCatalog = datastoreCatalog;

			setLayout(new VerticalLayout(4));

			_table = new DCTable();
			_table.setColumnControlVisible(false);

			InputColumn<?>[] highlightedColumns = result.getHighlightedColumns();
			List<InputColumn<?>> inputColumns = result.getInputColumns();

			DCPanel buttonPanel = new DCPanel();
			buttonPanel.setLayout(new HorizontalLayout(4));

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

				buttonPanel.add(comboBox);
			} else {
				applyDetailedView();
			}

			final JButton saveToFileButton = new JButton("Save dataset", ImageManager.getInstance().getImageIcon(
					"images/actions/save.png", IconUtils.ICON_SIZE_MEDIUM));
			saveToFileButton.addActionListener(new SaveDataSetActionListener(result.getInputColumns(), result.getRows(),
					_userPreferences, _datastoreCatalog, _usageLogger));
			buttonPanel.add(saveToFileButton);

			add(buttonPanel);
			add(_table.toPanel());
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

	@Inject
	UsageLogger usageLogger;

	@Override
	public AnnotatedRowResultPanel render(final AnnotatedRowsResult result) {
		AnnotatedRowResultPanel panel = new AnnotatedRowResultPanel(result, usageLogger, userPreferences, datastoreCatalog);
		return panel;
	}
}
