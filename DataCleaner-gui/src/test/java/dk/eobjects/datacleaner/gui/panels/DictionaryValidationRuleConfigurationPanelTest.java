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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JTabbedPane;

import junit.framework.TestCase;
import dk.eobjects.datacleaner.catalog.IDictionary;
import dk.eobjects.datacleaner.catalog.TextFileDictionary;
import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.datacleaner.gui.setup.GuiSettings;
import dk.eobjects.datacleaner.validator.BasicValidationRuleDescriptor;
import dk.eobjects.datacleaner.validator.IValidationRuleDescriptor;
import dk.eobjects.datacleaner.validator.ValidatorJobConfiguration;
import dk.eobjects.datacleaner.validator.dictionary.DictionaryValidationRule;

public class DictionaryValidationRuleConfigurationPanelTest extends TestCase {

	public void testDictionaryList() throws Exception {
		List<IDictionary> dictionaries = new ArrayList<IDictionary>();
		dictionaries.add(new TextFileDictionary("foo", new File("foo.txt")));
		dictionaries.add(new TextFileDictionary("bar", new File("bar.txt")));
		GuiSettings.getSettings().setDictionaries(dictionaries);

		IValidationRuleDescriptor descriptor = new BasicValidationRuleDescriptor(
				"Dictionary lookup", DictionaryValidationRule.class);
		ColumnSelection columnSelection = new ColumnSelection(null);
		DictionaryValidationRuleConfigurationPanel panel = new DictionaryValidationRuleConfigurationPanel();
		panel.initialize(new JTabbedPane(), descriptor, columnSelection,
				new ValidatorJobConfiguration(descriptor));
		panel.getPanel();

		JComboBox dropDown = panel.getDictionaryDropDown();
		assertEquals(2, dropDown.getItemCount());
		assertEquals("foo", dropDown.getItemAt(0));
		assertEquals("bar", dropDown.getItemAt(1));
	}
}