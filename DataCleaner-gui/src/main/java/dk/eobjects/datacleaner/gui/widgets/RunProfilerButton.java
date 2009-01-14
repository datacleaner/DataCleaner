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
package dk.eobjects.datacleaner.gui.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.data.DataContextSelection;
import dk.eobjects.datacleaner.execution.AbstractProgressObserver;
import dk.eobjects.datacleaner.execution.ExecutionConfiguration;
import dk.eobjects.datacleaner.execution.ProfileRunner;
import dk.eobjects.datacleaner.gui.DataCleanerGui;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.panels.IConfigurationPanel;
import dk.eobjects.datacleaner.gui.tasks.RunnerWrapper;
import dk.eobjects.datacleaner.gui.windows.ProfilerResultWindow;
import dk.eobjects.datacleaner.profiler.IProfileResult;
import dk.eobjects.datacleaner.profiler.ProfilerJobConfiguration;
import dk.eobjects.metamodel.schema.Table;

public class RunProfilerButton extends JButton implements ActionListener {

	private static final long serialVersionUID = 6789940894608611387L;
	protected final Log _log = LogFactory.getLog(getClass());
	private DataContextSelection _dataContextSelection;
	private ExecutionConfiguration _executionConfiguration;
	private Map<JPanel, IConfigurationPanel> _configurationPanels;

	@Override
	public void removeNotify() {
		super.removeNotify();
		_log.debug("removeNotify()");
		_dataContextSelection = null;
		_configurationPanels = null;
	}

	public RunProfilerButton(final DataContextSelection dataContextSelection,
			final Map<JPanel, IConfigurationPanel> panels,
			ExecutionConfiguration executionConfiguration) {
		super("Run profiling", GuiHelper.getImageIcon("images/toolbar_run.png"));
		addActionListener(this);
		_dataContextSelection = dataContextSelection;
		_configurationPanels = panels;
		_executionConfiguration = executionConfiguration;
	}

	public void actionPerformed(ActionEvent e) {
		final ProfileRunner profileRunner = new ProfileRunner();
		profileRunner.setExecutionConfiguration(_executionConfiguration);
		boolean foundConfiguration = false;
		for (IConfigurationPanel configurationsPanel : _configurationPanels
				.values()) {
			ProfilerJobConfiguration configuration = (ProfilerJobConfiguration) configurationsPanel
					.getJobConfiguration();
			if (configuration.getColumns().length > 0) {
				profileRunner.addJobConfiguration(configuration);
				foundConfiguration = true;
			}
		}

		if (foundConfiguration) {
			final ProfilerResultWindow resultWindow = new ProfilerResultWindow();
			DataCleanerGui.getMainWindow().addWindow(resultWindow);
			profileRunner.addProgressObserver(resultWindow);
			profileRunner.addProgressObserver(new StatusTabProgressObserver(
					resultWindow));
			profileRunner.addProgressObserver(new AbstractProgressObserver() {
				@Override
				public void notifySuccess(Table processedTable,
						long numRowsProcessed) {
					List<IProfileResult> results = profileRunner
							.getResultsForTable(processedTable);
					resultWindow.addResults(processedTable, results,
							_dataContextSelection.getDataContext());
				}
			});
			RunnerWrapper runnerWrapper = new RunnerWrapper(
					_dataContextSelection, profileRunner);
			runnerWrapper.execute();
		} else {
			GuiHelper
					.showErrorMessage(
							"Can't run profiling",
							"You need to select data and add profiles to run profiling.",
							new IllegalStateException());
		}
	}

}