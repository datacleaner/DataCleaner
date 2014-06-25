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
package org.eobjects.datacleaner.monitor.server.media;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eobjects.datacleaner.util.ResourceManager;
import org.eobjects.metamodel.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet used to host images from the classpath. This functionality is used
 * primarily in plug-ins, since they need to bundle everything in JAR files, so
 * resources originate from the classpath.
 */
public class ImageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(ImageServlet.class);

    private final ResourceManager _resourceManager;
    private final Map<String, String> _fallbackImages;

    public ImageServlet() {
        super();
        _resourceManager = ResourceManager.get();
        _fallbackImages = new ConcurrentHashMap<String, String>();
        _fallbackImages.put("datastore", "org/eobjects/datacleaner/monitor/resources/datastore.png");
        _fallbackImages.put("job", "org/eobjects/datacleaner/monitor/resources/job.png");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String pathInfo = req.getPathInfo();
        logger.debug("Path info: {}", pathInfo);

        validatePath(pathInfo, resp);

        URL url = _resourceManager.getUrl(pathInfo);
        if (url == null) {
            final String fallbackType = req.getParameter("fallback");
            if (fallbackType == null) {
                throw new IllegalArgumentException("No such resource: " + pathInfo);
            }
            final String fallbackImage = _fallbackImages.get(fallbackType);

            if (fallbackImage == null) {
                throw new IllegalArgumentException("No such resource: " + pathInfo);
            }

            logger.info("Could not find path resource '{}'. Using fallback resource: {}", pathInfo, fallbackImage);
            url = _resourceManager.getUrl(fallbackImage);
            if (url == null) {
                throw new IllegalStateException("Fallback image for '" + fallbackType + "' was not found");
            }
        }

        final InputStream in = url.openStream();
        try {
            final ServletOutputStream out = resp.getOutputStream();
            try {
                FileHelper.copy(in, out);
            } finally {
                FileHelper.safeClose(out);
            }
        } finally {
            FileHelper.safeClose(in);
        }
    }

    private void validatePath(String path, HttpServletResponse resp) {
        if (path == null) {
            throw new IllegalArgumentException("Resource path cannot be null");
        }
        path = path.toLowerCase();

        if (path.endsWith(".png")) {
            resp.setContentType("image/png");
            return;
        }
        if (path.endsWith(".jpg")) {
            resp.setContentType("image/jpeg");
            return;
        }
        if (path.endsWith(".gif")) {
            resp.setContentType("image/gif");
            return;
        }

        throw new IllegalArgumentException("Unsupported resource type: " + path);
    }
}
