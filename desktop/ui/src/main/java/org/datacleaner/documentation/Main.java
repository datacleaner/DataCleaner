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

import java.util.List;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.guice.DCModuleImpl;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.user.DataCleanerHome;

import com.google.inject.Guice;

/**
 * A simple main class for triggering documentation building.
 * 
 * Eventually we may want to integrate this into the normal CLI of DataCleaner
 * but at this stage it is experimentation-ware so keeping it separate is both
 * easier and more safe to avoid integration issues.
 */
public class Main {

    public static void main(String[] args) {
        final DCModuleImpl module = new DCModuleImpl(DataCleanerHome.get());
        final DataCleanerConfiguration configuration = Guice.createInjector(module).getInstance(
                DataCleanerConfiguration.class);

        final ComponentReferenceDocumentationBuilder docBuilder = new ComponentReferenceDocumentationBuilder(
                configuration.getEnvironment().getDescriptorProvider());
        final RepositoryFolder folder = configuration.getHomeFolder().toRepositoryFolder()
                .getOrCreateFolder("documentation");

        // clean up the directory
        final List<RepositoryFile> htmlFiles = folder.getFiles(null, ".html");
        for (RepositoryFile file : htmlFiles) {
            file.delete();
        }

        docBuilder.writeDocumentationToRepositoryFolder(folder);
        
        System.out.println("Documentation written to: " + folder.getQualifiedPath());
    }
}
