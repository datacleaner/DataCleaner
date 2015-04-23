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
package org.datacleaner.documentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.datacleaner.beans.CharacterSetDistributionAnalyzer;
import org.datacleaner.beans.ReferenceDataMatcherAnalyzer;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.SimpleDescriptorProvider;
import org.junit.Test;

public class ComponentReferenceDocumentationBuilderTest {

    @Test
    public void testGenerateDocsInTarget() throws Exception {
        final File directory = new File("target/component_reference_documentation");
        if (directory.exists()) {
            FileUtils.deleteDirectory(directory);
        }

        final SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider();
        descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(CharacterSetDistributionAnalyzer.class));
        descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(ReferenceDataMatcherAnalyzer.class));

        final ComponentReferenceDocumentationBuilder docBuilder = new ComponentReferenceDocumentationBuilder(
                descriptorProvider);
        final boolean success = docBuilder.writeDocumentationToDirectory(directory);
        assertTrue(success);

        assertTrue(new File(directory, "index.html").exists());
        assertEquals(3, directory.list().length);
    }
}
