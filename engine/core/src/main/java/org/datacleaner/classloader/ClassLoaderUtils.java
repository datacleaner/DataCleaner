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
package org.datacleaner.classloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for dealing with {@link ClassLoader}s. The primary focus of
 * this class is to ease with handling the diversity of situations that the
 * applcation can be deployed to: Embedded in a non-system classloader, a Java
 * Web Start classloader and running in a regular system classloader.
 * 
 * 
 */
public final class ClassLoaderUtils {

	private static final Logger logger = LoggerFactory.getLogger(ClassLoaderUtils.class);

	// to find out if web start is running, use system property
	// http://lopica.sourceforge.net/faq.html#under
	public static final boolean IS_WEB_START = System.getProperty("javawebstart.version") != null;

	private ClassLoaderUtils() {
		// prevent instantiation
	}

	/**
	 * Gets an appropriate classloader for usage when performing classpath
	 * lookups and scanning.
	 * 
	 * @return
	 */
	public static ClassLoader getParentClassLoader() {
		logger.debug("getParentClassLoader() invoked, web start mode: {}", IS_WEB_START);
		if (IS_WEB_START) {
			return Thread.currentThread().getContextClassLoader();
		} else {
			return ClassLoaderUtils.class.getClassLoader();
		}
	}

	public static ClassLoader createClassLoader(File[] files) {
		return createClassLoader(files, getParentClassLoader());
	}

	public static ClassLoader createClassLoader(File[] files, ClassLoader parentClassLoader) {
		try {
			final URL[] urls = new URL[files.length];
			for (int i = 0; i < urls.length; i++) {
				URL url = files[i].toURI().toURL();
				logger.debug("Using URL: {}", url);
				urls[i] = url;
			}
			return createClassLoader(urls, parentClassLoader);
		} catch (MalformedURLException e) {
			throw new IllegalStateException(e);
		}
	}

	public static ClassLoader createClassLoader(final URL[] urls) {
		return createClassLoader(urls, ClassLoaderUtils.getParentClassLoader());
	}

	public static ClassLoader createClassLoader(final URL[] urls, final ClassLoader parentClassLoader) {
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
}
