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
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.datacleaner.extensions.ClassLoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides service methods related to resources on the classpath or the file
 * system.
 */
public final class ResourceManager {

    private static final Logger logger = LoggerFactory.getLogger(ResourceManager.class);

    private static ResourceManager instance = new ResourceManager();

    private ResourceManager() {
        // only a single instance
    }

    /**
     * Gets the singleton instance of {@link ResourceManager}.
     *
     * @return
     */
    public static ResourceManager get() {
        return instance;
    }

    public List<URL> getUrls(final String path, ClassLoader... classLoaders) {
        if (classLoaders == null || classLoaders.length == 0) {
            if (ClassLoaderUtils.getParentClassLoader().equals(getClass().getClassLoader())) {
                classLoaders = new ClassLoader[] { ClassLoaderUtils.getParentClassLoader() };
            } else {
                classLoaders =
                        new ClassLoader[] { ClassLoaderUtils.getParentClassLoader(), getClass().getClassLoader() };
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Custom classloaders specified: {}", Arrays.toString(classLoaders));
            }
        }

        final List<URL> result = new LinkedList<>();
        final URL url = getClass().getResource(path);
        if (url != null) {
            result.add(url);
        }

        try {
            for (final ClassLoader classLoader : classLoaders) {
                final Enumeration<URL> resources = classLoader.getResources(path);
                while (resources.hasMoreElements()) {
                    final URL element = resources.nextElement();
                    if (element == null) {
                        logger.warn("ClassLoader {} returned a null URL resource for path '{}'", classLoader, path);
                    } else {
                        result.add(element);
                    }
                }
            }
        } catch (final IOException e) {
            logger.error("IOException when investigating classloader resources", e);
        }

        // when running in eclipse this file-based hack is necessary
        final File file = new File("src/main/resources/" + path);
        if (file.exists()) {
            try {
                result.add(file.toURI().toURL());
            } catch (final IOException e) {
                logger.error("IOException when adding File-based resource to URLs", e);
            }
        }

        return result;
    }

    public URL getUrl(final String path, final ClassLoader... classLoaders) {
        final List<URL> urls = getUrls(path, classLoaders);
        if (urls.isEmpty()) {
            return null;
        }
        return urls.get(0);
    }
}
