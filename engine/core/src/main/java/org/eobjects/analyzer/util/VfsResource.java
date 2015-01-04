/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.util;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Func;
import org.apache.metamodel.util.Resource;
import org.apache.metamodel.util.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Resource} implementation for Commons VFS {@link FileObject}s.
 */
public class VfsResource implements Resource {

    private static final Logger logger = LoggerFactory.getLogger(VfsResource.class);

    private final FileObject _fileObject;

    public VfsResource(FileObject fileObject) {
        _fileObject = fileObject;
    }
    
    @Override
    public String toString() {
        return "VfsResource[" + _fileObject + "]";
    }

    /**
     * Gets the wrapped {@link FileObject} of this resource.
     * 
     * @return
     */
    public FileObject getFileObject() {
        return _fileObject;
    }

    @Override
    public String getName() {
        return _fileObject.getName().getBaseName();
    }

    @Override
    public boolean isReadOnly() {
        try {
            return !_fileObject.isWriteable();
        } catch (FileSystemException e) {
            throw new ResourceException(this, e);
        }
    }

    @Override
    public boolean isExists() {
        try {
            return _fileObject.exists();
        } catch (FileSystemException e) {
            throw new ResourceException(this, e);
        }
    }
    
    @Override
    public String getQualifiedPath() {
        return _fileObject.getName().getURI();
    }

    @Override
    public long getSize() {
        try {
            return _fileObject.getContent().getSize();
        } catch (FileSystemException e) {
            throw new ResourceException(this, e);
        }
    }

    @Override
    public long getLastModified() {
        try {
            final long lastModified = _fileObject.getContent().getLastModifiedTime();
            if (lastModified == 0) {
                return -1;
            }
            return lastModified;
        } catch (FileSystemException e) {
            logger.warn("Failed to get lastModifiedTime of file object: " + _fileObject + ". Returning -1.", e);
            return -1;
        }
    }

    @Override
    public void write(Action<OutputStream> writeCallback) throws ResourceException {
        try {
            FileContent content = _fileObject.getContent();
            OutputStream out = content.getOutputStream();
            try {
                writeCallback.run(out);
            } finally {
                FileHelper.safeClose(out);
            }
        } catch (Exception e) {
            throw new ResourceException(this, e);
        }
    }

    @Override
    public void append(Action<OutputStream> appendCallback) throws ResourceException {
        try {
            FileContent content = _fileObject.getContent();
            OutputStream out = content.getOutputStream(true);
            try {
                appendCallback.run(out);
            } finally {
                FileHelper.safeClose(out);
            }
        } catch (Exception e) {
            throw new ResourceException(this, e);
        }
    }

    @Override
    public InputStream read() throws ResourceException {
        try {
            return _fileObject.getContent().getInputStream();
        } catch (FileSystemException e) {
            throw new ResourceException(this, e);
        }
    }

    @Override
    public void read(Action<InputStream> readCallback) throws ResourceException {
        try {
            InputStream in = _fileObject.getContent().getInputStream();
            try {
                readCallback.run(in);
            } finally {
                FileHelper.safeClose(in);
            }
        } catch (Exception e) {
            throw new ResourceException(this, e);
        }
    }

    @Override
    public <E> E read(Func<InputStream, E> readCallback) throws ResourceException {
        try {
            InputStream in = _fileObject.getContent().getInputStream();
            try {
                E result = readCallback.eval(in);
                return result;
            } finally {
                FileHelper.safeClose(in);
            }
        } catch (Exception e) {
            throw new ResourceException(this, e);
        }
    }
}
