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
package org.datacleaner.extensions;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.metamodel.util.HasName;
import org.datacleaner.descriptors.ClasspathScanDescriptorProvider;
import org.datacleaner.descriptors.CompositeDescriptorProvider;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.util.FileFilters;
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

    private static Collection<ClassLoader> _allExtensionClassLoaders = new ArrayList<>();
    private final File[] _files;
    private final String _name;
    private final String _scanPackage;
    private final boolean _scanRecursive;
    private final Map<String, String> _additionalProperties;
    private transient boolean _loaded = false;
    private transient int _loadedAnalyzers;
    private transient int _loadedTransformers;
    private transient int _loadedFilters;
    private transient int _loadedRenderers;
    private transient ClassLoader _classLoader;

    public ExtensionPackage(final String name, String scanPackage, final boolean scanRecursive, final File[] files) {
        _name = name;
        if (scanPackage == null) {
            scanPackage = "";
        }
        _scanPackage = scanPackage;
        _scanRecursive = scanRecursive;
        _files = files;
        _additionalProperties = new HashMap<>();
    }

    /**
     * Gets the classloader that represents the currently loaded extensions'
     * classes.
     *
     * @return
     */
    public static ClassLoader getExtensionClassLoader() {
        final Collection<ClassLoader> childClassLoaders = new ArrayList<>();
        childClassLoaders.addAll(_allExtensionClassLoaders);
        childClassLoaders.add(ClassLoaderUtils.getParentClassLoader());
        return new CompoundClassLoader(childClassLoaders);
    }

    public File[] getFiles() {
        return Arrays.copyOf(_files, _files.length);
    }

    /**
     * Determines if this extension is externally installed from a file not in
     * the primary classpath.
     *
     * @return
     */
    public boolean isExternal() {
        return _files != null && _files.length > 0;
    }

    /**
     * Determines if this extension is internally installed by being placed on
     * the primary classpath.
     *
     * @return
     */
    private boolean isInternal() {
        return !isExternal();
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

    public void loadExtension() {
        if (isInternal()) {
            // no reason to change _latestClassLoader
            _classLoader = ClassLoaderUtils.getParentClassLoader();
            return;
        }
        synchronized (ExtensionPackage.class) {
            // Each extension is loaded using its own ExtensionClassLoader. This
            // loader has 2 parents. The first is an URL class loader
            // provided by ClassLoaderUtils. This class loader loads classes
            // specific to the extension. The second class loader resolves all
            // classes already loaded from the main locations.
            _classLoader = ClassLoaderUtils.createClassLoader(getJarFiles(), ClassLoaderUtils.getParentClassLoader());
            _allExtensionClassLoaders.add(_classLoader);
        }
    }

    private File[] getJarFiles() {
        if (_files.length == 1 && _files[0].isDirectory()) {
            return _files[0].listFiles(FileFilters.JAR);
        }
        return _files;
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

            if (_classLoader == null) {
                loadExtension();
            }

            if (_scanPackage != null) {
                classpathScanner.scanPackage(_scanPackage, _scanRecursive, _classLoader, true, getJarFiles());
            }

            _loadedAnalyzers = classpathScanner.getAnalyzerDescriptors().size() - analyzersBefore;
            _loadedTransformers = classpathScanner.getTransformerDescriptors().size() - transformersBefore;
            _loadedFilters = classpathScanner.getFilterDescriptors().size() - filtersBefore;
            _loadedRenderers = classpathScanner.getRendererBeanDescriptors().size() - renderersBefore;

            _loaded = true;

            logger.info("Succesfully loaded extension '{}' containing {} analyzers, {} transformers, {} filters,"
                            + " {} renderers", getName(), getLoadedAnalyzers(), getLoadedTransformers(), getLoadedFilters(),
                    getLoadedRenderers());
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
