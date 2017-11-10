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

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColorsResourceBundle extends ResourceBundle {
    private static final String RGBA = "rgba";
    private static final Logger logger = LoggerFactory.getLogger(ImageLoadingPropertyResourceBundle.class);
    private final Map<String, String> _colors;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ColorsResourceBundle(final URL url) throws IOException {
        final Properties properties = new Properties();
        properties.load(url.openStream());
        _colors = new HashMap<>();
        properties.stringPropertyNames().forEach(key -> {
            _colors.put(key, properties.getProperty(key));
        });
    }

    @Override
    protected Object handleGetObject(final String key) {
        if (key == null) {
            throw new NullPointerException();
        }

        final String value = _colors.get(key);

        if (value == null) {
            logger.warn("No such key {}", key);
            return null;
        }

        return createColor(value);
    }

    private Color createColor(final String valueDefinition) {
        if (valueDefinition.startsWith("#")) {
            return Color.decode(valueDefinition);
        } else if (valueDefinition.startsWith(RGBA)) {
            try {
                final float[] values = parseColorValues(valueDefinition);
                return new Color(values[0], values[1], values[2], values[3]);
            } catch (final Exception e) {
                return Color.WHITE;
            }
        } else {
            return Color.WHITE;
        }
    }

    private float[] parseColorValues(final String text) throws IllegalArgumentException {
        // rgba(10, 20, 30, 0) => Color(10.0, 20.0, 30.0, 0)
        final String[] valueParts = text.replaceAll(" ", "").substring(RGBA.length() + 1, text.length() - 1).split(",");

        if (valueParts.length == 3) {
            return new float[] {
                    Float.parseFloat(valueParts[0]),
                    Float.parseFloat(valueParts[1]),
                    Float.parseFloat(valueParts[2]),
            };
        } else if (valueParts.length == 4) {
            return new float[] {
                    Float.parseFloat(valueParts[0]),
                    Float.parseFloat(valueParts[1]),
                    Float.parseFloat(valueParts[2]),
                    Float.parseFloat(valueParts[3]),
            };
        } else {
            throw new IllegalArgumentException("Definition '" + text + "' was not recognized as a valid color. ");
        }
    }

    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(keySet());
    }

    @Override
    protected Set<String> handleKeySet() {
        return _colors.keySet();
    }
}
