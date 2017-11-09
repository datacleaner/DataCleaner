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

package org.datacleaner.branding;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This loads images from a property file containing resource paths.
 */
public class ImageLoadingPropertyResourceBundle extends ResourceBundle {
    private static final Logger logger = LoggerFactory.getLogger(ImageLoadingPropertyResourceBundle.class);

    private final Map<String, String> _paths;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ImageLoadingPropertyResourceBundle(final URL url) throws IOException {
        final Properties properties = new Properties();
        properties.load(url.openStream());
        _paths = new HashMap<>();
        properties.stringPropertyNames().forEach(key -> {
            _paths.put(key, properties.getProperty(key));
        });
    }

    @Override
    protected Object handleGetObject(final String key) {
        if (key == null) {
            throw new NullPointerException();
        }

        final String path = _paths.get(key);
        if (path == null) {
            logger.warn("No such key {}", key);
            return null;
        }

        try {
            return ImageIO.read(this.getClass().getClassLoader().getResourceAsStream(path));
        } catch (final IOException e) {
            logger.warn("Could not load image with key {} and path {}", key, path, e);
            return null;
        }
    }

    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(keySet());
    }

    @Override
    protected Set<String> handleKeySet() {
        return _paths.keySet();
    }
}
