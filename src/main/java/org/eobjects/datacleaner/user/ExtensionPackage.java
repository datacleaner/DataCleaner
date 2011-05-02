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

import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.datacleaner.util.ResourceManager;
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

	private transient boolean _loaded = false;
	private transient int _loadedAnalyzers;
	private transient int _loadedTransformers;
	private transient int _loadedFilters;
	private final File[] _files;
	private final String _name;
	private final String _scanPackage;
	private final boolean _scanRecursive;

	public ExtensionPackage(String name, String scanPackage, boolean scanRecursive, File[] files) {
		_name = name;
		_scanPackage = scanPackage;
		_scanRecursive = scanRecursive;
		_files = files;
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

			ClassLoader classLoader = ResourceManager.getInstance().getClassLoader(_files);

			int analyzersBefore = classpathScanner.getAnalyzerBeanDescriptors().size();
			int transformersBefore = classpathScanner.getTransformerBeanDescriptors().size();
			int filtersBefore = classpathScanner.getFilterBeanDescriptors().size();

			classpathScanner.scanPackage(_scanPackage, _scanRecursive, classLoader);

			_loadedAnalyzers = classpathScanner.getAnalyzerBeanDescriptors().size() - analyzersBefore;
			_loadedTransformers = classpathScanner.getTransformerBeanDescriptors().size() - transformersBefore;
			_loadedFilters = classpathScanner.getFilterBeanDescriptors().size() - filtersBefore;

			_loaded = true;

			logger.info("Succesfully loaded extension '{}' containing {} analyzers, {} transformers, {} filters",
					new Object[] { getName(), getLoadedAnalyzers(), getLoadedTransformers(), getLoadedFilters() });
		}
		return this;
	}

	public boolean isLoaded() {
		return _loaded;
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
}
