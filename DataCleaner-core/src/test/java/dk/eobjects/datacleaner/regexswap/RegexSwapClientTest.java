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
package dk.eobjects.datacleaner.regexswap;

import java.util.Map;

import dk.eobjects.datacleaner.testware.DataCleanerTestCase;

public class RegexSwapClientTest extends DataCleanerTestCase {

	public void testUpdateContent() throws Exception {
		RegexSwapClient client = new RegexSwapClient();
		client.updateCategories();
		Map<String, Category> categories = client.getCategories();
		Map<String, Regex> regexes = client.getRegexes();
		assertFalse(categories.isEmpty());
		Category partials = categories.get("partials");
		assertEquals("partials", partials.getName());
		assertNotNull(partials.getDescription());

		int regexInCategoryCount = 0;
		for (Category category : categories.values()) {
			client.updateRegexes(category);
			regexInCategoryCount += category.getRegexes().size();
		}
		assertTrue(regexInCategoryCount >= regexes.size());

		assertFalse(partials.getRegexes().isEmpty());
		for (Regex regex : partials.getRegexes()) {
			assertFalse(regex.getCategories().isEmpty());
			assertTrue(regex.containsCategory(partials));
		}

		Regex regex = regexes.get("Integer or rounded decimal");
		client.updateRegex(regex);
		assertFalse(regex.getCategories().isEmpty());
		for (Category category : regex.getCategories()) {
			assertFalse(category.getRegexes().isEmpty());
			assertTrue(category.containsRegex(regex));
		}
	}
}
