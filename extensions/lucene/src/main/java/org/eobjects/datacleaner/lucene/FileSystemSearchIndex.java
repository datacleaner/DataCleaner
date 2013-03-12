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
package org.eobjects.datacleaner.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

/**
 * A file based {@link SearchIndex}. Specifically, this is based on the
 * {@link NIOFSDirectory} implementation from Lucene.
 */
public class FileSystemSearchIndex extends AbstractSearchIndex {

    private static final long serialVersionUID = 1L;
    private final Directory _directory;
    private final File _file;

    public FileSystemSearchIndex(String name, File directory) {
        super(name);
        _file = directory;

        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("File argument must be a directory");
        }

        try {
            _directory = new NIOFSDirectory(directory);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to construct Lucene directory", e);
        }
    }
    
    public File getFile() {
        return _file;
    }

    @Override
    protected Directory getDirectory() {
        return _directory;
    }

}
