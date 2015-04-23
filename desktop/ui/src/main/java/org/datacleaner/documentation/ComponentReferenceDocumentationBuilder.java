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

import java.io.File;
import java.io.OutputStream;
import java.util.Collection;

import org.apache.metamodel.util.Action;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.repository.file.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object that can build a complete Component Reference documentation folder
 */
public class ComponentReferenceDocumentationBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ComponentReferenceDocumentationBuilder.class);

    private final DescriptorProvider _descriptorProvider;

    public ComponentReferenceDocumentationBuilder(DescriptorProvider descriptorProvider) {
        _descriptorProvider = descriptorProvider;
    }

    public boolean writeDocumentationToDirectory(File directory) {
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return writeDocumentationToRepositoryFolder(new FileRepository(directory));
    }

    public boolean writeDocumentationToRepositoryFolder(RepositoryFolder folder) {
        boolean success = true;
        final RepositoryFile indexFile = folder.createFile("index.html", new Action<OutputStream>() {
            @Override
            public void run(OutputStream out) throws Exception {
                IndexDocumentationBuilder index = new IndexDocumentationBuilder(_descriptorProvider);
                index.write(out);
            }
        });
        logger.info("Wrote: {}", indexFile);

        final ComponentDocumentationBuilder componentDocumentationBuilder = new ComponentDocumentationBuilder(true);
        final Collection<? extends ComponentDescriptor<?>> componentDescriptors = _descriptorProvider
                .getComponentDescriptors();
        for (final ComponentDescriptor<?> componentDescriptor : componentDescriptors) {
            try {
                final String filename = IndexDocumentationBuilder.getFilename(componentDescriptor);
                final RepositoryFile file = folder.createFile(filename, new Action<OutputStream>() {
                    @Override
                    public void run(OutputStream out) throws Exception {
                        componentDocumentationBuilder.write(componentDescriptor, out);
                    }
                });
                logger.info("Wrote: {}", file);
            } catch (Exception e) {
                // don't crash the whole run if something goes wrong
                logger.error("Unexpected error occurred while writing documentation for {}", componentDescriptor, e);
                success = false;
            }
        }

        return success;
    }
}
