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
package org.eobjects.datacleaner.user;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.util.ClassLoaderUtils;
import org.eobjects.metamodel.util.HasName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an extension/plug-in package that the user has installed. An
 * extension is based on a set of (JAR) files and optionally some metadata about
 * these.
 * 
 * @author Kasper Sørensen
 */
public final class ExtensionPackage implements Serializable, HasName {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(ExtensionPackage.class);

    private static ClassLoader _latestClassLoader = ClassLoaderUtils.getParentClassLoader();

    private transient boolean _loaded = false;
    private transient int _loadedAnalyzers;
    private transient int _loadedTransformers;
    private transient int _loadedFilters;
    private transient int _loadedRenderers;
    private transient ClassLoader _classLoader;

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
            // each loaded extension package is loaded within it's own
            // classloader which is a child of the previous extension's
            // classloader. This mechanism ensures that classes occurring in
            // several extensions are only loaded once.
            _latestClassLoader = ClassLoaderUtils.createClassLoader(_files, _latestClassLoader);
            _classLoader = _latestClassLoader;
        }
    }

    public ExtensionPackage loadDescriptors(DescriptorProvider descriptorProvider) throws IllegalStateException {
        if (!_loaded) {
            if (!(descriptorProvider instanceof ClasspathScanDescriptorProvider)) {
                throw new IllegalStateException(
                        "Can only load user extensions when descriptor provider is of classpath scanner type.");
            }

            final ClasspathScanDescriptorProvider classpathScanner = (ClasspathScanDescriptorProvider) descriptorProvider;

            final int analyzersBefore = classpathScanner.getAnalyzerBeanDescriptors().size();
            final int transformersBefore = classpathScanner.getTransformerBeanDescriptors().size();
            final int filtersBefore = classpathScanner.getFilterBeanDescriptors().size();
            final int renderersBefore = classpathScanner.getRendererBeanDescriptors().size();

            if (_classLoader == null) {
                loadExtension();
            }

            classpathScanner.scanPackage(_scanPackage, _scanRecursive, _classLoader, true, _files);

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

    /**
     * Gets the classloader that represents the currently loaded extensions'
     * classes.
     * 
     * @return
     */
    public static ClassLoader getExtensionClassLoader() {
        return _latestClassLoader;
    }

    public String getDescription() {
        return _additionalProperties.get("description");
    }

    public String getVersion() {
        return _additionalProperties.get("version");
    }
}
