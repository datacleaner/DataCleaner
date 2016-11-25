/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.reference.regexswap;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;

import org.apache.http.impl.client.HttpClients;
import org.datacleaner.test.TestHelper;
import org.junit.Assume;
import org.junit.Test;

public class RegexSwapClientTest {

    @Test
    public void testUpdateContent() throws Exception {
        Assume.assumeTrue(TestHelper.isInternetConnected());

        final RegexSwapClient client = new RegexSwapClient(HttpClients.createSystem());
        client.getCategories();
        final Collection<Category> categories = client.getCategories();
        assertFalse(categories.isEmpty());
        final Category partials = client.getCategoryByName("partials");
        assertEquals("partials", partials.getName());
        assertNotNull(partials.getDescription());

        final List<Regex> partialsRegexes = client.getRegexes(partials);

        int regexInCategoryCount = 0;
        for (final Category category : categories) {
            final List<Regex> regexes = client.getRegexes(category);
            regexInCategoryCount += regexes.size();
        }
        assertTrue(regexInCategoryCount >= partialsRegexes.size());

        assertFalse(partialsRegexes.isEmpty());
        for (final Regex regex : partialsRegexes) {
            assertFalse(regex.getCategories().isEmpty());
            assertTrue(regex.containsCategory(partials));
        }

        Regex regex = client.getRegexByName("Integer or rounded decimal");
        assertNotNull(regex);
        regex = client.refreshRegex(regex);
        final List<Category> regexCategories = regex.getCategories();
        assertFalse(regexCategories.isEmpty());
        for (final Category category : regexCategories) {
            assertNotNull(category);
            assertTrue(regex.containsCategory(category));
            assertTrue(client.getRegexes(category).contains(regex));
        }
    }
}
