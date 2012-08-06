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
package org.eobjects.datacleaner.monitor.jobwizard.movedata;

import org.eobjects.metamodel.schema.Column;

/**
 * Simple struct like pojo for holding the information about a mapped columns.
 */
final class ColumnMapping {

    private final Column _sourceColumn;
    private final Column _targetColumn;
    private final boolean _id;

    public ColumnMapping(Column sourceColumn, Column targetColumn, boolean id) {
        _sourceColumn = sourceColumn;
        _targetColumn = targetColumn;
        _id = id;
    }

    public Column getSourceColumn() {
        return _sourceColumn;
    }

    public Column getTargetColumn() {
        return _targetColumn;
    }

    public boolean isId() {
        return _id;
    }
}
