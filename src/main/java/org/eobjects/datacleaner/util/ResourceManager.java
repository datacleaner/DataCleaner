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
package org.eobjects.datacleaner.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResourceManager {

	private static final Logger logger = LoggerFactory.getLogger(ResourceManager.class);

	private static ResourceManager instance = new ResourceManager();

	public static ResourceManager getInstance() {
		return instance;
	}

	private ResourceManager() {
	}
	
	public ClassLoader getClassLoader(File[] files) {
		try {
			final URL[] urls = new URL[files.length];
			for (int i = 0; i < urls.length; i++) {
				URL url = files[i].toURI().toURL();
				logger.debug("Using URL: {}", url);
				urls[i] = url;
			}
			return getClassLoader(urls);
		} catch (MalformedURLException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public ClassLoader getClassLoader(final URL[] urls) {
		final ClassLoader parentClassLoader = Thread.currentThread().getContextClassLoader();

		// removing the security manager is nescesary for classes in
		// external jar files to have privileges to do eg. system property
		// lookups etc.
		System.setSecurityManager(null);

		final URLClassLoader newClassLoader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
			@Override
			public URLClassLoader run() {
				return new URLClassLoader(urls, parentClassLoader);
			}
		});
		return newClassLoader;
	}

	public List<URL> getUrls(String path) {
		List<URL> result = new LinkedList<URL>();
		URL url = getClass().getResource(path);
		if (url != null) {
			result.add(url);
		}

		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Enumeration<URL> resources = classLoader.getResources(path);
			while (resources.hasMoreElements()) {
				result.add(resources.nextElement());
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

	public URL getUrl(String path) {
		List<URL> urls = getUrls(path);
		if (urls.isEmpty()) {
			return null;
		}
		return urls.get(0);
	}
}
