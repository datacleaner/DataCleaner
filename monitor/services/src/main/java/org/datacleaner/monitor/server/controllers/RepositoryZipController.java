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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.shared.model.SecurityRoles;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.RepositoryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;


@Controller
@RequestMapping("/{tenant}/zip")
public class RepositoryZipController {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryZipController.class);

    @Autowired
    TenantContextFactory _tenantContextFactory;

    @RequestMapping(method = RequestMethod.GET)
    @RolesAllowed(SecurityRoles.ADMIN)
    public void downloadRepository(@PathVariable("tenant") final String tenant, final HttpServletResponse resp)
            throws IOException {
        final TenantContext context = _tenantContextFactory.getContext(tenant);
        final RepositoryFolder rootFolder = context.getTenantRootFolder();

        logger.info("Creating repository ZIP file for tenant: {}", tenant);

        resp.setHeader("Content-Disposition", "attachment; filename=repository_" + tenant + ".zip");
        final ServletOutputStream out = resp.getOutputStream();
        final ZipOutputStream zipOutput = new ZipOutputStream(out);
        compress(rootFolder, zipOutput);

        FileHelper.safeClose(zipOutput);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    @RolesAllowed(SecurityRoles.ADMIN)
    public String uploadRepository(@PathVariable("tenant") final String tenant,
            @RequestParam("file") final MultipartFile file) throws IOException {
        final TenantContext context = _tenantContextFactory.getContext(tenant);
        final RepositoryFolder rootFolder = context.getTenantRootFolder();

        if (file.getSize() == 0) {
            return "Failure. The file is empty";
        }

        final String contentType = file.getContentType().trim();
        if ((!contentType.equals("application/zip")) && (!contentType.equals("application/octet-stream"))) {
            return "Failure. The file isn't a .zip archive";
        }

        logger.info("Uploading ZIP file for tenant repository: {}", tenant);
        try (InputStream inputStream = file.getInputStream()) {
            final ZipInputStream zipInputStream = new ZipInputStream(inputStream);
            if (isValidRepository(zipInputStream)) {
                final File oldRepositoryZipFile = createZipfolder(rootFolder);
                decompress(zipInputStream, rootFolder);
                return "Success.\nNew repository path: " + rootFolder.getQualifiedPath()
                        + ".\nOld repository backed up to: " + oldRepositoryZipFile.toString();
            } else {
                return "Failure. The repository does not contain a conf.xml file";
            }
        }
    }

    private File createZipfolder(final RepositoryFolder rootFolder) throws IOException {
        final long timeInMillis = Calendar.getInstance().getTimeInMillis();
        final File tempfile = File.createTempFile("repository_" + rootFolder.getName() + "_" + timeInMillis, ".zip");
        final FileOutputStream fos = new FileOutputStream(tempfile);
        final ZipOutputStream zipOutput = new ZipOutputStream(fos);
        compress(rootFolder, zipOutput);
        logger.info("The old repository has been compressed and uploaded to" + tempfile.getAbsolutePath());
        FileHelper.safeClose(zipOutput);
        return tempfile;
    }

    /**
     * The repository is valid if it contains a conf.xml file 
     * @param zipInputStream
     * @return
     * @throws IOException
     */
    private boolean isValidRepository(final ZipInputStream zipInputStream) throws IOException {
        for (ZipEntry entry = zipInputStream.getNextEntry(); entry != null; entry = zipInputStream.getNextEntry()) {
            if (entry.getName().trim().equals("conf.xml")) {
                return true;
            }
        }
        return false;
    }

    protected void decompress(final ZipInputStream zipInputStream, final RepositoryFolder rootFolder)
            throws IOException {
        deleteChildren(rootFolder);

        for (ZipEntry entry = zipInputStream.getNextEntry(); entry != null; entry = zipInputStream.getNextEntry()) {
            final String entryName = entry.getName();
            final int lastSlash = entryName.lastIndexOf('/');

            if (entry.isDirectory()) {
                if (entry.getSize() > 0L) {
                    logger.debug("Omitting directory entry: {}", entryName);
                } else {
                    getFolder(rootFolder, entryName.substring(0, lastSlash));
                }
            } else {
                final String filename;
                final RepositoryFolder folder;
                if (lastSlash != -1) {
                    filename = entryName.substring(lastSlash + 1);
                    folder = getFolder(rootFolder, entryName.substring(0, lastSlash));
                } else {
                    filename = entryName;
                    folder = rootFolder;
                }
                final RepositoryFile existingFile = folder.getFile(filename);
                final Action<OutputStream> writeCallback = fileOutput -> FileHelper.copy(zipInputStream, fileOutput);
                if (existingFile == null) {
                    folder.createFile(filename, writeCallback);
                } else {
                    existingFile.writeFile(writeCallback);
                }

            }
        }
    }

    private void deleteChildren(final RepositoryFolder folder) {
        final List<RepositoryFile> files = folder.getFiles();
        for (final RepositoryFile file : files) {
            file.delete();
        }

        final List<RepositoryFolder> folders = folder.getFolders();
        for (final RepositoryFolder subFolder : folders) {
            deleteChildren(subFolder);
            subFolder.delete();
        }
    }

    private RepositoryFolder getFolder(RepositoryFolder folder, final String substring) {
        final String[] directoryNames = substring.split("/");
        for (final String directoryName : directoryNames) {
            final RepositoryFolder existingFolder = folder.getFolder(directoryName);
            if (existingFolder == null) {
                folder = folder.createFolder(directoryName);
            } else {
                folder = existingFolder;
            }
        }
        return folder;
    }

    protected void compress(final RepositoryFolder folder, final ZipOutputStream zipOutput) throws IOException {
        addToZipOutput("", folder, zipOutput);
    }

    private void addToZipOutput(final String path, final RepositoryFolder folder, final ZipOutputStream zipOutput)
            throws IOException {
        int itemsCount = 0;

        final List<RepositoryFile> files = folder.getFiles();
        for (final RepositoryFile file : files) {
            logger.info("File: " + path + file.getName());
            zipOutput.putNextEntry(new ZipEntry(path + file.getName()));
            file.readFile(fileInput -> {
                FileHelper.copy(fileInput, zipOutput);
            });
            zipOutput.closeEntry();
            itemsCount++;
        }

        final List<RepositoryFolder> folders = folder.getFolders();
        for (final RepositoryFolder subFolder : folders) {
            final String name = subFolder.getName();
            logger.info("Directory: " + path + name + "/");
            addToZipOutput(path + name + "/", subFolder, zipOutput);
            itemsCount++;
        }

        if (itemsCount == 0 && !path.equals("")) {
            logger.info("Empty: " + path);
            final ZipEntry entry = new ZipEntry(path);
            zipOutput.putNextEntry(entry);
        }
    }
}
