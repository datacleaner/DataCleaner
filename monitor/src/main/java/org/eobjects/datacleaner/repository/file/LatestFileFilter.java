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

import java.io.File;
import java.io.FileFilter;

/**
 * A file filter decoration used to find the latest of all files that pass
 * through another {@link FileFilter}.
 */
public class LatestFileFilter implements FileFilter {

    private final FileFilter _delegate;
    private File _candidate;
    private long _latestModified;

    public LatestFileFilter(FileFilter delegate) {
        _delegate = delegate;
        _latestModified = Long.MIN_VALUE;
    }

    @Override
    public boolean accept(File candidate) {
        if (candidate.lastModified() > _latestModified && _delegate.accept(candidate)) {
            _latestModified = candidate.lastModified();
            _candidate = candidate;
        }
        // always return false, this filter should not be used as a mapping
        // function, but as an aggregate.
        return false;
    }

    public File getLatestFile() {
        return _candidate;
    }

}
