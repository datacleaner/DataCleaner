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
package org.eobjects.datacleaner.monitor.server.jaxb;

import java.io.InputStream;
import java.io.OutputStream;

import org.eobjects.datacleaner.monitor.jaxb.CustomJavaJob;

/**
 * Simple JAXB adaptor for {@link CustomJavaJob}s.
 */
public class JaxbCustomJavaJobAdaptor extends AbstractJaxbAdaptor<CustomJavaJob> {

    public JaxbCustomJavaJobAdaptor() {
        super(CustomJavaJob.class);
    }

    @Override
    public CustomJavaJob unmarshal(InputStream in) {
        return super.unmarshal(in);
    }

    @Override
    public void marshal(CustomJavaJob obj, OutputStream outputStream) {
        super.marshal(obj, outputStream);
    }
}
