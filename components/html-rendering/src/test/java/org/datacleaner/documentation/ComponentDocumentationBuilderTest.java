/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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

import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.datacleaner.api.Concurrent;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.ExternalDocumentation;
import org.datacleaner.api.ExternalDocumentation.DocumentationLink;
import org.datacleaner.api.ExternalDocumentation.DocumentationType;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.Descriptors;
import org.junit.Test;

public class ComponentDocumentationBuilderTest {

    @SuppressWarnings("unused")
    @Named("NameName")
    @Description("A transformer for which to generate docs")
    @ExternalDocumentation({ @DocumentationLink(title = "Internationalization in DataCleaner",
            url = "https://www.youtube.com/watch?v=ApA-nhtLbhI", type = DocumentationType.VIDEO, version = "3.0") })
    @Concurrent(true)
    private static class ExampleTransformer implements Transformer {

        @Inject
        @Configured
        String stringProp;

        @Inject
        @Configured
        InputColumn<String> columnProp;

        @Override
        public OutputColumns getOutputColumns() {
            throw new IllegalStateException("This won't work");
        }

        @Override
        public Object[] transform(InputRow inputRow) {
            throw new IllegalStateException("This won't work");
        }
    }

    @Test
    public void testGenerateSimple() throws Exception {
        final File directory = new File("target/component_reference_documentation");
        if (directory.exists()) {
            FileUtils.deleteDirectory(directory);
        }

        final ComponentDescriptor<?> componentDescriptor = Descriptors.ofTransformer(ExampleTransformer.class);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ComponentDocumentationBuilder builder = new ComponentDocumentationBuilder(true);
        builder.write(componentDescriptor, out);

        final String str = new String(out.toByteArray());
        assertTrue(str.length() > 100);
        assertTrue(str.indexOf("<title>NameName</title>") != -1);
        assertTrue(str.indexOf("String prop") != -1);
        assertTrue(str.indexOf("Column prop") != -1);
    }
}
