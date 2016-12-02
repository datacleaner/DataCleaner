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
package org.datacleaner.monitor.server.controllers;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Func;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.repository.file.FileRepository;

import com.google.common.io.Files;

import junit.framework.TestCase;

public class RepositoryZipControllerTest extends TestCase {

    public void testCompressAndDecompress() throws Exception {
        final RepositoryZipController controller = new RepositoryZipController();
        final RepositoryFolder repo = new FileRepository("src/test/resources/example_repo");
        final RepositoryFolder sourceFolder;
        try (ZipOutputStream zipOutput = new ZipOutputStream(new FileOutputStream("target/test_zipfile.zip"))) {
            sourceFolder = repo.getFolder("tenant1");
            controller.compress(sourceFolder, zipOutput);
        }

        final ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream("target/test_zipfile.zip"));
        final File targetRepoFolder = Files.createTempDir();
        targetRepoFolder.deleteOnExit();
        final RepositoryFolder targetFolder = new FileRepository(targetRepoFolder).createFolder("tenant1");
        targetFolder.createFolder("foobar_removeMe");
        targetFolder.createFile("Yes_remove_me.too", null);

        controller.decompress(zipInputStream, targetFolder);

        assertFoldersSimilar(sourceFolder, targetFolder);
    }

    private void assertFoldersSimilar(final RepositoryFolder sourceFolder, final RepositoryFolder targetFolder) {
        assertEquals(sourceFolder.getName(), targetFolder.getName());

        final List<RepositoryFile> files1 = sourceFolder.getFiles();
        final List<RepositoryFile> files2 = targetFolder.getFiles();

        assertEquals("Not the same amount of files in " + sourceFolder + " and " + targetFolder, files1.size(),
                files2.size());

        for (int i = 0; i < files1.size(); i++) {
            final RepositoryFile file1 = files1.get(i);
            final RepositoryFile file2 = files2.get(i);
            assertFilesSame(file1, file2);
        }

        final List<RepositoryFolder> folders1 = sourceFolder.getFolders();
        final List<RepositoryFolder> folders2 = targetFolder.getFolders();

        assertEquals("Not the same amount of sub-folders in " + sourceFolder + " and " + targetFolder, folders1.size(),
                folders2.size());

        for (int i = 0; i < folders1.size(); i++) {
            final RepositoryFolder folder1 = folders1.get(i);
            final RepositoryFolder folder2 = folders2.get(i);
            assertFoldersSimilar(folder1, folder2);
        }
    }

    private void assertFilesSame(final RepositoryFile file1, final RepositoryFile file2) {
        assertEquals(file1.getName(), file2.getName());

        final Func<InputStream, byte[]> readCallback = FileHelper::readAsBytes;
        final byte[] bytes1 = file1.readFile(readCallback);
        final byte[] bytes2 = file2.readFile(readCallback);

        assertArrayEquals(bytes1, bytes2);
    }
}
