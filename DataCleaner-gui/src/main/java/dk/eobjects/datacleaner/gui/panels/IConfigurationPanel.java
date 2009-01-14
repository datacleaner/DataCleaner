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
package dk.eobjects.datacleaner.gui.panels;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.datacleaner.execution.IJobConfiguration;

/**
 * This is the interface that defines the behaviour of a panel used to configure
 * a Profile or Validation Rule. The configuration panels are visible inside the
 * tabs of either the Profiler or Validator windows.
 * 
 * All IConfigurationPanel implementations must provide a no-arguments
 * constructor. The initialize method is used to initialize the objects, note
 * that the provided configuration object may contain configuration data already
 * as the initialize method is used both for brand new Profiles/Validation Rules
 * and for reloading of old ones from disk.
 * 
 * @see AbstractValidatorConfigurationPanel
 * @see DefaultProfilerConfigurationPanel
 */
public interface IConfigurationPanel {

	/**
	 * Initializes the configuration panel. This method will only be called
	 * once, before all other methods.
	 * 
	 * @param tabbedPane
	 *            the tabbedPane that is used to hold the panel
	 * @param descriptor
	 *            the ProfileDescriptor or ValidationRuleDescriptor that is
	 *            being used
	 * @param columnSelection
	 *            the current column selection
	 * @param jobConfiguration
	 *            the initial configuration of this panel
	 */
	public void initialize(JTabbedPane tabbedPane, Object descriptor,
			ColumnSelection columnSelection,
			IJobConfiguration jobConfiguration);

	/**
	 * Gets the Swing JPanel used to represent the panel. This method will only
	 * be called once (after initialize)
	 * 
	 * @return
	 */
	public JPanel getPanel();

	/**
	 * Retrieves a configuration based on the state of the panel. This method
	 * can be called several times as the user may want to execute more than
	 * once.
	 * 
	 * @return
	 */
	public IJobConfiguration getJobConfiguration();

	/**
	 * Signals that the window or tab has been closed. Use this method for
	 * optional garbage collection
	 * 
	 * @throws Exception
	 */
	public void destroy() throws Exception;
}