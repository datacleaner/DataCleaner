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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.datacleaner.data.DataContextSelection;
import dk.eobjects.datacleaner.execution.ValidationRuleRunner;
import dk.eobjects.datacleaner.gui.DataCleanerGui;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.panels.IConfigurationPanel;
import dk.eobjects.datacleaner.gui.tasks.RunnerWrapper;
import dk.eobjects.datacleaner.gui.windows.ValidatorResultWindow;
import dk.eobjects.datacleaner.validator.IValidationRuleResult;
import dk.eobjects.datacleaner.validator.ValidationRuleConfiguration;
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
			ValidationRuleConfiguration configuration = (ValidationRuleConfiguration) configurationsPanel
					.getConfiguration();
			if (configuration.getColumns().length > 0) {
				foundConfiguration = true;
				validationRuleRunner.addConfiguration(configuration);
			}
		}

		if (foundConfiguration) {
			// Add a dummy validation rule to ensure that all
			// columns are queried by the validation rule runner
			ValidationRuleConfiguration dummyConfiguration = new ValidationRuleConfiguration(
					DummyValidationRule.DESCRIPTOR);
			dummyConfiguration.setColumns(_columnSelection.getColumns());
			dummyConfiguration
					.setValidationRuleProperties(new HashMap<String, String>());
			validationRuleRunner.addConfiguration(dummyConfiguration);

			final ValidatorResultWindow internalFrame = new ValidatorResultWindow(
					_columnSelection.getColumns());
			DataCleanerGui.getMainWindow().addWindow(internalFrame);
			validationRuleRunner
					.addProgressObserver(new LogInternalFrameProgressObserver(
							internalFrame));
			RunnerWrapper runnerWrapper = new RunnerWrapper(
					_dataContextSelection, validationRuleRunner, internalFrame) {

				@Override
				public void notifyExecutionSuccess(Object executingObject) {
					if (executingObject instanceof Table) {
						Table table = (Table) executingObject;
						List<IValidationRuleResult> results = validationRuleRunner
								.getResultsForTable(table);
						internalFrame.addResults(table, results);
					}
				}
			};
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