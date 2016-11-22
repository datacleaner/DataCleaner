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
package org.datacleaner.monitor.server.jaxb;

import java.io.InputStream;
import java.io.OutputStream;

import org.datacleaner.monitor.jaxb.CustomJavaComponentJob;

/**
 * Simple JAXB adaptor for {@link CustomJavaJob}s.
 */
public class JaxbCustomJavaComponentJobAdaptor extends AbstractJaxbAdaptor<CustomJavaComponentJob> {

    public JaxbCustomJavaComponentJobAdaptor() {
        super(CustomJavaComponentJob.class);
    }

    @Override
    public CustomJavaComponentJob unmarshal(final InputStream in) {
        return super.unmarshal(in);
    }

    @Override
    public void marshal(final CustomJavaComponentJob obj, final OutputStream outputStream) {
        super.marshal(obj, outputStream);
    }
}
