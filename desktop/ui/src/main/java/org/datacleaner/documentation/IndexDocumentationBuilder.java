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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;

import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.util.StringUtils;

public class IndexDocumentationBuilder {

    private final DescriptorProvider _descriptorProvider;

    public IndexDocumentationBuilder(DescriptorProvider descriptorProvider) {
        _descriptorProvider = descriptorProvider;
    }

    public void write(OutputStream out) throws IOException {
        final OutputStreamWriter writer = new OutputStreamWriter(out);
        final Collection<? extends ComponentDescriptor<?>> componentDescriptors = _descriptorProvider
                .getComponentDescriptors();
        writer.write("<ul>");
        for (ComponentDescriptor<?> componentDescriptor : componentDescriptors) {
            String filename = getFilename(componentDescriptor);
            writer.write("<li><a href=\"");
            writer.write(filename);
            writer.write("\">");
            writer.write(componentDescriptor.getDisplayName());
            writer.write("</a></li>");
        }
        writer.write("</ul>");
        writer.flush();
    }

    public static String getFilename(ComponentDescriptor<?> componentDescriptor) {
        final String displayName = componentDescriptor.getDisplayName();
        final String filename = StringUtils.replaceWhitespaces(displayName.toLowerCase().trim(), "_")
                .replaceAll("\\/", "_").replaceAll("\\\\", "_");
        return filename + ".html";
    }

}
