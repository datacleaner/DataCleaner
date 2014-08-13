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
package org.eobjects.datacleaner.repository;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.Func;
import org.apache.metamodel.util.Resource;
import org.apache.metamodel.util.ResourceException;
import org.apache.metamodel.util.SerializableRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Resource} wrapping of a {@link RepositoryFile}.
 */
public class RepositoryFileResource implements Resource, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(RepositoryFileResource.class);

    private final SerializableRef<RepositoryFile> _fileRef;
    private final String _qualifiedPath;

    /**
     * Constructs a {@link RepositoryFileResource} based on a {@link Repository}
     * and a qualified path for the file
     * 
     * @param repo
     * @param qualifiedPath
     */
    public RepositoryFileResource(Repository repo, String qualifiedPath) {
        _qualifiedPath = qualifiedPath;

        final RepositoryFile file = (RepositoryFile) repo.getRepositoryNode(qualifiedPath);
        if (file == null) {
            logger.warn("Repository node did not exist: {}", qualifiedPath);
        }
        _fileRef = new SerializableRef<RepositoryFile>(file);
    }

    /**
     * Constructs a {@link RepositoryFileResource} based on a
     * {@link RepositoryFile}.
     * 
     * @param file
     */
    public RepositoryFileResource(RepositoryFile file) {
        _fileRef = new SerializableRef<RepositoryFile>(file);
        _qualifiedPath = file.getQualifiedPath();
    }

    /**
     * Recreates the {@link RepositoryFileResource} after deserialization, if
     * the {@link RepositoryFile} could not be included in the serialized
     * object.
     * 
     * @param repository
     * @return
     */
    public RepositoryFileResource recreate(Repository repository) {
        return new RepositoryFileResource(repository, _qualifiedPath);
    }

    /**
     * Gets the underlying {@link RepositoryFile}.
     * 
     * @return
     */
    public RepositoryFile getRepositoryFile() throws ResourceException {
        if (_fileRef == null || _fileRef.get() == null) {
            throw new ResourceException(
                    this,
                    "RepositoryFile '"
                            + _qualifiedPath
                            + "' is not available since it was not serializable. The RepositoryFileResource instance can be recreated using a live repository and the qualified path of the file.");
        }
        return _fileRef.get();
    }

    /**
     * Gets the qualified path of the file. If the resource has been
     * deserialized and {@link #getRepositoryFile()} returns null because the
     * file was not serializable, then the qualified path can be used to
     * recreate a new {@link RepositoryFileResource} if a {@link Repository} is
     * also available.
     * 
     * @see #recreate(Repository)
     * 
     * @return
     */
    public String getQualifiedPath() {
        return _qualifiedPath;
    }

    @Override
    public String toString() {
        return "RepositoryFileResource[" + _qualifiedPath + "]";
    }

    @Override
    public long getLastModified() {
        return getRepositoryFile().getLastModified();
    }

    @Override
    public String getName() {
        return getRepositoryFile().getName();
    }

    @Override
    public long getSize() {
        return getRepositoryFile().getSize();
    }

    @Override
    public boolean isExists() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public InputStream read() throws ResourceException {
        return getRepositoryFile().readFile();
    }

    @Override
    public void append(final Action<OutputStream> appendAction) throws ResourceException {
        getRepositoryFile().writeFile(appendAction, true);
    }

    @Override
    public void read(Action<InputStream> callback) throws ResourceException {
        getRepositoryFile().readFile(callback);
    }

    @Override
    public <E> E read(Func<InputStream, E> callback) throws ResourceException {
        return getRepositoryFile().readFile(callback);
    }

    @Override
    public void write(Action<OutputStream> callback) throws ResourceException {
        getRepositoryFile().writeFile(callback);
    }

}
