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
package org.eobjects.datacleaner.monitor.server.media;

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
import org.codehaus.jackson.map.ObjectMapper;
import org.eobjects.metamodel.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        final File tempFolder = FileHelper.getTempDir();
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
                    session.setAttribute(sessionKey, item.getStoreLocation());

                    final Map<String, String> resultItem = new LinkedHashMap<String, String>();
                    resultItem.put("field_name", item.getFieldName());
                    resultItem.put("file_name", item.getName());
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

        resp.setContentType("application/json");

        final Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
        resultMap.put("status", "success");
        resultMap.put("files", resultFileElements);

        // write result as JSON
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(resp.getOutputStream(), resultMap);
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
