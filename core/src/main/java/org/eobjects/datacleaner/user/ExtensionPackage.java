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
package org.eobjects.datacleaner.user;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.util.ClassLoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an extension/plug-in package that the user has installed. An
 * extension is based on a set of (JAR) files and optionally some metadata about
 * these.
 * 
 * @author Kasper Sørensen
 */
public final class ExtensionPackage implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(ExtensionPackage.class);

	private static ClassLoader _classLoader = ClassLoaderUtils.getParentClassLoader();

	private transient boolean _loaded = false;
	private transient int _loadedAnalyzers;
	private transient int _loadedTransformers;
	private transient int _loadedFilters;
	private transient int _loadedRenderers;

	private final File[] _files;
	private final String _name;
	private final String _scanPackage;
	private final boolean _scanRecursive;
	private final Map<String, String> _additionalProperties;

	public ExtensionPackage(String name, String scanPackage, boolean scanRecursive, File[] files) {
		_name = name;
		if (scanPackage == null) {
			scanPackage = "";
		}
		_scanPackage = scanPackage;
		_scanRecursive = scanRecursive;
		_files = files;
		_additionalProperties = new HashMap<String, String>();
	}

	public File[] getFiles() {
		return Arrays.copyOf(_files, _files.length);
	}

	public String getName() {
		return _name;
	}

	public String getScanPackage() {
		return _scanPackage;
	}

	public boolean isScanRecursive() {
		return _scanRecursive;
	}

	public ExtensionPackage loadExtension(DescriptorProvider descriptorProvider) throws IllegalStateException {
		if (!_loaded) {
			if (!(descriptorProvider instanceof ClasspathScanDescriptorProvider)) {
				throw new IllegalStateException(
						"Can only load user extensions when descriptor provider is of classpath scanner type.");
			}
			ClasspathScanDescriptorProvider classpathScanner = (ClasspathScanDescriptorProvider) descriptorProvider;

			synchronized (ExtensionPackage.class) {
				// each loaded extension package is loaded within it's own
				// classloader which is a child of the previous extension's
				// classloader. This mechanism ensures that classes occurring in
				// several extensions are only loaded once.
				_classLoader = ClassLoaderUtils.createClassLoader(_files, _classLoader);
			}

			int analyzersBefore = classpathScanner.getAnalyzerBeanDescriptors().size();
			int transformersBefore = classpathScanner.getTransformerBeanDescriptors().size();
			int filtersBefore = classpathScanner.getFilterBeanDescriptors().size();
			int renderersBefore = classpathScanner.getRendererBeanDescriptors().size();

			classpathScanner = classpathScanner.scanPackage(_scanPackage, _scanRecursive, _classLoader, true, _files);

			_loadedAnalyzers = classpathScanner.getAnalyzerBeanDescriptors().size() - analyzersBefore;
			_loadedTransformers = classpathScanner.getTransformerBeanDescriptors().size() - transformersBefore;
			_loadedFilters = classpathScanner.getFilterBeanDescriptors().size() - filtersBefore;
			_loadedRenderers = classpathScanner.getRendererBeanDescriptors().size() - renderersBefore;

			_loaded = true;

			logger.info(
					"Succesfully loaded extension '{}' containing {} analyzers, {} transformers, {} filters, {} renderers",
					new Object[] { getName(), getLoadedAnalyzers(), getLoadedTransformers(), getLoadedFilters(),
							getLoadedRenderers() });
		}
		return this;
	}

	public Map<String, String> getAdditionalProperties() {
		return _additionalProperties;
	}

	public boolean isLoaded() {
		return _loaded;
	}

	public int getLoadedRenderers() {
		return _loadedRenderers;
	}

	public int getLoadedAnalyzers() {
		return _loadedAnalyzers;
	}

	public int getLoadedFilters() {
		return _loadedFilters;
	}

	public int getLoadedTransformers() {
		return _loadedTransformers;
	}

	public static String autoDetectPackageName(File file) {
		try {
			Set<String> packageNames = new HashSet<String>();
			JarFile jarFile = new JarFile(file);
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				if (name.endsWith(".class")) {
					logger.debug("Considering package of entry '{}'", name);

					int lastIndexOfSlash = name.lastIndexOf('/');
					if (lastIndexOfSlash != -1) {
						name = name.substring(0, lastIndexOfSlash);
						packageNames.add(name);
					}

				}
			}

			if (packageNames.isEmpty()) {
				return null;
			}

			logger.info("Found {} packages in extension jar: {}", packageNames.size(), packageNames);

			// find the longest common prefix of all the package names
			Iterator<String> it = packageNames.iterator();
			String packageName = it.next();
			while (it.hasNext()) {
				if (packageName == "") {
					logger.debug("No common package prefix");
					return null;
				}
				String name = it.next();
				if (!name.startsWith(packageName)) {
					packageName = longestCommonPrefix(packageName, name, '/');
				}
			}

			packageName = packageName.replace('/', '.');
			return packageName;
		} catch (Exception e) {
			logger.warn("Error occurred while auto detecting package name", e);
			return null;
		}
	}

	protected static String longestCommonPrefix(String str1, String str2, char tokenizerChar) {
		StringBuilder result = new StringBuilder();
		String[] tokens1 = str1.split("\\" + tokenizerChar);
		String[] tokens2 = str2.split("\\" + tokenizerChar);
		for (int i = 0; i < Math.min(tokens1.length, tokens2.length); i++) {
			if (!tokens1[i].equals(tokens2[i])) {
				break;
			}
			if (i != 0) {
				result.append(tokenizerChar);
			}
			result.append(tokens1[i]);
		}
		return result.toString();
	}
}
