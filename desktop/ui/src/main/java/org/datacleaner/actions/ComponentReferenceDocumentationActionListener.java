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
package org.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerHomeFolder;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.documentation.ComponentReferenceDocumentationBuilder;
import org.datacleaner.documentation.IndexDocumentationBuilder;
import org.jdesktop.swingx.action.OpenBrowserAction;

/**
 * Action listener for buttons or menu items that should start the generation of
 * component reference documentation and open the browser to display it.
 */
public class ComponentReferenceDocumentationActionListener implements ActionListener {

    private final DataCleanerConfiguration _configuration;
    private final ComponentDescriptor<?> _componentDescriptor;

    public ComponentReferenceDocumentationActionListener(DataCleanerConfiguration configuration,
            ComponentDescriptor<?> componentDescriptor) {
        _configuration = configuration;
        _componentDescriptor = componentDescriptor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final DataCleanerHomeFolder homeFolder = _configuration.getHomeFolder();

        final File homeDirectory = homeFolder.toFile();
        final File docDirectory = new File(homeDirectory, "documentation");
        if (!docDirectory.exists()) {
            docDirectory.mkdir();
        }

        final File documentationFile = new File(docDirectory,
                IndexDocumentationBuilder.getFilename(_componentDescriptor));
        if (!documentationFile.exists()) {
            final ComponentReferenceDocumentationBuilder builder = new ComponentReferenceDocumentationBuilder(
                    _configuration.getEnvironment().getDescriptorProvider());
            builder.writeDocumentationToDirectory(docDirectory);
        }

        final OpenBrowserAction openBrowserAction = new OpenBrowserAction(documentationFile.toURI());
        openBrowserAction.actionPerformed(e);
    }

}
