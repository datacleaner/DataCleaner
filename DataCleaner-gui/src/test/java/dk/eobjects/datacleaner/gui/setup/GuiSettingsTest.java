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
package dk.eobjects.datacleaner.gui.setup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import dk.eobjects.datacleaner.catalog.IDictionary;
import dk.eobjects.datacleaner.catalog.NamedRegex;
import dk.eobjects.datacleaner.catalog.TextFileDictionary;

public class GuiSettingsTest extends TestCase {

	public void testInitialize() throws Exception {
		File file = new File(GuiSettings.SETTINGS_FILE);
		file.delete();
		GuiSettings.initialize();
		GuiSettings settings = GuiSettings.getSettings();
		assertEquals(
				"GuiSettings[lookAndFeelClassName=com.jgoodies.looks.plastic.PlasticXPLookAndFeel,dictionaries={},databaseDrivers={},regexes={}]",
				settings.toString());
		assertFalse(file.exists());
		settings.getDictionaries().clear();
		settings.getRegexes().clear();
		GuiSettings.saveSettings(settings);
		assertTrue(file.exists());
	}

	public void testLoadAndSave() throws Exception {
		GuiSettings settings = new GuiSettings();
		assertEquals(
				"GuiSettings[lookAndFeelClassName=null,dictionaries={},databaseDrivers={},regexes={}]",
				settings.toString());
		List<IDictionary> dictionaries = new ArrayList<IDictionary>();
		dictionaries.add(new TextFileDictionary("bar", new File(
				"src/test/resources/test-text-file.txt")));
		settings.setDictionaries(dictionaries);
		settings.getRegexes().add(
				new NamedRegex().setName("foo").setExpression("bar"));
		GuiSettings.saveSettings(settings);
		settings = GuiSettings.getSettings();
		assertEquals(
				"GuiSettings[lookAndFeelClassName=null,dictionaries={TextFileDictionary[name=bar]},databaseDrivers={},regexes={NamedRegex[name=foo,expression=bar]}]",
				settings.toString());

		settings.getDictionaries().clear();
		settings.getRegexes().clear();
		GuiSettings.saveSettings(settings);
	}
}