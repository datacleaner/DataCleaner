/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.repository;

import java.io.InputStream;
import java.io.OutputStream;

import org.eobjects.metamodel.util.Action;

/**
 * Represents a file in the {@link Repository}.
 */
public interface RepositoryFile extends RepositoryNode {

    /**
     * Represents the main file types in the repository.
     */
    public static enum Type {
        ANALYSIS_JOB, ANALYSIS_RESULT, TIMELINE_SPEC, OTHER;
    }

    /**
     * Opens up an {@link InputStream} to read from the file.
     * 
     * @return an {@link InputStream} to read from the file.
     */
    public InputStream readFile();

    /**
     * Opens up an {@link OutputStream} to write to the file, and allows a
     * callback to perform writing actions on it.
     * 
     * @param writeCallback
     *            a callback which should define what to write to the file.
     */
    public void writeFile(Action<OutputStream> writeCallback);

    /**
     * Gets the type of the file.
     * 
     * @return the type of the file.
     */
    public Type getType();
}
