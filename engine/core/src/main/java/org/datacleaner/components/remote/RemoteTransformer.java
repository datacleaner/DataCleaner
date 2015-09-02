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
package org.datacleaner.components.remote;

import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.descriptors.RemoteConfiguredPropertyDescriptorImpl;

/**
 * @Since 9/1/15
 */
public class RemoteTransformer implements Transformer {

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(new String[] {"TEST"}, new Class[] {String.class});
    }

    @Override
    public Object[] transform(InputRow inputRow) {
        return new Object[] {"AHOJ"};
    }

    public void setPropertyValue(RemoteConfiguredPropertyDescriptorImpl remoteConfiguredPropertyDescriptor, Object value) {
        // TODO
    }

    public Object getProperty(RemoteConfiguredPropertyDescriptorImpl remoteConfiguredPropertyDescriptor) {
        // TODO
        return null;
    }
}
