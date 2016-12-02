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
package org.datacleaner.monitor.server.controllers;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Bean which represents a single row of data, included in the
 * {@link JobInvocationPayload}.
 */
public class JobInvocationRowData implements Serializable {

    private static final long serialVersionUID = 1L;

    private Object[] values;

    public JobInvocationRowData() {
    }

    public JobInvocationRowData(final Object[] values) {
        this.values = values;
    }

    public Object[] getValues() {
        return values;
    }

    public void setValues(final Object[] values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }
}
