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
package org.eobjects.datacleaner.database;

import java.util.List;

import org.apache.metamodel.util.BaseObject;

/**
 * Default implementation of {@link DatabaseDriverDescriptor}.
 */
public final class DatabaseDescriptorImpl extends BaseObject implements DatabaseDriverDescriptor {

    private static final long serialVersionUID = 1L;
    private final String _displayName;
    private final String _iconImagePath;
    private final String _driverClassName;
    private final String[] _downloadUrls;
    private final String[] _connectionUrlTemplates;

    public DatabaseDescriptorImpl(String displayName, String iconImagePath, String driverClassName,
            String[] downloadUrls, String[] connectionUrlTemplates) {
        _displayName = displayName;
        _iconImagePath = iconImagePath;
        _driverClassName = driverClassName;
        _downloadUrls = downloadUrls;
        _connectionUrlTemplates = connectionUrlTemplates;
    }

    @Override
    public String getDisplayName() {
        return _displayName;
    }

    @Override
    public String getIconImagePath() {
        return _iconImagePath;
    }

    @Override
    public String getDriverClassName() {
        return _driverClassName;
    }

    @Override
    public String[] getConnectionUrlTemplates() {
        return _connectionUrlTemplates;
    }

    @Override
    public String[] getDownloadUrls() {
        return _downloadUrls;
    }

    @Override
    public int compareTo(DatabaseDriverDescriptor o) {
        if (this.equals(o)) {
            return 0;
        }
        int result = getDisplayName().compareTo(o.getDisplayName());
        if (result == 0) {
            result = getDriverClassName().compareTo(o.getDriverClassName());
            if (result == 0) {
                result = -1;
            }
        }
        return result;
    }

    @Override
    protected void decorateIdentity(List<Object> identifiers) {
        identifiers.add(_displayName);
        identifiers.add(_iconImagePath);
        identifiers.add(_driverClassName);
        identifiers.add(_downloadUrls);
        identifiers.add(_connectionUrlTemplates);
    }
}
