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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.shared.model.SecurityRoles;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.metamodel.util.Action;
import org.eobjects.metamodel.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    public void downloadRepository(@PathVariable("tenant") final String tenant, HttpServletResponse resp)
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

    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    @RolesAllowed(SecurityRoles.ADMIN)
    public Map<String, String> uploadRepository(@PathVariable("tenant") final String tenant,
            @RequestParam("file") final MultipartFile file) throws IOException {
        final TenantContext context = _tenantContextFactory.getContext(tenant);
        final RepositoryFolder rootFolder = context.getTenantRootFolder();

        logger.info("Uploading ZIP file for tenant repository: {}", tenant);

        final InputStream inputStream = file.getInputStream();
        try {
            ZipInputStream zipInputStream = new ZipInputStream(inputStream);
            decompress(zipInputStream, rootFolder);
        } finally {
            inputStream.close();
        }

        final Map<String, String> result = new HashMap<String, String>();
        result.put("status", "Success");
        result.put("repository_path", rootFolder.getQualifiedPath());

        return result;
    }

    protected void decompress(final ZipInputStream zipInputStream, final RepositoryFolder rootFolder)
            throws IOException {
        deleteChildren(rootFolder);
        
        for (ZipEntry entry = zipInputStream.getNextEntry(); entry != null; entry = zipInputStream.getNextEntry()) {
            final String entryName = entry.getName();
            if (entry.isDirectory()) {
                logger.debug("Omitting directory entry: {}", entryName);
            } else {
                int lastSlash = entryName.lastIndexOf('/');
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
                final Action<OutputStream> writeCallback = new Action<OutputStream>() {
                    @Override
                    public void run(OutputStream fileOutput) throws Exception {
                        FileHelper.copy(zipInputStream, fileOutput);
                    }
                };
                if (existingFile == null) {
                    folder.createFile(filename, writeCallback);
                } else {
                    existingFile.writeFile(writeCallback);
                }

            }
        }
    }

    private void deleteChildren(RepositoryFolder folder) {
        List<RepositoryFile> files = folder.getFiles();
        for (RepositoryFile file : files) {
            file.delete();
        }
        
        List<RepositoryFolder> folders = folder.getFolders();
        for (RepositoryFolder subFolder : folders) {
            deleteChildren(subFolder);
            subFolder.delete();
        }
    }

    private RepositoryFolder getFolder(RepositoryFolder folder, String substring) {
        String[] directoryNames = substring.split("/");
        for (String directoryName : directoryNames) {
            RepositoryFolder existingFolder = folder.getFolder(directoryName);
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
        final List<RepositoryFile> files = folder.getFiles();
        for (RepositoryFile file : files) {
            zipOutput.putNextEntry(new ZipEntry(path + file.getName()));
            file.readFile(new Action<InputStream>() {
                @Override
                public void run(InputStream fileInput) throws Exception {
                    FileHelper.copy(fileInput, zipOutput);
                }
            });
            zipOutput.closeEntry();
        }

        final List<RepositoryFolder> folders = folder.getFolders();
        for (RepositoryFolder subFolder : folders) {
            String name = subFolder.getName();
            addToZipOutput(path + name + "/", subFolder, zipOutput);
        }
    }
}
