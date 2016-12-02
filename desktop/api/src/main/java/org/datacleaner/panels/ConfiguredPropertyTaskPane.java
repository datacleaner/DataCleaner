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
package org.datacleaner.panels;

import java.util.List;

import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;

/**
 * Defines a task pane with configured properties for a component.
 */
public final class ConfiguredPropertyTaskPane {

    private final boolean _expanded;
    private final String _title;
    private final String _iconImagePath;
    private final List<ConfiguredPropertyDescriptor> _properties;

    public ConfiguredPropertyTaskPane(final String title, final String iconImagePath,
            final List<ConfiguredPropertyDescriptor> properties) {
        this(title, iconImagePath, properties, true);
    }

    public ConfiguredPropertyTaskPane(final String title, final String iconImagePath,
            final List<ConfiguredPropertyDescriptor> properties, final boolean expanded) {
        _title = title;
        _iconImagePath = iconImagePath;
        _properties = properties;
        _expanded = expanded;
    }

    public boolean isExpanded() {
        return _expanded;
    }

    public List<ConfiguredPropertyDescriptor> getProperties() {
        return _properties;
    }

    public String getIconImagePath() {
        return _iconImagePath;
    }

    public String getTitle() {
        return _title;
    }

}
