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
package org.datacleaner.monitor.pentaho;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBElement;

import org.datacleaner.monitor.pentaho.jaxb.PentahoJobType;
import org.datacleaner.monitor.server.jaxb.AbstractJaxbAdaptor;

/**
 * JAXB adaptor for {@link PentahoJobType}.
 */
public class JaxbPentahoJobTypeAdaptor extends AbstractJaxbAdaptor<PentahoJobType> {

    public JaxbPentahoJobTypeAdaptor() {
        super(PentahoJobType.class);
    }

    @Override
    public PentahoJobType unmarshal(final InputStream in) {
        return super.unmarshal(in);
    }

    @Override
    public void marshal(final JAXBElement<PentahoJobType> obj, final OutputStream outputStream) {
        super.marshal(obj, outputStream);
    }
}
