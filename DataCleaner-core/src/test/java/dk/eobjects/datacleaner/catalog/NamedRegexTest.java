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
package dk.eobjects.datacleaner.catalog;

import java.io.File;
import java.util.List;

import dk.eobjects.datacleaner.testware.DataCleanerTestCase;
import dk.eobjects.metamodel.util.FileHelper;

public class NamedRegexTest extends DataCleanerTestCase {

	public void testLoadAndSave() throws Exception {
		List<NamedRegex> regexes = NamedRegex
				.loadFromFile(getTestResourceAsFile("regexes-input.properties"));
		assertEquals(2, regexes.size());
		assertEquals(
				"[NamedRegex[name=email address,expression=[a-zA-Z0-9._%+-]*@[a-zA-Z0-9._%+-]*.[a-z]{2,4}], NamedRegex[name=foo,expression=bar]]",
				regexes.toString());

		regexes.add(new NamedRegex("word", "[a-Z]*"));
		File outputFile = getTestResourceAsFile("regexes-output.properties");
		NamedRegex.saveToFile(regexes, outputFile);

		String output = FileHelper.readFileAsString(outputFile);
		assertEquals(
				"#Regex registrations for DataCleaner\nemail address=[a-zA-Z0-9._%+-]*@[a-zA-Z0-9._%+-]*.[a-z]{2,4}\nfoo=bar\nword=[a-Z]*",
				output);

		outputFile.delete();
	}
}