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
package org.datacleaner.repository;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.Func;
import org.apache.metamodel.util.Resource;

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
     * @deprecated prefer to use {@link #readFile(Action)} or
     *             {@link #readFile(Func)} instead.
     */
    @Deprecated
    public InputStream readFile();

    /**
     * Opens up an {@link OutputStream} to write to the file.
     * 
     * @param append
     *            whether or not to append to the existing contents of the file.
     * @return prefer to use {@link #writeFile(Action)} or
     *         {@link #writeFile(Action, boolean)} instead.
     */
    @Deprecated
    public OutputStream writeFile(boolean append);

    /**
     * Gets the size (in number of bytes) of this file's data. An approximated
     * number is allowed.
     * 
     * If the size is not determinable without actually reading through the
     * whole contents of the resource, -1 is returned.
     * 
     * @return
     */
    public long getSize();

    /**
     * Opens up an {@link OutputStream} to write to the file, and allows a
     * callback to perform writing actions on it.
     * 
     * @param writeCallback
     *            a callback which should define what to write to the file.
     */
    public void writeFile(Action<OutputStream> writeCallback);

    /**
     * Opens up an {@link OutputStream} to write to the file, and allows a
     * callback to perform writing actions on it.
     * 
     * @param writeCallback
     * @param append
     *            whether or not to append to the existing content (if any) of
     *            the file.
     */
    public void writeFile(Action<OutputStream> writeCallback, boolean append);

    /**
     * Opens up an {@link InputStream} to read from the file, and allows a
     * callback to perform writing actions on it.
     * 
     * @param readCallback
     */
    public void readFile(Action<InputStream> readCallback);

    /**
     * Opens up an {@link InputStream} to read from the file, and allows a
     * callback function to perform writing actions on it and return the
     * function's result.
     * 
     * @param readCallback
     * @return the result of the function
     */
    public <E> E readFile(Func<InputStream, E> readCallback);

    /**
     * Gets the type of the file.
     * 
     * @return the type of the file.
     */
    public Type getType();

    /**
     * Gets the last modified timestamp, specified as the number of milliseconds
     * since the standard base time known as "the epoch", namely January 1,
     * 1970, 00:00:00 GMT.
     * 
     * @return the last modified timestamp, or -1 if there was an error reading
     *         the timestamp
     */
    public long getLastModified();

    /**
     * Gets a {@link Resource} representation of the {@link RepositoryFile}.
     * 
     * @return
     */
    public Resource toResource();
}
