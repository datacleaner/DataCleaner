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

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JScrollPane;

import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.panels.TableProfileResultsPanel;
import dk.eobjects.datacleaner.profiler.IProfileResult;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.schema.Table;

public class ProfilerResultWindow extends ResultWindow {

	public ProfilerResultWindow() {
		super("Profiling results");
	}

	@Override
	public ImageIcon getFrameIcon() {
		return GuiHelper.getImageIcon("images/task_result.png");
	}

	public void addResults(Table table, List<IProfileResult> results,
			DataContext dataContext) {
		String tableName = table.getName();
		TableProfileResultsPanel tableProfileResultsPanel = new TableProfileResultsPanel(
				dataContext, table, results);
		JScrollPane scrollPane = new JScrollPane(tableProfileResultsPanel);
		addTab(tableName, GuiHelper
				.getImageIcon("images/toolbar_preview_data.png"), scrollPane);
	}
}