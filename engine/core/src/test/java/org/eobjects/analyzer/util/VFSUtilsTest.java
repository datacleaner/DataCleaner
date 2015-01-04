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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

import junit.framework.TestCase;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;

public class VFSUtilsTest extends TestCase {

    public void test1VfsAssumptions() throws Exception {
        FileSystemManager manager = VFS.getManager();

        FileObject baseFile = manager.getBaseFile();
        assertTrue(baseFile == null || "core".equals(baseFile.getName().getBaseName()));

        File file = new File("src/main/java");
        assertNotNull(manager.resolveFile(file.getAbsolutePath()));

        ((DefaultFileSystemManager) manager).setBaseFile(new File("."));

        baseFile = manager.getBaseFile();

        assertEquals("core", baseFile.getName().getBaseName());

        FileObject javaFolder = manager.resolveFile("src/main/java");
        assertTrue(javaFolder.getType() == FileType.FOLDER);

        FileObject rootFolder = manager.resolveFile(".");
        assertTrue(rootFolder.getType() == FileType.FOLDER);
        assertEquals("core", rootFolder.getName().getBaseName());

        File javaFolderFile = VFSUtils.toFile(javaFolder);
        assertNotNull(javaFolderFile);
        assertTrue(javaFolderFile.exists());
        assertTrue(javaFolderFile.isDirectory());
    }

    public void test2HttpAccess() throws Exception {
        // first check if we have a connection
        try {
            InetAddress.getByName("eobjects.org");
        } catch (UnknownHostException e) {
            System.err.println("Skipping test " + getClass().getSimpleName() + "." + getName()
                    + " since we don't seem to be able to reach eobjects.org");
            e.printStackTrace();
            return;
        }

        FileObject file = VFSUtils.getFileSystemManager().resolveFile("http://eobjects.org");
        try (InputStream in = file.getContent().getInputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String readLine = reader.readLine();
            assertNotNull(readLine);
        }
    }

    public void testToFileObjectNull() throws Exception {
        assertNull(VFSUtils.toFileObject(null));
        assertNull(VFSUtils.toFile(null));
    }
}
