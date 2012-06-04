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
package org.eobjects.datacleaner.repository.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.metamodel.util.Action;
import org.eobjects.metamodel.util.FileHelper;

/**
 * {@link RepositoryFile} implementation based on a local file.
 */
final class FileRepositoryFile implements RepositoryFile {

    private static final long serialVersionUID = 1L;

    private final FileRepositoryFolder _parent;
    private final File _file;

    public FileRepositoryFile(FileRepositoryFolder parent, File file) {
        _parent = parent;
        _file = file;
    }

    @Override
    public RepositoryFolder getParent() {
        return _parent;
    }

    @Override
    public String getName() {
        return _file.getName();
    }

    @Override
    public String getQualifiedPath() {
        return _parent.getQualifiedPath() + "/" + getName();
    }

    @Override
    public InputStream readFile() {
        try {
            return new BufferedInputStream(new FileInputStream(_file));
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void writeFile(Action<OutputStream> writeCallback) {
        final OutputStream outputStream;
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(_file));
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }

        try {
            writeCallback.run(outputStream);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IllegalStateException("Error occurred while writing to file", e);
        } finally {
            FileHelper.safeClose(outputStream);
        }
    }

    @Override
    public Type getType() {
        if (getName().endsWith(FileFilters.ANALYSIS_XML.getExtension())) {
            return Type.ANALYSIS_JOB;
        } else if (getName().endsWith(FileFilters.ANALYSIS_RESULT_SER.getExtension())) {
            return Type.ANALYSIS_RESULT;
        } else if (getName().endsWith(FileFilters.ANALYSIS_TIMELINE_XML.getExtension())) {
            return Type.TIMELINE_SPEC;
        }

        return Type.OTHER;
    }

    @Override
    public String toString() {
        return "RepositoryFile[" + getName() + "]";
    }

    @Override
    public void delete() throws IllegalStateException {
        final boolean success = _file.delete();
        if (!success) {
            throw new IllegalStateException("Could not delete file: " + _file);
        }
    }
}
