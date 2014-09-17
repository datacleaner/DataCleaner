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
package org.eobjects.datacleaner.monitor.configuration;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.FileHelper;

/**
 * Writes a default conf.xml file for a new tenant
 */
final class WriteDefaultTenantConfigurationAction implements Action<OutputStream> {

    @Override
    public void run(OutputStream out) throws Exception {
        InputStream in = getClass().getResourceAsStream("default-conf.xml");
        try {
            FileHelper.copy(in, out);
        } finally {
            FileHelper.safeClose(in);
        }
    }

}
