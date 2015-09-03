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
package org.datacleaner.beans.transform;

import junit.framework.TestCase;

import org.datacleaner.api.InputColumn;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.SimpleDictionary;
import org.junit.Test;

public class RemoveDictionaryMatchesTransformerTest extends TestCase {

    private final InputColumn<String> col = new MockInputColumn<>("Job title");
    private final Dictionary dictionary = new SimpleDictionary("Title adjectives", "Junior", "Senior", "Lead",
            "Principal", "Assistant to");
    private RemoveDictionaryMatchesTransformer transformer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        transformer = new RemoveDictionaryMatchesTransformer(col, dictionary, new DataCleanerConfigurationImpl());
        transformer.init();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        transformer.close();
    }

    @Test
    public void testGetOutputColumns() throws Exception {
        assertEquals("OutputColumns[Job title (Title adjectives removed)]", transformer.getOutputColumns().toString());
    }

    public void testJobTitleScenario() throws Throwable {
        assertEquals("Software Engineer", transformer.transform("Senior Software Engineer"));
        assertEquals("Designer", transformer.transform("  Lead   Designer  "));
        assertEquals("Software Engineer", transformer.transform("Software  Engineer"));
        assertEquals("Guru of employees", transformer.transform("Principal Senior Lead Guru of Junior employees"));
    }

    @Test
    public void testRemoveMultiTokenPart() throws Exception {
        // we currently do not support this...

        // assertEquals("CEO", transformer.transform("Assistant to the CEO"));
    }
}
