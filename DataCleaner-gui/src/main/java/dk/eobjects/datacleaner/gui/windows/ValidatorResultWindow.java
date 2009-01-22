/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.gui.windows;

import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;

import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.panels.TableValidationRuleResultsPanel;
import dk.eobjects.datacleaner.validator.IValidationRuleResult;
import dk.eobjects.datacleaner.validator.trivial.DummyValidationRule;
import dk.eobjects.metamodel.MetaModelHelper;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public class ValidatorResultWindow extends ResultWindow {

	private List<Column> _columns;

	@Override
	public void disposeInternal() {
		super.disposeInternal();
		_columns = null;
	}

	public ValidatorResultWindow(List<Column> columns) {
		super("Validation results");
		_columns = columns;
	}

	@Override
	public ImageIcon getFrameIcon() {
		return GuiHelper.getImageIcon("images/task_result.png");
	}

	public void addResults(Table table, List<IValidationRuleResult> results) {
		// Remove dummy validation rules from the result
		for (Iterator<IValidationRuleResult> it = results.iterator(); it
				.hasNext();) {
			IValidationRuleResult validationRuleResult = it.next();
			if (validationRuleResult.getDescriptor() == DummyValidationRule.DESCRIPTOR) {
				it.remove();
			}
		}
		Column[] queriedColumns = MetaModelHelper.getTableColumns(table,
				_columns);
		TableValidationRuleResultsPanel tableProfileResultsPanel = new TableValidationRuleResultsPanel(
				table, queriedColumns, results);
		addTab(table.getName(), GuiHelper
				.getImageIcon("images/toolbar_preview_data.png"),
				tableProfileResultsPanel);
	}
}