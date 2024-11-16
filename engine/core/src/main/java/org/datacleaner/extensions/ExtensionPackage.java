/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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
package org.datacleaner.extensions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.metamodel.util.HasName;
import org.datacleaner.descriptors.ClasspathScanDescriptorProvider;
import org.datacleaner.descriptors.CompositeDescriptorProvider;
import org.datacleaner.descriptors.DescriptorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an extension/plug-in package that the user has installed. An
 * extension is based on a set of (JAR) files and optionally some metadata about
 * these.
 */
public final class ExtensionPackage implements Serializable, HasName {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(ExtensionPackage.class);

	private final String _name;
	private final String _scanPackage;
	private final boolean _scanRecursive;
	private final Map<String, String> _additionalProperties;
	private transient boolean _loaded = false;
	private transient int _loadedAnalyzers;
	private transient int _loadedTransformers;
	private transient int _loadedFilters;
	private transient int _loadedRenderers;

	public ExtensionPackage(final String name, String scanPackage, final boolean scanRecursive) {
		_name = name;
		if (scanPackage == null) {
			scanPackage = "";
		}
		_scanPackage = scanPackage;
		_scanRecursive = scanRecursive;
		_additionalProperties = new HashMap<>();
	}

	/**
	 * Gets the name of the extension
	 */
	@Override
	public String getName() {
		return _name;
	}

	public String getScanPackage() {
		return _scanPackage;
	}

	public boolean isScanRecursive() {
		return _scanRecursive;
	}

	public ExtensionPackage loadDescriptors(final DescriptorProvider descriptorProvider) throws IllegalStateException {
		if (!_loaded) {

			final ClasspathScanDescriptorProvider classpathScanner;
			if (descriptorProvider instanceof ClasspathScanDescriptorProvider) {
				classpathScanner = (ClasspathScanDescriptorProvider) descriptorProvider;
			} else if (descriptorProvider instanceof CompositeDescriptorProvider) {
				classpathScanner = ((CompositeDescriptorProvider) descriptorProvider).findClasspathScanProvider();
			} else {
				throw new IllegalStateException(
						"Can only load user extensions when descriptor provider is of classpath scanner type.");
			}

			final int analyzersBefore = classpathScanner.getAnalyzerDescriptors().size();
			final int transformersBefore = classpathScanner.getTransformerDescriptors().size();
			final int filtersBefore = classpathScanner.getFilterDescriptors().size();
			final int renderersBefore = classpathScanner.getRendererBeanDescriptors().size();

			if (_scanPackage != null) {
				final ClassLoader classLoader = ClassLoaderUtils.getParentClassLoader();
				classpathScanner.scanPackage(_scanPackage, _scanRecursive, classLoader, true);
			}

			_loadedAnalyzers = classpathScanner.getAnalyzerDescriptors().size() - analyzersBefore;
			_loadedTransformers = classpathScanner.getTransformerDescriptors().size() - transformersBefore;
			_loadedFilters = classpathScanner.getFilterDescriptors().size() - filtersBefore;
			_loadedRenderers = classpathScanner.getRendererBeanDescriptors().size() - renderersBefore;

			_loaded = true;

			logger.info(
					"Succesfully loaded extension '{}' containing {} analyzers, {} transformers, {} filters,"
							+ " {} renderers",
					getName(), getLoadedAnalyzers(), getLoadedTransformers(), getLoadedFilters(), getLoadedRenderers());
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

	public String getDescription() {
		return _additionalProperties.get("description");
	}

	public String getVersion() {
		return _additionalProperties.get("version");
	}

	public String getUrl() {
		return _additionalProperties.get("url");
	}

	public String getAuthor() {
		return _additionalProperties.get("author");
	}

	/**
	 * Returns the name of the package.
	 */
	public String toString() {
		return getName();
	}
}
