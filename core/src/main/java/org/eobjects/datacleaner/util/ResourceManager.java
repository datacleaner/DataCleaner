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
package org.eobjects.datacleaner.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.eobjects.analyzer.util.ClassLoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides service methods related to resources on the classpath or the file
 * system.
 * 
 * @author kasper
 * 
 */
public final class ResourceManager {

	private static final Logger logger = LoggerFactory.getLogger(ResourceManager.class);

	private static ResourceManager instance = new ResourceManager();

	public static ResourceManager getInstance() {
		return instance;
	}

	private ResourceManager() {
		// only a single instance
	}

	public List<URL> getUrls(String path, ClassLoader... classLoaders) {
		if (classLoaders == null || classLoaders.length == 0) {
			classLoaders = new ClassLoader[] { ClassLoaderUtils.getParentClassLoader() };
		} else {
		    if (logger.isDebugEnabled()) {
		        logger.debug("Custom classloaders specified: {}", Arrays.toString(classLoaders));
		    }
		}

		List<URL> result = new LinkedList<URL>();
		URL url = getClass().getResource(path);
		if (url != null) {
			result.add(url);
		}

		try {
			for (ClassLoader classLoader : classLoaders) {
				Enumeration<URL> resources = classLoader.getResources(path);
				while (resources.hasMoreElements()) {
					result.add(resources.nextElement());
				}
			}
		} catch (IOException e) {
			logger.error("IOException when investigating classloader resources", e);
		}

		// when running in eclipse this file-based hack is nescesary
		File file = new File("src/main/resources/" + path);
		if (file.exists()) {
			try {
				result.add(file.toURI().toURL());
			} catch (IOException e) {
				logger.error("IOException when adding File-based resource to URLs", e);
			}
		}

		return result;
	}

	public URL getUrl(String path, ClassLoader... classLoaders) {
		List<URL> urls = getUrls(path, classLoaders);
		if (urls.isEmpty()) {
			return null;
		}
		return urls.get(0);
	}
}
