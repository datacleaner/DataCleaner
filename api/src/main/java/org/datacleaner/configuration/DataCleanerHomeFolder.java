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
package org.datacleaner.configuration;

import java.io.File;

import org.datacleaner.repository.RepositoryFolder;

/**
 * Represents the home folder of a user's {@link DataCleanerConfiguration}.
 * 
 * In most cases the home folder is based on a physical directory ({@link File})
 * but in certain deployments it may be a virtual directory represented as a
 * {@link RepositoryFolder}. Therefore it is recommended to use the
 * {@link RepositoryFolder} type if it is appropriate - otherwise keep in mind
 * that the {@link File} representation may return a placeholder directory such
 * as a temporary directory or the user home directory.
 */
public interface DataCleanerHomeFolder {

    /**
     * Gets the {@link DataCleanerHomeFolder} represented as a {@link File}
     * object.
     * 
     * Note that the {@link #toRepositoryFolder()} is recommended whenever the
     * {@link RepositoryFolder} return type can be meaningfully used. This
     * method is not guaranteed to return the actual home folder if it is a
     * non-physical directory location.
     * 
     * @return
     */
    public File toFile();
    
    /**
     * Gets the {@link DataCleanerHomeFolder} represented as a
     * {@link RepositoryFolder} object.
     * 
     * @return
     */
    public RepositoryFolder toRepositoryFolder();
}
