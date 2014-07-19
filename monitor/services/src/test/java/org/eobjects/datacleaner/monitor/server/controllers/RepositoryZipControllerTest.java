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
package org.eobjects.datacleaner.monitor.server.controllers;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.repository.file.FileRepository;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Func;

public class RepositoryZipControllerTest extends TestCase {

    public void testCompressAndDecompress() throws Exception {
        RepositoryZipController controller = new RepositoryZipController();
        RepositoryFolder repo = new FileRepository("src/test/resources/example_repo");
        ZipOutputStream zipOutput = new ZipOutputStream(new FileOutputStream("target/test_zipfile.zip"));
        RepositoryFolder sourceFolder = repo.getFolder("tenant1");
        controller.compress(sourceFolder, zipOutput);
        zipOutput.close();

        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream("target/test_zipfile.zip"));
        File targetRepoFolder = new File("target/decompressed_repo");
        FileUtils.deleteDirectory(targetRepoFolder);
        targetRepoFolder.mkdirs();
        RepositoryFolder targetFolder = new FileRepository(targetRepoFolder).createFolder("tenant1");
        targetFolder.createFolder("foobar_removeMe");
        targetFolder.createFile("Yes_remove_me.too", null);
        
        
        controller.decompress(zipInputStream, targetFolder);

        assertFoldersSimilar(sourceFolder, targetFolder);
    }

    private void assertFoldersSimilar(RepositoryFolder sourceFolder, RepositoryFolder targetFolder) {
        assertEquals(sourceFolder.getName(), targetFolder.getName());

        List<RepositoryFile> files1 = sourceFolder.getFiles();
        List<RepositoryFile> files2 = targetFolder.getFiles();

        assertEquals("Not the same amount of files in " + sourceFolder + " and " + targetFolder, files1.size(),
                files2.size());

        for (int i = 0; i < files1.size(); i++) {
            RepositoryFile file1 = files1.get(i);
            RepositoryFile file2 = files2.get(i);
            assertFilesSame(file1, file2);
        }

        List<RepositoryFolder> folders1 = sourceFolder.getFolders();
        List<RepositoryFolder> folders2 = targetFolder.getFolders();

        assertEquals("Not the same amount of sub-folders in " + sourceFolder + " and " + targetFolder, folders1.size(),
                folders2.size());

        for (int i = 0; i < folders1.size(); i++) {
            RepositoryFolder folder1 = folders1.get(i);
            RepositoryFolder folder2 = folders2.get(i);
            assertFoldersSimilar(folder1, folder2);
        }
    }

    private void assertFilesSame(RepositoryFile file1, RepositoryFile file2) {
        assertEquals(file1.getName(), file2.getName());

        Func<InputStream, byte[]> readCallback = new Func<InputStream, byte[]>() {
            @Override
            public byte[] eval(InputStream in) {
                return FileHelper.readAsBytes(in);
            }
        };
        byte[] bytes1 = file1.readFile(readCallback);
        byte[] bytes2 = file2.readFile(readCallback);

        assertArrayEquals(bytes1, bytes2);
    }
}
