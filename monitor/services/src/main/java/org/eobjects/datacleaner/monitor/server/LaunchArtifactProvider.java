/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.monitor.server;

import java.io.InputStream;
import java.util.List;

/**
 * Interface for providing signed JAR files to the client-side DataCleaner
 * instances (launched via Java WebStart).
 */
public interface LaunchArtifactProvider {

    /**
     * Determines if launching artifacts are available. This to allow an
     * implementing class to signal that the artifacts cannot be resolved, and
     * thus disable launching in the user interface etc.
     * 
     * @return
     */
    public boolean isAvailable();

    /**
     * Gets a list of artifact filenames
     * 
     * @return
     */
    public List<String> getJarFilenames();

    /**
     * Reads the content of a single jar file
     * 
     * @param filename
     * @return
     * @throws IllegalArgumentException
     *             in case the filename is not found or not valid
     * @throws IllegalStateException
     *             in case an error occurred while reading from the file
     */
    public InputStream readJarFile(String filename) throws IllegalArgumentException, IllegalStateException;
}
