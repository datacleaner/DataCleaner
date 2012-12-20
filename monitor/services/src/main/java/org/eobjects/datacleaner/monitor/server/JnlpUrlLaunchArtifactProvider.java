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
package org.eobjects.datacleaner.monitor.server;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Reads launch artifacts (JAR files) from an existing JNLP file.
 */
public class JnlpUrlLaunchArtifactProvider implements LaunchArtifactProvider {

    private static final Logger logger = LoggerFactory.getLogger(JnlpUrlLaunchArtifactProvider.class);

    private final String _url;

    public JnlpUrlLaunchArtifactProvider() {
        this("http://datacleaner.org/resources/webstart/datacleaner.jnlp");
    }

    public JnlpUrlLaunchArtifactProvider(String url) {
        _url = url;
    }

    protected List<String> readFromUrl() throws Exception {
        final DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final Document document = documentBuilder.parse(_url);
        final Element documentElement = document.getDocumentElement();
        final String codebase = documentElement.getAttribute("codebase");

        final NodeList jarElements = document.getElementsByTagName("jar");
        final int length = jarElements.getLength();
        assert length > 0;

        final List<String> result = new ArrayList<String>(length);

        for (int i = 0; i < length; i++) {
            final Element jarElement = (Element) jarElements.item(i);
            final String href = jarElement.getAttribute("href");
            if (codebase == null) {
                result.add(href);
            } else if (codebase.endsWith("/")) {
                result.add(codebase + href);
            } else {
                result.add(codebase + '/' + href);
            }
        }

        return result;
    }

    @Override
    public boolean isAvailable() {
        return !getJarFilenames().isEmpty();
    }

    @Override
    public List<String> getJarFilenames() {
        try {
            return readFromUrl();
        } catch (Exception e) {
            logger.error("Failed to retrieve and parse JNLP file from url: " + _url, e);
            // return empty list to indicate an issue.
            return Collections.emptyList();
        }
    }

    @Override
    public InputStream readJarFile(String filename) throws IllegalArgumentException, IllegalStateException {
        throw new IllegalArgumentException(
                "This provider does not provide JAR files themselves. Expecting to not be invoked!");
    }

}
