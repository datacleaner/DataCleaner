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
package org.datacleaner.descriptors;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.inject.Named;

import org.apache.metamodel.util.ExclusionPredicate;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Predicate;
import org.apache.metamodel.util.Ref;
import org.apache.metamodel.util.TruePredicate;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Filter;
import org.datacleaner.api.Renderer;
import org.datacleaner.api.RendererBean;
import org.datacleaner.api.RenderingFormat;
import org.datacleaner.api.Transformer;
import org.datacleaner.extensions.ClassLoaderUtils;
import org.datacleaner.job.concurrent.SingleThreadedTaskRunner;
import org.datacleaner.job.concurrent.TaskListener;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.job.tasks.Task;
import org.kohsuke.asm5.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Descriptor provider implementation that works by scanning particular packages
 * in the classpath for annotated classes. Descriptors will be generated based
 * on encountered annotations.
 * 
 * This implementation also supports adding single descriptors by using the
 * add... methods.
 * 
 * Classes with the {@link Named} annotation will be picked by the classpath
 * scanner. Furthermore the legacy annotations {@link org.eobjects.analyzer.beans.api.AnalyzerBean},
 * {@link org.eobjects.analyzer.beans.api.TransformerBean} and {@link org.eobjects.analyzer.beans.api.FilterBean} are currently being checked
 * for.
 * 
 * <li>{@link RendererBean}</li>
 * 
 * 
 */
public final class ClasspathScanDescriptorProvider extends AbstractDescriptorProvider {

    private static final Logger logger = LoggerFactory.getLogger(ClasspathScanDescriptorProvider.class);

    private final Map<String, AnalyzerDescriptor<?>> _analyzerBeanDescriptors = new HashMap<String, AnalyzerDescriptor<?>>();
    private final Map<String, FilterDescriptor<?, ?>> _filterBeanDescriptors = new HashMap<String, FilterDescriptor<?, ?>>();
    private final Map<String, TransformerDescriptor<?>> _transformerBeanDescriptors = new HashMap<String, TransformerDescriptor<?>>();
    private final Map<String, RendererBeanDescriptor<?>> _rendererBeanDescriptors = new HashMap<String, RendererBeanDescriptor<?>>();
    private final TaskRunner _taskRunner;
    private final Predicate<Class<? extends RenderingFormat<?>>> _renderingFormatPredicate;
    private final AtomicInteger _tasksPending;

   
    /**
     * Default constructor. Will perform classpath scanning in the calling
     * thread(s).
     */
    public ClasspathScanDescriptorProvider() {
        this(new SingleThreadedTaskRunner());
    }

    /**
     * Constructs a {@link ClasspathScanDescriptorProvider} using a specified
     * {@link TaskRunner}. The taskrunner will be used to perform the classpath
     * scan, potentially in a parallel fashion.
     * 
     * @param taskRunner
     */
    public ClasspathScanDescriptorProvider(TaskRunner taskRunner) {
        this(taskRunner, new TruePredicate<Class<? extends RenderingFormat<?>>>());
    }

    /**
     * Constructs a {@link ClasspathScanDescriptorProvider} using a specified
     * {@link TaskRunner}. The taskrunner will be used to perform the classpath
     * scan, potentially in a parallel fashion.
     * 
     * @param taskRunner
     * @param excludedRenderingFormats
     *            rendering formats to exclude from loading into the descriptor
     *            provider
     */
    public ClasspathScanDescriptorProvider(TaskRunner taskRunner,
            Collection<Class<? extends RenderingFormat<?>>> excludedRenderingFormats) {
        this(taskRunner, createRenderingFormatPredicate(excludedRenderingFormats));
    }

    /**
     * Constructs a {@link ClasspathScanDescriptorProvider} using a specified
     * {@link TaskRunner}. The taskrunner will be used to perform the classpath
     * scan, potentially in a parallel fashion.
     * 
     * @param taskRunner
     * @param excludedRenderingFormats
     *            rendering formats to exclude from loading into the descriptor
     *            provider
     * @param autoLoadDescriptorClasses
     *            whether or not to automatically load descriptors when they are
     *            requested by class names.
     */
    public ClasspathScanDescriptorProvider(TaskRunner taskRunner,
            Collection<Class<? extends RenderingFormat<?>>> excludedRenderingFormats, boolean autoLoadDescriptorClasses) {
        this(taskRunner, createRenderingFormatPredicate(excludedRenderingFormats), autoLoadDescriptorClasses);
    }

    /**
     * Constructs a {@link ClasspathScanDescriptorProvider} using a specified
     * {@link TaskRunner}. The taskrunner will be used to perform the classpath
     * scan, potentially in a parallel fashion.
     * 
     * @param taskRunner
     * @param renderingFormatPredicate
     *            predicate function to apply when evaluating if a particular
     *            rendering format is of interest or not
     */
    public ClasspathScanDescriptorProvider(TaskRunner taskRunner,
            Predicate<Class<? extends RenderingFormat<?>>> renderingFormatPredicate) {
        this(taskRunner, renderingFormatPredicate, false);
    }

    /**
     * Constructs a {@link ClasspathScanDescriptorProvider} using a specified
     * {@link TaskRunner}. The taskrunner will be used to perform the classpath
     * scan, potentially in a parallel fashion.
     * 
     * @param taskRunner
     * @param renderingFormatPredicate
     *            predicate function to apply when evaluating if a particular
     *            rendering format is of interest or not
     * @param autoLoadDescriptorClasses
     *            whether or not to automatically load descriptors when they are
     *            requested by class names.
     */
    public ClasspathScanDescriptorProvider(TaskRunner taskRunner,
            Predicate<Class<? extends RenderingFormat<?>>> renderingFormatPredicate, boolean autoLoadDescriptorClasses) {
        super(autoLoadDescriptorClasses);
        _taskRunner = taskRunner;
        _tasksPending = new AtomicInteger(0);
        _renderingFormatPredicate = renderingFormatPredicate;
    }

    private static Predicate<Class<? extends RenderingFormat<?>>> createRenderingFormatPredicate(
            Collection<Class<? extends RenderingFormat<?>>> excludedRenderingFormats) {
        if (excludedRenderingFormats == null || excludedRenderingFormats.isEmpty()) {
            return new TruePredicate<Class<? extends RenderingFormat<?>>>();
        }
        return new ExclusionPredicate<Class<? extends RenderingFormat<?>>>(excludedRenderingFormats);
    }

    /**
     * Scans a package in the classpath (of the current thread's context
     * classloader) for annotated components.
     * 
     * @param packageName
     *            the package name to scan
     * @param recursive
     *            whether or not to scan subpackages recursively
     * @return
     */
    public ClasspathScanDescriptorProvider scanPackage(String packageName, boolean recursive) {
        return scanPackage(packageName, recursive, ClassLoaderUtils.getParentClassLoader(), false);
    }

    /**
     * Scans a package in the classpath (of a particular classloader) for
     * annotated components.
     * 
     * @param packageName
     *            the package name to scan
     * @param recursive
     *            whether or not to scan subpackages recursively
     * @param classLoader
     *            the classloader to use
     * @return
     */
    public ClasspathScanDescriptorProvider scanPackage(final String packageName, final boolean recursive,
            final ClassLoader classLoader) {
        return scanPackage(packageName, recursive, classLoader, true);
    }

    /**
     * Scans a package in the classpath (of a particular classloader) for
     * annotated components.
     * 
     * @param packageName
     *            the package name to scan
     * @param recursive
     *            whether or not to scan subpackages recursively
     * @param classLoader
     *            the classloader to use for discovering resources in the
     *            classpath
     * @param strictClassLoader
     *            whether or not classes originating from other classloaders may
     *            be included in scan (classloaders can sometimes discover
     *            classes from parent classloaders which may or may not be
     *            wanted for inclusion).
     * @return
     */
    public ClasspathScanDescriptorProvider scanPackage(final String packageName, final boolean recursive,
            final ClassLoader classLoader, final boolean strictClassLoader) {
        return scanPackage(packageName, recursive, classLoader, strictClassLoader, null);
    }

    /**
     * Scans a package in the classpath (of a particular classloader) for
     * annotated components. Optionally restricted by a set of JAR files to look
     * in.
     * 
     * @param packageName
     *            the package name to scan
     * @param recursive
     *            whether or not to scan subpackages recursively
     * @param classLoader
     *            the classloader to use for discovering resources in the
     *            classpath
     * @param strictClassLoader
     *            whether or not classes originating from other classloaders may
     *            be included in scan (classloaders can sometimes discover
     *            classes from parent classloaders which may or may not be
     *            wanted for inclusion).
     * @param jarFiles
     *            optionally (nullable) array of JAR files or class directories
     *            to scan. Note that if specified, the JAR files are assumed to
     *            be included in the classloaders available resources.
     * @return
     */
    public ClasspathScanDescriptorProvider scanPackage(final String packageName, final boolean recursive,
            final ClassLoader classLoader, final boolean strictClassLoader, final File[] jarFiles) {
        _tasksPending.incrementAndGet();
        final TaskListener listener = new TaskListener() {
            @Override
            public void onBegin(Task task) {
                logger.info("Scan of '{}' beginning", packageName);
            }

            @Override
            public void onComplete(Task task) {
                logger.info("Scan of '{}' complete", packageName);
                taskDone();
            }

            @Override
            public void onError(Task task, Throwable throwable) {
                logger.info("Scan of '{}' failed: {}", packageName, throwable.getMessage());
                logger.warn("Exception occurred while scanning and installing package: " + packageName, throwable);
                taskDone();
            }
        };

        final Task task = new Task() {
            @Override
            public void execute() throws Exception {
                final String packagePath = packageName.replace('.', '/');
                if (recursive) {
                    logger.info("Scanning package path '{}' (and subpackages recursively)", packagePath);
                } else {
                    logger.info("Scanning package path '{}'", packagePath);
                }

                logger.debug("Using ClassLoader: {}", classLoader);

                if (jarFiles != null && jarFiles.length > 0) {
                    for (File file : jarFiles) {
                        if (!file.exists()) {
                            logger.debug("Omitting JAR file because it does not exist: {}", file);
                        } else if (file.isDirectory()) {
                            logger.info("Scanning subdirectory of: {}", file);
                            final File packageDirectory = new File(file, packagePath);
                            if (packageDirectory.exists()) {
                                scanDirectory(packageDirectory, recursive, classLoader, strictClassLoader);
                            } else {
                                logger.debug("Omitting directory because it does not exist: {}", packageDirectory);
                            }
                        } else {
                            logger.info("Scanning JAR file: {}", file);

                            try (JarFile jarFile = new JarFile(file)) {
                                scanJar(jarFile, classLoader, packagePath, recursive, strictClassLoader);
                            } catch (Exception e) {
                                logger.error("Failed to scan package '" + packageName + "' in file: " + file, e);
                            }
                        }
                    }
                } else {

                    final Enumeration<URL> resources = classLoader.getResources(packagePath);
                    int count = 0;

                    while (resources.hasMoreElements()) {
                        count++;
                        final URL resource = resources.nextElement();
                        logger.debug("Scanning resource/URL no. {}: {}", count, resource);

                        try {
                            scanUrl(resource, classLoader, packagePath, recursive, strictClassLoader);
                        } catch (Exception e) {
                            logger.error("Failed to scan package '" + packageName + "' in resource/URL: " + resource, e);
                        }
                    }

                    logger.debug("Scanned resources of {}: {}", packageName, count);
                }
            }
        };
        _taskRunner.run(task, listener);
        return this;
    }

    private void scanUrl(URL resource, final ClassLoader classLoader, final String packagePath,
            final boolean recursive, final boolean strictClassLoader) throws IOException {

        final String file = resource.getFile();

        logger.debug("Resource file string: {}", file);

        final File dir = new File(file.replaceAll("\\%20", " "));
        if (dir.isDirectory()) {
            logger.info("Resource is a directory, scanning for files: {}", dir.getAbsolutePath());
            scanDirectory(dir, recursive, classLoader, strictClassLoader);
        } else {

            URLConnection connection = resource.openConnection();
            if (connection instanceof JarURLConnection) {
                logger.info("Getting JarFile from JarURLConnection: {}", connection);
                JarFile jarFile = ((JarURLConnection) connection).getJarFile();
                // note: We are NOT closing this JarFile, because it is still
                // used by the JarURLConnection
                scanJar(jarFile, classLoader, packagePath, recursive, strictClassLoader);
            } else {
                // We'll assume URLs of the format "jar:path!/entry", with the
                // protocol being arbitrary as long as following the entry
                // format. We'll also handle paths with and without leading
                // "file:" prefix.

                String rootEntryPath;

                final String jarFileUrl;
                final int separatorIndex = file.indexOf("!/");

                JarFile jarFile = null;
                try {
                    if (separatorIndex != -1) {
                        jarFileUrl = file.substring(0, separatorIndex);
                        rootEntryPath = file.substring(separatorIndex + "!/".length());
                        jarFile = getJarFile(jarFileUrl);
                    } else {
                        logger.info("Creating JarFile based on URI (without '!/'): {}", file);
                        jarFile = new JarFile(file);
                        jarFileUrl = file;
                        rootEntryPath = "";
                    }

                    if (!"".equals(rootEntryPath) && !rootEntryPath.endsWith("/")) {
                        // Root entry path must end with slash to allow for
                        // proper matching. The Sun JRE does not return a slash
                        // here, but BEA JRockit does.
                        rootEntryPath = rootEntryPath + "/";
                    }

                    scanJar(jarFile, classLoader, packagePath, recursive, strictClassLoader);
                } finally {
                    if (jarFile != null) {
                        jarFile.close();
                    }
                }
            }
        }
    }

    /**
     * Resolve the given jar file URL into a JarFile object.
     */
    private JarFile getJarFile(String jarFileUrl) throws IOException {
        if (jarFileUrl.startsWith("file:")) {
            try {
                final URI uri = new URI(jarFileUrl.replaceAll(" ", "\\%20"));
                final String jarFileName = uri.getSchemeSpecificPart();
                logger.info("Creating new JarFile based on URI-scheme filename: {}", jarFileName);
                return new JarFile(jarFileName);
            } catch (URISyntaxException ex) {
                // Fallback for URLs that are not valid URIs (should hardly ever
                // happen).
                final String jarFileName = jarFileUrl.substring("file:".length());
                logger.info("Creating new JarFile based on alternative filename: {}", jarFileName);
                return new JarFile(jarFileName);
            }
        } else {
            logger.info("Creating new JarFile based on URI (with '!/'): {}", jarFileUrl);
            return new JarFile(jarFileUrl);
        }
    }

    private boolean isClass(String entryName) {
        return entryName.endsWith(".class");
    }

    protected boolean isClassInPackage(String entryName, String packagePath, boolean recursive) {
        if (!entryName.startsWith(packagePath)) {
            return false;
        }
        if (!isClass(entryName)) {
            return false;
        }
        if (recursive) {
            return true;
        }
        String trailingPart = entryName.substring(packagePath.length());
        if (trailingPart.startsWith("/")) {
            trailingPart = trailingPart.substring(1);
        }
        return trailingPart.indexOf('/') == -1;
    }

    private void scanJar(final JarFile jarFile, final ClassLoader classLoader, final String packagePath,
            final boolean recursive, final boolean strictClassLoader) throws IOException {
        Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            final Ref<InputStream> entryInputStream = new Ref<InputStream>() {
                @Override
                public InputStream get() {
                    try {
                        return jarFile.getInputStream(entry);
                    } catch (IOException e) {
                        throw new IllegalStateException("Failed to read JAR entry InputStream", e);
                    }
                }
            };
            scanEntry(entry, packagePath, recursive, classLoader, strictClassLoader, entryInputStream);
        }
    }

    private void scanEntry(JarEntry entry, String packagePath, boolean recursive, ClassLoader classLoader,
            boolean strictClassLoader, Ref<InputStream> entryInputStream) throws IOException {
        String entryName = entry.getName();
        if (isClassInPackage(entryName, packagePath, recursive)) {
            logger.debug("Scanning JAR class file entry: {}", entryName);
            InputStream inputStream = entryInputStream.get();

            try {
                scanInputStreamOfClassFile(inputStream, classLoader, strictClassLoader);
            } catch (RuntimeException e) {
                logger.error("Failed to scan JAR class file entry: " + entryName, e);
            }
        } else {

            if (logger.isInfoEnabled()) {
                // log omitted .class files
                if (isClass(entryName)) {
                    logger.debug("Omitting JAR class file entry: {} (looking for package path: {})", entryName,
                            packagePath);
                } else {
                    logger.trace("Omitting JAR entry (not a class): {}", entryName);
                }
            }
        }
    }

    private void scanDirectory(File dir, boolean recursive, ClassLoader classLoader, final boolean strictClassLoader) {
        if (!dir.exists()) {
            throw new IllegalArgumentException("Directory '" + dir + "' does not exist");
        }
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("The file '" + dir + "' is not a directory");
        }
        logger.info("Scanning directory: {}", dir);

        final File[] classFiles = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String filename) {
                return filename.endsWith(".class");
            }
        });

        for (File file : classFiles) {
            final InputStream inputStream = FileHelper.getInputStream(file);
            try {
                scanInputStream(inputStream, classLoader, strictClassLoader);
            } catch (IOException e) {
                logger.error("Could not read file", e);
            } finally {
                FileHelper.safeClose(inputStream);
            }
        }

        if (recursive) {
            File[] subDirectories = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            });
            if (subDirectories != null) {
                if (logger.isInfoEnabled() && subDirectories.length > 0) {
                    logger.info("Recursively scanning " + subDirectories.length + " subdirectories");
                }
                for (File subDir : subDirectories) {
                    scanDirectory(subDir, true, classLoader, strictClassLoader);
                }
            }
        }
    }

    /**
     * 
     * @param inputStream
     * @param classLoader
     * @param strictClassLoader
     * @throws IOException
     * 
     * @{@link deprecated} use
     *         {@link #scanInputStreamOfClassFile(InputStream, ClassLoader, boolean)}
     *         instead.
     */
    @Deprecated
    protected void scanInputStream(final InputStream inputStream, final ClassLoader classLoader,
            final boolean strictClassLoader) throws IOException {
        scanInputStreamOfClassFile(inputStream, classLoader, strictClassLoader);
    }

    protected void scanInputStreamOfClassFile(final InputStream inputStream, final ClassLoader classLoader,
            final boolean strictClassLoader) throws IOException {
        try {
            final ClassReader classReader = new ClassReader(inputStream);
            final DCClassVisitor visitor = new DCClassVisitor(classLoader, _renderingFormatPredicate);
            classReader.accept(visitor, ClassReader.SKIP_CODE);

            Class<?> beanClass = visitor.getBeanClass();
            if (beanClass == null) {
                return;
            }

            if (strictClassLoader && classLoader != null && beanClass.getClassLoader() != classLoader) {
                logger.warn("Scanned class did not belong to required classloader: " + beanClass + ", ignoring");
                return;
            }

            if (visitor.isAnalyzer()) {
                @SuppressWarnings("unchecked")
                Class<? extends Analyzer<?>> analyzerClass = (Class<? extends Analyzer<?>>) beanClass;
                logger.info("Adding analyzer class: {}", beanClass);
                addAnalyzerClass(analyzerClass);
            }
            if (visitor.isTransformer()) {
                @SuppressWarnings("unchecked")
                Class<? extends Transformer> transformerClass = (Class<? extends Transformer>) beanClass;
                logger.info("Adding transformer class: {}", beanClass);
                addTransformerClass(transformerClass);
            }
            if (visitor.isFilter()) {
                @SuppressWarnings("unchecked")
                Class<? extends Filter<? extends Enum<?>>> filterClass = (Class<? extends Filter<?>>) beanClass;
                logger.info("Adding filter class: {}", beanClass);
                addFilterClass(filterClass);
            }
            if (visitor.isRenderer()) {
                @SuppressWarnings("unchecked")
                Class<? extends Renderer<?, ?>> rendererClass = (Class<? extends Renderer<?, ?>>) beanClass;
                logger.info("Adding renderer class: {}", beanClass);
                addRendererClass(rendererClass);
            }
        } finally {
            FileHelper.safeClose(inputStream);
        }
    }

    public ClasspathScanDescriptorProvider addAnalyzerClass(Class<? extends Analyzer<?>> clazz) {
        AnalyzerDescriptor<?> descriptor = _analyzerBeanDescriptors.get(clazz.getName());
        if (descriptor == null) {
            try {
                descriptor = Descriptors.ofAnalyzer(clazz);
                _analyzerBeanDescriptors.put(clazz.getName(), descriptor);
            } catch (Exception e) {
                logger.error("Unexpected error occurred while creating descriptor for: " + clazz, e);
            }
        }
        return this;
    }

    public ClasspathScanDescriptorProvider addTransformerClass(Class<? extends Transformer> clazz) {
        TransformerDescriptor<? extends Transformer> descriptor = _transformerBeanDescriptors.get(clazz
                .getName());
        if (descriptor == null) {
            try {
                descriptor = Descriptors.ofTransformer(clazz);
                _transformerBeanDescriptors.put(clazz.getName(), descriptor);
            } catch (Exception e) {
                logger.error("Unexpected error occurred while creating descriptor for: " + clazz, e);
            }
        }
        return this;
    }

    public ClasspathScanDescriptorProvider addFilterClass(Class<? extends Filter<?>> clazz) {
        FilterDescriptor<? extends Filter<?>, ?> descriptor = _filterBeanDescriptors.get(clazz.getName());
        if (descriptor == null) {
            try {
                descriptor = Descriptors.ofFilterUnbound(clazz);
                _filterBeanDescriptors.put(clazz.getName(), descriptor);
            } catch (Exception e) {
                logger.error("Unexpected error occurred while creating descriptor for: " + clazz, e);
            }
        }
        return this;
    }

    public ClasspathScanDescriptorProvider addRendererClass(Class<? extends Renderer<?, ?>> clazz) {
        RendererBeanDescriptor<?> descriptor = _rendererBeanDescriptors.get(clazz.getName());
        if (descriptor == null) {
            try {
                descriptor = Descriptors.ofRenderer(clazz);
                _rendererBeanDescriptors.put(clazz.getName(), descriptor);
            } catch (Exception e) {
                logger.error("Unexpected error occurred while creating descriptor for: " + clazz, e);
            }
        }
        return this;
    }

    private void taskDone() {
        int tasks = _tasksPending.decrementAndGet();
        if (tasks == 0) {
            synchronized (this) {
                notifyAll();
            }
        }
    }

    /**
     * Waits for all pending tasks to finish
     */
    private void awaitTasks() {
        if (_tasksPending.get() == 0) {
            return;
        }
        synchronized (this) {
            while (_tasksPending.get() != 0) {
                try {
                    logger.info("Scan tasks still pending, waiting");
                    wait();
                } catch (InterruptedException e) {
                    logger.debug("Interrupted while awaiting task completion", e);
                }
            }
        }
    }

    @Override
    public Collection<FilterDescriptor<?, ?>> getFilterDescriptors() {
        awaitTasks();
        return Collections.unmodifiableCollection(_filterBeanDescriptors.values());
    }

    @Override
    public Collection<AnalyzerDescriptor<?>> getAnalyzerDescriptors() {
        awaitTasks();
        return Collections.unmodifiableCollection(_analyzerBeanDescriptors.values());
    }

    @Override
    public Collection<TransformerDescriptor<?>> getTransformerDescriptors() {
        awaitTasks();
        return Collections.unmodifiableCollection(_transformerBeanDescriptors.values());
    }

    @Override
    public Collection<RendererBeanDescriptor<?>> getRendererBeanDescriptors() {
        awaitTasks();
        return Collections.unmodifiableCollection(_rendererBeanDescriptors.values());
    }

    public Predicate<Class<? extends RenderingFormat<?>>> getRenderingFormatPredicate() {
        return _renderingFormatPredicate;
    }
}
