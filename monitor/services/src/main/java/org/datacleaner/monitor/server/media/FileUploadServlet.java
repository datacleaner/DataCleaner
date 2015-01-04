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
package org.datacleaner.monitor.server.media;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.metamodel.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Servlet used to host images from the classpath. This functionality is used
 * primarily in plug-ins, since they need to bundle everything in JAR files, so
 * resources originate from the classpath.
 */
public class FileUploadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * Maximum size for an uploaded file (64 megs)
     */
    private static final long FILE_SIZE_MAX = 64 * 1024 * 1024;

    /**
     * Maximum size for a full request's payload (also 64 megs)
     */
    private static final long REQUEST_SIZE_MAX = FILE_SIZE_MAX;

    private static final Logger logger = LoggerFactory.getLogger(FileUploadServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        clearSession(req);

        File tempFolder = FileHelper.getTempDir();
        try {
            File subDirectory = new File(tempFolder, ".datacleaner_upload");
            if (subDirectory.mkdirs()) {
                tempFolder = subDirectory;
            }
        } catch (Exception e) {
            logger.warn("Could not create subdirectory in temp folder", e);
        }

        final FileItemFactory fileItemFactory = new DiskFileItemFactory(0, tempFolder);
        final ServletFileUpload servletFileUpload = new ServletFileUpload(fileItemFactory);
        servletFileUpload.setFileSizeMax(FILE_SIZE_MAX);
        servletFileUpload.setSizeMax(REQUEST_SIZE_MAX);

        final List<Object> resultFileElements = new ArrayList<Object>();
        final HttpSession session = req.getSession();

        try {
            int index = 0;
            @SuppressWarnings("unchecked")
            final List<DiskFileItem> items = servletFileUpload.parseRequest(req);
            for (DiskFileItem item : items) {
                if (item.isFormField()) {
                    logger.warn("Ignoring form field in request: {}", item);
                } else {
                    final String sessionKey = "file_upload_" + index;
                    final File file = item.getStoreLocation();

                    String filename = toFilename(item.getName());
                    logger.info("File '{}' uploaded to temporary location: {}", filename, file);

                    session.setAttribute(sessionKey, file);

                    final Map<String, String> resultItem = new LinkedHashMap<String, String>();
                    resultItem.put("field_name", item.getFieldName());
                    resultItem.put("file_name", filename);
                    resultItem.put("content_type", item.getContentType());
                    resultItem.put("size", Long.toString(item.getSize()));
                    resultItem.put("session_key", sessionKey);

                    resultFileElements.add(resultItem);

                    index++;
                }
            }
        } catch (FileUploadException e) {
            logger.error("Unexpected file upload exception: " + e.getMessage(), e);
            throw new IOException(e);
        }

        final String contentType = req.getParameter("contentType");
        if (contentType == null) {
            resp.setContentType("application/json");
        } else {
            resp.setContentType(contentType);
        }

        final Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
        resultMap.put("status", "success");
        resultMap.put("files", resultFileElements);

        // write result as JSON
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(resp.getOutputStream(), resultMap);
    }

    protected static String toFilename(String name) {
        if (name == null) {
            return null;
        }
        if (name.endsWith("/") || name.endsWith("\\")) {
            final String substr = name.substring(0, name.length() - 1);
            return toFilename(substr);
        }
        int index1 = name.lastIndexOf('\\');
        int index2 = name.lastIndexOf('/');
        int index = Math.max(index1, index2);
        if (index != -1) {
            return name.substring(index + 1);
        }
        return name;
    }

    /**
     * Clears the HTTP session of the request from any file-upload related
     * state.
     * 
     * @param req
     */
    public static final void clearSession(HttpServletRequest req) {
        final HttpSession session = req.getSession();
        clearSession(session);
    }

    /**
     * Clears the HTTP session from any file-upload related state.
     * 
     * @param session
     */
    public static final void clearSession(HttpSession session) {
        int index = 0;
        while (true) {
            final String sessionKey = "file_upload_" + index;
            final Object sessionValue = session.getAttribute(sessionKey);
            if (sessionValue == null) {
                return;
            } else {
                session.removeAttribute(sessionKey);
            }
            index++;
        }
    }
}
