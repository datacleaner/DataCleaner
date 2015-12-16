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
package org.datacleaner.util;

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.filechooser.FileFilter;

import org.apache.metamodel.util.Resource;

/**
 * A file filter for JFileChooser's which filters on behalf of the file
 * extension
 */
public class ExtensionFilter extends FileFilter implements FilenameFilter {

    private final String _desc;
    private final String _extension;
    private final boolean _includeDirectories;

    public ExtensionFilter(String desc, String extension) {
        this(desc, extension, true);
    }

    public ExtensionFilter(String desc, String extension, boolean includeDirectories) {
        _includeDirectories = includeDirectories;
        _desc = desc;
        _extension = extension.toLowerCase();
    }

    public boolean accept(Resource resource) {
        if (resource == null) {
            return false;
        }
        return accept(resource.getName());
    }

    public boolean accept(String filename) {
        if (filename.length() < _extension.length()) {
            return false;
        }

        return accept(null, filename);
    }

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return _includeDirectories;
        }
        final String filename = f.getAbsolutePath();
        return accept(filename);
    }

    @Override
    public boolean accept(File dir, String name) {
        final int startIndex = name.length() - _extension.length();
        if (startIndex < 0) {
            return false;
        }

        final String extension = name.substring(startIndex);
        if (extension.equalsIgnoreCase(_extension)) {
            return true;
        }

        return false;
    }

    @Override
    public String getDescription() {
        return _desc;
    }

    public String getExtension() {
        return _extension;
    }
}
