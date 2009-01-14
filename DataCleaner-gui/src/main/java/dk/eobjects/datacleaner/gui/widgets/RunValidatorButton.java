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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.datacleaner.data.DataContextSelection;
import dk.eobjects.datacleaner.execution.AbstractProgressObserver;
import dk.eobjects.datacleaner.execution.ValidationRuleRunner;
import dk.eobjects.datacleaner.gui.DataCleanerGui;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.panels.IConfigurationPanel;
import dk.eobjects.datacleaner.gui.tasks.RunnerWrapper;
import dk.eobjects.datacleaner.gui.windows.ValidatorResultWindow;
import dk.eobjects.datacleaner.validator.IValidationRuleDescriptor;
import dk.eobjects.datacleaner.validator.IValidationRuleResult;
import dk.eobjects.datacleaner.validator.ValidatorJobConfiguration;
import dk.eobjects.datacleaner.validator.trivial.DummyValidationRule;
import dk.eobjects.metamodel.schema.Table;

public class RunValidatorButton extends JButton implements ActionListener {

	private static final long serialVersionUID = -4118642086709810954L;
	protected final Log _log = LogFactory.getLog(getClass());
	private DataContextSelection _dataContextSelection;
	private ColumnSelection _columnSelection;
	private Map<JPanel, IConfigurationPanel> _configurationPanels;

	@Override
	public void removeNotify() {
		super.removeNotify();
		_log.debug("removeNotify()");
		_dataContextSelection = null;
		_columnSelection = null;
		_configurationPanels = null;
	}

	public RunValidatorButton(DataContextSelection dataContextSelection,
			ColumnSelection columnSelection,
			Map<JPanel, IConfigurationPanel> panels) {
		super("Run validation", GuiHelper
				.getImageIcon("images/toolbar_run.png"));
		_dataContextSelection = dataContextSelection;
		_columnSelection = columnSelection;
		_configurationPanels = panels;
		addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		final ValidationRuleRunner validationRuleRunner = new ValidationRuleRunner();
		boolean foundConfiguration = false;

		for (IConfigurationPanel configurationsPanel : _configurationPanels
				.values()) {
			ValidatorJobConfiguration configuration = (ValidatorJobConfiguration) configurationsPanel
					.getJobConfiguration();
			if (configuration.getColumns().length > 0) {
				foundConfiguration = true;
				validationRuleRunner.addJobConfiguration(configuration);
			}
		}

		if (foundConfiguration) {
			// Add a dummy validation rule to ensure that all
			// columns are queried by the validation rule runner
			ValidatorJobConfiguration dummyConfiguration = new ValidatorJobConfiguration(
					DummyValidationRule.DESCRIPTOR);
			dummyConfiguration.setColumns(_columnSelection.getColumns());
			dummyConfiguration
					.setValidationRuleProperties(new HashMap<String, String>());
			validationRuleRunner.addJobConfiguration(dummyConfiguration);

			final ValidatorResultWindow resultWindow = new ValidatorResultWindow(
					_columnSelection.getColumns());
			DataCleanerGui.getMainWindow().addWindow(resultWindow);
			validationRuleRunner.addProgressObserver(resultWindow);
			final StatusTabProgressObserver statusTab = new StatusTabProgressObserver(
					resultWindow);
			validationRuleRunner.addProgressObserver(statusTab);
			validationRuleRunner
					.addProgressObserver(new AbstractProgressObserver() {
						@Override
						public void notifySuccess(Table processedTable,
								long numRowsProcessed) {
							List<IValidationRuleResult> results = validationRuleRunner
									.getResultsForTable(processedTable);
							String tableName = processedTable.getName();

							// Print any errors out as exceptions into the
							// status tab.
							for (IValidationRuleResult result : results) {
								Exception error = result.getError();
								if (error != null) {
									IValidationRuleDescriptor descriptor = result
											.getDescriptor();
									StringWriter stringWriter = new StringWriter();
									stringWriter
											.append("Exception occurred when processing table '"
													+ tableName + "':\n");
									stringWriter.append("   ");
									stringWriter.append("Validation rule: ");
									stringWriter.append(descriptor
											.getDisplayName());
									stringWriter.append("\n");
									Map<String, String> properties = result
											.getProperties();
									for (Entry<String, String> entry : properties
											.entrySet()) {
										stringWriter.append("   ");
										stringWriter.append(entry.getKey());
										stringWriter.append(": ");
										stringWriter.append(entry.getValue());
										stringWriter.append("\n");
									}

									error.printStackTrace(new PrintWriter(
											stringWriter));
									statusTab.addLogMessage(stringWriter
											.toString());
								}
							}

							resultWindow.addResults(processedTable, results);
						}
					});
			RunnerWrapper runnerWrapper = new RunnerWrapper(
					_dataContextSelection, validationRuleRunner);
			runnerWrapper.execute();
		} else {
			GuiHelper
					.showErrorMessage(
							"Can't run validation",
							"You need to select data and add validation rules to run validation.",
							new IllegalStateException());
		}
	}
}