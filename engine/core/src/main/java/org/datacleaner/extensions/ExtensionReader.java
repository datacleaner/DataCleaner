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

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.DocumentBuilder;

import org.apache.metamodel.util.FileHelper;
import org.datacleaner.util.ResourceManager;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.http.HttpXmlUtils;
import org.datacleaner.util.xml.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Component used for reading {@link ExtensionPackage} objects from the
 * classpath (aka. "Internal" extensions) and from external files (aka.
 * "External" extensions).
 */
public class ExtensionReader {

    private static final Logger logger = LoggerFactory.getLogger(ExtensionReader.class);

    private final ResourceManager resourceManager = ResourceManager.get();

    public List<ExtensionPackage> getInternalExtensions() {
        final List<URL> extensionDescriptorUrls = resourceManager.getUrls("datacleaner-extension.xml");
        final List<ExtensionPackage> result = new ArrayList<>();
        for (final URL url : extensionDescriptorUrls) {
            final ExtensionPackage extension = getInternalExtension(url);
            if (extension != null) {
                result.add(extension);
            }
        }
        return result;
    }

    private ExtensionPackage getInternalExtension(final URL url) {
        logger.info("Reading extension descriptor: {}", url);
        try {
            final InputStream inputStream = url.openStream();
            try {
                return readExtension(inputStream);
            } finally {
                FileHelper.safeClose(inputStream);
            }
        } catch (final Exception e) {
            logger.error("Error reading internal extension of URL: " + url, e);
        }
        return null;
    }

    private ExtensionPackage readExtension(final InputStream inputStream) throws Exception {
        return readExtension(null, inputStream);
    }

    private ExtensionPackage readExtension(String name, final InputStream inputStream)
            throws Exception {
        final DocumentBuilder documentBuilder = XmlUtils.createDocumentBuilder();
        final Document document = documentBuilder.parse(inputStream);
        final Element documentElement = document.getDocumentElement();
        if (StringUtils.isNullOrEmpty(name)) {
            name = HttpXmlUtils.getChildNodeText(documentElement, "name");
        }
        final String scanPackage = HttpXmlUtils.getChildNodeText(documentElement, "package");

        final ExtensionPackage extensionPackage = new ExtensionPackage(name, scanPackage, true);

        final String description = HttpXmlUtils.getChildNodeText(documentElement, "description");
        if (!StringUtils.isNullOrEmpty(description)) {
            extensionPackage.getAdditionalProperties().put("description", description);
        }

        final String version = HttpXmlUtils.getChildNodeText(documentElement, "version");
        if (!StringUtils.isNullOrEmpty(version)) {
            extensionPackage.getAdditionalProperties().put("version", version);
        }

        final String icon = HttpXmlUtils.getChildNodeText(documentElement, "icon");
        if (!StringUtils.isNullOrEmpty(icon)) {
            extensionPackage.getAdditionalProperties().put("icon", icon);
        }

        final String url = HttpXmlUtils.getChildNodeText(documentElement, "url");
        if (!StringUtils.isNullOrEmpty(url)) {
            extensionPackage.getAdditionalProperties().put("url", url);
        }

        final String author = HttpXmlUtils.getChildNodeText(documentElement, "author");
        if (!StringUtils.isNullOrEmpty(url)) {
            extensionPackage.getAdditionalProperties().put("author", author);
        }

        return extensionPackage;
    }

    /**
     * Auto-detects a package name based on a JAR file's contents (finding the
     * common denominating package path)
     *
     * @param file
     * @return
     */
    public String autoDetectPackageName(final File file) {
        try {
            final Set<String> packageNames = new HashSet<>();
            try (JarFile jarFile = new JarFile(file)) {
                final Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    final JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.endsWith(".class")) {
                        logger.debug("Considering package of entry '{}'", name);

                        final int lastIndexOfSlash = name.lastIndexOf('/');
                        if (lastIndexOfSlash != -1) {
                            name = name.substring(0, lastIndexOfSlash);
                            packageNames.add(name);
                        }

                    }
                }
            }

            if (packageNames.isEmpty()) {
                return null;
            }

            logger.info("Found {} packages in extension jar: {}", packageNames.size(), packageNames);

            // find the longest common prefix of all the package names
            String packageName = StringUtils.getLongestCommonToken(packageNames, '/');
            if (packageName == "") {
                logger.debug("No common package prefix");
                return null;
            }

            packageName = packageName.replace('/', '.');
            return packageName;
        } catch (final Exception e) {
            logger.warn("Error occurred while auto detecting package name", e);
            return null;
        }
    }
}
