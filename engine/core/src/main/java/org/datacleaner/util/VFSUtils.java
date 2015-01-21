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
import java.lang.reflect.Method;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.local.LocalFile;

/**
 * Convenience methods for commons VFS.
 */
public class VFSUtils {

    /**
     * Gets the file system manager to use in typical scenarios.
     * 
     * @return
     */
    public static FileSystemManager getFileSystemManager() {
        try {
            final FileSystemManager manager = VFS.getManager();
            if (manager.getBaseFile() == null) {
                // if no base file exists, set the working directory to base
                // dir.
                ((DefaultFileSystemManager) manager).setBaseFile(new File("."));
            }
            return manager;
        } catch (FileSystemException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Converts a {@link File} to a FileObject.
     * 
     * @param file
     * @return
     */
    public static FileObject toFileObject(File file) {
        if (file == null) {
            return null;
        }
        try {
            return getFileSystemManager().toFileObject(file);
        } catch (FileSystemException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Converts (if possible) a {@link FileObject} to a {@link File}. Use with
     * caution since {@link FileObject} is generally preferred.
     * 
     * @param fileObject
     * @return
     */
    public static File toFile(FileObject fileObject) {
        if (fileObject instanceof LocalFile) {
            Method method = ReflectionUtils.getMethod(LocalFile.class, "getLocalFile");
            try {
                method.setAccessible(true);
                Object result = method.invoke(fileObject);
                return (File) result;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return null;
    }
}
